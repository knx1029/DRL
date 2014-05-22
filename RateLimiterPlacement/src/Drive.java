
import Solver.*;
import Topology.*;
import Policy.*;
import java.io.*;
import java.util.Vector;

public class Drive {

    public static double EPS = 1e-6;
    
    public static final String VPN = "VPN";
    public static final String HOSE = "HOSE";
    public static final String MIX = "MIX";

    public static String MODE;

    private static final int TESTCASES = 10;
    private static final String FOLDER = "../../Data_Prepare/ts100/";
    private static final String TOPO_FILENAME = FOLDER + "ts100-%d-topo.txt";
    private static final String ROUTING_FILENAME = FOLDER + "ts100-%d-routing.txt";
    private static final String OUT_FILENAME = FOLDER + "ts100-%d-1-bfs_ILP.txt";
    
    //private static final String TOPO_FILENAME = "./ts100/ts100-%d-topo.txt";
    //private static final String ROUTING_FILENAME = "./ts100/ts100-%d-routing.txt";
    //private static final String OUT_FILENAME = "./ts100/ts100-%d-1-bfs_ILP.txt";
	 
    public static boolean config_args(String[] args) {
	if (args.length < 6) {
	    System.out.print("ARGS: #IN_QUEUE, #OUT_QUEUE, MIN_EP, MAX_EP, #QUEUE_PER_ENTRY");
	    System.out.println(", MODE(HOSE|VPN|MIX),  [NUM_VPN, MAX_VPN_SIZE] [SEED]");
	    return false;
	}

	Graph.NUM_IN_QUEUE = Integer.parseInt(args[0]);
	Graph.NUM_OUT_QUEUE = Integer.parseInt(args[1]);
	Graph.MIN_EP = Integer.parseInt(args[2]);
	Graph.MAX_EP = Integer.parseInt(args[3]);
	TrafficProfile.MAX_Q_PER_ENTRY = Integer.parseInt(args[4]);
	MODE = args[5];

	if (args.length > 7) {
	    TrafficProfile.NUM_VPN = Integer.parseInt(args[6]);
	    TrafficProfile.MAX_VPN_SIZE = Integer.parseInt(args[7]);
	}

	if (args.length > 8) {
	    Graph.SEED = TrafficProfile.SEED = Integer.parseInt(args[8]);
	}

	return true;
		
    }

    public static void main(String[] args) {
	
	if (!config_args(args))
	    return;
        
	System.out.println("RECORD : " + ResultRecord.FORMAT_HEADER);
	ResultRecord[] xs = new ResultRecord[TESTCASES];
	for (int testcase = 0; testcase < TESTCASES; ++testcase) {
	    ResultRecord x  = run(String.format(TOPO_FILENAME, testcase), 
	    //	    ResultRecord x  = update(String.format(TOPO_FILENAME, testcase), 
				     String.format(ROUTING_FILENAME, testcase),
				     String.format(OUT_FILENAME, testcase),
				     testcase);
	    xs[testcase] = x;
	}
	
	//	System.out.println("idx : #switch, #links, #stubs, #eps");
	//	System.out.println("#queues, #entries, #vpn_size" );
	//	System.out.println("#r.max_"
	int cnt_x = 0;
	double min_x = 2, max_x = -1, avg_x = 0;
	for (int testcase = 0; testcase < TESTCASES; ++testcase) {
	    ResultRecord r = xs[testcase];
	    if (r == null)
		continue;
	    
	    if (r.x < 0)
		continue;
	    //	    System.out.println(r.n_switch + ", " + r.n_link + ", " + r.n_stub + ", " + r.n_ep + 
	    //			       ", " + r.n_queue + ", " + r.n_entry + ", "  + 
	    //  r.max_vpn_size + ", " + r.x);
	    if (r.x < min_x)
		min_x = r.x;
	    if (r.x > max_x)
		max_x = r.x;
	    ++cnt_x;
	    avg_x += r.x;
	}
	avg_x /= cnt_x;
	System.out.println("MIN | MAX | AVG");
	System.out.println("RES, " + Graph.NUM_OUT_QUEUE + ", " + min_x + ", " + max_x + ", " + avg_x);
    }
    
    public static void reset() {
	Link.reset();
    }
    
    public static ResultRecord run(String topo_filename, String routing_filename, String out_filename, int testcase) {
	try {
	    reset();
	    
	    Graph graph = new Graph();
	    graph.load(topo_filename);
	    
	    Routing routing = new Routing(graph);
	    routing.load(routing_filename);
	    //routing = newRouting(graph);
	    //Routing new_routing = newRouting(graph);
	    
	    TrafficProfile profile = new TrafficProfile();
	    if (MODE.equals(HOSE) || MODE.equals(MIX)) 
		profile.addHose(graph);
	    if (MODE.equals(VPN) || MODE.equals(MIX))
		profile.addVPN(graph);
	    
	    Solver solver;
	    solver = new ILPSolver(ILPSolver.EARLY_RATE_LIMIT);
	    //solver = new ILPSolver(ILPSolver.MIN_TOTAL_QUEUE);
	    //solver = new EdgeSolver(EdgeSolver.INGRESS_ONLY);
	    Solution sol = solver.solve(graph, routing, profile);
	    
	    // Save the result
	    ResultRecord res = new ResultRecord();
	    res.n_switch = graph.n_switch;
	    res.n_link = graph.n_link;
	    res.n_stub = graph.n_stub;
	    res.n_ep = graph.n_endpoints;
	    res.n_queue = 0;
	    res.n_entry = profile.size();
	    res.max_vpn_size = profile.max_vpn_size;
	    for (int i = 0; i < graph.n_link; ++i) {
		Link l = Link.linkAt(i);
		res.n_queue += l.totalQueue();
	    }


	    if (sol == null) {
		res.x = -1;
		System.out.println("INFEASIBLE");
	    }
	    else {
		showSol(out_filename, graph, profile, sol, testcase);
		evalSol(graph, profile, routing, sol, res, testcase);
	    }
	    return res;
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	}
	return null;   
    }


    public static ResultRecord update(String topo_filename, String routing_filename, String out_filename, int testcase) {
	try {
	    reset();
	    
	    Graph graph = new Graph();
	    graph.load(topo_filename);
	    
	    Routing routing = new Routing(graph);
	    routing.load(routing_filename);
	    
	    TrafficProfile profile = new TrafficProfile();
	    if (MODE.equals(HOSE) || MODE.equals(MIX)) 
		profile.addHose(graph);
	    if (MODE.equals(VPN) || MODE.equals(MIX))
		profile.addVPN(graph);
	    
	    Solver solver;
	    solver = new ILPSolver(ILPSolver.EARLY_RATE_LIMIT);
	    //solver = new ILPSolver(ILPSolver.MIN_TOTAL_QUEUE);
	    //solver = new EdgeSolver(EdgeSolver.INGRESS_ONLY);
	    Solution sol = solver.solve(graph, routing, profile);
	    
	    // Save the result
	    ResultRecord res = new ResultRecord();
	    res.n_switch = graph.n_switch;
	    res.n_link = graph.n_link;
	    res.n_stub = graph.n_stub;
	    res.n_ep = graph.n_endpoints;
	    res.n_queue = 0;
	    res.n_entry = profile.size();
	    res.max_vpn_size = profile.max_vpn_size;
	    for (int i = 0; i < graph.n_link; ++i) {
		Link l = Link.linkAt(i);
		res.n_queue += l.totalQueue();
	    }


	    if (sol == null) {
		res.x = -1;
		System.out.println("INFEASIBLE");
	    }
	    else {
		//		evalSol(graph, profile, routing, sol, res, testcase);
		for (int failed_link = 1; failed_link <= 5; ++failed_link) {
		    Routing new_routing = newRouting(graph);
		    //		new_routing = newRouting(graph);
		    ILPSolver new_solver;
		    //new_solver = new ILPSolver(ILPSolver.BALANCE_UPDATE);
		    //new_solver.setAlpha(1);
		    //new_solver = new ILPSolver(ILPSolver.EARLY_RATE_LIMIT);
		    new_solver = new ILPSolver(ILPSolver.MIN_DIFF);
		    new_solver.setRefSol(sol);
		    Solution new_sol = new_solver.solve(graph, new_routing, profile);
		    evalSol(graph, profile, new_routing, new_sol, res, testcase);
		    //sol = new_sol;
		}
	    }
	    return res;
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	}
	return null;   
    }
    

    private static Routing newRouting(Graph graph) {
	while (true) {
	    Link l = null, rev_l = null;
	    Point from, to;
	    int fromId = 0, toId = 0;
	    boolean success;
	    Routing r;
	    try {
		l = graph.pickSwitchLink();
		from = l.from();
		to = l.to();
		fromId = from.switchId();
		toId = to.switchId();
		
		success = true;
		success = success && graph.bringDownLink(l);
		rev_l = graph.getLinkBySwitchIds(toId, fromId);
		success = success && graph.bringDownLink(l);
		r = new Routing(graph);
		r.shortestPath();

		if (!success) {
		    graph.bringUpLink(l);
		    graph.bringUpLink(rev_l);
		}
		else
		    return r;
	    }
	    catch (Exception e) {
		System.out.println("failed: <" + fromId + ", " + toId + ">");
		graph.bringUpLink(l);
		graph.bringUpLink(rev_l);
		continue;
	    }

	}
    }
    
    public static void showSol(String filename, Graph graph, TrafficProfile profile, Solution sol, int testcase) {
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
    
    public static void evalSol(Graph graph, TrafficProfile profile, Routing routing, Solution sol,
			       ResultRecord r, int testcase) {
	r.target = sol.getTarget();
	double[][] ans = sol.getSol();
	
	double total_weight = 0;
	int total_path = 0;
	boolean[] edgeRatio = new boolean[graph.n_switch];
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
        	    
		    //		    double hops = path.size() + 1;
		    double hops = path.size();
		    ++total_path;
		    boolean find_cover = false;
		    if (!find_cover) {
			Link first = src_switch.getFromLink(src_stub);
			if (ans[first.index()][k] > EPS) {
			    total_weight += 1.0;
			    //total_weight += 0.0;
			    find_cover = true;
			}
		    }
		    for (int t = 0; !find_cover && t < path.size(); ++t) {
			Link l = path.linkAt(t);
			if (ans[l.index()][k] > EPS) {
			    //			    total_weight += (hops - t - 1) / hops;
			    total_weight += (hops - t) / hops;
			    //total_weight += t / hops;
			    edgeRatio[l.from().switchId()] = true;
			    find_cover = true;
			    break;
			}
		    }
		    if (!find_cover) {
			Link last = dst_switch.getToLink(dst_stub);
			if (ans[last.index()][k] > EPS) {
			    total_weight += 0.0;
			    //			    total_weight += 1.0;
			    edgeRatio[last.from().switchId()] = true;
			    find_cover = true;
			}
		    }
		}
	}
	
	int total_sw = 0, total_edge_sw = 0;
	for (int i = 0; i < graph.n_switch; ++i) 
	    if (edgeRatio[i]) {
		++total_sw;
		Switch sw = graph.getSwitchById(i);
		if (sw.isEdge())
		    ++total_edge_sw;
	    }

	r.total_sw = total_sw;
	r.total_edge_sw = total_edge_sw;
	r.x = total_weight / total_path;

        System.out.print("RECORD");
	System.out.print(testcase + " : ");
	System.out.println(r);
    }
}
