
import Solver.*;
import Topology.*;
import Policy.*;
import java.io.*;
import java.util.Vector;

public class Drive {

	public static double EPS = 1e-6;
	
	private static final int TESTCASES = 10;
	private static final String TOPO_FILENAME = "../Data_Prepare/ts100/ts100-%d-topo.txt";
	private static final String ROUTING_FILENAME = "../Data_Prepare/ts100/ts100-%d-routing.txt";
	private static final String OUT_FILENAME = "../Data_Prepare/ts100/ts100-%d-1-bfs_ILP.txt";
	
	 
	public static void main(String[] args) {
		String[] xs = new String[TESTCASES];
		for (int testcase = 0; testcase < TESTCASES; ++testcase) {
			String x  = run(String.format(TOPO_FILENAME, testcase), 
				String.format(ROUTING_FILENAME, testcase),
				String.format(OUT_FILENAME, testcase));
			xs[testcase] = x;
		}
		
		for (int testcase = 0; testcase < TESTCASES; ++testcase) {
			System.out.println(testcase + " : " + xs[testcase]);
		}
	}
	
	public static String run(String topo_filename, String routing_filename, String out_filename) {
	   try {
		   Graph graph = new Graph();
		   graph.load(topo_filename);
		   
		   Routing routing = new Routing(graph);
		   routing.load(routing_filename);
		   
		   TrafficProfile profile = new TrafficProfile();
		   profile.addHose(graph);
		   
		   Solver solver;
		   solver = new ILPSolver(ILPSolver.EARLY_RATE_LIMIT);
		   //solver = new ILPSolver(ILPSolver.MIN_TOTAL_QUEUE);
		   //solver = new EdgeSolver(EdgeSolver.INGRESS_ONLY);
		   Solution sol = solver.solve(graph, routing, profile);
		   
		   if (sol == null) {
			   System.out.println("INFEASIBLE");
		   }
		   else {
			   showSol(out_filename, graph, profile, sol);
		   
			   Double x = evalSol(graph, profile, routing, sol);
			   return x.toString(); 
		   }
	   }
	   catch (Exception ex) {
		   ex.printStackTrace();
	   }
	   return null;   
	}
	
	
	
	public static void showSol(String filename, Graph graph, TrafficProfile profile, Solution sol) {
		try { 
			PrintWriter fout = new PrintWriter(new FileWriter(filename));
			double[][] ans = sol.getSol();
		
			for (int k = 0; k < profile.size(); ++k) {
				fout.println("Entry : " + profile.entryAt(k));		
				for (int i = 0; i < graph.n_link; ++i) {			
					if (ans[i][k] > EPS) {
						fout.print(Link.linkAt(i) + " ");
					}
				}
				fout.println("\n");
			}
			
			fout.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static double evalSol(Graph graph, TrafficProfile profile, Routing routing, Solution sol) {
		double[][] ans = sol.getSol();
		
		double total_weight = 0;
		int total_path = 0;
		for (int k = 0; k < profile.size(); ++k) {
        	Stub[] src = profile.entryAt(k).src;
        	Stub[] dst = profile.entryAt(k).dst;
        	
        	for (int i = 0; i < src.length; ++i) 
        		for (int j = 0; j < dst.length; ++j) {
        	       Stub src_stub = src[i];
        	       Stub dst_stub = dst[j];
        	       if (src_stub == dst_stub)
        	    	   continue;
        	       
        	       int src_id = src_stub.edgeSwitchId();
        	       int dst_id = dst_stub.edgeSwitchId();
        	       Switch src_switch = graph.getSwitchById(src_id);
        	       Switch dst_switch = graph.getSwitchById(dst_id);
        	       Path path = routing.getPathBySwitcheIds(src_id, dst_id);
        	       
        	       double hops = path.size() + 2;
        	       ++total_path;
        	       boolean find_cover = false;
        	       if (!find_cover) {
        	    	   Link first = src_switch.getFromLink(src_stub);
        	    	   if (ans[first.index()][k] > EPS) {
        	    		   total_weight += 1.0;
        	    		   find_cover = true;
        	    	   }
        	       }
        	       for (int t = 0; !find_cover && t < path.size(); ++t) {
        	    	   Link l = path.linkAt(t);
        	    	   if (ans[l.index()][k] > EPS) {
        	    		   total_weight += (hops - t - 1) / hops;
        	    		   find_cover = true;
        	    		   break;
        	    	   }
        	       }
        	       if (!find_cover) {
        	    	   Link last = dst_switch.getToLink(dst_stub);
        	    	   if (ans[last.index()][k] > EPS) {
        	    		   total_weight += 1.0 / hops;
        	    		   find_cover = true;
        	    	   }
        	       }
        		}
		}
		
		return total_weight / total_path;
	}
}
