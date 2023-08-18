package ICA;

import java.io.IOException;

public abstract class Prediction {
	String trainData;
	String experimentalSettings;
	Object model;
	Boolean pruned;
	Prediction(String experimentalSettings){
		this.experimentalSettings = experimentalSettings;		
	}
	
	abstract void createModel(String trainingFileName) throws IOException;
	
	abstract Object classifyInstance(String testingFileName) throws IOException;
	
	void setModel(Object model){
		this.model = model;
	}
	Object getModel(){
		return model;
	}
}
