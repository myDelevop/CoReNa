/** @author Sgura Francesco */

package icaData;


/** <p>La classe <b>ContinuousAttribute</b> rappresenta un intervallo per un attributo continuo </p>*/
public class ContinuousAttribute extends Attribute {

	
	/** <p> Estremo superiore dell'intervallo </p>*/
	private double max;
	
	/** <p> Estremo inferiore dell'intervallo </p>*/
	private double min ;
	
	/** <p><b>Comportamento</b>:invoca il costruttore della classe madre e ne avvalora i membri</p>
	 * @param name (nome dell'attributo)
	 * @param index (identificativo numerico dell'attributo)
	 * @param min (estremo inferiore dell'intervallo)
	 * @param max (estremo superiore dell'intervallo) */
	public ContinuousAttribute(String name, int index, double min, double max){
		super(name, index);
		this.max = max;
		this.min = min;
	}
	
	/** <p><b>Comportamento</b>: Esegue la normalizzazione in base al valore passato in input e quello dei membri min e max </p>
	 * @param v (valore dell'attributo da normalizzare il quale e' un numero in quanto attributo continuo)
	 * @return double (valore normalizzato)*/
	public double getScaledValue(double v){
		double v1 = 0.0;
		if( (v >= min) && (v <= max) )
			v1 = (v - min) / (max - min);		
		return v1;
	}
}
