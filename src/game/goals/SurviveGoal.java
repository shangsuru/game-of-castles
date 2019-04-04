package game.goals;

import game.Goal;
import game.Player;
import game.map.Castle;
import game.players.Human;

public class SurviveGoal extends Goal{

	public SurviveGoal() {
		super("Last Man Standing", "Überlebe 10 Runden gegen einen übermächtigen Gegner.");
	}

	@Override
	public boolean isCompleted() {
		return getWinner() != null;
	}

	@Override
	public Player getWinner() {
		Player ai = null;
		Player human = null;
		boolean humanHasCastles = false;
		if(this.getGame().getRound() <= 1) {
			return null;
		}
		for(Player player : this.getGame().getPlayers()){
			if(player instanceof Human) {
				human = player;
				for(Castle castle : getGame().getMap().getCastles()) {
					if(castle.getOwner() == player) {
						humanHasCastles = true;
					}
				}
			}else {
				ai = player;
			}
		}
		if(humanHasCastles) {
			if(this.getGame().getRound() == 10) {
				return human;
			}else {
				return null;
			}
		}else {
			return ai;
		}
	}

	@Override
	public boolean hasLost(Player player) {
		if(isCompleted()) {
			if(getWinner() != null) {
				if(getWinner() != player) {
					return true;
				}
			}
		}
		return false;
	}

}
