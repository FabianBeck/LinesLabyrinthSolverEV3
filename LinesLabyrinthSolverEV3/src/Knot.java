import java.util.Vector;

public class Knot {
	Vector<Edge> options=new Vector<Edge>();
	Edge wayBack=new Edge(0, null, null);

	public Knot() {

	}

	public void addOptions(Vector<Integer> direction) {
		for (int i = 0; i < direction.size(); i++) {
			Edge newEdge = new Edge(direction.get(i), this, new Knot());
			options.addElement(newEdge);
		}

	}

	public void addWayBack(Edge edge) {
		wayBack = edge;
	}

	public Edge getWayBack() {
		return wayBack;
	}

	public Edge getNextOption(){
		if(options.size()>=0){
		Edge res=options.get(0);
		options.removeElementAt(0);
		return res;
		}else{ System.out.println("no elements left");}
		return wayBack;//TODO: Wichtig umbauen damit Rückfahrt eingeleitet wird!
	}
}
