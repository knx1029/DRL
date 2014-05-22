package Topology;

public class Stub implements Point {
	
	private int id;
	private int num_endpoints;

	public Stub(int id, int num_ep) {
		this.id = id;
		this.num_endpoints = num_ep;
	}
	
        @Override
	public int switchId() {
		return id;
	}
	
	public int numEndpoints() {
		return this.num_endpoints;
	}
	
	@Override
	public int numInQueue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int numOutQueue() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public String toString() {
		return "Stub@" + id;
	}
}
