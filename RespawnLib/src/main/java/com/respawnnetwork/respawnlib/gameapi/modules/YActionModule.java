package com.respawnnetwork.respawnlib.gameapi.modules;

import com.respawnnetwork.respawnlib.bukkit.Location;
import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;

/**
 * Handles actions that occur on a specific y height.
 *
 * @author spaceemotion
 * @version 1.0
 */
public class YActionModule<G extends Game> extends GameModule<G> implements Listener {
    private boolean yKill;
    private int y;
    private Location teleportLocation;


    public YActionModule(G game) {
        super(game);
    }

    @Override
    protected boolean onEnable() {
        y = getConfig().getInt("height", 1);
        yKill = getConfig().getBoolean("kills", true);

        teleportLocation = (Location) getConfig().get("teleport", new Location(getGame().getMap().getSpawnLocation()));

        return true;
    }

    @EventHandler
    public void onCheckHeight(PlayerMoveEvent event) {
        // We're not in range yet
        if (event.getTo().getBlockY() > y) {
            return;
        }

        // Pass the event to a protected method
        GamePlayer player = getGame().getPlayer(event.getPlayer());
        if (player == null) {
            return;
        }

        if (player.isSpectator()) {
            player.teleportTo(getGame().getMap().getSpawnLocation());
            event.getPlayer().setFlying(true);

        } else {
            onDoAction(player);
        }
    }

    /**
     * This method gets called whenever we reached the specified y height.
     * <p />
     * Subclasses can override this to add custom functionality.
     *
     * @param gamePlayer The player that triggered this call
     */
    protected void onDoAction(GamePlayer gamePlayer) {
        // Kill the player
        if (yKill) {
            Player player = gamePlayer.getPlayer();

            if (player == null || player.getGameMode() == GameMode.CREATIVE) {
                return;
            }

            // Build drop list
            List<ItemStack> drops = new LinkedList<>();
            for (ItemStack itemStack : player.getInventory().getContents()) {
                if (itemStack == null) {
                    continue;
                }

                drops.add(itemStack);
            }

            callEvent(new PlayerDeathEvent(player, drops, 0, null));

        } else {
            // Teleport the player
            gamePlayer.teleportTo(teleportLocation);
        }
    }

    @Override
    public String getDisplayName() {
        return "Y-Actions";
    }

    @Override
    public String getName() {
        return "yActions";
    }

}
