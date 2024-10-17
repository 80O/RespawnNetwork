package com.respawnnetwork.respawnlobby.statistics;

import com.respawnnetwork.respawnlobby.RespawnLobby;
import lombok.Getter;

/**
 * Represents a server statistic that fills in information on its own.
 *
 * @author spaceemotion
 * @version 1.0
 */
public abstract class ServerStatistic {
    @Getter
    private final RespawnLobby plugin;


    public ServerStatistic(RespawnLobby plugin) {
        this.plugin = plugin;
    }

    /**
     * Fills in the needed signs, heads and whatever we need.
     */
    public abstract void fillInformation();

}
