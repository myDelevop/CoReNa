/**@author Sgura Francesco */

package icaData;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>La classe <b>Example</b> serve a modellare una tupla. Implementa
 * l'interfaccia generica Comparable<Example> per il confronto di due oggetti
 * Example.
 * </p>
 */
public class Example implements Comparable<Example>, Cloneable {

	/** <p> Membro attributo atto a contenere i valori degli attributi che compongono la tupla. </p> */
	private List<Object> example = new ArrayList<Object>();
	public boolean train = true;
	/**<p><b>Comportamento</b>: Popola il contenitore con il valore (continuo o discreto) passato per argomento. </p> 
	 * @param o ( Oggetto da aggiungere al contenitore example )
	 * */
	public void add(Object o) {
		example.add(o);
	}

	@SuppressWarnings("unchecked")
	public Example clone(){
		Example e  = new Example();
		e.example = (List<Object>) ((ArrayList<Object>)this.example).clone();
		e.train = train;
		return e;
	}
/**<p><b>Comportamento</b>: Restituisce l'oggetto in posizione index nel contenitore. </p> 
 * 
 * @param index (indice del contenitore example da cui ricavare il valore)
 * @return Object (valore dell'attributo con indice index)
 */
	public Object get(int index) {
		Object obj = null;
		if (index < example.size() && index >= 0)
			obj = example.get(index);

		return obj;
	}
	
	/**<p><b>Comportamento</b>: Restituisce una stringa composta da ogni valore della tupla. In questo caso del contenitore example. </p>
	 * @return String (stringa contenente i valori della tupla)
	 */
	public String toString() {
		String str = "";
		for (Object o : example)
			str += o.toString() + " ";

		return str;
	}
	
	/**<p><b>Comportamento</b>: Implementazione dell'interfaccia Comparable <Example>. Verifica se due tuple sono identiche (quella su cui e' invocato il metodo e quella passata per argomento) e in tal caso restituisce 0, 
	 * un altro valore in caso contrario. </p>
	 *  @param arg0 (tupla da confrontare con quella corrente)
	 *  @return int (risultato del confronto)
	 */
	@Override
	public int compareTo(Example arg0) {
		int compare = 0;
		int i = 0;
		for (Object obj : example) {
			if (obj instanceof String)
				compare = obj.toString().compareTo((String) arg0.get(i));
			else if (obj instanceof Double)
				if ((Double) obj != (Double) arg0.get(i))
					compare = 1;
			if (compare != 0)
				break;
			else
				i++;
		}

		return compare;
	}
}