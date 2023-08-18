package Utility;

public class Arch implements Comparable<Arch>,Cloneable {
	private Node x;
	private Node y;
	private Object weigth;
	public boolean visited = false;
	
	public Arch(Node a, Node b, Object weigth){
		this.x=a;
		this.y=b;
		this.weigth=weigth;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		Arch o = (Arch)obj;
		if(this.getX().equals(o.getX()))
			return this.getY().equals(o.getY()); 
		else
			if(this.getX().equals(o.getY()))
				return this.getY().equals(o.getX());
			else
				return false;
	}

	public Node getX(){
		return x;		
	}
	public Node getY(){
		return y;		
	}
	public Object getWeight(){
		return weigth;		
	}
	public String toString(){
		String arch = "";
		arch += x.getId() + " " + x.getIsTrain() + " " + weigth + " " + y.getId() + " "+y.getIsTrain();
		return arch;
	}
	public void setWeigth(Object d){
		weigth = d;
	}
public Arch clone(){
	return new Arch(this.x.clone(),this.y.clone(),this.weigth);
}
	@Override
	public int compareTo(Arch o) {
		int res = (int) (this.getX().equals(o.getX()) ? this.getY().getId() - o.getY().getId() : this.getY().getId() - o.getX().getId());
		return res; 
		
	}

}
