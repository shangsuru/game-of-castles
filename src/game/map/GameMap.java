package game.map;

import base.*;
import game.GameConstants;
import gui.Resources;
import javafx.util.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Diese Klasse representiert das Spielfeld. Sie beinhaltet das Hintergrundbild, welches mit Perlin noise erzeugt wurde,
 * eine Liste mit Königreichen und alle Burgen und deren Verbindungen als Graphen.
 *
 * Die Karte wird in mehreren Schritten generiert, siehe dazu {@link #generateRandomMap(int, int, int, int, int)}
 */
public class GameMap {

    private BufferedImage backgroundImage;
    private Graph<Castle> castleGraph;
    private List<Kingdom> kingdoms;

    // Map Generation
    private double[][] noiseValues;
    private int width, height, scale;

    /**
     * Erzeugt eine neue leere Karte. Der Konstruktor sollte niemals direkt aufgerufen werden.
     * Um eine neue Karte zu erstellen, muss {@link #generateRandomMap(int, int, int, int, int)} verwendet werden
     * @param width die Breite der Karte
     * @param height die Höhe der Karte
     * @param scale der Skalierungsfaktor
     */
    private GameMap(int width, int height, int scale) {
        this.castleGraph = new Graph<>();
        this.width = width;
        this.height = height;
        this.scale = scale;
    }

    /**
     * Wandelt einen Noise-Wert in eine Farbe um. Die Methode kann nach belieben angepasst werden
     * @param value der Perlin-Noise-Wert
     * @return die resultierende Farbe
     */
    private Color doubleToColor(double value) {
        if (value <= 0.40)
            return GameConstants.COLOR_WATER;
        else if (value <= 0.5)
            return GameConstants.COLOR_SAND;
        else if (value <= 0.7)
            return GameConstants.COLOR_GRASS;
        else if (value <= 0.8)
            return GameConstants.COLOR_STONE;
        else
            return GameConstants.COLOR_SNOW;
    }

    /**
     * Hier wird das Hintergrund-Bild mittels Perlin-Noise erzeugt.
     * Siehe auch: {@link PerlinNoise}
     */
    private void generateBackground() {
        PerlinNoise perlinNoise = new PerlinNoise(width, height, scale);
        Dimension realSize = perlinNoise.getRealSize();

        noiseValues = new double[realSize.width][realSize.height];
        backgroundImage = new BufferedImage(realSize.width, realSize.height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < realSize.width; x++) {
            for (int y = 0; y < realSize.height; y++) {
                double noiseValue = perlinNoise.getNoise(x, y);
                noiseValues[x][y] = noiseValue;
                backgroundImage.setRGB(x, y, doubleToColor(noiseValue).getRGB());
            }
        }
    }

    /**
     * Hier werden die Burgen erzeugt.
     * Dabei wir die Karte in Felder unterteilt, sodass auf jedes Fals maximal eine Burg kommt.
     * Sollte auf einem Feld keine Position für eine Burg existieren (z.B. aufgrund von Wasser oder angrenzenden Burgen), wird dieses übersprungen.
     * Dadurch kann es vorkommen, dass nicht alle Burgen generiert werden
     * @param castleCount die maximale Anzahl der zu generierenden Burgen
     */
    private void generateCastles(int castleCount) {
        double square = Math.ceil(Math.sqrt(castleCount));
        double length = width + height;

        int tilesX = (int) Math.max(1, (width / length + 0.5) * square) + 5;
        int tilesY = (int) Math.max(1, (height / length + 0.5) * square) + 5;
        int tileW = (width * scale / tilesX);
        int tileH = (height * scale / tilesY);

        if (tilesX * tilesY < castleCount) {
            throw new IllegalArgumentException(String.format("CALCULATION Error: tilesX=%d * tilesY=%d < castles=%d", tilesX, tilesY, castleCount));
        }

        // Add possible tiles
        List<Point> possibleFields = new ArrayList<>(tilesX * tilesY);
        for (int x = 0; x < tilesX - 1; x++) {
            for (int y = 0; y < tilesY - 1; y++) {
                possibleFields.add(new Point(x, y));
            }
        }

        // Generate castles
        List<String> possibleNames = generateCastleNames();
        int castlesGenerated = 0;
        while (possibleFields.size() > 0 && castlesGenerated < castleCount) {
            Point randomField = possibleFields.remove((int) (Math.random() * possibleFields.size()));
            int x0 = (int) ((randomField.x + 0.5) * tileW);
            int y0 = (int) ((randomField.y + 0.5) * tileH);

            for (int x = (int) (0.5 * tileW); x >= 0; x--) {
                boolean positionFound = false;
                for (int y = (int) (0.5 * tileH); y >= 0; y--) {
                    int x_mid = (int) (x0 + x + 0.5 * tileW);
                    int y_mid = (int) (y0 + y + 0.5 * tileH);
                    if (noiseValues[x_mid][y_mid] >= 0.6) {
                        String name = possibleNames.isEmpty() ? "Burg " + (castlesGenerated + 1) :
                            possibleNames.get((int) (Math.random() * possibleNames.size()));
                        Castle newCastle = new Castle(new Point(x0 + x, y0 + y), name);
                        boolean doesIntersect = false;

                        for (Castle r : castleGraph.getAllValues()) {
                            if (r.distance(newCastle) < Math.max(tileW, tileH)) {
                                doesIntersect = true;
                                break;
                            }
                        }

                        if (!doesIntersect) {
                            possibleNames.remove(name);
                            castleGraph.addNode(newCastle);
                            castlesGenerated++;
                            positionFound = true;
                            break;
                        }
                    }
                }

                if (positionFound)
                    break;
            }
        }
    }

    /**
     * Hier werden die Kanten erzeugt. Dazu werden zunächst alle Burgen durch eine Linie verbunden und anschließend
     * jede Burg mit allen anderen in einem bestimmten Radius nochmals verbunden
     */
    private void generateEdges() {
    	connectClosestEdges();
    	connectGroup(castleGraph);
    	removeSubs();
    }
    
    /**
     * makes all nodes reachable from any node
     * @param graph graph
     */
    public static void connectGroup(Graph<Castle> graph) {
    	// initialized by getting list of all groups
    	List<List<Node<Castle>>> allGroups = GameMap.getAllGroups(graph);
    	// memory array that saves edges already created
    	boolean[][] alreadyConnected = new boolean[allGroups.size()][allGroups.size()];
    	if(allGroups.size() < 2);
    	else {
    		for(int i = 0; i < allGroups.size(); i++) {
    			Pair<Double, Pair<Node<Castle>, Node<Castle>>> closestGroup = new Pair<Double, Pair<Node<Castle>, Node<Castle>>>(Double.MAX_VALUE, null);
    			List<Node<Castle>> group1 = allGroups.get(i);
    			int closestGroupNum = -1;
    			for(int o = 0; o < allGroups.size(); o++) {
    				if(i == o | alreadyConnected[i][o]) continue;
    				List<Node<Castle>> group2 = allGroups.get(o);
    				Pair<Double, Pair<Node<Castle>, Node<Castle>>> dist = groupDist(group1, group2);
    				if(dist.getKey() < closestGroup.getKey()) {
    					closestGroup = dist;
    					closestGroupNum = o;
    				}
    			}
    			Pair<Node<Castle>, Node<Castle>> closestNodes = closestGroup.getValue();
    			if(closestNodes != null)
    				graph.addEdge(closestNodes.getKey(), closestNodes.getValue());
    			if(closestGroupNum > -1)
    				alreadyConnected[closestGroupNum][i] = true;
    		}
    		connectGroup(graph);
    	}
    }
    
    /**
     * connects the closest edges with each other
     * @param castles list of castles on the map
     * @param edges list of edges
     */
    public void connectClosestEdges() {
    	List<Node<Castle>> castles = castleGraph.getNodes();
    	for(int i = 0; i < castles.size(); i++) {
    		Node<Castle> current = castles.get(i);
    		List<Edge<Castle>> alreadyConnected = castleGraph.getEdges(current);
    		connectClosestEdge(current, alreadyConnected, true);
    	}
    }
    
    
    /**
     * searches for the closest node with no edge between given node
     * @param current node that should be connected
     * @param alreadyConnected list of nodes that are already connected to given node
     */
    public void connectClosestEdge(Node<Castle> current, List<Edge<Castle>> alreadyConnected, boolean removeLeafs) {
    	List<Node<Castle>> listOfNearbyNodes = getNodesByDist(current);
		for(int i = 0; i < listOfNearbyNodes.size();i++) {
			Node<Castle> nearbyCastle = listOfNearbyNodes.get(i);
			boolean ac = false;
			for(Edge<Castle> edge : alreadyConnected) {
				if(edge.getOtherNode(current).equals(nearbyCastle)) {
					ac = true;
				}
			}
			if(!ac && (!isThereABetterWay(current, nearbyCastle) || !removeLeafs)) {
				castleGraph.addEdge(current, nearbyCastle);
				break;
			}
		}
    }
    
    
    /**
     * returns a list of nodes sorted by closest distance
     * @param castle castle that given list should be sorted by
     * @return list of nodes sorted by closest distance
     */
    public List<Node<Castle>> getNodesByDist(Node<Castle> castle){
    	List<Node<Castle>> sortedList = new ArrayList<Node<Castle>>();
    	List<Node<Castle>> blacklist = new ArrayList<Node<Castle>>();
    	blacklist.add(castle);
    	int end = castleGraph.getNodes().size();
    	while(sortedList.size() != end) {
    		Node<Castle> closest = getClosestNode(castle, blacklist);
    		if(closest == null) {
    			break;
    		}
    		sortedList.add(closest);
    		blacklist.add(closest);
    	}
    	return sortedList;
    }
    
    
    /**
     * 
     * @param castle target castle node
     * @param blacklist the nodes that should be ignored
     * @return closest node to castle
     */
    public Node<Castle> getClosestNode(Node<Castle> castle, List<Node<Castle>> blacklist){
    	List<Node<Castle>> allCastles = castleGraph.getNodes();
    	int i = 0;
    	Node<Castle> closestCastle = null;
    	double distToClosestCastle = Double.MAX_VALUE;
    	for(; i< allCastles.size(); i++) {
    		Node<Castle> curr = allCastles.get(i);
    		if(!blacklist.contains(curr)) {
    			closestCastle = curr;
    			distToClosestCastle = castle.getValue().distance(closestCastle.getValue());
    			break;
    		}
    	}
    	for(; i< allCastles.size(); i++) {
    		Node<Castle> comp = allCastles.get(i);
    		if(blacklist.contains(comp)) {
    			continue;
    		}
    		double distToComp = castle.getValue().distance(comp.getValue());
    		if(distToComp < distToClosestCastle) {
    			closestCastle = comp;
    			distToClosestCastle = distToComp;
    		}
    	}
    	return closestCastle;
    }
    
    
    /**
     * for debugging purposes only
     * @param alreadyConnected 
     * @return
     */
    public static <T> String arrayToString(boolean[][] alreadyConnected){
    	String out = "[ ";
    	for(int i = 0; i < alreadyConnected.length; i++) {
    		out += "[ ";
    		for(int o = 0; o < alreadyConnected[i].length; o++) {
    			out += alreadyConnected[i][o] + ", ";
    		}
    		out += "] ";
    	}
    	out += "]";
    	return out;
    }


    /**
     * returns nodes with 2 edges or less
     * @return nodes with 2 edges or less
     */
    public Node<Castle> getSub(){
        for(Node<Castle> node : castleGraph.getNodes()){
            if(castleGraph.getEdges(node).size() < 2){
               return node;
            }else if(castleGraph.getEdges(node).size() < 3) {
            	for(Node<Castle> neighbor : getNeighbors(node, castleGraph)) {
            		if(castleGraph.getEdges(neighbor).size() < 3) {
            			return node;
            		}
            	}
            }
        }
        return null;
    }
    
    
    /**
     * removes "subs" by connecting them to another node
     */
    public void removeSubs() {
    	Node<Castle> leaf = getSub();
    	while(leaf != null) {
        	List<Edge<Castle>> alreadyConnected = castleGraph.getEdges(leaf);
    		connectClosestEdge(leaf, alreadyConnected, true);
    		leaf = getSub();
    	}
    }
    
    
    public boolean isThereABetterWay(Node<Castle> start, Node<Castle> dest) {
    	Castle startingCastle = start.getValue();
    	Castle destCastle = dest.getValue();
    	for(Node<Castle> neighbor : getNeighbors(start, this.castleGraph)) {
    		Castle neighborCastle = neighbor.getValue();
    		for(Node<Castle> neighborOfNeighbor : getNeighbors(neighbor, this.castleGraph)) {
    			if(neighborOfNeighbor == dest) {
    				if(startingCastle.distance(neighborCastle) 
    						+ destCastle.distance(neighborCastle) 
    						< startingCastle.distance(destCastle) *  1.3) {
    					return true;
    				}
    			}
    		}
    	}
    	return false;
    }
    
    
    
    /**
     * calculates the distance between two groups of castles
     * @param group1
     * @param group2
     * @return a Pair storing distance information and node information
     */
    public static Pair<Double, Pair<Node<Castle>, Node<Castle>>> groupDist(List<Node<Castle>> group1, List<Node<Castle>> group2) {
    	Pair<Double, Pair<Node<Castle>, Node<Castle>>> closestRange = new Pair<Double, Pair<Node<Castle>, Node<Castle>>>(Double.MAX_VALUE , null);
    	for(Node<Castle> node1 : group1) {
    		for(Node<Castle> node2 : group2) {
    			Double dist = node1.getValue().distance(node2.getValue());
    			if(dist.compareTo(closestRange.getKey()) < 0) {
    				closestRange = new Pair<Double, Pair<Node<Castle>, Node<Castle>>>(dist, new Pair(node1, node2));
    			}
    		}
    	}
    	return closestRange;
    }
    
    /**
     * divides connected castles into groups
     */
    public static <T> List<List<Node<T>>> getAllGroups(Graph<T> graph){
    	List<List<Node<T>>> out = new ArrayList<>();
    	ArrayList<Node<T>> allNodes = new ArrayList<Node<T>>(graph.getNodes());
    	for(; allNodes.size() > 0;) {
    		List<Node<T>> group = GameMap.getGroup(allNodes.get(0), graph, null);
    		for(Node<T> node : group) {
    			allNodes.remove(node);
    		}
    		out.add(group);
    	}
    	return out;
    }
    
    /**
     * 
     * @param n starting node
     * @param graph graph
     * @param collector collecting list for recursion
     * @return list of nodes reachable from the given node over the edges in the path
     */
    public static <T> List<Node<T>> getGroup(Node<T> n, Graph<T> graph, List<Node<T>> collector){
    	if(collector == null) collector = new ArrayList<Node<T>>();
    	if(!collector.contains(n)) collector.add(n);
    	for(Node<T> node : getNeighbors(n, graph)) {
    		if(!collector.contains(node))
    			getGroup(node, graph, collector);
    	}
    	return collector;
    }
    
    
    /**
     * returns all neighbours of a Node in a Graph
     * @param n node
     * @param graph graph
     * @return list of neighbors of the node in the graph
     */
    public static <T> List<Node<T>> getNeighbors(Node<T> n, Graph<T> graph){
    	List<Edge<T>> edges = graph.getEdges();
    	List<Node<T>> chain = new ArrayList<>();
    	for(Edge<T> edge : edges) {
    		if(edge.getNodeA().equals(n)) {
    			chain.add(edge.getNodeB());
    		}else if(edge.getNodeB().equals(n)) {
    			chain.add(edge.getNodeA());
    		}
    	}
    	return chain;
    }

    
    /**
     * Hier werden die Burgen in Königreiche unterteilt. Dazu wird der {@link Clustering} Algorithmus aufgerufen.
     * @param kingdomCount die Anzahl der zu generierenden Königreiche
     */
    private void generateKingdoms(int kingdomCount) {
        if(kingdomCount > 0 && kingdomCount < castleGraph.getAllValues().size()) {
            Clustering clustering = new Clustering(castleGraph.getAllValues(), kingdomCount);
            kingdoms = clustering.getPointsClusters();
        } else {
            kingdoms = new ArrayList<>();
        }
    }

    /**
     * Eine neue Spielfeldkarte generieren.
     * Dazu werden folgende Schritte abgearbeitet:
     *   1. Das Hintergrundbild generieren
     *   2. Burgen generieren
     *   3. Kanten hinzufügen
     *   4. Burgen in Köngireiche unterteilen
     * @param width die Breite des Spielfelds
     * @param height die Höhe des Spielfelds
     * @param scale die Skalierung
     * @param castleCount die maximale Anzahl an Burgen
     * @param kingdomCount die Anzahl der Königreiche
     * @return eine neue GameMap-Instanz
     */
    public static GameMap generateRandomMap(int width, int height, int scale, int castleCount, int kingdomCount) {

        width = Math.max(width, 15);
        height = Math.max(height, 10);

        if (scale <= 0 || castleCount <= 0)
            throw new IllegalArgumentException();

        System.out.println(String.format("Generating new map, castles=%d, width=%d, height=%d, kingdoms=%d", castleCount, width, height, kingdomCount));
        GameMap gameMap = new GameMap(width, height, scale);
        gameMap.generateBackground();
        gameMap.generateCastles(castleCount);
        gameMap.generateEdges();
        gameMap.generateKingdoms(kingdomCount);

        if(!gameMap.getGraph().allNodesConnected()) {
            System.out.println("Fehler bei der Verifikation: Es sind nicht alle Knoten miteinander verbunden!");
            return null;
        }

        return gameMap;
    }

    /**
     * Generiert eine Liste von Zufallsnamen für Burgen. Dabei wird ein Prefix (Schloss, Burg oder Festung) an einen
     * vorhandenen Namen aus den Resourcen angefügt. Siehe auch: {@link Resources#getcastleNames()}
     * @return eine Liste mit Zufallsnamen
     */
    private List<String> generateCastleNames() {
        String[] prefixes = {"Schloss", "Burg", "Festung"};
        List<String> names = Resources.getInstance().getCastleNames();
        List<String> nameList = new ArrayList<>(names.size());

        for (String name : names) {
            String prefix = prefixes[(int) (Math.random() * prefixes.length)];
            nameList.add(prefix + " " + name);
        }

        return nameList;
    }

    public int getWidth() {
        return this.backgroundImage.getWidth();
    }

    public int getHeight() {
        return this.backgroundImage.getHeight();
    }

    public BufferedImage getBackgroundImage() {
        return this.backgroundImage;
    }

    public Dimension getSize() {
        return new Dimension(this.getWidth(), this.getHeight());
    }

    public List<Castle> getCastles() {
        return castleGraph.getAllValues();
    }

    public Graph<Castle> getGraph() {
        return this.castleGraph;
    }

    public List<Edge<Castle>> getEdges() {
        return this.castleGraph.getEdges();
    }

    public List<Kingdom> getKingdoms() {
        return this.kingdoms;
    }
}
