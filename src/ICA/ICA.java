package ICA;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.File;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;


import icaData.Data;
import icaData.Tuple;
import weka.core.Instance;
import weka.core.Instances;
import Utility.*;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Discretize;
/* Classe per modellare l' algoritmo di ica. 
 * nota: data.Data si riferisce al package di MOSMOTI, icaData al package dei dati per ICA
 */
public class ICA {
	  public static String datasetName = null;
	  String testingFileName;
	  String trainingFileName;
	  String OriginalTestingData;
	  String algorithm;
	  String aggregationMode;
	  Graph  TestGraph = null;
	  int iterationsNumber;
	  double min_distance = -1;
	  double max_distance = -1;
	  public String experimentalSettings;
	  public String discretizeSettings;
	  public String distancesFile;
	  public String autocorrelationMethod;
	  String StopOfIter;
	  BufferedWriter resultsFile;
	
	  public void acquireSettings(String settingsFileName) throws SettingError, IOException{
		
			BufferedReader readerFile = new BufferedReader(new FileReader(settingsFileName));
			String line = readerFile.readLine();
			while(line!=null){
				StringTokenizer token=new StringTokenizer(line);
				token.nextToken(" ");			
				String value = token.nextToken(" ");
			
				if(line.contains("aggregationMode")){	
					if(value.compareTo("WA") == 0 || value.compareTo("DISCR") == 0 || value.compareTo("ROA") == 0)
						this.aggregationMode = value;	
					else{
						readerFile.close();
						throw new SettingError("Aggregation mode isn't correct.");
					}
				}
				if(line.contains("maxIterations"))
					this.iterationsNumber = Integer.parseInt(value);
				if(line.contains("TestingDataForEvaluation"))
					this.OriginalTestingData = value;
				if(line.contains("StoppingCheck"))
					this.StopOfIter = value;
				if(line.contains("autocorrelationMethod"))
					this.autocorrelationMethod = value;
				if(line.contains("discretizeSettings"))
					this.discretizeSettings=value;
				if(line.contains("algorithm")){
					if(value.compareTo("SMOTI") == 0 || value.compareTo("M5P") == 0 || value.compareTo("MTSMOTI") == 0)
					 this.algorithm = value;
					else{
						readerFile.close();
						throw new SettingError("Algorithm choise isn't correct.");
					}
				}
				if(line.contains("experimentalSettings"))
					this.experimentalSettings = value;
				if(line.contains("distancesFile"))
					this.distancesFile = value;
				
				line = readerFile.readLine();
			}
			readerFile.close();	
			if(this.OriginalTestingData.compareTo("null") == 0)
				this.OriginalTestingData = this.testingFileName;
	  }
	  
	public void setDataStructure(Graph graph){
		this.TestGraph = graph;
	}
	
	public Graph getDataStructure(){
		return this.TestGraph;
	}
	
public static Graph createDataStructure(String data, String test, String distances,String expSettings) throws IOException, SettingError{
		
		Data dt = test == null? new Data(data) : new Data(data,test);	
		dt.setConfig(expSettings);
		Graph graph = new Graph(dt);
		HashSet<Double> al = new HashSet<Double>();	
		System.out.println("Graph creation. . ");
		Instances spatial = new Instances(new BufferedReader(new FileReader(data)));
	
		if(spatial.attribute("X") != null && spatial.attribute("Y") != null){
			graph.isSpatial = true;
		}
		for(int i = 0;i < dt.getNumberOfExamples(); i++){
			double id = (graph.isSpatial == false ? (double) dt.getItemSet(i).get(0).getValue() : i);
			graph.add(new Node(id, dt.getItemSet(i)));
		}
		BufferedReader readerFile = new BufferedReader(new FileReader(distances));
		String line = readerFile.readLine();			
		while(line != null){
			String s[] = line.split(",");
			Node n1 = graph.getNodeWithID(Integer.parseInt(s[0]));
			Node n2 = graph.getNodeWithID(Integer.parseInt(s[1]));					
			if(n1 != null && n2 != null){				
				Arch arch = new Arch(n1, n2, Double.parseDouble(s[2]));			
				if(graph.add(arch))
					al.add(Double.parseDouble(s[2]));				
				line = readerFile.readLine();
			}
		}		
		
		readerFile.close();
		graph.setMax(Collections.max(al));
		graph.setMin(Collections.min(al));
		
		for(Node n : graph.getNodeSet()){
			for(Arch a : graph.getEdgeSet(n)){
				Node n1 = n.getNeighbor(a);
				n1.setVisited(true);
				for(Arch a1 : graph.getEdgeSet(n1)){
					Node n2 = n1.getNeighbor(a1);
					if(n2.hasBeenVisited() == false && !graph.existsArch(n, n2)){
						n.indirectNeighbors.put(n2, a1.getWeight());
						n2.setVisited(true);
					}
				}				
			}
			graph.resetNodesVisited();
		}
		
		return graph;
	}

	void inizializeGraph() throws SettingError, IOException{
		
			if(TestGraph == null) {
				this.TestGraph = createDataStructure(this.trainingFileName, this.testingFileName, this.distancesFile, this.experimentalSettings);
			}
			this.max_distance=TestGraph.getMax();
			this.min_distance=TestGraph.getMin();
			BufferedReader reader = new BufferedReader(new FileReader(this.testingFileName));
			String line = reader.readLine();
			while(line.contains("data") == false)
				line = reader.readLine();
			line = reader.readLine();	
			int i = 0;
			Data dat = new Data(this.testingFileName);		
			dat.setConfig(experimentalSettings);
			while(line != null){
				if(this.TestGraph.isSpatial == false) {
					double ID = Double.parseDouble(line.split(",")[0]);		
					TestGraph.getNodeWithID(ID).setTrain(false);	
				} else {
					Tuple values = dat.getItemSet(i);		
					TestGraph.getNodeWithValues(values).setTrain(false);
					i++;
				}
				line = reader.readLine();				
			}
			reader.close();	
	
		}		
			
	  	String resultsFileName;
		
		public ICA(String trainData, String testData, String settingsFileName, String resultsFileName) throws SettingError, IOException{	
			this.trainingFileName = trainData;
			this.testingFileName = testData;
			File file = new File("Temp");
			if(file.mkdir())
				System.out.println("Created directory for temporary files.");			
			
			this.acquireSettings(settingsFileName);
			this.resultsFile = new BufferedWriter(new FileWriter(resultsFileName));
			this.resultsFileName = resultsFileName;
			if(this.aggregationMode.compareTo("Discretize") == 0)
				this.setDiscretizeSettings();	
		}
		
	
		String tempTesting = "Temp/testingTmp.arff";	
	
	/* La fase di bootstrapping serve a fornire una etichetta iniziale ai nodi che ne sono sprovvisti poichï¿½ sconosciuta.
	  * Sulla base dell'algoritmo scelto, costruira' il classificatore con le tuple di training e il modello sara' 
	  * applicato al grafo  di testing per la classificazione iniziale dei nodi.
	  * Il metodo scandisce le tuple apparteneti al set di dati, se il target e' nullo, viene predetto il valore sulla base dei dati di train.
	  * In seguito viene assegnato al nodo. AL termine del processo, tutti i nodi avranno un target e questo e' un requisito indispensabile per
	  * iniziare la classificazione collettiva.
	  * Al termine verra' creato un file .arff denominato col nome dell'algoritmo utilizzato, contentente tutte le tuple di testing etichettate.
	  * */
	void bootStrapping() throws Exception {	
		System.out.println("Extension nodes. .\n");	
		double target = 0;
		
		String trainingDataAfterExtension =	this.extendNodes(true,0);
		Prediction model = null;
		
		if(this.algorithm.compareTo("M5P") == 0){
			model = new M5PPrediction(this.experimentalSettings);
			((M5PPrediction)model).createModel(trainingDataAfterExtension);
		}
		else{
			model = new SmotiPrediction(this.experimentalSettings);	 
			if(this.algorithm.compareTo("MTSMOTI") == 0)
				((SmotiPrediction)model).setIsMT(true);
			((SmotiPrediction) model).createModel(trainingDataAfterExtension);
		}
				
		System.out.println("Bootstrapping. . ");

		double mae = 0,mape=0,mse=0;
		int testInst = 0;
		Instances testing = new Instances(new BufferedReader(new FileReader(this.OriginalTestingData)));
		testing.setClassIndex(targetIndex());
		
		Instances all = null;
		if(datasetName == null) {
			all = new Instances(new BufferedReader(new FileReader(this.trainingFileName)));
			Instances test = new Instances(new BufferedReader(new FileReader(this.testingFileName)));
			for(int i =0;i<test.numInstances();i++)
				all.add(test.instance(i));
		} else
			all = new Instances(new BufferedReader(new FileReader(datasetName)));
		
		all.setClassIndex(targetIndex());
		
		int index = 0;
		for(Node node : this.TestGraph.getNodeSet()){
			if(TestGraph.getEdgeSet(node).isEmpty() == true){
				node.setIgnored(true);
			}
			else{
				if(node.getIsTrain() == false){
					TestGraph.NodetoArff(node, tempTesting);							
		          	if(model instanceof SmotiPrediction)		    		
						target = (Double) ((SmotiPrediction) model).classifyInstance(tempTesting);	
			        else if(model instanceof M5PPrediction)
			          	target = (Double) ((M5PPrediction)   model).classifyInstance(tempTesting);	     
		          	
			      	target = Double.parseDouble(String.format("%.5f", target).replace(",", "."));          
					node.setLabel(target);
					testInst++;
					Instance instance = null;
					if(this.TestGraph.isSpatial)
						instance = ICA.instance(all, node.getId());
					else
						instance = ICA.instance(testing, node.getId());
					if(instance != null){
						double realTarget = instance.classValue();								
						mae += Math.abs(target - realTarget);
						if(realTarget != 0)
							mape += Math.abs((realTarget - target) / realTarget);	
						mse += Math.pow(target - realTarget, 2);
						String realTargetToString = String.valueOf(realTarget).replace(".", ",");
						String firstTargetPredicted = String.valueOf(target).replace(".", ",");
						String error =  String.valueOf(target - realTarget).replace(".", ",");					
						resultsFile.write( (index+1) + "	" + realTargetToString + "	" + firstTargetPredicted + "	" + error + "\n");
					}
					index++;
					}
				}
			}   	      	
		mae /= testInst;
		mape = (mape/testInst)*100;
		double rmse = Math.sqrt(mse / testInst);
		
		resultsFile.write("MAE:	" + String.valueOf(mae).replace(".", ",") + "\nMAPE:	" + String.valueOf(mape).replace(".", ",") + "\nRMSE:	" + String.valueOf(rmse).replace(".", ","));
        System.out.println("Bootstapping complete.");       	
	}
	
	Instances load(String filename) throws Exception {
	     Instances       result;
	     BufferedReader  reader;
	 
	     reader = new BufferedReader(new FileReader(filename));
	     result = new Instances(reader);
	     result.setClassIndex(this.targetIndex()-1);
	     reader.close();
	 
	     return result;
	   }
	 
	   protected static void save(Instances data, String filename) throws Exception {
	     BufferedWriter  writer;
	 
	     writer = new BufferedWriter(new FileWriter(filename));  
	     writer.write(data.toString());
	     writer.newLine();
	     writer.flush();
	     writer.close();
	     
	   }
	//Metodo per sostituire il valore discretizzato con uno del tipo i_a_b.
	  String replaceName(String value){
		
				String temp = "";
				if(value != null){
					value = value.replace("'\\''","");
					value = value.replace("'//'","");
					value = value.replace("'(","");
					value = value.replace("]'","");
					value = value.replace(")'","");
					value = value.replace("'[","");
		//			System.out.println(value);
					if(value.compareTo("'All'") == 0)
						return "i_-inf-inf";
					String vet[]=new String[3];
					
					if(value.contains("-inf--")){
						vet[0] = "-inf";
						vet[1] = "-" + value.split("--")[1];
					}
					else if(value.contains("-inf-")){
						vet[0] = "-inf";
						vet[1] = value.split("-")[2];
					}
					else if(value.contains("inf")){
						if(value.indexOf("-") == 0)
							vet[0] = "-" + value.split("-")[1];
						else
							vet[0] = value.split("-")[0];
						vet[1] = "inf";
					}
					else{
						if(value.indexOf("-") == 0)
							vet[0] = "-" + value.split("-")[1];
						else 
							vet[0] = value.split("-")[0];
						if(value.contains("--"))
							vet[1] = "-" + value.split("--")[1];
						else{
							if(value.indexOf("-") == 0)
								vet[1] = value.split("-")[2];
							else
								vet[1] = value.split("-")[1];
						}
						
					}
					temp = "i_"+vet[0]+"_"+vet[1];
				}
				return temp;
	  }
	  
	  Discretize  filter;
	  void setDiscretizeSettings() throws IOException{
		  	filter = new Discretize();	
			BufferedReader reader = new BufferedReader(new FileReader(this.discretizeSettings));
			String line = reader.readLine();			
			while(line != null){
				StringTokenizer token=new StringTokenizer(line);
				token.nextToken(" ");			
				String value = token.nextToken(" ");
				if(line.contains("bins"))
					filter.setBins(Integer.parseInt(value));
				if(line.contains("desiredWeightOfInstancesPerInterval"))
					filter.setDesiredWeightOfInstancesPerInterval(Double.parseDouble(value));
				if(line.contains("useEqualFrequency"))
					filter.setUseEqualFrequency(Boolean.parseBoolean(value));
				if(line.contains("findNumBins"))
					filter.setFindNumBins(Boolean.parseBoolean(value));
				line = reader.readLine();
			}
			reader.close();
		
	 }
	  
	  public static Instance instance(Instances instances, double ID){
		  Instance instance = null;
		  if(instances.attribute("X") == null && instances.attribute("Y") == null) {
		  for(int k = 0;k < instances.numInstances();k++){
				double id = instances.instance(k).value(0);	
				if(Double.compare(ID, id) == 0){
					instance =  instances.instance(k);
					break;
				}
			}
		  } else {
			  instance = instances.instance((int) ID);
		  }
		  
		  return instance;
	  }
	  
	  double NormalizeDistance(double distance){
		  return ((distance - min_distance) / (max_distance - min_distance));
	  }
	  //Estende i nodi inserendo in quelli di train il valore in base al tipo di aggregatore e ai vicini, in quelli di testing 0
	protected String extendNodes(boolean onlyTrain,int it) throws Exception{
		int targetIndex = targetIndex();
		String trainAfterExtension = "Temp/TrainExtension"+it+".arff";
		Instances inputTrain, filteredData = null;
		String g ="Temp/grafo.arff";				
		//Preparazione per l'esecuzione delle estensioni
		if(this.aggregationMode.compareTo("Discretize") == 0){
			this.TestGraph.graphToArff(g,"all");
			inputTrain = load(g);			  		  	   
			filter.setAttributeIndices(String.valueOf(this.targetIndex() + 1));						
			filter.setInputFormat(inputTrain);
		    filteredData = Filter.useFilter(inputTrain, filter);
	        save(filteredData,"Temp/filteredData.arff");
	        filteredData.setClassIndex(targetIndex);	    
		}
		 
	//Inizio calcolo aggregati

		for(Node node : TestGraph.getNodeSet()){
			if(node.isIgnored() || TestGraph.getEdgeSet(node).isEmpty()){
				continue;
			}
			
			switch(this.aggregationMode){
				case "DISCR":						 
					TreeMap<String, Integer> hm =  new TreeMap<String, Integer>(new DiscrComparator());	
				    for(int j = 0; j < filteredData.attribute(targetIndex).numValues();j++)
				    	hm.put(replaceName(filteredData.attribute(targetIndex).value(j).toString()), 0);		
				    
					ArrayList<String> l = new ArrayList<String>();
				    ArrayList<Double> list = new ArrayList<Double>();				
				    for(Arch a : this.TestGraph.getEdgeSet(node))
				    	list.add((double) node.getNeighbor(a).getId());				    	
  			    
				    for(int j = 0;j < list.size();j++){
				    	double idOfNeighbor = list.get(j);
				    	Instance nearInstance = instance(filteredData, idOfNeighbor);	
				    	if(onlyTrain && nearInstance != null){ 
				    	  if(TestGraph.getNodeWithID(idOfNeighbor).getIsTrain())				    			    	
				    		  l.add(replaceName(nearInstance.stringValue(targetIndex)));				//Recupero del valore target discreto relativo al j-esimo vicino del nodo										    	
				    		} else	
				    			l.add(replaceName(nearInstance.stringValue(targetIndex)));	
				    }			    
					for(int j = 0;j < l.size();j++)			 
						hm.put(l.get(j), Collections.frequency(l, l.get(j)));	//Inserimento della chiave (valore discreto) e la frequenza nei vicinori									
			 	    
					node.extendNode(this.aggregationMode, hm);		
			 	    break;		 	    
			 	    
				case "WA":
					double avg = 0.0;	
					int count = 0;			
					
					for(Arch a : this.TestGraph.getEdgeSet(node)){
						double distance = (double)a.getWeight();						
						Node directNeighbor = node.getNeighbor(a);
						double weigth = 1 - NormalizeDistance(distance);
						if(onlyTrain) {
							if(directNeighbor.getIsTrain()){	
								avg += weigth * directNeighbor.getLabel();			
								count++;	
							}
						} else {
							avg += weigth * directNeighbor.getLabel();			
							count++;
						}
					}
					for(Node indirectNeighbor : node.indirectNeighbors.keySet()){
						double target = indirectNeighbor.getLabel();	
						double distance = (Double) node.indirectNeighbors.get(indirectNeighbor);
						if(onlyTrain) {
							if(indirectNeighbor.getIsTrain()) {
								double weigth = 1 - NormalizeDistance(distance);																						
								avg += (target * weigth);		
								count++;	
							}
						} else {
							double weigth = 1 - NormalizeDistance(distance);																						
							avg += (target * weigth);		
							count++;		
						}												
					}															
					if(count > 0){
						node.extendNode(this.aggregationMode, avg / count);
					} else { 
						if(onlyTrain && TestGraph.getEdgeSet(node).isEmpty() == false)
							node.extendNode(this.aggregationMode, 0.0);																			
					}
					
					break;			
					
				case "ROA":
					TreeMap<String, Double> results = new TreeMap<String, Double>();
					results.put("ratioOfAverage_B1B2", 0.0);
					results.put("Average_B1", 0.0);
					results.put("Average_B2", 0.0);
					results.put("StandardDev_B1", 0.0);
					results.put("StandardDev_B2", 0.0);
					List <Double> B1 = new ArrayList <Double>();
					List <Double> B2 = new ArrayList <Double>();			
					double Avgbeta1 = 0, Avgbeta2 = 0, Sigma1 = 0, Sigma2 = 0;	
					for(Arch a : TestGraph.getEdgeSet(node)){						
						Node directNeighbor = node.getNeighbor(a);
						double distance = (Double)a.getWeight();
						double target = directNeighbor.getLabel();
						if(onlyTrain) {
							if(directNeighbor.getIsTrain()) {
								double weigth = 1 - NormalizeDistance(distance);			
								Avgbeta1 += (target * weigth);
								B1.add(target * weigth);	
							}
						} else {
							double weigth = 1 - NormalizeDistance(distance);			
							Avgbeta1 += (target * weigth);
							B1.add(target * weigth);
						}					
					}
					
					for(Node indirectNeighbor : node.indirectNeighbors.keySet()){
						double target = indirectNeighbor.getLabel();	
						double distance = (Double) node.indirectNeighbors.get(indirectNeighbor);
						if(onlyTrain) {
							if(indirectNeighbor.getIsTrain()) {
								double weigth = 1 - NormalizeDistance(distance);																						
								Avgbeta2 += (target * weigth);		
								B2.add(target * weigth);	
							}
						} else {
							double weigth = 1 - NormalizeDistance(distance);																						
							Avgbeta2 += (target * weigth);		
							B2.add(target * weigth);	
						}												
					}								
																						
					if(!B1.isEmpty() && !B2.isEmpty()){
						Avgbeta1 /= B1.size();
						Avgbeta2 /= B2.size();
					} else {
						node.extendNode(this.aggregationMode, results);
						continue;
					}
				
					for(Double Xi : B1){
						Sigma1 += Math.pow(Xi - Avgbeta1, 2);
					}		
					
					for(Double Xi : B2){
						Sigma2 += Math.pow(Xi - Avgbeta2, 2);
					}
		
					if(Avgbeta2 > 0){						
						Sigma1 = Math.sqrt(Sigma1 / B1.size());
						Sigma2 = Math.sqrt(Sigma2 / B2.size());
						results.put("ratioOfAverage_B1B2", Avgbeta1 / Avgbeta2);
						results.put("Average_B1", Avgbeta1);
						results.put("Average_B2", Avgbeta2);
						results.put("StandardDev_B1", Sigma1);
						results.put("StandardDev_B2", Sigma2);
					}
				
					node.extendNode(this.aggregationMode, results);										
				break;					
			}
		}
								
		this.TestGraph.graphToArff(trainAfterExtension, "onlyTrain");
		
		return trainAfterExtension;
	}
		
	public int targetIndex(){
		return this.TestGraph.getData().targetIndex();
	}
			
	//Esegue le predizioni in seguito alle estensioni, e attraverso l'algoritmo di LocalMoranI o GI*, determina se l'etichetta predetta è migliore
	//rispetto a quella corrente assegnata. 
	public boolean IcaCore(Prediction model, Node node) throws Exception{	
		boolean change = false;	
		this.TestGraph.NodetoArff(node, tempTesting);			//Crezione file arff con i vicini di train del nodo attuale		
		double currentTarget = node.getLabel();
		double newTarget = 0.0;
		
		if(model instanceof M5PPrediction)	
			newTarget = (Double) ((M5PPrediction) model).classifyInstance(tempTesting); 
		else
			newTarget = (Double) ((SmotiPrediction) model).classifyInstance(tempTesting);	   	        
    	
		currentTarget =  Double.parseDouble(String.format("%.6f", currentTarget).replace(",","."));		//Troncamento
  		newTarget =  Double.parseDouble(String.format("%.6f", newTarget).replace(",","."));
  		
  		double autocorrelationOldTarget = 0;
  		double autocorrelationNewTarget = 0;
  		Autocorrelation correlation = null;
  		Set<Arch> nodeNeighbors = TestGraph.getEdgeSet(node);
  		if(this.autocorrelationMethod.compareTo("GI*") == 0){
  			correlation = new GetisOrd(node, nodeNeighbors,this.min_distance,this.max_distance);					
			autocorrelationOldTarget = ((GetisOrd)correlation).calculateAutocorrelation(currentTarget);				   	
			autocorrelationNewTarget = ((GetisOrd)correlation).calculateAutocorrelation(newTarget);
	    	System.out.println("\nID Node: " + node.getId() + "	\nCurrent target: " + String.valueOf(currentTarget).replace(".", ",") + " ---- New target value after extension: " +  String.valueOf(newTarget).replace(".", ","));
	     	System.out.println("GI* of old target:	" + autocorrelationOldTarget + "\nGI* of new target: "+ autocorrelationNewTarget);
	  	}
  		else if(this.autocorrelationMethod.compareTo("LocalMoranI") == 0){
		  		correlation = new LocalMoran(node, nodeNeighbors,this.min_distance,this.max_distance);					
		  		autocorrelationOldTarget = ((LocalMoran)correlation).calculateAutocorrelation(currentTarget);				   	
		  		autocorrelationNewTarget = ((LocalMoran)correlation).calculateAutocorrelation(newTarget);
		    	System.out.println("\nID Node: " + node.getId() + "	\nCurrent target: " + String.valueOf(currentTarget).replace(".", ",") + " ---- New target value after extension: " +  String.valueOf(newTarget).replace(".", ","));
		     	System.out.println("Local moran current target:	" + autocorrelationOldTarget + "\nLocal moran new target: "+ autocorrelationNewTarget);
		 }
	  		
    	if(correlation instanceof GetisOrd ? autocorrelationNewTarget > autocorrelationOldTarget : autocorrelationNewTarget < autocorrelationOldTarget ){
    		node.setLabel(newTarget);
    		System.out.println("Target for node " + node.getId() +" will be changed. New target: " + newTarget+"\n");
    		change = true;
    	}
    	else
    		System.out.println("Target will not changed.\n");
    
    	return change;
	}
	
	/* Al termine della esecuzione dell'algoritmo, possiamo effettuare la valutazione, cioè confronto con
	 * i precedenti per il calcolo dei valutatori (Root mean square error, mean absolute error, mean absolute percentage error).
	 * I valori ottenuti dipenderanno dall'algoritmo scelto(SMOTI/M5P) e dal metodo di estensione che si è deciso di adottare. Lo scopo è verificare l' accuratezza del modello.
	 */
	 
	public HashMap<String, Double> EvaluateModel(String Originaldataset, String dataset, BufferedWriter out) throws Exception{
		String evaluation = "";

		BufferedReader readerOriginal = new BufferedReader(new FileReader(Originaldataset));	
		BufferedReader readerTest = new BufferedReader(new FileReader(dataset));
			
		Instances Original = new Instances(readerOriginal);
		Instances all = null;
		if(this.TestGraph.isSpatial){
			if(datasetName == null) {
				all = new Instances(new BufferedReader(new FileReader(this.trainingFileName)));
				Instances test = new Instances(new BufferedReader(new FileReader(this.testingFileName)));
				for(int i =0;i<test.numInstances();i++)
					all.add(test.instance(i));
			} else
				all = new Instances(new BufferedReader(new FileReader(datasetName)));			
			all.setClassIndex(targetIndex());
		}
		Original.setClassIndex(this.targetIndex());	 	
		readerOriginal.close();
		readerTest.close();
		
		out.write("\n ===== Test for evaluation ===== \ninst#	actual,	 predicted,	 error\n");
		double mape = 0.0, mae = 0.0, mse = 0.0;
		int count = 0;
		for(Node node : TestGraph.getNodeSet()){
			if(node.getIsTrain() == false && node.isIgnored() == false){	
				double currentTarget = 0;
				double newTarget = 0;
				if(this.TestGraph.isSpatial) {
					currentTarget = ( ICA.instance(all, node.getId()).classValue() );				
				} else {
					currentTarget = ( ICA.instance(Original, node.getId()).classValue() );
				}			 		 
				newTarget = ( node.getLabel() );	
				out.write((count+1) + "	" + String.format("%.6f", (currentTarget)) + "	" + String.format("%.6f", (newTarget)) + "	" + String.format("%.6f", (newTarget-currentTarget))+"\n");
				if(currentTarget != 0) {
					mape += Math.abs((currentTarget - newTarget) / (currentTarget));	
				}
				mae  += Math.abs(newTarget - currentTarget);
				mse  += Math.pow(currentTarget - newTarget, 2);
				count++;
			}
		}	
		
		mae /= count;
		mape = (mape / count) * 100;	
		double rmse = Math.sqrt(mse / count);
		evaluation =  "\nEvaluation Results: \nMean absolute error:  " + mae +"\n" + 
					  "Mean absolute percentage error: " + mape + "%\n" +
					  "Root mean squared error: " + rmse + "\n" +
					  "Number of instances: "+ count + "\n";
					  
		out.write(evaluation);	
		HashMap<String, Double> results = new HashMap<String, Double>();
		results.put("mae", mae);
		results.put("mape", mape);
		results.put("rmse", rmse);
		
		return results;
	}
	
	public int IcaMiner() throws Exception{
		int iter = 0;
		double startTimeProgram = System.currentTimeMillis();
		inizializeGraph();
		resultsFile.write("		===== RUN INFORMATION ===== 	\n");
		resultsFile.write("ALGORITHM: ICA\n");
		resultsFile.write("Regression algorithm adopted: " + algorithm + "\n");
		resultsFile.write("Extension mode adopted: " + aggregationMode + "\n");	
		resultsFile.write("Autocorrelation method: " + this.autocorrelationMethod + "\n");
		resultsFile.write("Datasets: " + (trainingFileName + " and " + testingFileName)  + "\n");
		resultsFile.write("\nTrain instances: " + this.getDataStructure().getNumberOfTrainNodes() + "\n");
		resultsFile.write("Attributes: " + this.getDataStructure().getData().getNumberOfExplanatoryAttributes() + "\n");
	    for(int j = 0 ; j < this.getDataStructure().getData().getNumberOfExplanatoryAttributes() ;j++)
	    	resultsFile.write("	" + this.getDataStructure().getData().getAttributeSchema().get(j).getName() + "\n");
	    resultsFile.write(" 		===== Predictions on test split ===== \n");
	    resultsFile.write("\nBootstap\n");
	    resultsFile.write("inst#	 original	 predicted	error\n");
	    
		bootStrapping();	
		Instances all = null;
		if(datasetName == null) {
			all = new Instances(new BufferedReader(new FileReader(this.trainingFileName)));
			Instances test = new Instances(new BufferedReader(new FileReader(this.testingFileName)));
			for(int i =0;i<test.numInstances();i++)
				all.add(test.instance(i));
		} else
			all = new Instances(new BufferedReader(new FileReader(datasetName)));
		
		all.setClassIndex(targetIndex());
	
		Instances testing = new Instances(new BufferedReader(new FileReader(this.OriginalTestingData)));
		testing.setClassIndex(this.targetIndex());
		String stoppingCriterion = this.StopOfIter;
		System.out.println("\nAlgorithm for predictions: " + algorithm);
		double maeValues[] = new double[this.iterationsNumber];		
		for(iter = 1; iter <= this.iterationsNumber; iter++){
			double startTimeIteration = System.currentTimeMillis();
			resultsFile.write("\nIteration number: " + iter+ "\n");
			resultsFile.write("inst#	 original	 predicted	 error\n");		
			System.out.println("\nIteration number: " + iter + "\nExtending nodes with " + aggregationMode +" mode..\n");		
			
			boolean predictionsStabilized = true;
			double mae = 0, mape = 0, mse = 0;
			int cont = 0;		
			Prediction Model = null;	
			
			String trainingDataAfterExtension = extendNodes(false, iter);	
			
			if(this.algorithm.compareTo("M5P") == 0){
				Model = new M5PPrediction(this.experimentalSettings);
				((M5PPrediction)Model).createModel(trainingDataAfterExtension);
			}
			else{
				Model = new SmotiPrediction(this.experimentalSettings);
				if(this.algorithm.compareTo("MTSMOTI") == 0)
					((SmotiPrediction)Model).setIsMT(true);
				((SmotiPrediction) Model).createModel(trainingDataAfterExtension);
			}
			double newTarget = 0, realTarget = 0;
			int ter = 0;		
		
			Set<Node> nodi = TestGraph.getNodeSet();
			for(Node node : nodi){	
				if(node.getIsTrain() == false && node.isIgnored() == false){
					if(this.TestGraph.isSpatial == false)
						realTarget = ICA.instance(testing, node.getId()).classValue();
					else
						realTarget = ICA.instance(all, node.getId()).classValue();
				
					boolean change = IcaCore(Model, node);
					newTarget = node.getLabel();
					resultsFile.write(cont + "	" +  String.valueOf(realTarget).replace(".", ",") + "	" +  String.valueOf((newTarget)).replace(".", ",") + "	" + String.format("%.6f", ((newTarget - realTarget) )).replace(".",",") + "\n");		    
				    
					if( change )
						predictionsStabilized = false;
					else
						ter++;
					
					if(realTarget != 0) {
						mape += Math.abs((realTarget - newTarget) / realTarget);	
					}
					mae  += Math.abs(newTarget - realTarget);	
					mse  += Math.pow(newTarget - realTarget, 2);			
					cont++;										
				}
			}
			mae /= cont;
			mape = (mape/cont) * 100;
			maeValues[iter - 1] = (mae);
			double rmse = Math.sqrt(mse/cont);
			resultsFile.write("MAE: " + mae + "\nMAPE: "+ mape + "\nRMSE: " + rmse + "\n");
			resultsFile.write("Time elapsed for iteration number: " + iter + " -> " + (System.currentTimeMillis() - startTimeIteration) / 1000 + " seconds.");
			System.out.println("Time elapsed for iteration number: " + iter + " -> " + (System.currentTimeMillis() - startTimeIteration) / 1000 + " seconds.");
			
			if(stoppingCriterion.compareTo("ALL") == 0){
				if(predictionsStabilized)
					break;
			}
			else if(stoppingCriterion.compareTo("HALF") == 0){
				if(ter > (cont / 2))
					break;
			}
			else if(stoppingCriterion.compareTo("MAEBASED") == 0){
				double EPS = 1;
				if(mae < 1){
					String eps = String.valueOf(mae).replace("0.", "");
					for(int s = 0; s < eps.length(); s++)
						if(eps.charAt(s) == '0')
							EPS *= 0.1;
					EPS *= 0.01;
				}
				if(iter > 1 && Math.abs(maeValues[iter-2] - maeValues[iter-1]) < EPS)
					break;
			
			}
		}
		getDataStructure().graphToArff("testingDataPrediction_" + algorithm + ".arff","onlyTesting");
		System.out.println("\nPrediction complete. See files " + "testingDataPrediction_" + algorithm + ".arff" + " and " +  resultsFileName +" for result.");
		EvaluateModel(this.OriginalTestingData, "testingDataPrediction_" + algorithm + ".arff", resultsFile);
	
		double endTimeProgram = System.currentTimeMillis();
		resultsFile.write("Time elapsed for ICA : " + (endTimeProgram - startTimeProgram) / 1000 + " seconds.");
		System.out.println("Testing number of instances: " + this.getDataStructure().getNumberOfTestNodes());
		System.out.println("\nTime elapsed for ICA program: " + (endTimeProgram - startTimeProgram) / 1000 + " seconds.\n");
		resultsFile.flush();
		resultsFile.close();
		
		return iter;
	}
		/*
		private Set<Node> generateCasualOrdering(){			
			int k = 0;
			List<Node> nodi = new ArrayList<Node>();
			Set<Node> nodiCasual = new HashSet<Node>();
			for(Node n : this.TestGraph.getNodeSet()){
				if(!n.getIsTrain() && !n.isIgnored()){
					nodi.add(n);
					k++;
				}
			}
			 int[] Index_Nodes = new int[k];
			 Arrays.fill(Index_Nodes, -1);
			   
			 for(int i = 0;i < k;i++){
				 int t = (int) (Math.random() * k);
				 for(int j = 0; j < k;j++)
					 if(t == Index_Nodes[j]){
						 t = (int) (Math.random() * k);
						 j = 0;
					 }
				 Index_Nodes[i] = t;
			 }
			
			for(int j = 0;j < nodi.size();j++)
				nodiCasual.add(nodi.get(Index_Nodes[j]));		
	
			return nodiCasual;
		}
		*/
	public static void main(String args[]) throws IOException, SettingError, CloneNotSupportedException{
		try {
			if(args[0].compareTo("-xval") == 0){
				CrossValidation.RunCrossValidation(args);
			}
			else {
				ICA ica = new ICA(args[0],args[1],args[2],args[3]);
				int iterations = ica.IcaMiner();
				System.out.println("Number of iterations :" + iterations);				
			}
			
		} catch (SettingError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); 
		}
		
	}
	class DiscrComparator implements Comparator<String>{
		@Override
		public int compare(String o1, String o2) {
		int res=0;
		if(o1.compareTo("i_-inf-inf") == 0)
			res = 0;
		else{
			if(o1.split("_")[1].compareTo("-inf")!=0 && o2.split("_")[1].compareTo("-inf")!=0){
			double d1 = Double.parseDouble(o1.split("_")[1]);
			double d2 = Double.parseDouble(o2.split("_")[1]);
	
			if(d1>d2)
				res = 1;
			else if(d1<d2)
				res = -1;
			}else
				res = o1.compareTo(o2);
		}
			return res;
			
		}		
	}
}
