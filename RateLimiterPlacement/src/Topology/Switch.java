package Topology;

import java.util.*;

public class Switch {

    private int id;
    private int in_queue;
    private int out_queue;
    private boolean is_edge;
    
    private Map<Switch, Link> to_switch_links;
    private Map<Switch, Link> from_switch_links;
    
    private Map<Stub, Link> to_stub_links;
    private Map<Stub, Link> from_stub_links;

    private Vector<Port> ports;
    
    public Switch(int id, int in_queue, int out_queue) {
	this.in_queue = in_queue;
	this.out_queue = out_queue;
	this.id = id;
	this.is_edge = false;
	
	from_switch_links = new HashMap<Switch, Link>();
	to_switch_links = new HashMap<Switch, Link>();
	
	to_stub_links = new HashMap<Stub, Link>();
	from_stub_links = new HashMap<Stub, Link>();

	ports = new Vector<Port>();
    }
    
    public int getId() {
	return this.id;
    }
    
    public void setEdge() {
	is_edge = true;
    }
    
    public boolean isEdge() {
	return is_edge;
    }
    
    public Port genPort() {
	Port p = new Port(id, in_queue, out_queue);
	ports.add(p);
	return p;
    }
    
    public void addLink(Switch s, Link to_s, Link from_s) {
	from_switch_links.put(s, from_s);
	to_switch_links.put(s, to_s);
    }
    
    public void addStub(Stub s) {
	Port port = this.genPort();
	Link link_to_s = new Link(port, s);
	Link link_from_s = new Link(s, port);
	
	to_stub_links.put(s, link_to_s);
	from_stub_links.put(s, link_from_s);
    }
    
    public Link getToLink(Switch to) {
	return to_switch_links.get(to);
    }
    
    
    public Link getFromLink(Switch from) {
	return from_switch_links.get(from);
    }
    
    public Link getToLink(Stub to) {
	return to_stub_links.get(to);
    }
    
    
    public Link getFromLink(Stub from) {
	return from_stub_links.get(from);
    }
    
    // for comparison
    public void emptyNonEdgeSwitchPort() {
	if (is_edge)
	    return;

	for (Port p : ports) 
	    p.emptyQueues();
    }

    
    public static void addLink(Switch a, Switch b) {
	Port a_port = a.genPort();
	Port b_port = b.genPort();
	Link link_ab = new Link(a_port, b_port);
	Link link_ba = new Link(b_port, a_port);
	
	a.addLink(b, link_ab, link_ba);
	b.addLink(a, link_ba, link_ab);
    }
}
