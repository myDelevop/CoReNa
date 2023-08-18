package ICA;

import java.util.Set;

import Utility.Arch;
import Utility.Node;

public abstract class Autocorrelation {
	Set<Arch> nodeNeighbors;
	Node node;
	double min_distance;
	double max_distance;
	
	public Autocorrelation(Node node, Set<Arch> nodeNeighbors, double min_distance, double max_distance){
		this.nodeNeighbors =  nodeNeighbors;
		this.node = node;		
		this.min_distance = min_distance;
		this.max_distance = max_distance;
	}
	public abstract double calculateAutocorrelation(double label);
	
}
