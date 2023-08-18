package ICA;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import Utility.Arch;
import Utility.Node;

public class LocalMoran extends Autocorrelation{
				
	public LocalMoran(Node node, Set<Arch> nodeNeighbors, double min, double max) {
		super(node, nodeNeighbors, min, max);
		
		// TODO Auto-generated constructor stub
	}

	public double calculateAutocorrelation(double label) {
		double localMoranI = 0.0;

		double AVG_Y = 0.0;
		double Yi  = label;
		double min = label;
		double max = Yi;	
		
		Map<Node,Double> l = new HashMap<Node,Double>();
		
		Iterator<Arch> it = nodeNeighbors.iterator();
		while(it.hasNext()){
			Arch a = it.next();
			Node node1 = this.node.getNeighbor(a);
			if(node1.getIsTrain()){
				if(node1.getLabel() < min)
					min = node1.getLabel();
				if(node1.getLabel() > max)
					max = node1.getLabel();				
				Double weight = (Double) a.getWeight();		
				l.put(node1, (Double) weight);
			}
		}
		
		int n = l.size();			
		int j = 0;
		double sum_Y = 0.0;
		double sum_W_i_j = 0.0;
		double wij[ ] = new double[n];
		
		for(Node neighbor : l.keySet()){
			double distance = (l.get(neighbor)-this.min_distance)/(this.max_distance-this.min_distance);
			sum_Y += ((neighbor.getLabel() - min) / (max - min));
			if(distance > 0)
				 wij[j] = 1.0 - distance;	
			else
				wij[j] = 1;		
			sum_W_i_j += wij[j];
			j++;
		}
		AVG_Y = sum_Y / n;
		double sum_W_i_j_Yj_AVG_Y = 0.0; 
		double sum_Yj_AVG_Y = 0.0;
		double sumWij = 0;
		j=0;
		for(Node neighbor : l.keySet()){							
			double Yj = (neighbor.getLabel()-min)/(max-min);
			sum_Yj_AVG_Y += Math.pow(Yj - AVG_Y, 2);
			sum_W_i_j_Yj_AVG_Y += Math.pow(Yj - AVG_Y, 2) * (wij[j]/sum_W_i_j);		
			sumWij += wij[j]/sum_W_i_j;
			j++;		
		}			
		double Wi = sumWij;
		localMoranI = -1.0;
		if( n < 2 )
			localMoranI = 0.0;
		else{
			double numerator = ( ( (Yi - min)/(max - min) ) - AVG_Y ) * sum_W_i_j_Yj_AVG_Y;							
			double denominator = sum_Yj_AVG_Y / n;		
			if(numerator == 0)
				localMoranI = 0.0;
			else{
				if(denominator == 0)
					localMoranI = 1.0;
				else 
					localMoranI = Math.abs((numerator / denominator) - ( - Wi/ (n - 1) ));
			}			
		}
	
		return localMoranI;
	} 
}
