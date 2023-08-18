/** @author Sgura Francesco */
package icaData;


/** <p>La classe <b>Attribute</b> permette la modellazione di un attributo generico sia discreto che continuo</p> */
public abstract class Attribute implements Cloneable
{
	
	/** <p>Nome simbolico dell'attributo</p> */
	protected String name; 
	
	/** <p>identificativo numerico dell'attributo</p> */
	protected int index;
	
	protected boolean isTarget = false;
	  
	/** <p><b>Comportamento</b>: Inizializza i valori dei membri "name" e "index"</p>
	 * 	@param name (valore per il nome simbolico dell'attributo)
	 * 	@param index (valore dell'identificativo numerico dell'attributo) 
	 */
	Attribute(String name, int index){
		this.name = name;
		this.index = index;		   
	}
	   
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		result = prime * result + (isTarget ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Attribute other = (Attribute) obj;
		if (index != other.index)
			return false;
		if (isTarget != other.isTarget)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public boolean isTarget(){
		return isTarget;
	}
	public void setTarget(){
		 isTarget=true;
	}
	/** <p><b> Comportamento </b>: Restituisce il nome simbolico dell'attributo </p>
	 * 
	 * @return String ( nome simbolico dell'attributo )
	 */
	public String getName(){
		return this.name;
	}; 
	   
	/** <p><b> Comportamento </b>: Restituisce l'identificativo numerico dell'attributo </p>
	 * 
	 * @return Int ( identificativo numerico dell'attributo )
	 */
	int getIndex(){
		return this.index;
	};
	   
	/** <p><b> Comportamento </b>: Restituisce il nome simbolico dell'attributo </p>
	 * 
	 * @return String ( nome identificativo dell'attributo )
	 */
	public String toString(){
		return (this.name); 
    };   
}
