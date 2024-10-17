package com.respawnnetwork.respawnlib.bukkit.events;

import lombok.Delegate;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Represents an event that gets triggered whenever an entity is about to be
 * killed by an entity.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class EntityKillsEntityEvent extends CancellableEvent {
    private static final HandlerList handlers = new HandlerList();

    /** The parent entity death event */
    @Delegate(excludes = {Event.class})
    private final EntityDeathEvent parent;

    /** The entity that killed the entity */
    @Getter
    private final Entity killer;

    /** True if the entity was a projectile */
    @Getter
    private final boolean projectileKill;


    public EntityKillsEntityEvent(boolean async, EntityDeathEvent event) {
        super(async);

        this.parent = event;
        this.killer = Events.getKillerFromDeathEvent(parent);
        this.projectileKill = Events.wasShotByProjectile(parent);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
