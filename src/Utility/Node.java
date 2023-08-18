package Utility;

import java.util.HashMap;
import java.util.TreeMap;

import icaData.Tuple;

public class Node implements Comparable<Node>,Cloneable {

	double ID;
	private Tuple Values;
	private Double Label;
	private boolean isTrain=true;
	boolean ignored = false;
	public boolean visited=false;
	public HashMap<Node,Object> indirectNeighbors = new HashMap<Node,Object>(); 
	
	public Node(double d, Tuple Values){	
		this.Values = Values;
		this.Label = Values.getTarget();
		isTrain = true;		
		this.ID = d;
	}
		
	@SuppressWarnings("unchecked")
	public Node clone(){
		Node n = new Node(this.ID,this.Values.clone());
		n.isTrain = this.isTrain;
		n.ignored = this.ignored;
		n.Label   = new Double(this.Label);
		n.indirectNeighbors = (HashMap<Node, Object>) this.indirectNeighbors.clone();
		return n;
	}
	
	public double getId(){
		return ID;
	}
	public boolean hasBeenVisited(){
		return visited;
	}
	public void setVisited(boolean visited){
		this.visited = visited;
	}
	
	public void setIgnored(boolean ignored){
		this.ignored = ignored;
	}
	public boolean isIgnored(){
		return ignored;
	}
	public Tuple getValues(){
		return Values;
	}
	public Node getNeighbor(Arch a){
		if(this.equals(a.getX()))
			return a.getY();
		else
			return a.getX();
	}
	public TreeMap<String,Object> getAggr(){
		return aggr;
	}
	
	public Double getLabel(){
		return Label;
	}
	
	public void setLabel(Double label){
		this.Label = label;
		Values.setTarget(label);
	}
	
	public void setTrain(boolean b){
		isTrain = b;
	}
	
	public boolean getIsTrain(){
		return isTrain;
	}
	
	public int getNumAttributes(){
		return this.Values.getLength();
	}
	
	private TreeMap<String,Object> aggr = new TreeMap<String,Object>();

	@SuppressWarnings("unchecked")
	public void extendNode(String aggregationType,Object value){
		if(aggregationType.compareTo("weightedAverage") == 0)// || aggregationType.compareTo("Discretize") == 0)
			aggr.put(aggregationType, value);
		else
			this.aggr = (TreeMap<String, Object>) value;			
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result + (int)ID);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
	
		Node other = (Node) obj;
		if (compareTo(other) == 0)
			return true;
		
		return false;
	}
	public String toString(){
		String tmp = "";
		tmp += this.ID + this.Values.toString() + this.isTrain;
		return tmp;
	}
	@Override
	public int compareTo(Node arg0) {
		// TODO Auto-generated method stub
		int result=1;	
		if(this.ID == arg0.ID)
			result = 0;
		
		return result;
		}
}
