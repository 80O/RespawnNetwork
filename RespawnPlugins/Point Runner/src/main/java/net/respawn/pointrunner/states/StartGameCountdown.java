package net.respawn.pointrunner.states;

import com.respawnnetwork.respawnlib.gameapi.states.CountdownState;
import net.respawn.pointrunner.PointRunner;
import org.bukkit.Sound;


public class StartGameCountdown extends CountdownState<PointRunner> {

    public StartGameCountdown(PointRunner game) {
        super(game);
    }

    @Override
    protected int getOffset() {
        PrepareGameState state = getGame().getState(PrepareGameState.class);
        return state != null ? state.getSeconds() : 0;
    }

    @Override
    protected Sound getFinishSound() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Start countdown";
    }

    @Override
    public String getName() {
        return "start-countdown";
    }

}
