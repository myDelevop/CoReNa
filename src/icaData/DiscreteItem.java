/** @author Sgura Francesco */

package icaData;

/** <p>La classe <b>DiscreteItem</b> modella un item (coppia attributo discreto - valore discreto)  di una tupla</p> */
public class DiscreteItem extends Item implements Cloneable{
	
	
	/**<p><b>Comportamento</b>: Invoca il costruttore della classe madre </p>
	 * 
	 * @param attr (attributo discreto coinvolto nell'item)
	 * @param val  (valore assunto dall'attributo discreto)
	 */
	DiscreteItem(DiscreteAttribute attr, String val){
		super(attr,val);
	}
	
	/**<p><b>Comportamento</b>: Restituisce 0 se i valori sono identici, 1 altrimenti</p>
	 * @param a (item con cui calcolare la distanza)
	 * @return double (distanza tra i due item)
	 */
	@Override
	double distance(Object a) {
		int distanza = 1;	
		if(this.getValue().equals(((DiscreteItem) a).getValue()))
			distanza = 0;	
		return distanza;
	}
	
	public DiscreteItem clone(){
		DiscreteItem dit = new DiscreteItem((DiscreteAttribute)this.attribute, (String)this.value);
		return dit;
	}

}
