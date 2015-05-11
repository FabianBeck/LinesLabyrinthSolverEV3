public class Edge {
	int direction;
	// North=0;
	// South =2;
	// West = 1;
	// East = 3;
	Knot parent;
	Knot child;

	public Edge(int directionEdge, Knot parentKnot, Knot childKnot) {
		parent = parentKnot;
		direction = directionEdge;
		child=childKnot;
	}
	public Knot getChild(){
		return child;
	}
	public Knot getParent(){
		return parent;
	}
	
}
