package com.respawnnetwork.respawnlib.gameapi.events;

import com.respawnnetwork.respawnlib.bukkit.events.EntityKillsEntityEvent;
import com.respawnnetwork.respawnlib.bukkit.events.Events;
import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.statistics.Statistic;
import com.respawnnetwork.respawnlib.network.menu.InventoryMenu;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

/**
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
@AllArgsConstructor
public class GameEventListener implements Listener {
    @Getter(AccessLevel.PRIVATE)
    private final Game game;

    @EventHandler()
    public void onInteract(PlayerInteractEvent e) {

        if (getGame().isSpectator(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler()
    public void onEntityInteract(PlayerInteractEntityEvent e) {

        if (getGame().isSpectator(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (getGame() == null || !getGame().isRunning()) {
            return;
        }

        getGame().callEvent(new PlayerJoinGameEvent(getGame().addPlayer(event.getPlayer())));
    }

    @EventHandler(ignoreCancelled = true)
    public void onLooseHunger(FoodLevelChangeEvent event) {
        HumanEntity entity = event.getEntity();

        if (entity instanceof Player && getGame().isSpectator(((Player) entity))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpenStuff(InventoryOpenEvent event) {
        if (event.isCancelled() || event.getInventory().getHolder() instanceof InventoryMenu) {
            return;
        }

        HumanEntity entity = event.getPlayer();

        if (entity instanceof Player && getGame().isSpectator((Player) entity)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpectatorChange(PlayerSetSpectatorEvent event) {
        Player player = event.getGamePlayer().getPlayer();
        if (player == null) {
            return;
        }

        Game currentGame = event.getGamePlayer().getGame();

        // Add invisibility potion
        if (event.enableSpectator()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true), true);

        } else {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }

        // Hide player from non-spectators
        for (GamePlayer gamePlayer : currentGame.getPlayers()) {
            Player bukkitPlayer = gamePlayer.getPlayer();
            if (bukkitPlayer == null) {
                continue;
            }

            if (!gamePlayer.isSpectator() && event.enableSpectator()) {
                bukkitPlayer.hidePlayer(player);

            } else {
                bukkitPlayer.showPlayer(player);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTargetEntity(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player)) {
            return;
        }

        Player playerTarget = (Player) event.getTarget();
        if (getGame().isSpectator(playerTarget)) {
            if (event.getEntity() instanceof ExperienceOrb){
                repellExpOrb(playerTarget, (ExperienceOrb) event.getEntity());
            }

            event.setTarget(null);
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageEntity(EntityDamageEvent event) {
        handleEntityDamage(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow && event.getEntity() instanceof Player) {
            if (getGame().isSpectator(((Player) event.getEntity()))) {
                Arrow arrow = (Arrow) event.getDamager();
                Player victim = (Player) event.getEntity();

                victim.teleport(victim.getLocation().clone().add(0, 2, 0));

                Vector vector = arrow.getVelocity();
                Location loc = arrow.getLocation();
                Arrow newArrow = victim.launchProjectile(Arrow.class);

                newArrow.teleport(loc);
                newArrow.setVelocity(vector);
                newArrow.setShooter(arrow.getShooter());

                arrow.remove();
            }
        }

        // Cancel if the damager is a spectator
        if (event.getDamager() instanceof Player && getGame().isSpectator((Player) event.getDamager())) {
            event.setCancelled(true);
            return;
        }

        handleEntityDamage(event);
    }

    private void handleEntityDamage(EntityDamageEvent event) {
        // Cancel if the damaged entity is a spectator
        if (event.getEntity() instanceof Player && getGame().isSpectator(((Player) event.getEntity()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickupStuff(PlayerPickupItemEvent event) {
        if (getGame().isSpectator(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    // ---------------- Statistics related event handlers ----------------

    @EventHandler(priority = EventPriority.MONITOR)
    public void onShootArrow(ProjectileLaunchEvent event) {
        if (event.isCancelled() || !getGame().getStatistics().isTracking(Statistic.SHOTS_FIRED)) {
            return;
        }

        ProjectileSource projectileSource = event.getEntity().getShooter();

        // We only need player shooters
        if (!(projectileSource instanceof Player)) {
            return;
        }

        GamePlayer shooter = getGame().getPlayer((Player) projectileSource);

        if (shooter != null) {
            shooter.getStatistics().increase(Statistic.SHOTS_FIRED);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player)) {
            return;
        }

        GamePlayer player = getGame().getPlayer((Player) event.getEntity());
        if (player == null) {
            return;
        }

        // Get last damager
        Entity lastDamager = Events.getLastDamager(event.getEntity());

        boolean trackingDamageTaken = getGame().getStatistics().isTracking(Statistic.DAMAGE_TAKEN);

        // Increase damage taken statistic
        if (lastDamager != null && lastDamager instanceof Player) {
            GamePlayer damagerPlayer = getGame().getPlayer((Player) lastDamager);

            if (trackingDamageTaken) {
                player.getStatistics().increase(Statistic.DAMAGE_TAKEN, damagerPlayer, event.getDamage());
            }

            // Also track stats for last damager
            if (damagerPlayer != null && getGame().getStatistics().isTracking(Statistic.DAMAGE_DEALT)) {
                damagerPlayer.getStatistics().increase(Statistic.DAMAGE_DEALT, player, event.getDamage());
            }

        } else {
            if (trackingDamageTaken) {
                player.getStatistics().increase(Statistic.DAMAGE_TAKEN, event.getDamage());
            }

            if (lastDamager instanceof Arrow) {
                ProjectileSource projectileSource = ((Arrow) lastDamager).getShooter();

                if (!(projectileSource instanceof Player)) {
                    return;
                }

                GamePlayer shooter = getGame().getPlayer((Player) projectileSource);

                if (getGame().getStatistics().isTracking(Statistic.SHOTS_TAKEN)) {
                    player.getStatistics().increase(Statistic.SHOTS_TAKEN, shooter);
                }

                // Also track stats for shooter
                if (shooter != null && getGame().getStatistics().isTracking(Statistic.SHOTS_HIT)) {
                    shooter.getStatistics().increase(Statistic.SHOTS_HIT, player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityKill(EntityKillsEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        // Get killed entity
        GamePlayer killedPlayer = getGame().getPlayer((Player) event.getEntity());

        if (killedPlayer == null) {
            return;
        }

        boolean trackingDeaths = getGame().getStatistics().isTracking(Statistic.DEATHS);

        // Add statistics
        if (event.getKiller() instanceof Player) {
            GamePlayer killerPlayer = getGame().getPlayer((Player) event.getKiller());

            if (trackingDeaths) {
                killedPlayer.getStatistics().increase(Statistic.DEATHS, killerPlayer);
            }

            // Also track for killer player
            if (killerPlayer != null && getGame().getStatistics().isTracking(Statistic.KILLS)) {
                killerPlayer.getStatistics().increase(Statistic.KILLS, killedPlayer);
            }

        } else if (trackingDeaths) {
            killedPlayer.getStatistics().increase(Statistic.DEATHS);
        }
    }


    /**
     * Attempt some workaround for experience orbs:
     * prevent it getting near the player.
     *
     * Original code by Vanish:
     * https://github.com/asofold/SimplyVanish/blob/master/SimplyVanish/src/me/asofold/bpl/simplyvanish/listeners/TargetListener.java
     */
    private void repellExpOrb(Player player, ExperienceOrb orb) {
        Location pLoc = player.getLocation();
        Location oLoc = orb.getLocation();

        Vector dir = oLoc.toVector().subtract(pLoc.toVector());

        double dx = Math.abs(dir.getX());
        double dz = Math.abs(dir.getZ());

        if ( (dx == 0.0) && (dz == 0.0)) {
            // Special case probably never happens
            dir.setX(0.001);
        }

        if ((dx < 3.0d) && (dz < 3.0d)){
            Vector nDir = dir.normalize();
            Vector newV = nDir.clone().multiply(3.0d);

            newV.setY(0);
            orb.setVelocity(newV);

            if ((dx < 1.0d) && (dz < 1.0d)){
                // maybe oLoc
                orb.teleport(oLoc.clone().add(nDir.multiply(1.0d)), PlayerTeleportEvent.TeleportCause.PLUGIN);
            }

            if ((dx < 0.5d) && (dz < 0.5d)){
                orb.remove();
            }
        }
    }

}
