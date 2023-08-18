/** @author Sgura Francesco */

package icaData;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/** <p>La classe <b>DicreteAttribute</b> rappresenta un attributo discreto</p> */
public class DiscreteAttribute extends Attribute implements Iterable<String>{
	
	/** <p> Contenitore generico per contenere i possibili valori assunti dagli attributi preordinati lessicograficamente</p> */
	private TreeSet<String> values;
	
	/** <p><b>Comportamento</b>:Costruttore : invoca il costruttore della classe madre ed avvalora il contenitore con i valori discreti
	 * in input che l'attributo può assumere</p>
	 * @param name (valore del nome simbolico dell'attributo)
	 * @param index (valore dell'identificativo numerico dell'attributo)
	 * @param values (valori discreti)
	 */
	public DiscreteAttribute(String name, int index, TreeSet<String> values){
		super(name, index);
		this.values = values;	
	}
	
	/**<p><b>Comportamento</b>: restituisce la cardinalità del treeset
	 * 
	 * @return int (numero dei valori discreti dell' attributo)
	 */
	int getNumberOfDistinctValues(){
		return values.size();
	}
	
	/**<p><b>Comportamento</b>: Determina il numero di occorrenze del valore v dato come argomento nel sotto-insieme di tuple nell’oggetto idList
	 * 
	 * @param data (insieme delle transazioni distinte)
	 * @param idList (insieme di indici di tuple)
	 * @param v (valore discreto di cui determinare la frequenza)
	 * @return int (numero di occorrenze del valore v)
	 */
	int frequency(Data data,Set<Integer> idList, String v){
		int cont = 0;
		for(int i = 0;i < data.getNumberOfExamples();i++){		
			if(idList.contains(i) && data.getAttributeValue(i,this.index).toString().compareTo(v) == 0)
				cont++;
		}
		return cont;
	}
	
	/**<p></b>Comportamento</b>: Restituisce un riferimento all'oggetto iterator del membro values 
	 * @return iterator<String> (riferimento alla classe <b>Iterator<String></b> 
	 * */
	@Override
	public Iterator<String> iterator() {
		return values.iterator();
	}
	
}

