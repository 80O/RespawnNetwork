package com.respawnnetwork.respawnlobby.runnables;

import com.respawnnetwork.respawnlobby.GameServer;
import com.respawnnetwork.respawnlobby.RespawnLobby;
import com.respawnnetwork.respawnlobby.network.Ping;


public class UpdateServerPings extends LobbyRunnable {

    public UpdateServerPings(RespawnLobby plugin) {
        super(plugin);
    }

    @Override
    public void run() {
        for (GameServer gameServer : getPlugin().getGameServers()) {
            Ping ping = new Ping(gameServer.getAddress(), gameServer.getPort(), GameServer.DEFAULT_TIMEOUT);
            gameServer.setOnline(0);

            // Get server information
            if (ping.fetch(getPlugin().getLogger())) {
                gameServer.setOffline(ping.getMotd() == null || ping.getMotd().equalsIgnoreCase("ended"));

            } else {
                gameServer.setOffline(true);
            }

            // Update general server information
            gameServer.setMotd(ping.getMotd());
            gameServer.setOnline(ping.getOnline());
            gameServer.setMax(ping.getMax());
        }
    }

}
