package com.respawnnetwork.respawnlib.gameapi;

import com.respawnnetwork.respawnlib.gameapi.statistics.PlayerStatistics;
import com.respawnnetwork.respawnlib.network.accounts.MojangAccount;
import lombok.Delegate;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents a player that plays a game.
 *
 * @author spaceemotion
 * @version 1.0.1
 */
public abstract class GamePlayer<G extends Game> {
    /** The game he's participating */
    @Getter
    private final G game;

    /** The Mojang account object */
    @Delegate
    private final MojangAccount account;

    /** The UUID assigned by bukkit */
    private final UUID bukkitUID;

    @Getter
    private final PlayerStatistics statistics;

    /** Amount of tokens to give at the end */
    @Getter
    @Deprecated
    private int tokensEarned = 0;

    /**
     * Creates a new game player.
     *
     * @param game The game instance
     * @param player The bukkit player
     */
    public GamePlayer(@NotNull G game, Player player) {
        this.game = game;

        this.account = new MojangAccount(player);
        this.bukkitUID = player.getUniqueId();

        this.statistics = game.getStatistics().getFor(this);
    }

    @Deprecated
    public void addTokensEarned(int amount) {
        this.tokensEarned += amount;
    }

    @Deprecated
    public int tokensEarned() {
        return getTokensEarned();
    }

    /**
     * Completely heals this player (including fire ticks, food level, ...).
     *
     * @since 1.0.1
     */
    public void heal() {
        Player player = getPlayer();
        if (player == null) {
            return;
        }

        player.setHealth(player.getMaxHealth());
        player.setFireTicks(0);
        player.setFoodLevel(20);
        player.setExhaustion(0);
        player.setFallDistance(0);

        clearPotionEffects();
    }

    /**
     * Toggles the player spectator mode.
     * <p />
     * This might fail if: the player hasn't been added to the game yet, or the
     * bukkit player equals nil.
     *
     * @param spectator True if he should be a spectator, false if not
     * @since 1.0.1
     * @return True if we were able to set the spectators status, false if not
     */
    @SuppressWarnings("unchecked")
    public boolean setSpectator(boolean spectator) {
        Player player = getPlayer();
        if (player == null) {
            return false;
        }

        if (spectator) {
            return getGame().addSpectator(this);

        } else {
            return getGame().removeSpectator(this);
        }
    }

    /**
     * Checks if the player is a spectator.
     *
     * @return True if the player is a spectator, false if not
     * @since 1.0.1
     */
    public boolean isSpectator() {
        return getGame().isSpectator(getPlayer());
    }

    /**
     * Safely teleports this player to another game player.
     *
     * @param other The other game player
     * @since 1.0.1
     */
    public void teleportTo(GamePlayer other) {
        if (other == null) {
            return;
        }

        teleportTo(other.getPlayer());
    }

    /**
     * Safely teleports this player to another entity.
     *
     * @param entity The other entity
     * @since 1.0.1
     */
    public void teleportTo(Entity entity) {
        if (entity == null) {
            return;
        }

        teleportTo(entity.getLocation());
    }

    /**
     * Safely teleports this player to another location.
     *
     * @param location The other location
     * @since 1.0.1
     */
    public void teleportTo(Location location) {
        if (location == null) {
            return;
        }

        Player player = getPlayer();

        if (player == null) {
            return;
        }

        player.teleport(location);
        player.setFallDistance(0f);
    }

    /**
     * Clears the full inventory of this player, including armor.
     *
     * @since 1.0.1
     */
    public void clearInventory() {
        Player player = getPlayer();

        if (player == null) {
            return;
        }

        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().clear();
    }

    /**
     * Clears all active potion effects on this player.
     *
     * @since 1.0.1
     */
    public void clearPotionEffects() {
        Player player = getPlayer();
        if (player == null) {
            return;
        }

        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
    }

    /**
     * Returns the original bukkit player.
     *
     * @return The player or null if player is not online
     */
    @Nullable
    public Player getPlayer() {
        // Always use the bukkit ID
        // This will support offline servers as well then
        return Bukkit.getPlayer(bukkitUID);
    }

    /**
     * Returns the offline player instance.
     * <p />
     * If the server runs on spigot, this function can only be called in a <b>non-blocking
     * thread</b>!
     *
     * @return The offline player
     * @since 1.0.1
     */
    @Nullable
    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(bukkitUID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GamePlayer that = (GamePlayer) o;

        return !(bukkitUID != null ? !bukkitUID.equals(that.bukkitUID) : that.bukkitUID != null) && game.equals(that.game);
    }

    @Override
    public int hashCode() {
        return 31 * (game.hashCode()) + (bukkitUID != null ? bukkitUID.hashCode() : 0);
    }

}
