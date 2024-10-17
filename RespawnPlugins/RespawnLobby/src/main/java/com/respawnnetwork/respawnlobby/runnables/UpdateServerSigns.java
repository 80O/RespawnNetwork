package com.respawnnetwork.respawnlobby.runnables;

import com.respawnnetwork.respawnlib.network.signs.SignBuilder;
import com.respawnnetwork.respawnlobby.GameServer;
import com.respawnnetwork.respawnlobby.RespawnLobby;
import lombok.Getter;

import java.util.List;

/**
 * Represents a task that updates all lobby signs.
 *
 * @author spaceemotion
 * @author TomShar
 * @version 1.0.1
 */
public class UpdateServerSigns extends LobbyRunnable {
    /** The amount of players online */
    @Getter
    private int playersOnline = 0;

    /** The amount of servers online */
    @Getter
    private int serversOnline = 0;


    /**
     * Creates a new update sign task.
     *
     * @param plugin The lobby plugin instance
     */
    public UpdateServerSigns(RespawnLobby plugin) {
        super(plugin);
    }

    @Override
    public void run() {
        // Reset amounts
        playersOnline = 0;
        serversOnline = 0;

        List<GameServer> signs = getPlugin().getGameServers();
        if(signs.isEmpty()) {
            return;
        }

        // Update server signs
        for(GameServer gameServer : signs) {
            gameServer.update();

            playersOnline += gameServer.getOnline();
            serversOnline += gameServer.isOffline() ? 0 : 1;
        }

        // Update global server statistics
        SignBuilder signBuilder = new SignBuilder().provide("servers", serversOnline)
                .provide("players", playersOnline)
                .lines("Servers online:", "&5{servers}", "Players online:", "&9{players}");

        // Add all status signs and apply the changes
        signBuilder.add(getPlugin().getStatusSigns());
        signBuilder.apply();
    }

}
