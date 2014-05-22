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


    // run shortest path algorithm on the graph
    public void shortestPath() throws Exception{
	int n_switch = graph.n_switch;
	int infinity = n_switch * n_switch;
	int[][] dist = new int[n_switch][n_switch];
	int[][] mid = new int[n_switch][n_switch];

	for (int i = 0; i < n_switch; ++i)
	    for (int j = 0; j < n_switch; ++j) {
		if (i == j)
		    dist[i][j] = 0;
		else {
		    Link l = graph.getLinkBySwitchIds(i, j);
		    if (l == null)
			dist[i][j] = infinity;
		    else
			dist[i][j] = 1;
		}
	    }
		

	for (int k = 0; k < n_switch; ++k)
	    for (int i = 0; i < n_switch; ++i)
		for (int j = 0; j < n_switch; ++j) 
		    if (dist[i][k] + dist[k][j] < dist[i][j]) {
			dist[i][j] = dist[i][k] + dist[k][j];
			mid[i][j] = k;
		    }
	for (int i = 0; i < n_switch; ++i) {
	    Switch from = graph.getSwitchById(i);
	    if (!from.isEdge())
		continue;
	    for (int j = 0; j < n_switch; ++j) 
		if (i != j) {
		    Switch to = graph.getSwitchById(j);
		    if (!to.isEdge())
			continue;
		    if (dist[i][j] == infinity) {
			throw new Exception("Unconnected graph!");
		    }

		    Path path = new Path();
		    constructPath(path, i, j, mid, dist);
		    routing[i][j] = path;
		}
	}
    }

    // Recursively find the links along the path
    private void constructPath(Path p, int from, int to, int[][] mid, int[][] dist) {
	if (dist[from][to] == 1) {
	    Link l = graph.getLinkBySwitchIds(from, to);
	    p.addLink(l);
	}
	else {
	    int via = mid[from][to];
	    constructPath(p, from, via, mid, dist);
	    constructPath(p, via, to, mid, dist);
	}
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
		if (i > 0) {
		    Link l = graph.getLinkBySwitchIds(from, to);
		    path.addLink(l);
		}
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
