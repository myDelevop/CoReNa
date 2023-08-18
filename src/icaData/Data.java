/**@author Sgura Francesco */

package icaData;

import icaData.ContinuousAttribute;
import icaData.ContinuousItem;
import icaData.Attribute;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;

import ICA.SettingError;


/** <p>La classe <b>Data</b> modella l'insieme delle transazioni </p> */
public class Data implements Cloneable
{
	/**<p> Lista generica che contiene riferimenti ad oggetti Attribute </p> */
	List<Attribute> explanatorySet = new LinkedList<Attribute>();
	
	/**<p> Lista generica che contiene riferimenti alle transazioni che sono di tipo Example </p> */
	List<Example> data = new LinkedList<Example>();
	
	/*	Vengono settati i parametri relativi alla sperimentazione. 
	*	Di un certo dataset vengono settati l'attributo target.
	*/
	
	public Data (){};
	public void setConfig(String config) throws SettingError{
		 String line="";
		try {
			BufferedReader in = new BufferedReader(new FileReader(config));
			line = in.readLine();
			while(line != null){
				StringTokenizer token=new StringTokenizer(line);
				token.nextToken(" ");			
				String values=token.nextToken(" ");		
				if(line.contains("target"))	
					for(String s:values.split(",")){
						int tg = new Integer(s);
						if(tg < 0 || tg >= this.getNumberOfExplanatoryAttributes()){
							in.close();
							throw new SettingError("Numero attributo target errato.");
						}
						this.getAttributeSchema().get(new Integer(s)).setTarget();  //L'attributo sara' settato target
					}	
				line = in.readLine();
			}
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public int targetIndex(){
		return this.getItemSet(0).targetIndex();
	}
	public void loadingData(String dataSource) throws IOException, SettingError{	
		data.Data temp = new data.Data();
		temp.AquireData(dataSource);
		for(int i=0;i<temp.getNumberOfAttributes();i++){
				if(temp.getAttributDeclaration(i).isContinuous()){				
					double max = 0.0;
					double min = (Double) temp.getTrainingItemValue(0, i).getValue();
					for(int j=0;j<temp.getCountTrainingItems();j++){
						data.Value v = temp.getTrainingItemValue(j, i);
						if((Double)v.getValue()>max)
							max = (Double)v.getValue();
						if((Double)v.getValue()<min)
							min = (Double)v.getValue();
					}
					
					explanatorySet.add(new ContinuousAttribute(temp.getAttributDeclaration(i).getName(),i,min,max));
							
				} else {
					TreeSet<String> values = new TreeSet<String>();
					for(String s : temp.getAttributDeclaration(i).getDistinctValuesList())
						values.add(s);
					explanatorySet.add(new DiscreteAttribute(temp.getAttributDeclaration(i).getName(),i,values));
				}													
			}
	
			for(int i=0;i<temp.getCountTrainingItems();i++){
				Example e = new Example();			
				for(int j=0;j<temp.getNumberOfAttributes();j++){
					if(!temp.getAttributDeclaration(j).isContinuous()){
						String s = temp.getTrainingItemValue(i, j).getValue().toString();
						e.add(s);
					}
					else
						e.add(temp.getTrainingItemValue(i, j).getValue());
			
				}
		
			data.add(e);
			}
		}
	
		
	public void loadingData(String dataTrainFile, String dataTestFile) throws IOException, SettingError {		//Caricamento dei dati
		data.Data temp = new data.Data(dataTrainFile, dataTestFile);	
		for(int i=0;i<temp.getNumberOfAttributes();i++){
			if(temp.getAttributDeclaration(i).isContinuous()){				
				double max = 0.0;
				double min = (Double) temp.getTrainingItemValue(0, i).getValue();
				for(int j=0;j<temp.getCountTrainingItems();j++){
					data.Value v = temp.getTrainingItemValue(j, i);
					if((Double)v.getValue()>max)
						max = (Double)v.getValue();
					if((Double)v.getValue()<min)
						min = (Double)v.getValue();
				}
				explanatorySet.add(new ContinuousAttribute(temp.getAttributDeclaration(i).getName(),i,min,max));
			} else {
				TreeSet<String> values = new TreeSet<String>();
				for(String s : temp.getAttributDeclaration(i).getDistinctValuesList()){
					values.add(s);
				}
				explanatorySet.add(new DiscreteAttribute(temp.getAttributDeclaration(i).getName(),i,values));
			}													
		}
	
		for(int i=0;i<temp.getCountTrainingItems();i++){
			Example e = new Example();			
			for(int j=0;j<temp.getNumberOfAttributes();j++){
				if(!temp.getAttributDeclaration(j).isContinuous()){
					String s = temp.getTrainingItemValue(i, j).getValue().toString();
					e.add(s);
				}
				else
					e.add(temp.getTrainingItemValue(i, j).getValue());			
			}
			
			data.add(e);
		}
		
		for(int i=0;i<temp.getCountTestingItems();i++){
				Example e = new Example();			
				for(int j=0;j<temp.getNumberOfAttributes();j++){
					if(!temp.getAttributDeclaration(j).isContinuous()){
						String s = temp.getTestingItemValue(i, j).getValue().toString();
						e.add(s);
					}
					else
						e.add(temp.getTestingItemValue(i, j).getValue());
				}
			e.train=false;
			data.add(e);
		}	
	}
	
	
	public Data(String dataFile) throws IOException, SettingError {
		this.loadingData(dataFile);
	}	
	
	public Data(String dataTrainFile, String dataTestFile) throws IOException, SettingError {
		System.out.println("Loading tuples ("+dataTrainFile + " and " + dataTestFile + ")...\n");
		this.loadingData(dataTrainFile, dataTestFile);
	}	
	
	//Conversione membro data in file arff
	public void toArff(String temp) throws IOException{
	     BufferedWriter  writer = null;
		 String tmp = "";
	     try {
			writer = new BufferedWriter(new FileWriter(temp));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	     tmp += "@relation 'train'\n";
	     int numAtt = getNumberOfExplanatoryAttributes();
	     for(int i=0;i<numAtt;i++){
	    	 if(this.getAttributeSchema().get(i) instanceof ContinuousAttribute){
	    		 tmp+="@attribute " + getAttributeSchema().get(i).getName() + " real";
	    	 System.out.println("ciao");
	    	 }
	    	 else{
	    		 tmp += "@attribute " + getAttributeSchema().get(i).getName() + " {";
	    		 Iterator<String> discreteIter = ((DiscreteAttribute) getAttributeSchema().get(i)).iterator();
	    		 while(discreteIter.hasNext()){
	    			 tmp += discreteIter.next() + ",";
	    		 }
	    		 tmp = tmp.substring(0, tmp.length() - 1);
	    		 tmp += "}";
	    	 }
	    		 tmp+="\n";
	     }
	     tmp+="@data\n";
	     for(int i=0;i<this.getNumberOfExamples();i++){	 
	    	 Example neig = this.data.get(i);
	    	 for(int j=0;j<numAtt;j++){
	    		 tmp += neig.get(j).toString();
	     		 if(j!=numAtt-1)
	     			 tmp += ",";
	     		 else
	     			 tmp+="\n";
	    	 }
	    	
	     }
	  
	    	 writer.write(tmp);
	     	 writer.flush();
	    	 writer.close();
	     
	}
	
	

	/**<p><b>Comportamento</b>: Restituisce la cardinalita' del membro "data" </p>
	 * 
	 * @return int (numero di transazioni distinte)
	 */
	
	public int getNumberOfExamples(){
		return this.data.size();
	}
	
	/**<p><b>Comportamento</b>: Restituisce la cardinalita' del membro "explanatorySet" </p>
	 * 
	 * @return int (numero di attributi)
	 */
	public int getNumberOfExplanatoryAttributes(){
		return explanatorySet.size();
	}
	
	
	/**<p><b>Comportamento</b>: Restituisce il contenitore generico "explanatorySet" </p>
	 * 
	 * @return List<Attribute> (insieme degli attributi)
	 */
	public List<Attribute> getAttributeSchema(){
		return this.explanatorySet;
	}
	
	/**<p><b>Comportamento</b>:Restituisce il valore dell'attributo identificato da attributeIndex, che si trova nella transazione exampleIndex </p>
	 * 
	 * @param exampleIndex (indice del membro data in cui si trova la tupla da considerare)
	 * @param attributeIndex (indice dell'attributo da cui si vuole estrapolare il valore)
	 * @return Object (valore assunto dall'attributo per la transazione indicizzata in input)
	 */
	Object getAttributeValue(int exampleIndex, int attributeIndex) {
		Object result = null;
		if((exampleIndex <= getNumberOfExamples()) || (attributeIndex <= getNumberOfExplanatoryAttributes()))
			result = this.data.get(exampleIndex).get(attributeIndex);		
		
		return result;
	}
	
	/**<p><b>Comportamento</b>: Restituisce una stringa contentente per ogni transazione, i valori assunti dagli attributi che la compongono </p>
	 * @return String 
	 */
	public String toString(){
		String testo = new String();
		testo += "  " + explanatorySet + "\n";		    
		for(int i = 0; i < getNumberOfExamples(); i++){
			testo +=  i + ": " ;
			testo += this.data.get(i).toString();
			testo =  testo + "\n";
		}	
		
		return testo;	
	}
	
	/**<p><b>Comportamento</b>: Crea e restituisce un oggetto Tuple costituito con le coppie Attributo-valore della transazione che si trova all' indice index. </p>
	 *  
	 * @param index (indice sull'insieme delle tuple)
	 * @return Tuple (oggetto Tuple con gli item che lo compongono)
	 */
	public Tuple getItemSet(int index){
		Tuple t = new Tuple(getNumberOfExplanatoryAttributes());
		t.train = data.get(index).train;
		for(int i = 0; i <  getNumberOfExplanatoryAttributes(); i++){
			if(this.explanatorySet.get(i) instanceof ContinuousAttribute){
				ContinuousItem c1 = new ContinuousItem((ContinuousAttribute)this.getAttributeSchema().get(i), (Double) this.getAttributeValue(index, i));
				t.add(c1, i);
			}
			else{
				DiscreteItem di = new DiscreteItem((DiscreteAttribute)this.getAttributeSchema().get(i), this.getAttributeValue(index, i).toString());
				t.add(di, i);
			}
		}
		return t;
	}
	
	
}
	

