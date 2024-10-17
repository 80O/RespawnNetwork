package net.respawn.slicegames.states;

import com.respawnnetwork.respawnlib.gameapi.states.DefaultInGameState;
import net.respawn.slicegames.SliceGames;

/**
 * Represents the death match state in a Slice Games game.
 *
 * @author spaceemotion
 * @author TomShar
 * @version 1.0
 */
public class DeathMatchState extends DefaultInGameState<SliceGames> {

    public DeathMatchState(SliceGames game) {
        super(game);
    }

    @Override
    protected void onEnter() {
        // Immediately go to the next state when we already have a winner
        if (getGame().getMap().getWinner() != null) {
            getGame().nextState();
        }
    }

    @Override
    public String getName() {
        return "death-match";
    }

    @Override
    public String getDisplayName() {
        return "Death Match";
    }

}
