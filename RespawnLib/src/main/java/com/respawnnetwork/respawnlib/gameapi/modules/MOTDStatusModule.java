package com.respawnnetwork.respawnlib.gameapi.modules;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

/**
 * Represents the game status in the MOTD (Message Of The Day) in server pings.
 *
 * @author TomShar
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class MOTDStatusModule<G extends Game> extends GameModule<G> implements Listener {

    /**
     * Creates a new MOTD status module instance.
     *
     * @param game The game instance
     */
    public MOTDStatusModule(G game) {
        super(game);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void serverPing(ServerListPingEvent e) {
        if (getGame().isRunning()) {
            e.setMotd(getGame().getCurrentState().getDisplayName());

        } else if (getGame().hasEnded()) {
            e.setMotd("ended");

        } else {
            e.setMotd("waiting");
        }
    }

    @Override
    public String getDisplayName() {
        return "MOTD Status";
    }

    @Override
    public String getName() {
        return "status";
    }

}
