package game.goals;

import java.util.List;
import java.util.stream.Collectors;
import game.Goal;
import game.Player;

public class BlitzGoal extends Goal {
  
  public BlitzGoal() {
    super("Blitzkrieg", "Derjenige Spieler gewinnt, der zuerst eine gegnerische Hauptstadt erobert.");
  }

  @Override
  public boolean isCompleted() {
    return this.getWinner() != null;
  }

  @Override
  public Player getWinner() {
    // when the player ownes more than one capital, he is the winner
    List<Player> players = this.getGame().getPlayers();
    List<Player> owners = this.getGame()
                              .getCapitals()
                              .stream()
                              .map(castle -> castle.getOwner())
                              .collect(Collectors.toList());
    for (Player player : players) {
      if (owners.stream().filter(owner -> owner == player).count() > 1) {
        return player;
      }
    }
    return null;
  }

  @Override
  public boolean hasLost(Player player) {
    return false;
  }
  
}
