public class Edge {
    
    /** 
       Eine Kante hat einen Parentknot und einen Childknot, auf dem Hinweg bewegt sich das Mindstorm immer Richtung Childknot,
       die Richtung daf��r l��sst sich mit direction erkennen.
       **/
	public int direction;
	// North=0;
	// South =2;
	// West = 1;
	// East = 3;
	Knot parent;
	Knot child;

	public Edge(int directionEdge, Knot parentKnot) {
	    
	    /** Dem Konstruktor wird die Richtung der Kante (Parent->Child) und sein Parentknot mitgegeben
	     *  und schlie��lich dem jeweiligen Attribut zugewiesen**/
		parent = parentKnot;
		direction = directionEdge;
	
	}
	
	
	/**Hier folgen simple Funktionen, um Parentknot und ChildKnot einer Kante zu bekommen bzw. Childknot zu setzen **/ 
	
	public void setChild(Knot childKnot){
		child=childKnot;
	}
	public Knot getChild(){
		return child;
	}
	public Knot getParent(){
		return parent;
	}
	
}
