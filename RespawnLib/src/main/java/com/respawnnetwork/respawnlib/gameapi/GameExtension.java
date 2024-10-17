package com.respawnnetwork.respawnlib.gameapi;

import com.respawnnetwork.respawnlib.lang.Displayable;
import com.respawnnetwork.respawnlib.lang.Nameable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Represents a base class for stuff that is extensible.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public abstract class GameExtension<G extends Game> implements Displayable, Nameable {
    /** The game we're extending */
    private final G game;

    /** The module specific configuration section */
    @Getter
    private final ConfigurationSection config;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private boolean isLoaded;

    /** The private module logger */
    private Logger extensionLogger;

    /** A list of listeners we registered */
    private List<Listener> listeners;


    /**
     * Creates a new extensible base for the game engine.
     *
     * @param game The game we extend
     * @param type The type of extension
     * @param logFormat The log format (like "[%s]", where %s contains the name of the extension)
     */
    GameExtension(G game, String type, String logFormat) {
        this.game = game;
        this.listeners = new LinkedList<>();

        // Create logger
        String format = String.format(logFormat, getDisplayName());
        this.extensionLogger = new ExtensionLogger(getClass(), format, game.getLogger());

        // Get config
        if (game.getConfig().isSet(getName())) {
            if (!game.getConfig().isConfigurationSection(getName())) {
                game.getLogger().warning(
                        "Invalid config for " + type + ' ' + getDisplayName() + ": '" + getName() + "' not a section"
                );

            } else {
                this.config = game.getConfig().getConfigurationSection(getName());
                return;
            }
        }

        // Create empty section
        this.config = game.getConfig().createSection(getName());
    }

    /**
     * Registers an event listener.
     * <p />
     * Every listener that has been registered through this method should only be unregistered
     * using {@link #unregisterListener(org.bukkit.event.Listener)}!
     *
     * @param listener The listener to register
     */
    public final <L extends Listener> L registerListener(L listener) {
        listeners.add(listener);
        return getGame().getPlugin().registerListener(listener);
    }

    /**
     * Unregisters an event listener
     *
     * @param listener The listener to unregister
     */
    public final void unregisterListener(Listener listener) {
        HandlerList.unregisterAll(listener);
        listeners.remove(listener);
    }

    /**
     * Unregisters all registered listeners.
     */
    public final void unregisterListeners() {
        // We make a linked list copy of the "original" listeners so we have a list
        // of the original listeners, since they're getting removed instantly when we call
        // the unregisterListener() method
        for (Listener listener : new LinkedList<>(listeners)) {
            unregisterListener(listener);
        }
    }

    /**
     * Calls a bukkit event.
     *
     * @param event The event to call
     */
    public final void callEvent(Event event) {
        getGame().callEvent(event);
    }

    /**
     * Returns the game instance.
     *
     * @return The game
     */
    public final G getGame() {
        return game;
    }

    /**
     * Returns the module logger associated with this game's logger. The returned logger automatically
     * prefixes all log messages with the module's display name.
     *
     * @return Logger associated with this module
     */
    public final Logger getLogger() {
        return extensionLogger;
    }


    private final static class ExtensionLogger extends Logger {
        private String prefix = "";

        /**
         * Creates a new logger that uses the name from a module.
         *
         * @param clazz A reference to the module class
         * @param format The prefix format
         * @param parent The parent logger to use
         */
        public ExtensionLogger(Class<?> clazz, String format, Logger parent) {
            super(clazz.getCanonicalName(), null);

            if (parent instanceof Game.GameLogger) {
                this.prefix += ((Game.GameLogger) parent).prefix;
            }

            this.prefix += format + ' ';

            setParent(parent);
            setLevel(Level.ALL);
        }

        @Override
        public void log(LogRecord logRecord) {
            logRecord.setMessage(prefix + logRecord.getMessage());

            super.log(logRecord);
        }
    }

}
