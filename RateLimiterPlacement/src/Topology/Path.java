package Topology;

import java.util.*;

public class Path {
    Vector<Link> links;
    
    public Path() {
    	links = new Vector<Link>();
    }
    
    public void addLink(Link a) {
    	links.add(a);
    }
    
    public int size() {
    	return links.size();
    }
    
    public Link linkAt(int kth) {
    	return links.elementAt(kth);
    }
}
