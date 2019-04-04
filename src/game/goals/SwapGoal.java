package game.goals;

import game.Game;
import game.Goal;
import game.Player;
import game.map.Castle;

public class SwapGoal extends Goal {

    public SwapGoal() {
        super("Vertauschen", "Pro Zug kann eine eigene Burg mit der eines Gegners getauscht werden.");
    }

    @Override
    public boolean isCompleted() {
        return this.getWinner() != null;
    }

    @Override
    public Player getWinner() {
        Game game = this.getGame();
        if(game.getRound() < 2)
            return null;

        Player p = null;
        for(Castle c : game.getMap().getCastles()) {
            if(c.getOwner() == null)
                return null;
            else if(p == null)
                p = c.getOwner();
            else if(p != c.getOwner())
                return null;
        }

        return p;
    }

    @Override
    public boolean hasLost(Player player) {
        if(getGame().getRound() < 2)
            return false;

        return player.getNumRegions(getGame()) == 0;
    }
}
