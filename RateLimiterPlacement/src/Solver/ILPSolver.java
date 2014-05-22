package Solver;
/**
 * Author: Nanxi Kang (nkang@cs.princeton.edu) 
 * All rights reserved.
 * 
 * This class solve the rate limiter placement by ILP
 */

import gurobi.*;
import Policy.*;
import Topology.*;
import java.util.*;

public class ILPSolver implements Solver {

    public static final double EPS = 1e-6;
	
    public static final int MIN_TOTAL_QUEUE = 1;
    public static final int EARLY_RATE_LIMIT = 2;
    public static final int MIN_DIFF = 3;
    public static final int BALANCE_UPDATE = 4;


    private double alpha; // only used by balance update
    private int mode;
    private Solution ref_sol;
    
    public ILPSolver(int mode) {
	this.mode = mode;
	ref_sol = null;
    }
    
    public void setAlpha(double a) {
	alpha = a;
    }

    public void setRefSol(Solution s) {
	ref_sol = s;
    }
    
    public Solution solve(Graph graph, Routing routing, TrafficProfile profile) {
	
	try {
	    GRBEnv env = new GRBEnv();
	    GRBModel model = new GRBModel(env);        
	    
	        
	    // Answers, R[i][k] denotes whether Link[i] gives one queue to Entry[k]
	    GRBVar[][] R = new GRBVar[graph.n_link][profile.size()];
	    /*for (int i = 0; i < graph.n_link; ++i)
	      for (int k = 0; k < profile.size(); ++k)
	      R[i][k] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY,"");
	    */
	    
	    
	    //Init answer, and compute link_weight
	    Vector<GRBLinExpr> pathRes = new Vector<GRBLinExpr>();
	    double[][] link_weight = new double[graph.n_link][profile.size()];
	    
	    for (int k = 0; k < profile.size(); ++k) {
		Stub[] src = profile.entryAt(k).src;
		Stub[] dst = profile.entryAt(k).dst;
		
		for (int i = 0; i < src.length; ++i) 
		    for (int j = 0; j < dst.length; ++j) {
			Stub src_stub = src[i];
			Stub dst_stub = dst[j];
			if (src_stub == dst_stub)
			    continue;
			
			int src_id = src_stub.switchId();
			int dst_id = dst_stub.switchId();
			Switch src_switch = graph.getSwitchById(src_id);
			Switch dst_switch = graph.getSwitchById(dst_id);
			Path path = routing.getPathBySwitcheIds(src_id, dst_id); 
	        	
			//			double hops = path.size() + 1;
			double hops = path.size();
			for (int t = 0; t < path.size(); ++t) {
			    Link l = path.linkAt(t);
			    if (R[l.index()][k] == null)
				R[l.index()][k] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY,"");
			    //			    link_weight[l.index()][k] += t / hops;
			    link_weight[l.index()][k] += (hops - t) / hops;
			}
			Link first = src_switch.getFromLink(src_stub);
			Link last = dst_switch.getToLink(dst_stub);
	        	
			if (R[first.index()][k] == null)
			    R[first.index()][k] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY,"");
			if (R[last.index()][k] == null)
			    R[last.index()][k] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY,"");
			
			link_weight[first.index()][k] += 0.0;
			link_weight[last.index()][k] += 1.0;
		    }
		
	    }
	    
	    model.update();
	    
	    //Entry constraints, every single path in Entry[k] is covered by one queue
	    for (int k = 0; k < profile.size(); ++k) {
		Stub[] src = profile.entryAt(k).src;
		Stub[] dst = profile.entryAt(k).dst;
	        
		for (int i = 0; i < src.length; ++i) 
		    for (int j = 0; j < dst.length; ++j) {
			Stub src_stub = src[i];
			Stub dst_stub = dst[j];
			if (src_stub == dst_stub)
			    continue;
			
			int src_id = src_stub.switchId();
			int dst_id = dst_stub.switchId();
			Switch src_switch = graph.getSwitchById(src_id);
			Switch dst_switch = graph.getSwitchById(dst_id);
			Path path = routing.getPathBySwitcheIds(src_id, dst_id); 
	        	
			GRBLinExpr res = new GRBLinExpr();
			for (int t = 0; t < path.size(); ++t) {
			    Link l = path.linkAt(t);
			    res.addTerm(1.0, R[l.index()][k]);
			}
			Link first = src_switch.getFromLink(src_stub);
			Link last = dst_switch.getToLink(dst_stub);
	        	
			res.addTerm(1.0, R[first.index()][k]);
			res.addTerm(1.0, R[last.index()][k]);
	        	
			model.addConstr(res, GRB.GREATER_EQUAL, 1, "");
			pathRes.add(res);	        	       
		    }
		
	    }
	    
	    //Queue constraints, for Port : sum_k R[i][k] <= C[i]
	    GRBLinExpr portRes[] = new GRBLinExpr[graph.n_link];
	    for (int i = 0; i < graph.n_link; ++i) {
		portRes[i] = new GRBLinExpr();
		for (int k = 0; k < R[i].length; ++k)
		    if (R[i][k] != null)
			portRes[i].addTerm(1.0, R[i][k]);
		int num_queue = Link.linkAt(i).totalQueue();	            
		model.addConstr(portRes[i], GRB.LESS_EQUAL, num_queue, "");
	    }
	    
	    //Queue constraints, for Entry : sum_i R[i][k] <= U[k]
	    GRBLinExpr entryRes[] = new GRBLinExpr[profile.size()];
	    for (int k = 0; k < profile.size(); ++k) {
		entryRes[k] = new GRBLinExpr();
		for (int i = 0; i < graph.n_link; ++i) {
		    if (R[i][k] != null)
			entryRes[k].addTerm(1.0, R[i][k]);
		}
		int num_queue = profile.entryAt(k).totalQueue();	            
		model.addConstr(entryRes[k], GRB.LESS_EQUAL, num_queue, "");
	    }
	    


	    GRBLinExpr obj = new GRBLinExpr();
	    if (mode == ILPSolver.MIN_TOTAL_QUEUE) {
		for (int i = 0; i < graph.n_link; ++i)
		    for (int k = 0; k < profile.size(); ++k)
			if (R[i][k] != null)
			    obj.addTerm(1.0, R[i][k]);
		model.setObjective(obj, GRB.MINIMIZE);
	    }
            
	    if (mode == ILPSolver.EARLY_RATE_LIMIT) {
		for (int i = 0; i < graph.n_link; ++i)
		    for (int k = 0; k < profile.size(); ++k)
			if (link_weight[i][k] > EPS)
			    obj.addTerm(link_weight[i][k], R[i][k]);
		model.setObjective(obj, GRB.MAXIMIZE);
		//model.setObject(obj, GRB.MINIMIZE);
	    }

	    if (mode == ILPSolver.MIN_DIFF) {
		double[][] d = ref_sol.getSol();
		for (int i = 0; i < graph.n_link; ++i)
		    for (int k = 0; k < profile.size(); ++k)
			if (R[i][k] != null) {
			    if (d[i][k] < EPS)
				obj.addTerm(1.0, R[i][k]);
			    else
				obj.addTerm(-1.0, R[i][k]);
			}
		model.setObjective(obj, GRB.MINIMIZE);
	    }

	    if (mode == ILPSolver.BALANCE_UPDATE) {
		double[][] d = ref_sol.getSol();
		for (int i = 0; i < graph.n_link; ++i)
		    for (int k = 0; k < profile.size(); ++k) {
			double cov = 0.0;
			if (link_weight[i][k] > EPS)
			    cov =(1 - alpha) * link_weight[i][k];
			if (R[i][k] != null)
			    if (d[i][k] < EPS)
				cov += alpha;
			    else cov -= alpha;
			if (cov > EPS || cov < -EPS)
			    obj.addTerm(cov, R[i][k]);
		    }
		model.setObjective(obj, GRB.MINIMIZE);
	    }
	    
	    model.update();
	    model.optimize();
	    
	    int status = model.get(GRB.IntAttr.Status);
	    if (status == GRB.OPTIMAL) {
		System.out.println("OPTIMAL");
	        
		double[][] ans = new double[graph.n_link][profile.size()];
		for (int i = 0; i < R.length; ++i) 
		    for (int k = 0; k < R[i].length; ++k)
			if (R[i][k] != null) 
			    ans[i][k] = R[i][k].get(GRB.DoubleAttr.X);


		double target = model.get(GRB.DoubleAttr.ObjVal);
		if (ref_sol != null) {
		    int total = 0;
		    target = 0.0;
		    double[][] d = ref_sol.getSol();
		    for (int i = 0; i < ans.length; ++i)
			for (int k = 0; k < ans[i].length; ++k) 
			    if (R[i][k] != null) {
				if (d[i][k] >= EPS)
				    ++total;
				if (d[i][k] < EPS)
				    target += ans[i][k];
				else 
				    target += 1 - ans[i][k];	
			    }
		    target /= total;
		}

		Solution sol = new Solution(ans, target);
	        
		model.dispose();
		env.dispose();
	    	
		return sol;
	    }
	    else if (status == GRB.INFEASIBLE)  {
		System.out.println("INFEASIBLE");
		model.dispose();
		env.dispose();
		return null;
	    }
	    else if (status == GRB.UNBOUNDED) {
		System.out.println("UN_BOUNDED");
		model.dispose();
		env.dispose();
		return null;
	    }
	    else {
		model.dispose();
		env.dispose();
	    	
		System.out.println("LP Stopped with status = " + status);
	    }
	}
	catch (GRBException e) {
	    e.printStackTrace();
	    System.out.println("Error code = " + e.getErrorCode());
	    System.out.println(e.getMessage());
	}
	catch (Exception e) {
	    e.printStackTrace();
	    System.out.println("Exception during optimization: " + e.getMessage());
	}
	return null;
    }

    public void dispose() {
	
    }
	
}