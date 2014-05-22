package Topology;

public class Port implements Point {
	
    private int id;
    private int num_in_queue;
    private int num_out_queue;
    
    public Port(int id, int num_in_queue, int num_out_queue) {
	this.id = id;
	this.num_in_queue = num_in_queue;
	this.num_out_queue = num_out_queue;
    }
    
    @Override
    public int numInQueue() {
	return this.num_in_queue;
    }
    
    @Override
    public int numOutQueue() {
	return this.num_out_queue;
    }

    @Override
    public int switchId() {
	return this.id;
    }

    // For testing
    public void emptyQueues() {
	num_in_queue = num_out_queue = 0;
    }

    public String toString() {
	return "Port@" + id;
    }

}
