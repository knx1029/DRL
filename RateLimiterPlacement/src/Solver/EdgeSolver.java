package Solver;

import Policy.*;
import Topology.*;

public class EdgeSolver implements Solver {

	public static final int INGRESS_ONLY = 0;
	public static final int EGRESS_ONLY = 1;
	
	private int mode;
	
	public EdgeSolver(int mode) {
		this.mode = mode;
	}

	@Override
	public Solution solve(Graph graph, Routing routing, TrafficProfile profile) {
		if (mode == INGRESS_ONLY) {
			return solve_ingress(graph, profile);
		}
		else if (mode == EGRESS_ONLY) {
			return solve_egress(graph, profile);
		}
		
		return null;
	}
	
	private Solution solve_ingress(Graph graph, TrafficProfile profile) {
		double[][] ans = new double[graph.n_link][profile.size()];
		
		for (int k = 0; k < profile.size(); ++k) {
        	Stub[] src = profile.entryAt(k).src;
        	
        	for (int i = 0; i < src.length; ++i) {
        		Stub src_stub = src[i];
        	    
        		int src_id = src_stub.switchId();
        	    Switch src_switch = graph.getSwitchById(src_id);
        	    Link l = src_switch.getFromLink(src_stub);
        	    ans[l.index()][k] = 1;
        	}
		}
        	
		// TODO Auto-generated method stub
		return new Solution(ans);
	}
        	
    private Solution solve_egress(Graph graph, TrafficProfile profile) {
    	double[][] ans = new double[graph.n_link][profile.size()];
		
		for (int k = 0; k < profile.size(); ++k) {
        	Stub[] dst = profile.entryAt(k).dst;
        	
        	for (int i = 0; i < dst.length; ++i) {
        		Stub dst_stub = dst[i];
        	    
        		int dst_id = dst_stub.switchId();
        	    Switch dst_switch = graph.getSwitchById(dst_id);
        	    Link l = dst_switch.getToLink(dst_stub);
        	    ans[l.index()][k] = 1;
        	}
		}
        	
		// TODO Auto-generated method stub
		return new Solution(ans);
    }
        		
	
	
}
