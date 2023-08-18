/**@author Sgura Francesco */

package icaData;

import icaData.ContinuousAttribute;
import icaData.ContinuousItem;
import icaData.Item;

/** <p>La classe <b>ContinuouseItem</b> modella un item (coppia attributo continuo - valore continuo) di una tupla</p> */
public class ContinuousItem extends Item implements Cloneable{

	/**<p><b>Comportamento</b> Costruttore: invoca il costruttore della classe madre </p>
	 * @param att (attributo continuo coinvolto nell'item)
	 * @param value  (valore assunto dall'attributo continuo)
	 */
	public ContinuousItem(ContinuousAttribute att, Double value) {
		super(att, value);
	}
	
	public ContinuousItem clone(){
		ContinuousItem it = new ContinuousItem((ContinuousAttribute)this.attribute, (Double) this.value);
		return it;
	}
	/**<p><b>Comportamento</b>: Determina la distanza (in valore assoluto) tra il valore normalizzato dall'item corrente e quello normalizzato dell'item passato
			per argomento.</p>
	 * @param a (item con cui calcolare la distanza)
	 * @return double (distanza tra i due item)
	 */
	@Override
	public double distance(Object a) {
		double val1 = ((ContinuousAttribute)this.getAttribute()).getScaledValue((Double) this.getValue());	
		double val2 = ((ContinuousAttribute)this.getAttribute()).getScaledValue(((Double) ((ContinuousItem) a).getValue()));	
		return(Math.abs(val2 - val1));
		
		// TODO Auto-generated method stub
	}
}
