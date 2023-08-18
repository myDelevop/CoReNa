/** @author Sgura Francesco */

package icaData;

import icaData.Attribute;


/** <p>La classe <b>Item</b> modella un generico item (coppia attributo-valore) di una tupla</p> */
public abstract class Item implements Cloneable{


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Item other = (Item) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (attribute instanceof ContinuousAttribute) {
			if ( Double.compare((Double) value, (Double) other.value) != 0 )
				return false;
		} else if (attribute instanceof DiscreteAttribute) {
			if ( ((String) value).compareTo((String) other.value) != 0 )
				return false;
		}
	
		return true;
	}

	/**<p> Attributo che viene utilizzato per l'item</p> */
	protected Attribute attribute;
	
	/**<p> Valore assunto dall'attributo dell'item</p> */
	protected Object value; 
	
	/**<p><b>Comportamento</b>: Inizializza i membri "attribute" e "value" assegnando i valori passati in input </p>
	 * 
	 * @param att (attributo coinvolto nell'item)
	 * @param object (valore che deve assumere)
	 */
	Item(Attribute att,Object object){
		attribute = att;
		value = object;
	}
	

	/**<p><b>Comportamento</b>: Restituisce il membro attribute </p>
	 * 
	 * @return Attribute (attributo coinvolto nell'item)
	 */
	public Attribute getAttribute(){
		return attribute;
	}
	
	
	/**<p><b>Comportamento</b>: Restituisce il membro value </p>
	 * 
	 * @return Object (valore dell'attributo coinvolto nell'item)
	 */
	public Object getValue(){
		return value;
	}
	
	/**<p><b>Comportamento</b>: Restituisce una stringa composta dalla concatenazione dei contenuti degli oggetti membro </p>
	 * 
	 * @return String (stringa contenente i valori degli oggetti membro)
	 */
	public String toString(){
		String str = this.getValue().toString();
		return str;
	}
	
	/**<p><b>Comportamento</b>: Determina la distanza tra l’Item corrente (sul quale il metodo è invocato) e quello passato come argomento. </p>
	 * 
	 * @param a 
	 * @return double 
	 */
	abstract double distance(Object a);

	
}
