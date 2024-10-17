package com.respawnnetwork.respawnlobby.runnables;

import com.respawnnetwork.respawnlobby.RespawnLobby;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Represents a runnable for the lobby plugin holding the plugin instance.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public abstract class LobbyRunnable extends BukkitRunnable {

    /** The lobby plugin */
    @Getter
    private final RespawnLobby plugin;


    /**
     * Creates a new lobby task.
     *
     * @param plugin The lobby plugin instance
     */
    LobbyRunnable(RespawnLobby plugin) {
        this.plugin = plugin;
    }


    /**
     * Reloads configuration related things within this runnable.
     *
     * @param section The new, updated configuration
     */
    public void loadConfig(ConfigurationSection section) {
        // Nothing to see here ...
    }

}
