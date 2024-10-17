package com.respawnnetwork.respawnlib.gameapi.modules;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.GameState;
import com.respawnnetwork.respawnlib.network.bungee.Bungee;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Represents a module for bungee stuff.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class BungeeModule<G extends Game> extends GameModule<G> implements Listener {
    private String lobbyServerName;
    private boolean kickAlways;
    private int kickDelay;


    public BungeeModule(G game) {
        super(game);

        // This will get added after the game has added all its general states
        // since modules are added after the states
        game.addState(new BungeeState<>(game));
    }

    @Override
    protected boolean onEnable() {
        this.lobbyServerName = getConfig().getString("lobbyServerName", "");
        this.kickAlways = getConfig().getBoolean("kickAlways", false);
        this.kickDelay = getConfig().getInt("kickDelay", 10);

        return true;
    }

    @EventHandler
    protected void onEndGame(PluginDisableEvent event) {
        if (event.getPlugin().equals(getGame().getPlugin())) {
            kickPlayers();
        }
    }

    public void kickPlayers() {
        if (lobbyServerName.isEmpty()) {
            getLogger().warning("No lobby server name set, players won't be kicked!");
            return;
        }

        getLogger().info("Kicking players to lobby '" + lobbyServerName + "'...");

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        byte[] message;

        try {
            dataOut.writeUTF("Connect");
            dataOut.writeUTF(lobbyServerName);

            message = byteOut.toByteArray();

            byteOut.close();
            dataOut.close();

        } catch (IOException io) {
            getLogger().log(Level.SEVERE, "Could not prepare kick message", io);
            return;
        }

        // Send message to all players
        for(Player p : getGame().getPlugin().getServer().getOnlinePlayers()) {
            p.sendPluginMessage(getGame().getPlugin(), Bungee.CHANNEL_NAME, message);
        }
    }

    @Override
    public String getDisplayName() {
        return "BungeeCord";
    }

    @Override
    public String getName() {
        return "bungee";
    }


    /**
     * Represents a custom game state for the bungee stuff
     *
     * @since 1.0.1
     */
    public final class BungeeState<H extends Game> extends GameState<H> {

        public BungeeState(H game) {
            super(game);
        }

        @Override
        protected void onEnter() {
            // Set everybody to spectator
            for (GamePlayer gamePlayer : getGame().getRealPlayers()) {
                gamePlayer.setSpectator(true);
            }

            getLogger().info("Lobby kicks set to " + (kickAlways ? "always" : "only on server stop"));

            new BukkitRunnable() {
                @Override
                public void run() {
                    getGame().nextState();
                }
            }.runTaskLater(getGame().getPlugin(), kickDelay * 20);
        }

        @Override
        protected void onLeave() {
            if (!kickAlways) {
                return;
            }

            kickPlayers();
        }

        @Override
        public String getDisplayName() {
            return "Bungee-EndGame";
        }

        @Override
        public String getName() {
            return "bungee-endgame";
        }

    }

}
