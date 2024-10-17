package com.respawnnetwork.respawnlib.plugin;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author spaceemotion
 * @version 1.0.1
 */
public class PluginConfig extends YamlConfiguration {
    private final Logger log;
    private final File file;


    public PluginConfig(Plugin plugin, String name) {
        if (name == null || name.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        file = new File(plugin.getDataFolder(), name.replace('\\', '/'));
        log = plugin.getPluginLog();

        try {
            // Only save if it doesn't exist
            if (!file.exists()) {
                plugin.saveResource(name, false);
            }

        } catch (IllegalArgumentException ex) {
            log.log(Level.WARNING, "Config does not exist: " + name, ex);
        }

        reload();
    }

    /**
     * Reloads the configuration file
     */
    public final void reload() {
        try {
            load(file);

        } catch (IOException | InvalidConfigurationException e) {
            log.log(Level.WARNING, "Could not reload config file for '" + file, e);
        }
    }

    /**
     * Saves the configuration file.
     */
    public void save() {
        try {
            save(file);

        } catch (IOException ex) {
            log.log(Level.WARNING, "Could not save config file " + file.getName(), ex);
        }
    }

    /**
     * Merges two configuration sections together.
     * <p />
     * Values from the second config override the ones from the first one.
     *
     * @param first The first config
     * @param second The second config.
     * @return The merged configuration
     * @since 1.0.1
     */
    public static ConfigurationSection mergeConfigurations(ConfigurationSection first, ConfigurationSection second) {
        // If the first is null, jut return the second one
        if (first == null) {
            return second;
        }

        // If the second is null, jut return the first one
        if (second == null) {
            return first;
        }

        // Otherwise just merge them
        Configuration merged = new MemoryConfiguration();
        copyValues(first, merged);
        copyValues(second, merged);

        return merged;
    }

    /**
     * Copies values from one config to another.
     *
     * @param from The config to copy from
     * @param to The config to paste into
     * @since 1.0.1
     */
    public static void copyValues(ConfigurationSection from, ConfigurationSection to) {
        if (from == null || to == null) {
            return;
        }

        for (Map.Entry<String, Object> entry : from.getValues(true).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection) {
                continue;
            }

            to.set(entry.getKey(), entry.getValue());
        }
    }

}
