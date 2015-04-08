public class Edge {
	int direction;
	// left=1;
	// front = 2;
	// right = 3;
	// back = 0;
	Knot parent;
	Knot child;

	public Edge(int directionEdge, Knot parentKnot) {
		parent = parentKnot;
		direction = directionEdge;

	}

	public void addChild(Knot childKnot) {
		child=childKnot;
	}
}
