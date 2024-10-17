package com.respawnnetwork.respawnlib.gameapi.maps;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a class that loads game maps.
 * <p />
 * Maps are getting loaded from a plugin-wide template folder, replacing any existing world maps inside the server
 * folder.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class MapLoader {
    /** The logger we use for debugging purposes */
    @Getter
    private final Logger logger;

    /** The template folder we get the original maps from */
    @Getter
    private File templateFolder;


    /**
     * Creates a new map loader.
     *
     * @param logger The logger to use for debug and error messages
     * @param templateFolder The template folder we find the template worlds in
     */
    public MapLoader(Logger logger, File templateFolder) {
        this.logger = logger;
        this.templateFolder = templateFolder;

        loadTemplateFolder();
    }

    /**
     * Loads the world of a game map from the template folder.
     * <p />
     * This might return null if the specified map is either null or the world for the map does not exist.
     *
     * @param map The name of the map
     * @return The loaded world or null
     */
    @Nullable
    public World loadMap(GameMap map) {
        File mapFolder = new File(templateFolder, map.getName());

        // Check if the folder exists, if not return null
        // This also prevents having newly generated maps
        if (!mapFolder.exists()) {
            logger.warning("No template world folder with name \"" + map.getName() + "\" found in " + templateFolder);
            return null;
        }

        // Get world folder
        File worldFolder = new File(Bukkit.getServer().getWorldContainer(), map.getName());

        // If the folder already exist, check if it's loaded and if not, clear it
        if (worldFolder.exists()) {
            if (isLoaded(map.getName())) {
                logger.warning("Could not load new world, please unload the old map first!");
                return null;
            }

            // Clear "old" world
            logger.info("Clearing already existing world folder");

            try {
                FileUtils.deleteDirectory(worldFolder);

            } catch (IOException io) {
                logger.log(Level.WARNING, "Could not clear old world folder", io);
                return null;
            }
        }

        logger.info("Cloning template world to server folder...");

        // Copy folder
        try {
            FileUtils.copyDirectory(mapFolder, worldFolder, true);

        } catch (IOException io2) {
            logger.log(Level.WARNING, "Could not copy folder contents", io2);
            return null;
        }

        // Return the created map
        return Bukkit.createWorld(new WorldCreator(map.getName()));
    }

    /**
     * Checks if a given world is loaded by the server.
     *
     * @param name The name of the world
     * @return True if it is, false if not
     */
    public boolean isLoaded(String name) {
        for (World world : Bukkit.getServer().getWorlds()) {
            if (world.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Loads the template folder.
     * If it doesn't exist yet, this will create an empty folder instead.
     */
    private void loadTemplateFolder() {
        if (templateFolder == null) {
            logger.warning("No template folder given, using default map location!");
            templateFolder = new File(Bukkit.getServer().getWorldContainer().getParentFile(), "map_templates");
        }

        logger.info("World template folder is located at: " + templateFolder);

        if (!templateFolder.exists()) {
            logger.info("Template folder does not exist, creating an empty new folder...");

            if (!templateFolder.mkdirs()) {
                logger.warning("Could not create template folder, please check permissions!");
            }
        }
    }

}
