package com.respawnnetwork.respawnlib.plugin;

import com.respawnnetwork.respawnlib.bukkit.Location;
import com.respawnnetwork.respawnlib.bukkit.events.RLibEventListener;
import com.respawnnetwork.respawnlib.network.bungee.Bungee;
import com.respawnnetwork.respawnlib.network.command.CommandManager;
import com.respawnnetwork.respawnlib.network.database.Database;
import com.respawnnetwork.respawnlib.network.menu.InventoryMenuEventListener;
import com.respawnnetwork.respawnlib.network.messages.ChatMessageListener;
import com.respawnnetwork.respawnlib.network.messages.Message;
import com.respawnnetwork.respawnlib.network.messages.VaultChatListener;
import gnu.trove.map.hash.THashMap;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Represents a basic plugin that handles the registering of commands and
 * other basic tasks.
 *
 * @author spaceemotion
 * @version 1.0.1
 */
public class Plugin extends JavaPlugin {

    /** The default name of the messages configs folder */
    public static final String MESSAGE_CONFIG_FOLDER = "messages";

    /** A cache for extra plugin configs */
    private final Map<String, PluginConfig> configCache = new THashMap<>();

    /** A list of all log files */
    private final Map<String, PluginLog> logFiles = new THashMap<>();

    /** A list of all message configs we loaded */
    private final List<String> messageConfigs = new LinkedList<>();

    /** Custom plugin log file */
    private PluginLog log;

    /** The plugin dependency instance */
    private PluginDependency pluginDependency;

    /** The per-plugin command manager */
    private CommandManager commandManager;

    /** The database manager */
    private Database databaseManager;

    @Setter
    private boolean autoSavesConfigs;


    @Override
    public void onLoad() {
        // Register custom serializations
        ConfigurationSerialization.registerClass(Location.class);

        // Copy config file if available
        saveDefaultConfig();

        this.pluginDependency = new PluginDependency(getPluginLog());
        this.commandManager = new CommandManager(getPluginLog());

        // Initialize database
        ConfigurationSection mysqlCfg = getConfig().getConfigurationSection("mysql");
        if (mysqlCfg != null) {
            this.databaseManager = new Database(
                    getLogger(),
                    mysqlCfg.getString("host"),
                    mysqlCfg.getString("port"),
                    mysqlCfg.getString("database"),
                    mysqlCfg.getString("username"),
                    mysqlCfg.getString("password")
            );
        }

        // Requested by parker 27.04.2014 03:12 GMT - Should be configurable
        this.autoSavesConfigs = getConfig().getBoolean("autoSaveConfigs", false);
    }

    @Override
    public void onEnable() {
        // Register custom, needed listeners
        registerListener(new RLibEventListener());

        // Register custom chat stuff
        if (usesCustomChatMessages()) {
            if (getPluginDependency() != null && getPluginDependency().isInstalled("Vault")) {
                // Register the one that uses vault
                registerListener(new VaultChatListener(this));

            } else {
                // Register "fallback" listener
                registerListener(new ChatMessageListener());
            }
        }

        // Register the plugin channel for bungee.
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, Bungee.CHANNEL_NAME);
//        Bukkit.getMessenger().registerIncomingPluginChannel(this, Bungee.CHANNEL_NAME, new BungeeChannelListener(getPluginLog()));

        // Register the Inventory Menu API if needed
        if (usesInventoryMenuAPI()) {
            registerListener(new InventoryMenuEventListener());
        }
    }

    @Override
    public void onDisable() {
        // Save all configs
        if (autoSavesConfigs()) {
            saveConfig();

            for (PluginConfig config : configCache.values()) {
                config.save();
            }
        }

        // Close all log files
        for (PluginLog log : logFiles.values()) {
            log.close();
        }

        // Close database if connected
        Database database = getDatabaseManager();

        if (database != null) {
            database.close();
        }
    }

    /**
     * Represents a method for plugin reloads.
     * This is not a bukkit function, so developers should call this method either in
     * <ol>
     *     <li>The {@link #onEnable()} method</li>
     *     <li>Commands that reload the plugin (like <code>/xyz reload</code>)</li>
     * </ol>
     */
    public void onReload() {
        // Reload configurations
        reloadConfig();

        for (PluginConfig config : configCache.values()) {
            config.reload();
        }

        // Clear message defaults, and reload values from the configs
        Message.clearDefaults();

        for (String messageConfig : messageConfigs) {
            loadMessages(messageConfig);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        return commandManager.onCommand(sender, command, label, args);
    }

    /**
     * Determines whether or not this plugin uses the inventory menu API.
     *
     * @return True if it uses the API, false if not
     */
    public boolean usesInventoryMenuAPI() {
        return false;
    }

    /**
     * Indicates whether or not this plugin uses the integrated chat listener.
     *
     * @return True if it uses the custom chat messages, false if not
     * @since 1.0.1
     */
    public boolean usesCustomChatMessages() {
        return true;
    }

    /**
     * Determines whether or not this plugin automatically saves the configurations.
     *
     * @return True if the plugin automatically saves configs on disabling
     */
    public boolean autoSavesConfigs() {
        return autoSavesConfigs;
    }

    /**
     * Returns the plugin dependency manager for this plugin.
     *
     * @return The dependency manager
     */
    @Nullable
    public PluginDependency getPluginDependency() {
        return pluginDependency;
    }

    /**
     * Returns this plugins command manager.
     *
     * @return The command manager for this plugin
     */
    @Nullable
    public CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * Returns the database for this plugin.
     *
     * @return The database manager
     */
    @Nullable
    public Database getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Gets a plugin-specific logger.
     *
     * @return The custom logger
     */
    @NotNull
    public Logger getPluginLog() {
        if(log == null) {
            log = new PluginLog(this);
            logFiles.put(log.getName(), log);
        }

        return log;
    }

    /**
     * Gets a custom, plugin-specific logger.
     *
     * @param name The internal name of the logger
     * @param folder The log folder name
     * @return The custom logger
     * @since 1.0.1
     */
    @NotNull
    public Logger getPluginLog(String name, String folder) {
        PluginLog log = logFiles.get(name);

        if (log != null) {
            return log;
        }

        log = new PluginLog(this, name, folder);
        logFiles.put(name, log);

        return log;
    }

    /**
     * Gets another plugin configuration by its name.
     * If it does not exist, this will create a new one.
     *
     * @param name The name of the configuration
     * @return The plugin config instance
     */
    @NotNull
    public PluginConfig getConfig(String name) {
        PluginConfig config = configCache.get(name);

        if (config == null) {
            if (name == null) {
                name = "null";
            }

            config = new PluginConfig(this, name);

            configCache.put(name, config);
        }

        return config;
    }

    /**
     * Loads all the messages in a configuration file.
     *
     * @param configName The config file name
     */
    public void loadMessages(String configName) {
        if (configName == null) {
            return;
        }

        // Add to config list
        messageConfigs.add(configName);

        // Get config file
        FileConfiguration config = getConfig(MESSAGE_CONFIG_FOLDER + File.separatorChar + configName);

        // Load defaults
        for (Map.Entry<String, Object> entry : config.getValues(true).entrySet()) {
            if (!(entry.getValue() instanceof String)) {
                continue;
            }

            Message.addDefault(entry.getKey(), (String) entry.getValue());
        }

        getPluginLog().info("Loaded default messages from file: " + configName);
    }

    /**
     * Registers an event listener
     *
     * @param listener The listener to register
     * @param <L> The listener type
     * @return The given listener, for method chaining
     * @since 1.0.1
     */
    public <L extends Listener> L registerListener(L listener) {
        getServer().getPluginManager().registerEvents(listener, this);

        return listener;
    }

}
