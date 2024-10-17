package net.respawn.pointrunner.states;

import com.respawnnetwork.respawnlib.bukkit.Location;
import com.respawnnetwork.respawnlib.gameapi.modules.ScoreModule;
import com.respawnnetwork.respawnlib.gameapi.states.CountdownState;
import net.respawn.pointrunner.PRPlayer;
import net.respawn.pointrunner.PointRunner;
import net.respawn.pointrunner.modules.PointRunnerModule;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.Iterator;


public class PrepareGameState extends CountdownState<PointRunner> {

    public PrepareGameState(PointRunner game) {
        super(game);
    }

    @Override
    public void onEnter() {
        // Create location iterator
        PointRunnerModule module = getGame().getModule(PointRunnerModule.class);
        Iterator<Location> locationIterator = null;

        if (module != null) {
           locationIterator = module.getSpawnlocations().iterator();
        }

        // Show scoreboard
        ScoreModule scoreModule = getGame().getModule(ScoreModule.class);
        if (scoreModule != null) {
            scoreModule.getScoreboards().assignToAll();
            scoreModule.getScoreboards().displaySlot(DisplaySlot.SIDEBAR);
        }

        // Make all players non-spectators until we run out of spawn places
        for (PRPlayer gamePlayer : getGame().getPlayers()) {
            if (locationIterator == null || !locationIterator.hasNext()) {
                continue;
            }

            // gamePlayer.resetPlayer();
            gamePlayer.setSpectator(false);
            gamePlayer.teleportTo(locationIterator.next());
        }

        super.onEnter();
    }

}
