package Policy;


import java.util.*;
import Topology.Graph;
import Topology.Stub;

public class TrafficProfile {

    public static int MAX_Q_PER_ENTRY = 4;
    public static int MAX_VPN_SIZE = 10;
    public static int SEED = 29;
    public static int NUM_VPN = 10;
	
    public class Entry {
	public Stub[] src;
	public Stub[] dst;
	
	
	public int totalQueue() {
	    return TrafficProfile.MAX_Q_PER_ENTRY;
	}
	
	public String toString() {
	    StringBuilder builder = new StringBuilder();
	    builder.append(src.length);
	    builder.append(" -> ");
	    builder.append(dst.length);
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
    public int max_vpn_size;
    
    public TrafficProfile() {
	entries = new Vector<Entry>();
	
    }
    
    public void addHose(Graph graph) {
	Stub[] stubs = graph.stubs;
	
	for (int i = 0; i < stubs.length; ++i) 
	    for (int k = 0; k < stubs[i].numEndpoints(); ++k) {
		//receiving hose
		Entry recv_e = new Entry();
		recv_e.src = stubs;
		recv_e.dst = new Stub[]{stubs[i]};
		entries.add(recv_e);
		//sending hose
		Entry send_e = new Entry();
		send_e.src = new Stub[]{stubs[i]};
		send_e.dst = stubs;
		entries.add(send_e);
	    }
    }
    
    public Entry entryAt(int kth) {
	return entries.elementAt(kth);
    }
    
    public int size() {
	return entries.size();
    }

    private static Stub[] sample(Stub[] stubs, int size, Random rand) {
	Stub[] res = new Stub[size];
	for (int i = 0; i < stubs.length; ++i) {
	    if (i < size)
		res[i] = stubs[i];
	    else {
		int r = rand.nextInt(i + 1);
		if (r < size)
		    res[r] = stubs[i];
	    }
	}
	return res;
    }
    

    public void addVPN(Graph graph) {
	if (NUM_VPN <= 0 || MAX_VPN_SIZE <= 1)
	    return;

	Stub[] stubs = graph.stubs;

	// pick endpoints into VPN
      	Random rand = new Random(SEED);
	max_vpn_size = 0;
	int upper = stubs.length / NUM_VPN * 2;
	for (int i = 0; i < NUM_VPN; ++i) {
	    int vpn_size = rand.nextInt(MAX_VPN_SIZE - 2) + 2;
	    Stub[] src = sample(stubs, vpn_size, rand);
	    Stub[] dst = src;
	    if (vpn_size > max_vpn_size)
		max_vpn_size = vpn_size;

	    Entry entry = new Entry();
	    entry.src = src;
	    entry.dst = dst;
	    entries.add(entry);
	}

    }


    // Segement VPN
    public void addVPN_(Graph graph) {

	if (NUM_VPN <= 0)
	    return;

	Stub[] stubs = graph.stubs;

	// pick endpoints into VPN
      	Random rand = new Random(SEED);
	int[] vpn_id = new int[stubs.length];
	int[] vpn_num_ep = new int[NUM_VPN];
	for (int i = 0; i < stubs.length; ++i) {
	    int j = rand.nextInt(NUM_VPN);
	    vpn_id[i] = j; 
	    vpn_num_ep[j]++;
	}

	// Create src
	Stub[][] src = new Stub[NUM_VPN][];
	Stub[][] dst = new Stub[NUM_VPN][];
	for (int j = 0; j < NUM_VPN; ++j) {
	    src[j] = new Stub[vpn_num_ep[j]];
	    dst[j] = new Stub[vpn_num_ep[j]];
	    if (vpn_num_ep[j] > max_vpn_size)
		max_vpn_size = vpn_num_ep[j];
	    vpn_num_ep[j] = 0;
	}

	for (int i = 0; i < stubs.length; ++i) {
	    int j = vpn_id[i];
	    src[j][vpn_num_ep[j]] = stubs[i];
	    dst[j][vpn_num_ep[j]] = stubs[i];
	    vpn_num_ep[j]++;
	}

	for (int j = 0; j < NUM_VPN; ++j) 
	    if (vpn_num_ep[j] == max_vpn_size) {
		Entry entry = new Entry();
		entry.src = src[j];
		entry.dst = dst[j];
		entries.add(entry);
	    }
    }
    
    
    // Not implemented
    public void optimize() {
    }
    
    
}
