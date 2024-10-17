package com.respawnnetwork.respawnlobby.runnables;

import com.respawnnetwork.respawnlobby.RespawnLobby;
import com.respawnnetwork.respawnlobby.ScoreboardSign;

import java.util.List;


/**
 * The task that updates the scoreboard signs with the top players on the servers.
 * Also updates the player skull if found.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class UpdateScoreboardSigns extends LobbyRunnable {

    public UpdateScoreboardSigns(RespawnLobby plugin) {
        super(plugin);
    }

    @Override
    public void run() {
        List<ScoreboardSign> signs = getPlugin().getScoreboardSigns();

        // Don't do stuff when we have an empty list
        if (signs.isEmpty()) {
            return;
        }

        for (ScoreboardSign sign : signs) {
            sign.update();
        }
    }

}
