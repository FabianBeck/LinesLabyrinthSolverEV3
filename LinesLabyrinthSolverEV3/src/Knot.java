import java.util.Vector;


public class Knot {
	Vector<Edge> options;
	public Knot() {
		
	}
	public void addOptions(int[] direction){
		for (int i = 0; i < direction.length; i++) {
			Edge newEdge=new Edge(direction[i], this);
			options.addElement(newEdge);
		}
		
	}
}
