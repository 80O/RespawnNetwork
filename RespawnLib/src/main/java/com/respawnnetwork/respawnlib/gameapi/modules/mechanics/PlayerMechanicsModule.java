package com.respawnnetwork.respawnlib.gameapi.modules.mechanics;

import com.respawnnetwork.respawnlib.bukkit.Item;
import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.states.InGameState;
import com.respawnnetwork.respawnlib.network.accounts.MojangAccount;
import com.respawnnetwork.respawnlib.network.database.Database;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.List;

import static com.respawnnetwork.respawnlib.network.database.generated.Tables.KILL_LOG;

/**
 * Represents a module that adds a few player mechancis to the game.
 * <p />
 * Every mechanic is toggleable. This module currently contains:
 * <ul>
 *     <li>No item drops (neither by pressing the drop key or on death)</li>
 *     <li>No respawns (they're out of the game)</li>
 * </ul>
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
@Getter
public class PlayerMechanicsModule<G extends Game> extends GameModule<G> implements Listener {
    private final List<ItemStack> killRewards;

    @Setter
    private boolean itemDrops;

    @Setter
    private boolean playerRespawns;

    @Setter
    private boolean noHunger;

    @Setter
    private boolean invincible;

    
    public PlayerMechanicsModule(G game) {
        super(game);

        this.killRewards = new LinkedList<>();
    }

    protected void onPlayerRespawns(GamePlayer gamePlayer, PlayerDeathEvent event) {
        respawnPlayer(gamePlayer, getGame().getMap().getSpawnLocation());
    }

    @Override
    protected boolean onEnable() {
        // Basic toggles
        itemDrops = getConfig().getBoolean("itemDrops", false);
        playerRespawns = getConfig().getBoolean("respawns", false);
        noHunger = getConfig().getBoolean("noHunger", false);
        invincible = getConfig().getBoolean("invincible", false);

        // Rewards
        ConfigurationSection rewardsSection = getConfig().getConfigurationSection("rewards");

        if (rewardsSection != null) {
            // Kill rewards
            List<?> killList = rewardsSection.getList("kill");

            if (killList != null) {
                Item.parseInventory(getLogger(), killList, killRewards);
            }
        }

        return true;
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (event.isCancelled() || isItemDrops()) {
            return;
        }

        // Just cancel the event
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onChangeHunger(FoodLevelChangeEvent event) {
        if (event.isCancelled() || !isNoHunger()) {
            return;
        }

        if (!getGame().isSpectator(((Player) event.getEntity()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player)) {
            return;
        }

        // Cancel damage event when players should be invincible
        if (invincible) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent e) {
        if(getGame().getCurrentState() instanceof InGameState) {
            checkGameEnd();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerKick(PlayerKickEvent e) {
        if(getGame().getCurrentState() instanceof InGameState) {
            checkGameEnd();
        }
    }

    private void checkGameEnd() {
        // Otherwise just go to the next state
        if (getGame().getNumberOfRealPlayers() <= 1) {
            getGame().stopGame();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        // Remove cursor item
        InventoryView openInventory = e.getEntity().getOpenInventory();
        if (openInventory != null) {
            openInventory.setCursor(null);
        }

        // Close inventory
        e.getEntity().closeInventory();

        // Clear drops first
        if (!itemDrops && e.getDrops() != null) {
            e.getDrops().clear();
        }

        // Get killed player and handle respawns if set
        final Player killed = e.getEntity();

        if (isPlayerRespawns()) {
            GamePlayer gamePlayer = getGame().getPlayer(killed);

            if (gamePlayer == null) {
                getLogger().info("Could not get game player, will not trigger respawn method, but still log kill!");

            } else {
                onPlayerRespawns(gamePlayer, e);
            }
        }

        // Just ignore that for now I guess
        if(killed.getKiller() == null) {
            return;
        }

        final Player killer = killed.getKiller();

        // Give item rewards to killer
        if (!getKillRewards().isEmpty()) {
            killer.getInventory().addItem(getKillRewards().toArray(new ItemStack[getKillRewards().size()]).clone());
        }

        final Database db = getGame().getPlugin().getDatabaseManager();
        if(db == null) {
            getLogger().warning("Database is null, couldn't log kill.");
            return;
        }

        final int gameId = getGame().getDescriptor().getId();
        final int mapId = getGame().getMap().getId();

        if (gameId == 0 || mapId == 0) {
            // We logged this earlier
            return;
        }

        if (!db.connected()) {
            db.open();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                db.getContext().insertInto(KILL_LOG)
                        .set(KILL_LOG.GAME_ID, gameId)
                        .set(KILL_LOG.MAP_ID, mapId)
                        .set(KILL_LOG.KILLER, new MojangAccount(killer).getUuid())
                        .set(KILL_LOG.KILLED, new MojangAccount(killed).getUuid())
                        .execute();
            }
        }.runTask(getGame().getPlugin());
    }

    @Override
    public String getDisplayName() {
        return "Player Mechanics";
    }

    @Override
    public String getName() {
        return "mechanics";
    }

    /**
     * Respawns the player at the given location.
     *
     * @param gamePlayer The player to respawn
     * @param location The location he will respawn
     */
    protected void respawnPlayer(GamePlayer gamePlayer, Location location) {
        if (gamePlayer == null || location == null) {
            return;
        }

        Player player = gamePlayer.getPlayer();

        if (player == null) {
            getLogger().warning("Could not respawn player '" + gamePlayer.getName() + "', bukkit player not found!");
            return;
        }

//        gamePlayer.clearInventory();
        gamePlayer.heal();
        gamePlayer.teleportTo(location);

        // Run respawn event
        callEvent(new PlayerRespawnEvent(player, location, false));

        // Fuuuuuuuuuuuu bukkit
        player.updateInventory();
    }

}
