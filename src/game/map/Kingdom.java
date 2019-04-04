package game.map;

import game.Player;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

/**
 * Diese Klasse representiert ein Königreich. Jedes Königreich hat eine Liste von Burgen sowie einen Index {@link #type} im Bereich von 0-5
 *
 */
public class Kingdom {

    private List<Castle> castles;
    private int type;
    private Castle center;
    
    public Castle getCenter() {
      return this.center;
    }
    
    public void setCenter(Castle center) {
      this.center = center;
      if (!getCastles().contains(center)) {
        addCastle(center);
      }
    }
    
    
    /**
     * Erstellt ein neues Königreich
     * @param type der Typ des Königreichs (im Bereich 0-5)
     */
    public Kingdom(int type) {
        this.castles = new LinkedList<>();
        this.type = type;
    }

    /**
     * Eine Burg zum Königreich hinzufügen
     * @param castle die Burg, die hinzugefügt werden soll
     */
    public void addCastle(Castle castle) {
        this.castles.add(castle);
    }

    /**
     * Gibt den Typen des Königreichs zurück. Dies wird zur korrekten Anzeige benötigt
     * @return der Typ des Königreichs.
     */
    public int getType() {
        return this.type;
    }

    /**
     * Eine Burg aus dem Königreich entfernen
     * @param castle die zu entfernende Burg
     */
    public void removeCastle(Castle castle) {
        this.castles.remove(castle);
    }

    /**
     * Gibt den Spieler zurück, der alle Burgen in dem Köngreich besitzt.
     * Sollte es keinen Spieler geben, der alle Burgen besitzt, wird null zurückgegeben.
     * @return der Besitzer oder null
     */
    public Player getOwner() {
        if(castles.isEmpty())
            return null;

        Player owner = castles.get(0).getOwner();
        for(Castle castle : castles) {
            if(castle.getOwner() != owner)
                return null;
        }

        return owner;
    }

    /**
     * Gibt alle Burgen zurück, die in diesem Königreich liegen
     * @return Liste von Burgen im Königreich
     */
    public List<Castle> getCastles() {
        return this.castles;
    }
    
    /**
     * @author shangsuru
     * Bestimmt neues Zentrum für das Königreich, indem
     * der Mittelpunkt aller Burgen bestimmt und die Burg,
     * die am nächsten zu diesem Punkt ist, als neues Zentrum
     * ausgewählt wird.
     */
    public void determineNewCenter() {
      setCenter(getClosestCastle(getCenterOfCastles()));
    }
    
    /**
     * @author shangsuru
     * @param point
     * @return nächstgelegene Burg zum gegebenen Punkt
     */
    private Castle getClosestCastle(Point point) {
      Castle closest = getCastles().get(0);
      Castle current;
      for (int i = 1; i < getCastles().size(); i++) {
        current = getCastles().get(i);
        if (current.distance(point) < closest.distance(point)) {
          closest = current;
        }
      }
      return closest;
    }
    
    /**
     * Diese Methode gibt den Mittelpunkt aller Burgen innerhalb des 
     * Königreichs wieder, indem sie den Durschnitt über 
     * alle x und y Koordinaten der Burgen bestimmt.
     * @return Mittelpunkt 
     */
    private Point getCenterOfCastles() {
      double meanX = 0;
      double meanY = 0;
      int numberOfCastles = getCastles().size();
      
      for (Castle castle : getCastles()) {
        meanX += castle.getLocationOnMap().getX();
        meanY += castle.getLocationOnMap().getY();
      }
      meanX /= numberOfCastles;
      meanY /= numberOfCastles;
      
      return new Point((int) meanX, (int) meanY);
    }
    
    /**
     * Überprüft ob die zwei Königreiche gleich sind.
     * @param kingdom
     * @return
     */
    public boolean isSame(Kingdom kingdom) {
      if (this.type != kingdom.type) {
        return false;
      }
      if (!this.center.isSame(kingdom.getCenter())) {
        return false;
      }
      if (this.getCastles().size() != kingdom.getCastles().size()) {
        return false;
      }
      for (int i = 0; i < this.getCastles().size(); i++) {
        if (!this.getCastles().get(i).isSame(kingdom.getCastles().get(i))) {
          return false;
        }
      }
      
      return true;
    }

    public void clearCastles() {
      this.castles = new LinkedList<>();
    }
    
}
  