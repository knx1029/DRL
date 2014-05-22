/**
 * Author: Nanxi Kang (nkang@cs.princeton.edu) 
 * All rights reserved.
 *
 * This file stores essential topology information for LP.
 */

package Topology;

import java.io.*;
import java.util.*;

public class Graph {

    public static double eps = 1e-6;

    public static int NUM_IN_QUEUE = 8;
    public static int NUM_OUT_QUEUE = 8;
    
    // random number of endpoints 
    public static int MIN_EP = 1;
    public static int MAX_EP = 2;
    public static int SEED = 29;
    
    public int n_switch;
    public int n_link;
    public int n_stub;
    public int n_endpoints;
    // Number of rules needed by path
    
    public Switch[] switches;
    public Stub[] stubs;
    public Link[][] links;

    private Random rand;
    
    public Graph(){
	rand = new Random(SEED);
	
    }
	
    public void load(String filename) throws IOException {
	BufferedReader in = new BufferedReader(new FileReader(filename));
	StringTokenizer token;
	
	token = new StringTokenizer(in.readLine());
	n_switch = Integer.parseInt(token.nextToken());
	n_link = Integer.parseInt(token.nextToken());
	Link.init(n_link * 2);
	
	switches = new Switch[n_switch];
	for (int i = 0; i < n_switch; ++i)
	    switches[i] = new Switch(i, NUM_IN_QUEUE, NUM_OUT_QUEUE);
	
	links = new Link[n_switch][n_switch];
	for (int i = 0; i < n_link; ++i) {
	    token = new StringTokenizer(in.readLine());
	    int from = Integer.parseInt(token.nextToken());
	    int to = Integer.parseInt(token.nextToken());
	    
	    Switch.addLink(switches[from], switches[to]);
	    links[from][to] = switches[from].getToLink(switches[to]);
	    links[to][from] = switches[to].getToLink(switches[from]);
	}
	
	n_stub = Integer.parseInt(in.readLine());
	n_endpoints = 0;
	stubs = new Stub[n_stub];
	for (int i = 0; i < n_stub; ++i) {
	    int idx = Integer.parseInt(in.readLine());
	    switches[idx].setEdge();
	    int endpoints = rand.nextInt(MAX_EP - MIN_EP) + MIN_EP;
	    n_endpoints += endpoints;
	    stubs[i] = new Stub(idx, endpoints);
	    switches[idx].addStub(stubs[i]);
	}
		
	n_link = Link.Total_Links;
	System.out.println(n_switch + " " + n_stub + " " + n_link + " " + n_endpoints);
	in.close();

	// For testing
	//useEdgeSwitchOnly();
    }

    public Link pickSwitchLink() {
	while (true) {
	    int kth = rand.nextInt(n_link);
	    Link l = Link.linkAt(kth);
	    Point from = l.from();
	    Point to = l.to();
	    int fromId = from.switchId();
	    int toId = to.switchId();
	    if (from instanceof Port && to instanceof Port &&
		links[fromId][toId] != null) {
		return l;
	    }
	}
    }

    public boolean bringDownLink(Link l) {
	if (l == null)
	    return false;
	Point from = l.from();
	Point to = l.to();
	int fromId = from.switchId();
	int toId = to.switchId();
	if (from instanceof Port && to instanceof Port) {	    
	    links[fromId][toId] = null;
	    return true;
	}
	else
	    return false;
    }
    

    public boolean bringUpLink(Link l) {
	if (l == null)
	    return false;

	Point from = l.from();
	Point to = l.to();
	int fromId = from.switchId();
	int toId = to.switchId();
	if (from instanceof Port && to instanceof Port) {	    
	    links[fromId][toId]  = l;
	    return true;
	}
	else
	    return false;
    }

    // For testing
    public void useEdgeSwitchOnly() {
	for (int i = 0; i < n_switch; ++i)
	    switches[i].emptyNonEdgeSwitchPort();
    }
    
    public Switch getSwitchById(int id) {
	return switches[id];
    }
    
    public Link getLinkBySwitches(Switch from, Switch to) {
	return links[from.getId()][to.getId()];
    }
    
    public Link getLinkBySwitchIds(int from, int to) {
	return links[from][to];
    }
	
}
