package com.respawnnetwork.respawnlib.bukkit.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

/**
 * Represents a cancellable event.
 * <p />
 * Use this as a base for custom events that can be cancelled.
 *
 * @author spaceemotion
 * @version 1.0
 */
public abstract class CancellableEvent extends Event implements Cancellable {
    private boolean cancelled = false;


    protected CancellableEvent() {
        this(false);
    }

    protected CancellableEvent(boolean isAsync) {
        super(isAsync);
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
