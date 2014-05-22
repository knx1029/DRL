public class ResultRecord {
    double x;
    int n_switch;
    int n_stub;
    int n_link;
    int n_ep;
    int n_queue;
    int n_entry;
    int max_vpn_size;
    int total_sw;
    int total_edge_sw;
    double target;

    public static final String FORMAT_HEADER = "#switches, #stubs, #links, #eps, #queues, #entries, max_vpn_size, total_sw, total_edge_sw, perf, [target]";
    
    ResultRecord() {
    }

    public String toString() {
	return n_switch + "," + n_stub + "," + n_link + "," + n_ep + "," + n_queue + "," + n_entry + 
	    "," + max_vpn_size + "," + total_sw + "," + total_edge_sw + "," + x + "," + target;
    }
}
