package net.respawn.goldrush.states;

import com.respawnnetwork.respawnlib.gameapi.modules.team.Team;
import com.respawnnetwork.respawnlib.gameapi.states.DefaultInGameState;
import net.respawn.goldrush.GRPlayer;
import net.respawn.goldrush.GoldRush;
import org.bukkit.World;


public class InGRGameState extends DefaultInGameState<GoldRush> {

    public InGRGameState(GoldRush game) {
        super(game);
    }

    @Override
    public void onEnter() {
        // Reset difficulty
        World world = getGame().getMap().getWorld();
        if (world != null) {
            world.setDifficulty(getGame().getMap().getDifficulty());
        }

        // Reset levels
        for (GRPlayer grPlayer : getGame().getPlayers()) {
            grPlayer.reset();
            grPlayer.addSpeed();
        }

        super.onEnter();
    }

    @Override
    public void onLeave() {
        super.onLeave();

        Team winner = getGame().getMap().getWinningTeam();
        if (winner == null) {
            return;
        }

        // Set and announce winner
        getGame().getMap().setWinningTeam(winner);
        getGame().createMessage().provide("winner", winner.getDisplayName()).sendKey("goldrush.win");
    }

}
