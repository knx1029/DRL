package Topology;

import java.util.*;

public class Link {

    private Point from;
    private Point to;
    private int index;
    
    public static int Total_Links = 0;
    
    private static Vector<Link> Link_Map;
    
    public static void reset() {
	Link_Map = null;
	Total_Links = 0;
    }
	
    public static void init(int size) {
	Link_Map = new Vector<Link>(size);
    }

    public static Link linkAt(int kth) {
	return Link_Map.elementAt(kth);
    }
	
    public Link(Point from, Point to) {
	this.from = from;
	this.to = to;
	this.index = Total_Links ++;
	Link.Link_Map.add(this);
    }
	
    public int index() {
	return this.index;
    }
	
    public int totalQueue() {
	return from.numOutQueue() + to.numInQueue();
    }

    public Point from() {
	return from;
    }

    public Point to() {
	return to;
    }
	
    public String toString() {
	return "(" + from + ", " + to + ")";
    }
    
}
