package Policy;

import java.io.*;
import java.util.*;
import Topology.*;

public class Routing {

	Graph graph;
	Path[][] routing;
	
	public Routing(Graph graph) {
		this.graph = graph;
		routing = new Path[graph.n_switch][graph.n_switch];
	}
	
	public void load(String filename) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(filename));
		StringTokenizer token;
		int n_path = Integer.parseInt(in.readLine());
		
		for (int j = 0; j < n_path; ++j) {
			Path path = new Path();
			token = new StringTokenizer(in.readLine());
			int m = Integer.parseInt(token.nextToken());
			int from = -1, to = -1;
			int first = -1, last = -1;
			for (int i = 0; i < m; ++i) {
				to = Integer.parseInt(token.nextToken());
				if (i > 0)
					path.addLink(graph.getLinkBySwitcheIds(from, to));
				else first = to;
				from = to;
		     }
			last = to;
			routing[first][last] = path;
		 }
	}
	
	public Path getPathBySwitches(Switch from, Switch to) {
		return routing[from.getId()][to.getId()];
	}
	
	public Path getPathBySwitcheIds(int from, int to) {
		return routing[from][to];
	}
}
