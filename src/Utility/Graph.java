package Utility;

import icaData.Attribute;
import icaData.ContinuousAttribute;
import icaData.Data;
import icaData.DiscreteAttribute;
import icaData.Tuple;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class Graph implements Cloneable {
	HashMap<Node, TreeSet<Arch>> nodi;
	public boolean isSpatial = false;
	String attributesOfExtensionNames;
	Data data;
	double max_distance;
	double min_distance;

	  public Graph(Data data) {
	    nodi = new HashMap<Node, TreeSet<Arch>>();
	    this.data=data;
	  }
	  public void setMax(double max){
		  this.max_distance=max;
	  }
	  public void setMin(double min){
		  this.min_distance=min;
	  }
	  public double getMax(){
		  return this.max_distance;
	  }
	  public double getMin(){
		  return this.min_distance;
	  }
	  public int nodesNumber() {
	    return nodi.size();
	  }

	  /**
	   * add(x) aggiunge un nodo al grafo con valore x se non esiste, nulla altrimenti
	   * L'aggiunta di un nodo significa aggiungere la coppia (x, lista) nella HashMap
	   * dove lista è una HashSet nuovo vuoto.
	   */
	  public void add(Node x) {
	    if (!nodi.containsKey(x)) 
	      nodi.put(x,new TreeSet<Arch>());	    
	  }
	  
	  public boolean existsArch(Node n1, Node n2){
		   return nodi.get(n1).contains(new Arch(n1,n2,0));
	  }
	  
	
	  public void remove(Node x) {
	    if (nodi.containsKey(x)) {
	      Iterator<Arch> arcoIncidenteI = nodi.get(x).iterator();
	      Arch a;
	      Object y;
	      while (arcoIncidenteI.hasNext()) {
	        a = (Arch) arcoIncidenteI.next();
	        y = ( a.getX().equals(x) ) ? a.getY() : a.getX();
	       this.remove(x,(Node)y,(double)a.getWeight());
	        
	      }
	      nodi.remove(x);
	    }
	  }


	  /**
	   * add(x,y,v) aggiunge un arco tra i nodi x e y con peso v
	   */
	  public boolean add(Node x, Node y, Object value) {
	    boolean flag = false;
	    if (nodi.containsKey(x) && nodi.containsKey(y)){
	    	Arch a1 = new Arch(x,y,value);	    
	    	Arch a2 = new Arch(y,x,value);	 
	    	if(nodi.get(x).add(a1) && nodi.get(y).add(a2)){
	    		flag=true; 		
	    	}
	    }
	    return flag;
	  }

	  public boolean add(Arch a) {
	    return add(a.getX(),a.getY(),a.getWeight());
	  }
	
	  public boolean remove(Node x, Node y,double value) {
	    Arch a = new Arch(x,y,value);
	    return remove(a);
	  }

	  public int getNumberOfNodes(){
		  return this.nodi.size();
	  }
	 
	  public int getNumberOfTestNodes(){
		 int count = 0;
		 for(Node n : nodi.keySet())
			 if(n.getIsTrain() == false)
				 count++;
		 return count;
	  }
	  
	  public int getNumberOfTrainNodes(){
		 int count = 0;
		 for(Node n : nodi.keySet())
			 if(n.getIsTrain() == true)
				 count++;
		 return count;
	  }
	  public boolean remove(Arch a) {
	    boolean flag = false,  flag1 = false;
	    if (nodi.containsKey(a.getX()) && nodi.containsKey(a.getY())) {
	      flag = (nodi.get(a.getX())).remove(a);
	      flag1 = (nodi.get(a.getY())).remove(a);
	   
	    }
	    return flag || flag1;
	  }


	  public Set<Arch> getEdgeSet(Node nodo) {
		  return nodi.get(nodo);
	  }


	  public Set<Node> getNodeSet() {
	    return nodi.keySet();
	  }
	  
	  public Node getNodeWithID(double id){		  
		  Iterator<Node> it = nodi.keySet().iterator();
		  Node n = null;
		  while(it.hasNext()){
			  n = (Node)it.next();		  
			  if(n.getId() == id)
				  break; 
		  }
		  return n;
	  }
	
	  public Node getNodeWithValues(Tuple tuple){		  
		  Iterator<Node> it = nodi.keySet().iterator();
		  Node n = null;
		  while(it.hasNext()){
			  n = (Node)it.next();			
			  if(n.getValues().equals(tuple)){
				  break; 
			  }
		  }
		  return n;
	  }
	  
	  public int getNumberOfAttributes(){
			return ((Node)this.nodi.keySet().iterator().next()).getNumAttributes();
		}
	  
	  public void NodetoArff(Node node, String fileName) throws IOException{
		String tmp = "";
		int numAttributes = node.getNumAttributes();
		BufferedWriter writer = null;
				writer = new BufferedWriter(new FileWriter(fileName));
		  tmp += "@relation 'test'\n";
		  
		  int numAtt = node.getNumAttributes();
		  for(int i=0;i<numAtt;i++){
		    	 if(node.getValues().get(i).getAttribute() instanceof ContinuousAttribute)
		    		 tmp+="@attribute " + node.getValues().get(i).getAttribute().getName() + " real\n";
		    		 else{ 		  
			    		  tmp += "@attribute " + node.getValues().get(i).getAttribute().getName() + " {";
			    		  Iterator<String> it = ((DiscreteAttribute)node.getValues().get(i).getAttribute()).iterator();
			    		  while(it.hasNext())
			    			  tmp += it.next() + ",";
			    		  tmp = tmp.substring(0, tmp.length()-1);//.replace(",","}");
			    		  tmp+="}\n";
			    	  }
		  }
		  ArrayList<String> attributesName = null;
		  if(node.getAggr()!= null && node.getAggr().size() != 0){
	    	 Iterator<String> iter = node.getAggr().keySet().iterator();
	    	 attributesName = new ArrayList<String>();
	    	 while(iter.hasNext()){
	    		 String attName = iter.next();
	    		 attributesName.add(attName);
	    		 tmp+="@attribute " + attName + " real\n";
	    	 }
	      }
		  tmp+="@data\n";
		  for(int i = 0;i < numAttributes; i++){
			  tmp += node.getValues().get(i).getValue().toString();
			  if(i != numAttributes - 1)
				  tmp += ",";
		  }
		  
  	 	  if(attributesName != null){	
  	 		  tmp += ",";
  	 		  int j = 0;
  	 		  for(String t : attributesName){
  	 			  tmp += node.getAggr().get(t);	
  	 			  j++;
  	 			  if(j != attributesName.size())
  	 				  tmp += ",";
  	 		  }
  	 	  }
  	 	  tmp+="\n";	    			  
		  writer.write(tmp);
		  writer.flush();
		  writer.close();
	  }
	
	  //Creazione file arff contenente i vicini del nodo passato in input. 
	  public boolean neighborsToArff(Node node,String fileName) throws IOException{
		  	boolean flag = false;
		  	String tmp = new String();
		  	int numAttributes = this.getNumberOfAttributes();
		  	BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		  	tmp += "@relation '"+ "train" +"'\n";
		  	for(int i = 0;i < numAttributes;i++){
		    	  Attribute attribute = data.getAttributeSchema().get(i);
		    	  tmp += "@attribute " + attribute.getName();
		    	  tmp += (attribute instanceof ContinuousAttribute) ?  " real\n" : " {";
		    	  if(attribute instanceof DiscreteAttribute){
		    		  Iterator<String> it = ( (DiscreteAttribute) attribute ).iterator();
		    		  while(it.hasNext())
		    			  tmp += it.next() + ",";
		    		  tmp = tmp.substring(0, tmp.length()-1);
		    		  tmp += "}\n";
			      }  		 
	    	}	      
		    ArrayList<String> attributesName = null;
			  if(node.getAggr()!= null && node.getAggr().size() != 0){
		    	 Iterator<String> iter = node.getAggr().keySet().iterator();
		    	 attributesName = new ArrayList<String>();
		    	 while(iter.hasNext()){
		    		 String attName = iter.next();
		    		 attributesName.add(attName);
		    		 tmp+="@attribute " + attName + " real\n";
		    	 }
		      }
		    tmp+="@data\n";	
		    tmp+=printNode(node) + "," + printAggr(node) + "\n"; 
		    Iterator<Arch> it =  this.getEdgeSet(node).iterator(); 		//Iteratore sull'insieme di archi del nodo
		
		    while(it.hasNext()){	 
		    	 Arch neig = (Arch) it.next();
		    	 Node n = node.getNeighbor(neig);
				 if(!n.getIsTrain()){  			    					
					 tmp += printNode(n);
			   		if(attributesName != null){	
			     		tmp += ",";
			     		for(String str : attributesName)	     	 			  			
			     			 tmp += (n.getAggr().get(str)) + ",";		
			     		tmp = tmp.substring(0, tmp.length() - 1);
			     	}
					
			   		tmp+="\n";
				 }
		    }
		     
		//    System.out.println(nTrain);
		  
		     writer.write(tmp);
		   	 writer.flush();
		   	 writer.close();
	
	    return flag;
		}
	  public Data getData(){
		  return data;
	  }
	 public ArrayList <String> getExtendedAttributesNames(){
	     ArrayList<String> attributesName = null;
	 
	     Node node = null;
	     for(Node n : nodi.keySet())
	    	 if(n.getAggr().isEmpty() == false){
	    		 node = n;
	    		 break;
	    	 }
 	    	 Iterator<String> iter = node.getAggr().keySet().iterator();
	    	 attributesName = new ArrayList<String>();
	    	 while(iter.hasNext()){
	    		 String attName = iter.next();
	    		 attributesName.add(attName);
	    	 }
	    	 return attributesName;
	  }
	 
	public void graphToArff(String fileName,String data) throws IOException{
		  String tmp = new String();
		  int numAttributes = this.data.getNumberOfExplanatoryAttributes();
		  BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
	      tmp += "@relation '"+ data.replace("only", "") +"'\n";
	      for(int i = 0;i < numAttributes;i++){
	    	  Attribute attribute = this.data.getAttributeSchema().get(i);
	    	  tmp += "@attribute " + attribute.getName();
	    	  tmp += (attribute instanceof ContinuousAttribute) ?  " real\n" : " {";
	    	  if(attribute instanceof DiscreteAttribute){
	    		  Iterator<String> it = ( (DiscreteAttribute) attribute ).iterator();
	    		  while(it.hasNext())
	    			  tmp += it.next() + ",";
	    		  tmp = tmp.substring(0, tmp.length()-1);
	    		  tmp += "}\n";
		      }  		 
    	  }	      
	      if(data.compareTo("onlyTrain") == 0){
	    	  ArrayList<String> attributesName = getExtendedAttributesNames();
	    	  for(int i = 0;i < attributesName.size();i++)
	    		  tmp += "@attribute " + attributesName.get(i) + " real\n"; 
	      }
		 
	      tmp+="@data\n";	      
	      int index = 0;
	      for(Node node : nodi.keySet()){
	    	  if(this.isSpatial)
	    		  node = this.getNodeWithID(index);
	    	  if(data.compareTo("onlyTesting") == 0)
	    		 tmp += ( !node.getIsTrain() ) ? printNode(node) + "\n" : "";	
	    	  else if(data.compareTo("onlyTrain") == 0)
	    		 tmp += (this.getEdgeSet(node).isEmpty() == false && node.getIsTrain() && node.getAggr().isEmpty() == false ) ? printNode(node) + "," + printAggr(node) + "\n" : "";	
	    	  else
	    		 tmp += printNode(node) + "\n" ;
	    	  index++;
		      }		      
    	  writer.write(tmp);
     	  writer.flush();
    	  writer.close();	     
	}
	public void resetTrain(){
		for(Node n:nodi.keySet()){
			n.setTrain(true);
		}
	}
	public String  printNode(Node node){
		String tmp = new String();
		for(int j = 0;j < node.getNumAttributes(); j++)
    	 	tmp += node.getValues().get(j).getValue().toString() + ",";
		tmp = tmp.substring(0, tmp.length() - 1);
    	return tmp;
	}
	public String printAggr(Node node){
		String tmp = new String();
		for(String str : node.getAggr().keySet()){
			tmp += node.getAggr().get(str) + ",";
		}
		tmp = tmp.substring(0, tmp.length() - 1);
		return tmp;
	}
	
	
	  public void resetNodesVisited(){
		  for(Node n : nodi.keySet())
			  n.setVisited(false);
	  }
	  
	  public String toString() {
	    StringBuffer out = new StringBuffer();
	    Node nodo = null;
	    Arch a;
	    Iterator<Node> nodoI = nodi.keySet().iterator();
	    while (nodoI.hasNext()) {
	     	nodo = (Node)nodoI.next();
	     	Iterator<Arch> arcoI =nodi.get(nodo).iterator();
	     out.append("Nodo " + nodo.toString() + ": ");
	      while (arcoI.hasNext()) {
	        a = (Arch)arcoI.next();
	        out.append( ((a.getX().equals(nodo) ) ? a.getY().toString() : a.getX().toString()) + "("+a.getWeight()+"), ");
	    //    out.append(a.getY().toString()+", ");
	      }
	      out.append("\n");
	    }
	    return out.toString();
	  }
	  
 
@Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((nodi == null) ? 0 : nodi.hashCode());
	return result;
}

}