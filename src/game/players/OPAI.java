package game.players;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import base.Edge;
import base.Graph;
import base.Node;
import game.AI;
import game.Game;
import game.map.Castle;
import gui.AttackThread;

public class OPAI extends AI {

  public OPAI(String name, Color color) {
    super(name, color);
  }

  /**
   * Picks the castles closest to Point (0|0).
   * @param game
   * @throws InterruptedException
   */
  private void pickCastles(Game game) throws InterruptedException {
	  Graph<Castle> graph = game.getMap().getGraph();
    List<Castle> availableCastles = game.getMap()
        .getCastles()
        .stream()
        .filter(c -> c.getOwner() == null)
        .collect(Collectors.toList());

    while(availableCastles.size() > 0 && getRemainingTroops() > 0) {
    	Castle chosenCastle = null;
    	if(this.getCastles(game).size() < 1) {
    		chosenCastle = availableCastles.get(0);
    	      Point point = new Point(0, 0);
    	      // Get available castle with closest distance to Point (0|0)
    	      for (Castle castle : availableCastles) {
    	        if (castle.distance(point) < chosenCastle.distance(point)) {
    	          chosenCastle = castle;
    	        }
    	      }
    	}else {
    		boolean castleSet = false;
    		for(Castle castle : this.getCastles(game)) {
    			for(Node<Castle> neighbor : graph.getNeighbors(graph.getNode(castle))) {
    				if(availableCastles.contains(neighbor.getValue())) {
    					chosenCastle = neighbor.getValue();
    					castleSet = true;
    				}
    			}
    			if(castleSet) {
    				break;
    			}
    		}
    		if(!castleSet) {
    			chosenCastle = availableCastles.get(0);
    		}
    	}
      sleep(500);
      availableCastles.remove(chosenCastle);
      game.chooseCastle(chosenCastle, this);
    }
  }

  /**
   * Places all available troops at the castle with least adjacent enemy castles.
   * @param game
   * @return the attacking castle
   * @throws InterruptedException
   */
  private Castle gatherForAttack(Game game) throws InterruptedException {
	    List<Castle> castlesNearEnemy = getCastlesNearEnemy(game);
	    if(castlesNearEnemy.size() < 1) {
	      return null;
	    }
	    // Add all remaining troops to the castle with the least adjacent enemy castles (attacking castle)
	    Castle attackingCastle = castlesNearEnemy.get(0); // castle with the least adjacent enemy castles

	    // Place the troops you get each turn onto the attacking castle.
	    while(this.getRemainingTroops() > 0) {
	      sleep(500);
	      game.addTroops(this, attackingCastle, 1);
	    }

	    // Draw all available troops from connected castles
	    for (Castle castle : this.getCastles(game)) {
	      sleep(1000);
	      if (castle.getTroopCount() > 1) {
	        game.moveTroops(castle, attackingCastle, castle.getTroopCount() - 1);
	      }
	    }
	    return attackingCastle;
	  }

  /**
   * Attacks
   * @return attacking castle
   * @throws InterruptedException 
   */
  private Castle attack(Game game) throws InterruptedException {
    boolean attackTerminated = false;
    Graph<Castle> graph = game.getMap().getGraph();
    Castle attackingCastle = gatherForAttack(game);
    do {
      if(attackingCastle == null) {
        break;
      }
      Node<Castle> nodeOfAttackingCastle = graph.getNode(attackingCastle);
      List<Castle> adjacentEnemyCastles = graph.getEdges(nodeOfAttackingCastle)
          .stream()
          .map((edges) -> edges.getOtherNode(nodeOfAttackingCastle).getValue())
          .filter(s -> s.getOwner() != this)
          .collect(Collectors.toList());
      if(adjacentEnemyCastles.size() < 1) {
        break;
      }

      // Determine adjacent enemy castle with fewest troops.
      Castle weakestEnemy = adjacentEnemyCastles.get(0);
      for (Castle castle : adjacentEnemyCastles) {
        if (castle.getTroopCount() < weakestEnemy.getTroopCount()) {
          weakestEnemy = castle;
        }
      }

      /* attack if attacking castle has at least four times 
       * the amount of troops than the target enemy castle*/
      if (attackingCastle.getTroopCount() >= 3 + weakestEnemy.getTroopCount()) {
        AttackThread attackThread = game.startAttack(attackingCastle, weakestEnemy, attackingCastle.getTroopCount());
        if(fastForward) {
          attackThread.fastForward(); 
        }
        attackThread.join();
        attackingCastle = gatherForAttack(game);
        
      } else {
        attackTerminated = true;
      }
    } while (!attackTerminated);
    return attackingCastle;
  }


  /**
   * Distributes all available troops equally over all castles 
   * with an adjacent enemy castle
   */
  private void distributeTroopsOverBoarders(Game game, Castle castleWithMostTroops) {
    if(castleWithMostTroops == null) return;
    List<Castle> castlesNearEnemy = getCastlesNearEnemy(game);
    int troopsRemaining = castleWithMostTroops.getTroopCount();
    Castle castleWithFewestTroops = getCastleWithFewestTroops(castlesNearEnemy);
    while(troopsRemaining - castleWithFewestTroops.getTroopCount() > 1){
      game.moveTroops(castleWithMostTroops, castleWithFewestTroops, 1);
      troopsRemaining--;
      castleWithFewestTroops = getCastleWithFewestTroops(castlesNearEnemy);
    }
  }

  /**
   * Returns a list of all castles who have an adjacent enemy castle.
   * The list is sorted in ascending order according to the number
   * of adjacent enemy castles.
   * @param game
   * @return
   */
  private List<Castle> getCastlesNearEnemy(Game game) {
    // Gets the list of all castles near an enemy castle
    Graph<Castle> graph = game.getMap().getGraph();
    List<Castle> castlesNearEnemy = new ArrayList<>();
    for(Castle castle : this.getCastles(game)) {
      Node<Castle> node = graph.getNode(castle);
      /* Go through all edges of the castle and adds it to the list,
       *if there is at least one enemy castle.*/
      for(Edge<Castle> edge : graph.getEdges(node)) {
        Castle otherCastle = edge.getOtherNode(node).getValue();
        if(otherCastle.getOwner() != this) {
          castlesNearEnemy.add(castle);
          break;
        }
      }
    }

    /*Sorts the list of castles near an enemy castle in ascending order 
     * by the number of adjacent enemy castles*/
    Collections.sort(castlesNearEnemy, new Comparator<Castle>() {
      @Override
      public int compare(Castle o1, Castle o2) {
        return getNumberOfAdjacentEnemyCastles(o1) - getNumberOfAdjacentEnemyCastles(o2);
      }

      private int getNumberOfAdjacentEnemyCastles(Castle o1) {
        int numberOfEnemies = 0;
        List<Node<Castle>> neighbors = graph.getNeighbors(graph.getNode(o1));
        for (Node<Castle> node : neighbors) {
          if (node.getValue().getOwner() != o1.getOwner()) {
            numberOfEnemies++;
          }
        }
        return numberOfEnemies;
      }
    });
    return castlesNearEnemy;
  }
  private List<Castle> getCastlesNearEnemyTerritory(Game game, List<Castle> territory) {
    // Gets the list of all castles near an enemy castle
    Graph<Castle> graph = game.getMap().getGraph();
    List<Castle> castlesNearEnemy = new ArrayList<>();
    for(Castle castle : territory) {
      Node<Castle> node = game.getMap().getGraph().getNode(castle);
      /* Go through all edges of the castle and adds it to the list,
       *if there is at least one enemy castle.*/
      for(Edge<Castle> edge : graph.getEdges(node)) {
        Castle otherCastle = edge.getOtherNode(node).getValue();
        if(otherCastle.getOwner() != this) {
          castlesNearEnemy.add(castle);
          break;
        }
      }
    }

    /*Sorts the list of castles near an enemy castle in ascending order 
     * by the number of adjacent enemy castles*/
    Collections.sort(castlesNearEnemy, new Comparator<Castle>() {
      @Override
      public int compare(Castle o1, Castle o2) {
        return getNumberOfAdjacentEnemyCastles(o1) - getNumberOfAdjacentEnemyCastles(o2);
      }

      private int getNumberOfAdjacentEnemyCastles(Castle o1) {
        int numberOfEnemies = 0;
        List<Node<Castle>> neighbors = graph.getNeighbors(graph.getNode(o1));
        for (Node<Castle> node : neighbors) {
          if (node.getValue().getOwner() != o1.getOwner()) {
            numberOfEnemies++;
          }
        }
        return numberOfEnemies;
      }
    });
    return castlesNearEnemy;
  }

  /**
   * Gets the castle with fewest troops out of given list of castles.
   * @param castles
   * @return
   */
  private Castle getCastleWithFewestTroops(List<Castle> castles) {
    Castle fewestTroops = castles.get(0);
    for(Castle castle : castles) {
      if(castle.getTroopCount() < fewestTroops.getTroopCount()) {
        fewestTroops = castle;
      }
    }
    return fewestTroops;
  }

  @Override
  protected void actions(Game game) throws InterruptedException {
    if (game.getRound() == 1) {
      pickCastles(game);
    } else {
      Castle attackingCastle = attack(game);
      distributeTroopsOverBoarders(game, attackingCastle);
    }
  }
}
