package Solver;

import Policy.*;
import Topology.Graph;

public interface Solver {
	
	public Solution solve(Graph graph, Routing routing, TrafficProfile profile);
}
