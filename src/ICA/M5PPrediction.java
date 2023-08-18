package ICA;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.M5P;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;

public class M5PPrediction extends Prediction{

	FilteredClassifier filter = null;
	int target;
	
	public M5PPrediction(String experimentalSettings){	
		super(experimentalSettings);
		super.setModel(new M5P());
	}
	 public void createModel(String trainData) throws IOException{
			int classIndex = 0,minNum=0;
			String attrIgnored = null;
			BufferedReader in = new BufferedReader(new FileReader(this.experimentalSettings));
			BufferedReader reader = new BufferedReader(new FileReader(trainData));
			Instances Train = new Instances(reader);
		
			String line = in.readLine();
			while(line != null){
				StringTokenizer token=new StringTokenizer(line);
				token.nextToken(" ");			
				String values=token.nextToken(" ");		
				if(line.contains("target"))
					classIndex = Integer.parseInt(values);
				if(line.contains("prunedTree"))
					this.pruned = Boolean.parseBoolean(values);
				if(line.contains("minExamples"))
					minNum = Integer.parseInt(values);
				if(line.contains("ignored")){
					attrIgnored = values;
				}
				line = in.readLine();
			}
			in.close();
			
			target = classIndex;
			if(Train.numInstances() > 0){		
				Train.setClassIndex(classIndex);	 
				M5P classifier = (M5P)super.getModel();
				classifier.setUnpruned(!pruned);
				classifier.setMinNumInstances(minNum);
				try{					
					if(attrIgnored != null){
						Remove r = new Remove();
						String [] ignored = attrIgnored.split(",");
						String attributesIgnored=new String();

						for(String s : ignored){
							int index = Integer.parseInt(s)+1;
							attributesIgnored += String.valueOf(index) +",";
						}
						r.setAttributeIndices(attributesIgnored);
						filter = new FilteredClassifier();											
						filter.setClassifier(classifier);
						filter.setFilter(r);				
						filter.buildClassifier(Train);	
					}
					else{
						classifier.buildClassifier(Train);
					}
				}catch(Exception e){
					e.printStackTrace();
					}
				}
			reader.close();
			
	 }
	 
	public Object classifyInstance(String testingData) throws IOException{
	
		double Target = 0;
	
		BufferedReader readerTest = new BufferedReader(new FileReader(testingData));					
		Instances Testing = new Instances(readerTest);	
		Testing.setClassIndex(this.target);	
		if(filter != null)
			try {				
				Target = filter.classifyInstance(Testing.firstInstance());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else
			try {
				Target = ((M5P) super.getModel()).classifyInstance(Testing.firstInstance());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		     
		readerTest.close();
		
        return Target;
	}
}
