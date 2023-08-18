package ICA;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

import Utility.Graph;
import Utility.Node;
import weka.core.Instance;
import weka.core.Instances;

public class CrossValidation {
	
	public CrossValidation(){	
		
	}
	
		
	static void resetGraph(Graph graph, String originalData, int classIndex) throws FileNotFoundException, IOException{
		Instances originals = new Instances (new BufferedReader(new FileReader(originalData)));
		originals.setClassIndex(classIndex);
		for(Node node : graph.getNodeSet()){
			node.setLabel(ICA.instance(originals, node.getId()).classValue());
			node.setTrain(true);
			node.getAggr().clear();
		}
	}
	
	public static void RunCrossValidation(String[] args) throws Exception{
		String ICASettings = args[2];
		double startTimeProgram = System.currentTimeMillis();
		int seed = Integer.parseInt(args[4]);
		BufferedReader settings = new BufferedReader(new FileReader(ICASettings));
		int folds = Integer.parseInt(args[3]);
		String trainData = args[1];
		
		int classIndex = 0;
		String algorithm = null;
		String autocorrelationMethod = null;
		boolean pruned = false;
		String expSettings=null;
		String aggregationMode=null;
		Random rand = new Random(seed);
		String distFile=null;
		String line = settings.readLine();
		while(line != null){
				StringTokenizer token=new StringTokenizer(line);
				token.nextToken(" ");			
				String value = token.nextToken(" ");
				if(line.contains("algorithm"))
					algorithm = value;
				if(line.contains("aggregationMode"))
					aggregationMode = value;
				if(line.contains("distancesFile"))
					distFile = value;
				if(line.contains("autocorrelationMethod"))
					autocorrelationMethod = value;
				if(line.contains("experimentalSettings")){
					expSettings = value;
					BufferedReader experimental = new BufferedReader(new FileReader(value));
					String line_1 = experimental.readLine();
					while(line_1 != null){
						StringTokenizer token_1=new StringTokenizer(line_1);
						token_1.nextToken(" ");			
						String value_1 = token_1.nextToken(" ");
						if(line_1.contains("target"))
							classIndex = Integer.parseInt(value_1);
						if(line_1.contains("prunedTree"))
							pruned = Boolean.parseBoolean(value_1);
						line_1 = experimental.readLine();
					}
					experimental.close();						
				}
				line = settings.readLine();
			}
			settings.close();
			
			File fil = new File("Partitions");
			fil.mkdir();		
			String tmp = trainData;
			File file1 = new File(tmp);
			String dataset = file1.getName().replace(".arff", "");			
			if(!distFile.contains(dataset))
				throw new SettingError("Distances file isn't correct.");
		
		BufferedReader reader = new BufferedReader(new FileReader(trainData));		
		Instances randData = new Instances(reader);		
	    randData.setClassIndex(classIndex);
	    randData.randomize(rand);							//Mescola le istanze in modo che siano inserite in modo casuale.
	    
	    String dirTop = "evaluationResults/" + dataset;
		String dir = dirTop + "/ICA_" + algorithm + "_" + aggregationMode + "_" + (autocorrelationMethod.compareTo("GI*") == 0 ? "GI" : "lmI") + (pruned ? "_Pruned" : "");
	    File file = new File(dir);
		file.mkdirs();
		String invertCrossValidation = args[5];
	    double mae =0,mape=0,rmse=0;
	    BufferedWriter out = new BufferedWriter(new FileWriter(dir+"/evaluationWith_" + folds + "_crossValidation_"+ (Boolean.parseBoolean(invertCrossValidation) ? "INV" : "NOR")+".xls"));
	 
	    out.write("		===== RUN INFORMATION ===== 	\n");
	    out.write("ALGORITHM: ICA\n");
	    out.write("Regression algorithm adopted: " + algorithm + "\n");
	    out.write("Extension mode adopted: " + aggregationMode + "\n");
	    out.write("Folds: " + folds + "\n");
	    out.write("Pruning : " + pruned + "\n");
	    out.write("Invert Cross Validation: " + (Boolean.parseBoolean(invertCrossValidation) ? "YES" : "NO") + "\n");
	    out.write("Dataset: " + (args[2]) + "\n");
	    out.write("\nInstances: " + randData.numInstances() + "\n");
	    out.write("Attributes: " + randData.numAttributes() + "\n");
	    for(int j = 0 ; j < randData.numAttributes();j++)
	    	out.write("            " + randData.attribute(j).name() + "\n");	
	   
	    Graph graph = ICA.createDataStructure(trainData, null, distFile, expSettings);
	 
	    for (int n = 1; n <= folds; n++) {
	    	double startTimeFold = System.currentTimeMillis();	    
	    	out.write("\n\n		 ===== Predictions of partition number: "  + (n) + " =====\n");
	    	System.out.println(" ====== CROSS VALIDATION ====== \n\nCreating partition number: " + (n));
	    
		      String trainPartition = "Partitions/"  + dataset.replace(".arff", "") + n + "train.arff";
		      String testPartition  = "Partitions/"  + dataset.replace(".arff", "") + n + "test.arff";	    	  
		   	  Instances train = randData.trainCV(folds,n-1);
		   	  Instances test = randData.testCV(folds,n-1);
		      save(train, trainPartition);
	    	  save(test, testPartition);
	      
		      System.out.println("Classifying testing partition using ICA algorithm. .");
		  
		      ICA classifier;
		      ICA.datasetName = trainData;
		      if(Boolean.parseBoolean(invertCrossValidation) == false)
		    	  classifier = new ICA(trainPartition, testPartition, ICASettings, dir+"/Predictions_foldNumber_" + n + ".xls");
		      else
		      	  classifier = new ICA(testPartition,  trainPartition, ICASettings, dir+"/Predictions_foldNumber_" + n + ".xls");
		      
		      classifier.setDataStructure(graph);		
		      classifier.IcaMiner();	      
		      
		      HashMap<String, Double> results = new HashMap<String, Double>();
		      if(Boolean.parseBoolean(invertCrossValidation) == false)
		    	  results = classifier.EvaluateModel(testPartition, "testingDataPrediction_" + classifier.algorithm +".arff",out);
		      else
		    	  results = classifier.EvaluateModel(trainPartition,"testingDataPrediction_" + classifier.algorithm +".arff",out);
		      
		      mae  += results.get("mae");
		      mape += results.get("mape");
		      rmse += results.get("rmse");
		      out.write("Time elapsed for fold number: " + (n) + " -> " + (System.currentTimeMillis()-startTimeFold) / 1000 + " seconds.\n");	      
		      System.out.println("Time elapsed for fold number: " + (n) + " -> " + (System.currentTimeMillis()-startTimeFold) / 1000 + " seconds.");     
		      CrossValidation.resetGraph(graph, trainData, classIndex);
	    }
		    mae /= folds;
		    mape /= folds;
		    rmse /= folds;
		   
		    double endTimeProgram = System.currentTimeMillis();
		    out.write("\n 	===== Cross validation Summary =====\n\nMean Absolute Error: " + mae + "\n");
			System.out.println("\n\nMean Absolute Error: " + (mae));
			out.write("Mean Absolute Percentage Error: " + (mape)+ "\n");
			System.out.println("Mean Absolute Percentage Error: " + (mape));
			out.write("Root Mean Squared Error: " + rmse+ "\n");
			System.out.println("Root Mean Squared Error: " + (rmse));
			out.write("Time elapsed for cross validation : " + (endTimeProgram - startTimeProgram) / 1000 + " seconds.");
			System.out.println("Total number of instances: " + randData.numInstances());
			 System.out.println("Time elapsed for cross validation : " + (endTimeProgram - startTimeProgram) / 1000 + " seconds.");
			out.close();
		}
	 static void save(Instances data, String filename) throws Exception {
		     BufferedWriter  writer;
		 
		     writer = new BufferedWriter(new FileWriter(filename));  
		     writer.write(data.toString());
		  
		     writer.newLine();
		     writer.flush();
		     writer.close();
		   }
	 
	 public static void main(String args[]) throws FileNotFoundException, IOException{
		  Instances spatial = new Instances(new BufferedReader(new FileReader("Datasets/Spaziali/ff.arff")));
		  BufferedWriter  writer = new BufferedWriter(new FileWriter("Datasets/Spaziali/ff_distances.csv"));  
		  int XIndex = ( spatial.attribute("X").index() == -1 ? spatial.attribute("XCoord").index() :  spatial.attribute("X").index() );
		  int YIndex = ( spatial.attribute("Y").index() == -1 ? spatial.attribute("YCoord").index() :  spatial.attribute("Y").index() );
		  double distance = 0,max=0;
		  for(int i = 0;i < spatial.numInstances();i++) {
			 Instance instance = spatial.instance(i);
			 for(int j = i + 1 ; j< spatial.numInstances();j++) {
				 distance = CrossValidation.euclideanDistance(instance.value(XIndex), instance.value(YIndex), spatial.instance(j).value(XIndex), spatial.instance(j).value(YIndex));			 

				 if(distance > max)
					 max=distance;
			 }
		 }
		 max *= 0.25;
		 for(int i = 0;i < spatial.numInstances();i++) {
			 Instance instance = spatial.instance(i);
			 for(int j = i + 1 ; j< spatial.numInstances();j++) {
				 distance = CrossValidation.euclideanDistance(instance.value(XIndex), instance.value(YIndex), spatial.instance(j).value(XIndex), spatial.instance(j).value(YIndex));			 
				 if(distance < max)				
					 writer.write(i + "," + j + "," + distance + "\n");
			 }
		 }
		 writer.flush();
		 writer.close();
	 }
	 public static float euclideanDistance(double X1, double Y1, double X2, double Y2) {
		 float distance = 0;
		 double X = Math.pow((X1 - X2), 2);
		 double Y = Math.pow((Y1 - Y2), 2);
		 distance = new Float (Math.sqrt(X + Y));
		
		 return distance;
	 }
	 public static float distFrom(double lat1, double lng1, double lat2, 
			 double lng2) {
			          double earthRadius = 3958.75;
			          double dLat = Math.toRadians(lat2-lat1);
			          double dLng = Math.toRadians(lng2-lng1);
			          double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
			                     Math.cos(Math.toRadians(lat1)) * 
			 Math.cos(Math.toRadians(lat2)) *
			                     Math.sin(dLng/2) * Math.sin(dLng/2);
			          double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
			          double dist = earthRadius * c;

			          int meterConversion = 1609;

			          return new Float(dist * meterConversion).floatValue();
			          }

}
