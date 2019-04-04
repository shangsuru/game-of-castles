package game.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Diese Klasse teilt Burgen in Königreiche auf
 */
public class Clustering {

  private Random random;
  private final List<Castle> allCastles;
  private final int kingdomCount;

  /**
   * Ein neues Clustering-Objekt erzeugen.
   * 
   * @param castles Die Liste von Burgen, die aufgeteilt werden sollen
   * @param kingdomCount Die Anzahl von Königreichen die generiert werden sollen
   */
  public Clustering(List<Castle> castles, int kingdomCount) {
    if (kingdomCount < 2)
      throw new IllegalArgumentException("Ungültige Anzahl an Königreichen");

    this.random = new Random();
    this.kingdomCount = kingdomCount;
    this.allCastles = Collections.unmodifiableList(castles);
  }

  /**
   * Gibt eine Liste von Königreichen zurück. Jedes Königreich sollte dabei einen Index im Bereich
   * 0-5 bekommen, damit die Burg richtig angezeigt werden kann. Siehe auch
   * {@link Kingdom#getType()}
   */
  public List<Kingdom> getPointsClusters() {
    
    List<Kingdom> kingdoms = createKingdoms();
    kingdoms = getOptimizedKingdoms(kingdoms);
    
    while (!isSame(kingdoms, getOptimizedKingdoms(kingdoms))) {
      kingdoms = getOptimizedKingdoms(kingdoms);
    }
    
    return kingdoms;
  }
  
  /**
   * Gibt true zurück, wenn es keinen Unterschied zwischen 
   * den Listen von Königreichen gibt.
   * @param kingdoms
   * @param optimizedKingdoms
   * @return
   */
  private boolean isSame(List<Kingdom> kingdoms, List<Kingdom> optimizedKingdoms) {
    if (kingdoms.size() != optimizedKingdoms.size()) {
      return false;
    }
    for (int i = 0; i < kingdoms.size(); i++) {
      if (!kingdoms.get(i).isSame(optimizedKingdoms.get(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Fügt jede Burg zu dem Königreich hinzu, dessen Zentrum am nächsten ist.
   * Dann werden die Zentren der Königreiche neu gesetzt, indem die Burg, die 
   * am nächsten am Mittelpunkt des Königreichs liegt, als neues Zentrum 
   * bestimmt wird. 
   * @param kingdoms
   * @return optimized 
   */
  private List<Kingdom> getOptimizedKingdoms(List<Kingdom> kingdoms) {
    List<Kingdom> optimized = kingdoms.stream().collect(Collectors.toList());
    for (Kingdom kingdom : optimized) {
      kingdom.clearCastles();
    }
    for (Castle castle : allCastles) {
      castle.setKingdom(getClosestKingdom(castle, optimized));
    }
    
    for (Kingdom kingdom : optimized) {
      kingdom.determineNewCenter();
    }
    
    return optimized;
  }
  
  private List<Castle> getArbitraryKingdomCenters() {
    List<Castle> kingdomCenters = new ArrayList<>();
    Castle randomCastle;
    while (kingdomCenters.size() < kingdomCount) {
      randomCastle = allCastles.get(random.nextInt(allCastles.size()));
      if (!kingdomCenters.contains(randomCastle)) {
        kingdomCenters.add(randomCastle);
      }
    }
    return kingdomCenters;
  }
  
  /**
   * Erzeugt eine Liste von Königreichen und fügt jedem Königreich
   * eine zufällig gewählte Burg als Zentrum hinzu.
   */
  private List<Kingdom> createKingdoms() {
    List<Castle> kingdomCenters = getArbitraryKingdomCenters();
    
    List<Kingdom> kingdoms = new ArrayList<>();
    for (int i = 0; i < kingdomCount; i++) {
      Kingdom newKingdom = new Kingdom(random.nextInt(6));
      newKingdom.setCenter(kingdomCenters.get(i));
      kingdoms.add(newKingdom);
    }
    
    return kingdoms;
  }

  /**
   * @param castle
   * @return Königreich, das am nächsten zur gegeben Burg liegt
   */
  private Kingdom getClosestKingdom(Castle castle, List<Kingdom> kingdoms) {
    Iterator<Kingdom> it = kingdoms.iterator();
    if (kingdoms.isEmpty()) {
      return null;
    }
    Kingdom nearest = it.next();
    Kingdom current;
    while (it.hasNext()) {
      current = it.next();
      if (current.getCenter().distance(castle) < nearest.getCenter().distance(castle)) {
        nearest = current;
      }
    }
    return nearest;
  }

}