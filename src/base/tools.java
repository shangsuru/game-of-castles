package base;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import game.Game;
import game.Player;
import game.map.Castle;

public class tools {
	/**
	   * 
	   * @param game
	   * @return biggest Territory
	   */
	  public static List<Castle> getBiggestTerritory(Game game, Player player){
		  List<List<Castle>> territories = getTerritories(game, player);
		  List<Castle> biggestTerritory = territories.get(0);
		  for(List<Castle> territory : territories) {
			  if(territory.size() > biggestTerritory.size()) {
				  biggestTerritory = territory;
			  }
		  }
		  return biggestTerritory;
	  }
	  /**
	   * gets a list of allied territories
	   * @param game
	   * @return
	   */
	  public static List<List<Castle>> getTerritories(Game game, Player player){
		  List<List<Castle>> returnList = new ArrayList<>();
		  List<Castle> myCastles = new ArrayList<>(player.getCastles(game));
		  while(myCastles.size() > 0) {
			  List<Castle> territory = getTerritory(myCastles.get(0), new ArrayList<Castle>(), game, player);
			  if(!territory.isEmpty()) {
				  returnList.add(territory);
			  }
			  subtractLists(myCastles, territory);
		  }
		  return returnList;
	  }
	  
	  /**
	   * substract one list from another
	   * @param from list ot be substracted from
	   * @param of list of elements to be substracted
	   */
	  public static <T> void subtractLists(List<T> from, List<T> of){
		  for(T element : of) {
			  if(from.contains(element)) {
				  from.remove(element);
			  }
		  }
	  }
	  
	  /**
	   * gets all allied castles reachable from @param castle in a list
	   * @param castle
	   * @param result
	   * @param game
	   * @return list of reachable allied castles
	   */
	  public static List<Castle> getTerritory(Castle castle, List<Castle> result, Game game, Player player){
		  List<Castle> alliedNeighbors = getAlliedNeighbors(castle, game, player);
		  List<Castle> added = new ArrayList<>();
		  boolean notCovered = false;
		  for(Castle castl : alliedNeighbors) {
			  if(!result.contains(castl)) {
				  notCovered = true;
				  result.add(castl);
				  added.add(castl);
			  }
		  }
		  if(notCovered){
			  for(Castle cstl : added) {
				  mergeLists(result, getTerritory(cstl, result, game, player));
			  }
		  }
		  return result;
	  }
	  
	  /**
	   * merges two lists without making duplicates
	   * @param list1 
	   * @param list2
	   * @return merged list
	   */
	  public static <T> List<T> mergeLists(List<T> list1, List<T> list2){
		  for(T element : list2) {
			  if(!list1.contains(element)) {
				  list1.add(element);
			  }
		  }
		  return list1;
	  }
	  
	  
	  public static String arrayToString(List<Castle> list) {
		  String out = "";
		  int count = 0;
		  for(Castle castle : list) {
			  if(count < 1) {
				  out += castle.getName() + " (" + castle.getTroopCount() + ")";
			  }else {
				  out += ", " +castle.getName() + " (" + castle.getTroopCount() + ")";
			  }
			  count++;
		  }
		  return out;
	  }
	  
	  public static List<Castle> getAlliedNeighbors(Castle castle, Game game, Player player){
		  return game.getMap()
				  	 .getGraph()
				  	 .getNeighbors(game.getMap().getGraph().getNode(castle))
				  	 .stream()
				  	 .map(s -> s.getValue()).filter(s -> s.getOwner() == player)
				  	 .collect(Collectors.toList());
	  }
}
