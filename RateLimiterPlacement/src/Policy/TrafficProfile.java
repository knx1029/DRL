package Policy;


import java.util.*;
import Topology.Graph;
import Topology.Stub;

public class TrafficProfile {
	
	public class Entry {
		public Stub[] src;
		public Stub[] dst;
		public static final int MAX_Q_PER_ENTRY = 4;

		
		public int totalQueue() {
			return MAX_Q_PER_ENTRY;
		}
		
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("src :[");
			for (int i = 0; i < src.length; ++i)
				builder.append(src[i] + ", ");
			builder.append("],  dst :[");
			for (int i = 0; i < dst.length; ++i)
				builder.append(dst[i] + ", ");
			builder.append("]");
			
			return builder.toString();
		}
	}
	
	private Vector<Entry> entries;

	public TrafficProfile() {
		entries = new Vector<Entry>();
		
	}
	
	public void addHose(Graph graph) {
		Stub[] stubs = graph.stubs;
		
		for (int i = 0; i < stubs.length; ++i) 
		for (int k = 0; k < stubs[i].numEndpoints(); ++k) {
			Entry recv_e = new Entry();
			recv_e.src = stubs;
			recv_e.dst = new Stub[]{stubs[i]};
			
			Entry send_e = new Entry();
			send_e.src = new Stub[]{stubs[i]};
			send_e.dst = stubs;
			
			entries.add(recv_e);
			entries.add(send_e);
		}
	}
	
	public Entry entryAt(int kth) {
		return entries.elementAt(kth);
	}
	
	public int size() {
		return entries.size();
	}
	
	//Not implemented
	public void addVPN() {
		
	}
	
	
}
