package com.respawnnetwork.respawnlobby;

import lombok.Getter;
import org.bukkit.event.Listener;

/**
 * Created by spaceemotion on 11/05/14.
 */
public class LobbyListener implements Listener {
    /** The lobby plugin */
    @Getter
    private final RespawnLobby plugin;


    public LobbyListener(RespawnLobby plugin) {
        this.plugin = plugin;
    }

}
