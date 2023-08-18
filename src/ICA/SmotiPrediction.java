package ICA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

import data.Data;

import SMOTITree.Attribute;
import SMOTITree.SMOTITree;

public class SmotiPrediction extends Prediction{

	boolean MT = false;

	public SmotiPrediction(String experimentalSettings){
		super(experimentalSettings);
	}
	public void setIsMT(boolean isMt){
		this.MT = isMt;
	}
	public void createModel(String trainingFileName) throws IOException{
		     
        int minTrainingExamplesInNodes = 0, minExamples = 0, maximumNumberOfVariablesInPruning = 0;
		BufferedReader smoti = new BufferedReader(new FileReader(this.experimentalSettings));
		String line = smoti.readLine();
		int step=0;
		while(line != null){
			StringTokenizer token=new StringTokenizer(line);
			token.nextToken(" ");			
			String value = token.nextToken(" ");
			if(line.contains("minTrainingExamplesInNodes"))
				minTrainingExamplesInNodes = Integer.parseInt(value);
			if(line.contains("minExamples"))
				minExamples = Integer.parseInt(value);
			if(line.contains("maximumNumberOfVariablesInPruning"))
				maximumNumberOfVariablesInPruning = Integer.parseInt(value);
			if(line.contains("step"))
				step = Integer.parseInt(value);
			if(line.contains("prunedTree"))
				this.pruned = Boolean.parseBoolean(value);
			line = smoti.readLine();
		}
		smoti.close();
			
		data.Data data = new data.Data();
		data.AquireData(trainingFileName);
		data.setExperiementalSetting(experimentalSettings);
		int j=0;			
		while(j < data.getNumberOfAttributes()){
			if(this.MT && (data.getAttributDeclaration(j).getName().contains("i_") ||data.getAttributDeclaration(j).getName().contains("Average") || data.getAttributDeclaration(j).getName().compareTo("weightedAverage") == 0 || data.getAttributDeclaration(j).getName().contains("StandardDev") || data.getAttributDeclaration(j).getName().contains("ratioOfAverage")))
				data.getAttributDeclaration(j).setTarget();
			j++;
		}
		
		HashMap<Integer,Attribute> XResiduals = this.determineAttributesResidualStructure(data);	
        SMOTITree.MinExamples = minExamples;
		SMOTITree.maximumNumberOfVariablesInPruning = maximumNumberOfVariablesInPruning;
		SMOTITree tree = new SMOTITree(data, minTrainingExamplesInNodes,data.getCountTrainingItems(),data.getCountTestingItems());
		this.trainData = trainingFileName;
		tree.createTree(data, step, XResiduals); 		
		 	
	 	if(pruned){
	 		XResiduals = this.determineAttributesResidualStructure(data);	
	 		tree.prune(data, XResiduals);
	 	}
		super.setModel(tree);
	}
	
	public Object classifyInstance(String testInstance) throws IOException{
			HashMap<Integer,Double> rootMSEF = new HashMap<Integer,Double>();
			HashMap<Integer,Double> rootMSEAvgF = new HashMap<Integer,Double>();
			HashMap<Integer,Double> sumYPredY= new HashMap<Integer,Double>(); 
	        HashMap<Integer,Double> sumY= new HashMap<Integer,Double>();
	        HashMap<Integer,Double> sumPredY= new HashMap<Integer,Double>();
	        HashMap<Integer,Double> sumSquareY= new HashMap<Integer,Double>();
	        HashMap<Integer,Double> sumSquarePredY= new HashMap<Integer,Double>();
	        
	        data.Data dataTmp = new data.Data(this.trainData, testInstance);
		    dataTmp.setExperiementalSetting(experimentalSettings);
		    int j=0;
			while(j < dataTmp.getNumberOfAttributes()){
				if(this.MT && (dataTmp.getAttributDeclaration(j).getName().contains("i_") ||dataTmp.getAttributDeclaration(j).getName().contains("Average") || dataTmp.getAttributDeclaration(j).getName().compareTo("weightedAverage") == 0 || dataTmp.getAttributDeclaration(j).getName().contains("StandardDev") || dataTmp.getAttributDeclaration(j).getName().contains("ratioOfAverage")))
					dataTmp.getAttributDeclaration(j).setTarget();
				j++;
			}
			String FileName = "Temp/" + "predict.out";		    		
		   	BufferedWriter out = new BufferedWriter(new FileWriter(FileName)); 		    		
		   	for(int i=0;i<dataTmp.getNumberOfAttributes();i++)
				if(dataTmp.getAttributDeclaration(i).isTarget())
					rootMSEF.put(new Integer(i),new Double(0.0));
			
			for(int i=0;i<dataTmp.getNumberOfAttributes();i++)
				if(dataTmp.getAttributDeclaration(i).isTarget())
					rootMSEAvgF.put(new Integer(i),new Double(0.0));
			
			for(int att=0; att<dataTmp.getNumberOfAttributes();att++){
		        	if(dataTmp.getAttributDeclaration(att).isTarget()){
		        		sumYPredY.put(att,new Double(0));
		        		sumY.put(att,new Double(0));
		        		sumPredY.put(att,new Double(0));
		        		sumSquareY.put(att,new Double(0));
		        		sumSquarePredY.put(att,new Double(0));
		        	}        	
		     }
	      String temp = "";
	      temp += "@relation \'test\'\n";
	        temp += "@leaf lId real\n";
	        for(j=0;j<dataTmp.getNumberOfAttributes();j++){
	        	if(dataTmp.getAttributDeclaration(j).isTarget()){
	   	 			temp += "@target "+ dataTmp.getAttributDeclaration(j).getName() + " real\n";
	   	 			temp += "@target ORIGINAL" + dataTmp.getAttributDeclaration(j).getName() +" real\n";
	   	 	}
	        }
	       temp+=("@data\n");
	       out.write(temp); 
	      
	       ((SMOTITree) super.getModel()).predictTree(dataTmp, 0, dataTmp.getCountTestingItems() - 1, rootMSEF, rootMSEAvgF, sumY, sumSquareY, sumPredY, sumSquarePredY, sumYPredY, out);
	       out.close();
	       
		BufferedReader reader = new BufferedReader(new FileReader(FileName));
        String line1 = reader.readLine();
        while(!line1.contains("data"))
        	line1 = reader.readLine();        	 
    	line1 = reader.readLine();       	
    	reader.close();
		String attr[] = line1.split(",");
		double target = Double.parseDouble(attr[1]);
			
		return target;
	}
	
	private HashMap<Integer,Attribute> determineAttributesResidualStructure(Data data){
		HashMap<Integer,Attribute> XResiduals=new HashMap<Integer,Attribute>();
		for(int i=0; i<data.getNumberOfAttributes();i++){
			if(data.getAttributDeclaration(i).isTarget())
				//residuals on response attribute
				XResiduals.put(new Integer(i),new Attribute(data.getAttributDeclaration(i).getName(),i));
				
			else
				if(data.getAttributDeclaration(i).isContinuous() && data.getAttributDeclaration(i).isExplanatory() && !data.getAttributDeclaration(i).getIgnored())
					//residuals on explanatory attributes
					XResiduals.put(new Integer(i),new Attribute(data.getAttributDeclaration(i).getName(),i));
		}
		return XResiduals;
	}
}
