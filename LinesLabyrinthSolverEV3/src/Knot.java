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
         *  Dem Konstruktor wird die Kante ��bergeben aus der man kommt, daraus berechnet sich dann die wayback Edge.(die Richtung muss umgedreht werden)
         */
        wayBack = new Edge((wayBackEdge.direction+2)%4,wayBackEdge.getParent());
        wayBack.setChild(wayBackEdge.getChild());
    }

    public void addOption(int direction) {
        /** Diese Funktion f��gt dem Vector eine Kante  hinzu, welche von dem Knoten zu einem neuen Knoten f��hrt. Dabei wird die Richtung der gew��nschten Kante 
           mit ��bergeben.
           
      
        
        1. Zuerst erstellen wir die gew��nschte Kante, dabei ��bergeben wir die Richtung die bei Funktionsaufruf mitgegeben wurde unver��ndert weiter und
           ��bergeben den Knoten, dem die Option hinzugef��gt wird, als Parent (this).
        
        2. Der gerade erstellten Kante fehlt noch eine Childknot (Also der Knoten, welchen wir beim Folgen dieser Kante als n��chstes treffen).
           Diesen erstellen wir direkt beim setzen und geben unsere erstellte Kante mit, damit der Knoten am Ende daraus seinen R��ckweg berechnen kann.
           
        3. Zum Schluss f��gen wir mit addElement() unsere Kante dem Vector hinzu.
        
       
        **/
        Edge newEdge = new Edge(direction, this);
        newEdge.setChild(new Knot(newEdge));
        options.addElement(newEdge);
        
    }

    public Edge getWayBack() {
        
         /** Gibt uns die Edge f��r den R��ckweg von diesem Knoten zur��ck **/ 
        return wayBack;
    }

    public Edge getNextOption() {
        
        /** 
           Diese Funktion gibt die erste im Vector gespeicherte Kante zur��ck und l��scht diese danach,
           sodass beim n��chsten Eintreffen an diesem Knoten diese Option nicht mehr zur Verf��gung steht.(So werden keine Wege doppelt gefahren)
           
           Wenn der Vector leer ist und somit schon alle Wege gefahren wurden, gibt die Funktion null zur��ck und
           wir k��nnen daraus schlie��en, dass wir mit dem Knoten fertig sind.
           **/
        if (options.size() > 0) {
            Edge res = options.get(0);
            options.removeElementAt(0);
            return res;
        } else   {
           // System.out.println("no elements left");
            return null;
        }
    }
    
    
}
