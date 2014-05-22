package Solver;

public class Solution {
    double target;
    double[][] ans;
	
    public Solution(double[][] a, double t) {
	ans = a;
	target = t;
    }

    public Solution(double[][] a ){
	ans = a;
	target = -1; //not available
    }
    
    public double[][] getSol() {
	return ans;
    }
	
    public double getTarget() {
	return target;
    }
}
