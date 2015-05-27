import java.util.Vector;

public class Knot {
    
    /** 
      Ein Knoten besteht aus einem Vector options, welcher Edge Objekte aufnimmt um die Abbiegeoptionen zu speichern.
      Als zweites Attribut ist der R��ckweg eines Knoten zu seinem Vaterknoten gespeichert.
     **/
    Vector<Edge> options = new Vector<Edge>();
    Edge wayBack;

    public Knot(Edge wayBackEdge) {
        /** 
         *  Dem Konstruktor wird die Kante uebergeben aus der man kommt, daraus berechnet sich dann die wayback Edge.(die Richtung muss umgedreht werden)
         */
        wayBack = new Edge((wayBackEdge.direction+2)%4,wayBackEdge.getParent());
        wayBack.setChild(wayBackEdge.getChild());
    }

    public void addOption(int direction) {
        /** Diese Funktion fuegt dem Vector eine Kante  hinzu, welche von dem Knoten zu einem neuen Knoten fuehrt. Dabei wird die Richtung der gewuenschten Kante 
           mit uebergeben.
           
      
        
        1. Zuerst erstellen wir die gewuenschte Kante, dabei uebergeben wir die Richtung die bei Funktionsaufruf mitgegeben wurde unveraendert weiter und
           uebergeben den Knoten, dem die Option hinzugefuegt wird, als Parent (this).
        
        2. Der gerade erstellten Kante fehlt noch eine Childknot (Also der Knoten, welchen wir beim Folgen dieser Kante als naechstes treffen).
           Diesen erstellen wir direkt beim setzen und geben unsere erstellte Kante mit, damit der Knoten am Ende daraus seinen Rueckweg berechnen kann.
           
        3. Zum Schluss fuegen wir mit addElement() unsere Kante dem Vektor hinzu.
        
       
        **/
        Edge newEdge = new Edge(direction, this);
        newEdge.setChild(new Knot(newEdge));
        options.addElement(newEdge);
        
    }

    public Edge getWayBack() {
        
         /** Gibt uns die Edge fuer den Rueckweg von diesem Knoten zurueck **/ 
        return wayBack;
    }

    public Edge getNextOption() {
        
        /** 
           Diese Funktion gibt die erste im Vector gespeicherte Kante zurueck und loescht diese danach,
           sodass beim naechsten Eintreffen an diesem Knoten diese Option nicht mehr zur Verfuegung steht.(So werden keine Wege doppelt gefahren)
           
           Wenn der Vector leer ist und somit schon alle Wege gefahren wurden, gibt die Funktion null zurueck und
           wir koennen daraus schliessen, dass wir mit dem Knoten fertig sind.
           **/
        if (options.size() > 0) {
            Edge res = options.get(0);
            options.removeElementAt(0);
            return res;
        } else   {
        	//no elements left
            return null;
        }
    }
    
    
}
