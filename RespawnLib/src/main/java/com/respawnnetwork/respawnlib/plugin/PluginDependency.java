package com.respawnnetwork.respawnlib.plugin;

import gnu.trove.map.hash.THashMap;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class that helps with plugin dependencies.
 *
 * @author spaceemotion
 * @version 1.0
 */
public class PluginDependency {
    private static final Map<String, Boolean> INSTALL_CACHE = new THashMap<>();
    private Logger log;


    /**
     * Creates a new plugin dependency helper instance.
     *
     * @param log The log to use for error messages
     */
    public PluginDependency(Logger log) {
        this.log = log;
    }

    /**
     * Determines whether or not a plugin is stalled.
     *
     * @param name The name of the plugin to check
     * @return True if it is installed, false if not
     */
    public boolean isInstalled(String name) {
        Boolean status = INSTALL_CACHE.get(name);

        if (status == null) {
            status = Bukkit.getServer().getPluginManager().getPlugin( name ) != null;

            INSTALL_CACHE.put(name, status);
        }

        return status;
    }

    /**
     * Tries to get an instance of the specified class of the given plugin.
     *
     * @param name The name of the plugin to check
     * @param c The class of the dependency
     * @param <T> The class type
     * @return The instance, or null if it could not be found
     */
    public <T> T getInstance(String name, Class<T> c) {
        if (isInstalled(name)) {
            try {
                RegisteredServiceProvider<T> rsp = Bukkit.getServer().getServicesManager().getRegistration(c);

                if (rsp == null || rsp.getProvider() == null) {
                    throw new NullPointerException("Provider validates to null (" + c.getName() + ")!");
                }

                return rsp.getProvider();

            } catch (NullPointerException ex) {
                log.log(Level.WARNING, "Error getting plugin dependency", ex);
            }
        } else {
            log.warning("No plugin with name " + name + " found!");
        }

        return null;
    }

}
