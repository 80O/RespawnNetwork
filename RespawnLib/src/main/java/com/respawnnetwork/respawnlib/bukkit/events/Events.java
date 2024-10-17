package com.respawnnetwork.respawnlib.bukkit.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Represents a helper utility for bukkit events.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public final class Events {

    private Events() {
        // Private constructor...
    }

    /**
     * Returns the entity that damaged the entity the last time.
     *
     * @param entity The entity to get the damager for
     * @return The entity that his the entity lately
     */
    public static Entity getLastDamager(Entity entity) {
        EntityDamageEvent damageEvent = entity.getLastDamageCause();

        if (damageEvent instanceof EntityDamageByEntityEvent) {
            return ((EntityDamageByEntityEvent) entity.getLastDamageCause()).getDamager();
        }

        return null;
    }

    /**
     * Returns true if the entity died in this event was shot by a projectile
     *
     * @param event The {@link EntityDeathEvent} to check
     * @return True if the entity was shot by a projectile, otherwise false
     */
    public static boolean wasShotByProjectile(EntityDeathEvent event) {
        if (event == null || !(event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
            return false;
        }

        Entity damager = getLastDamager(event.getEntity());

        if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;

            if (projectile.getShooter() instanceof Player) {
               return true;
            }
        }

        return false;
    }

    /**
     * Gets the killer from a death event.
     * <p />
     * If the killer is a tamable animal, this will return its owner.
     * If the "killer" is an arrow, this will return its shooter.
     *
     * @param event The {@link EntityDeathEvent} to check
     * @return The killer, or null if no killer could be found
     */
    public static Entity getKillerFromDeathEvent(EntityDeathEvent event) {
        if(event == null || !(event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
            return null;
        }

        Entity attacker = getLastDamager(event.getEntity());

        // Use the owner of the tamed animal, if present
        if(attacker instanceof Tameable) {
            Tameable tameable = (Tameable) attacker;

            if (tameable.isTamed() && tameable.getOwner() instanceof Player) {
                return (Player) tameable.getOwner();
            }
        }

        // Use the shooter from the arrow
        if(attacker instanceof Projectile) {
            Projectile projectile = (Projectile) attacker;

            if(projectile.getShooter() instanceof Player) {
                return (Player) projectile.getShooter();
            }
        }

        // This covers the normal player as well as untamed animals
        return attacker;
    }

}
