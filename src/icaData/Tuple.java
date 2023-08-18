/**@author Sgura Francesco */

package icaData;

import java.util.Arrays;

/** <p>La classe <b>Tuple</b> rappresenta una tupla dell’insieme di transazioni </p> */
public class Tuple implements Cloneable{
	
	/** <p> Membro che rappresenta la tupla come un array di Item </p> */
	Item[] tuple;
	public boolean train = true;
	/**<p><b> Comportamento </b>: Si occupa della inizializzazione del membro tuple, assegnando all'array dimensione size </p> 
	 * @param size (cardinalita' dell'insieme di Item della tupla)
	 * */ 
	public Tuple(int size){
		tuple = new Item[size];
	}
	
	public Tuple clone(){
		Tuple t = new Tuple(tuple.length);
		for(int i=0;i<tuple.length;i++){
			if(tuple[i] instanceof ContinuousItem)
				t.add(((ContinuousItem)tuple[i]).clone(), i);
			else
				t.add(((DiscreteItem)tuple[i]).clone(), i);
		}
		return t;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(tuple);
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
		Tuple other = (Tuple) obj;
		if (this.getDistance(other)!=0)
			return false;
		return true;
	}
	/**<p><b> Comportamento </b>: restituisce l'indice dell'attributo target </p> 
	 * 
	 * @return int ( Indice dell'attributo target )
	 */
	public int targetIndex(){
		int index = -1;
		for(int i = 0;i < tuple.length && index == -1;i++)
			if(tuple[i].getAttribute().isTarget())
				index = i;
		return index;
	}
	/**<p><b>  Comportamento </b>: restituisce il valore dell'attributo target </p> 
	 * 
	 * @return Double ( valore dell'attributo target )
	 */
	public Double getTarget(){
		Item item = null ;
		for(int i=0;i<tuple.length;i++)
			if(tuple[i].getAttribute().isTarget())
				item = (tuple[i]);
		return (Double)item.getValue();
	}
	/**<p><b>  Comportamento </b>: setta l'indice dell'attributo target </p> 
	 * 
	 */
	public void setTarget(Double it){
		for(int i=0;i<tuple.length;i++)
			if(tuple[i].getAttribute().isTarget())
				tuple[i].value = it;
	}
	
	/**<p><b> Comportamento </b>: restituisce la dimensione del membro tuple</p> 
	 * @return int (cardinalita' dell'insieme di Item della tupla)
	 * */ 
	public int getLength(){
		return tuple.length;
	}
	
	/**<p><b> Comportamento </b>: restutuisce l' Item in posizione i della tupla 
	 * </p> 
	 * @param i (indice dell' Item da considerare rispetto all'array tuple)
	 * @return	(item generico all'indice i dell'array)
	 */
	public Item get(int i){
			return tuple[i];	
	}
	
	/**<p><b> Comportamento </b>: aggiorna il membro tuple con un nuovo Item passato in input, che sara' inserito in posizione i
	 *  </p> 
	 * @param c (item da aggiungere all'array tuple)
	 * @param i (posizione in cui aggiungere l'item)
	 */
	public void add(Item c,int i){
		tuple[i] = c;
	}
	
	
	/**<p><b> Comportamento </b>: determina la distanza tra la tupla passata per argomento e la tupla corrente cioe' quella sulla quale il metodo e' invocato. 
	 * utilizza il metodo distance(Object o), di una sottoclasse di item, scelta opportunamente distinguendo caso discreto e continuo </p>
	 * 
	 * @param obj (tupla con cui calcolare la distanza)
	 * @return double (distanza totale intesa come somma delle distanze per ogni singolo item delle 2 tuple) 
	 */
	public double getDistance(Tuple obj){
		double dist = 0.0;
		for(int i = 0;i < getLength();i++){
			Item item = this.get(i);
			if(item instanceof ContinuousItem)
				dist += ((ContinuousItem) item).distance(obj.get(i));
			else if(item instanceof DiscreteItem)
				dist += ((DiscreteItem) item).distance(obj.get(i));
		}
		
		return dist;	
	}
	

	
	/**<p><b> Comportamento </b>: Crea una stringa formata da ciascun Item del membro tuple fornendo informazioni sul nome dell'attributo e sul valore che assume. </p>
	 * @return String (stringa che rappresenta il membro tuple)
	 */
	public String toString(){
		String str = "[ ";
		for(int j = 0;j < this.getLength();j++){
			 if(j < this.getLength() - 1)
				 str += this.get(j).toString() + ", ";
			 else								//Per fare in modo di non inserire la virgola alla fine
				 str += this.get(j).toString();
		}
		str += " ]";
		return str;
	}
}