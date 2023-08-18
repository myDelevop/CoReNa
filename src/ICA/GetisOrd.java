package ICA;

import java.util.Set;

import Utility.Arch;
import Utility.Node;

public class GetisOrd extends Autocorrelation{	
	
	public GetisOrd(Node node, Set<Arch> nodeNeighbors, double min, double max) {
		super(node, nodeNeighbors,min,max);
	}

	public double calculateAutocorrelation(double label){
		double gi = 0;
		double Sum_wij_xj = 0;
		double Sum_wij = 0;
		double Sum_xj = 0;
		
		double Sum_wijSquared = 0;
		double Sum_xjSquared = 0;
		int n = 0;
	
		for(Arch a : nodeNeighbors){
			if(node.getNeighbor(a).getIsTrain()){
				double distance = (((Double) a.getWeight()-min_distance)/(max_distance-min_distance));
				double wij = ( 1 - distance ) ;
				double xj = node.getNeighbor(a).getLabel();
				Sum_wij_xj += (wij * xj);
				Sum_wij += wij;
				Sum_xj += xj;
				Sum_wijSquared += Math.pow(wij, 2);
				Sum_xjSquared += Math.pow(xj, 2);
				n++;
			}
		}
		Sum_wij += 1;
		Sum_xj += label;
		Sum_wij_xj += label;
		Sum_wijSquared += Math.pow(1, 2);
		Sum_xjSquared += Math.pow(label, 2);
		n++;
		double numerator = 0;
		double denominator = 0;
		if(n > 2){ 
			 double X = Sum_xj / n;
			 double S = Math.sqrt( (Sum_xjSquared / n) - Math.pow(X, 2) );
			 double a = Math.abs( (n * Sum_wijSquared) - Math.pow(Sum_wij, 2) );			 
			 numerator = Sum_wij_xj - (X * Sum_wij);
			 denominator = S * ( Math.sqrt(a / (n-1)) );			
		}
		
		if(numerator == 0)
				gi = 0.0;
			else{
				if(denominator == 0)
					gi = 1.0;
				else
					gi = numerator / denominator;
			}
		
			return Math.abs(gi);
		
	
	}
	
}
