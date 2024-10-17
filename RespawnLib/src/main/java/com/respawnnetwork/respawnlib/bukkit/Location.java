package com.respawnnetwork.respawnlib.bukkit;

import gnu.trove.map.hash.THashMap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Represents a serializable location
 *
 * @author spaceemotion
 * @version 1.0
 */
@SerializableAs("Location")
public class Location extends org.bukkit.Location implements ConfigurationSerializable {
    private static final String WORLD = "world";
    private static final String X = "x";
    private static final String Y = "y";
    private static final String Z = "z";
    private static final String YAW = "yaw";
    private static final String PITCH = "pitch";


    /**
     * Copy constructor.
     *
     * @param location The original location
     */
    public Location(org.bukkit.Location location) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ());
    }

    /**
     * Constructs a new Location with the given coordinates
     *
     * @param world The world in which this location resides
     * @param x The x-coordinate of this new location
     * @param y The y-coordinate of this new location
     * @param z The z-coordinate of this new location
     */
    public Location(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    /**
     * Constructs a new Location with the given coordinates and direction
     *
     * @param world The world in which this location resides
     * @param x The x-coordinate of this new location
     * @param y The y-coordinate of this new location
     * @param z The z-coordinate of this new location
     * @param yaw The absolute rotation on the x-plane, in degrees
     * @param pitch The absolute rotation on the y-plane, in degrees
     */
    public Location(World world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, yaw, pitch);
    }

    public Location(Map<String, Object> map) {
        super(getWorld((String) map.get(WORLD)),
                getFloat(map, X, true), getFloat(map, Y, true), getFloat(map, Z, true),
                getFloat(map, YAW, false), getFloat(map, PITCH, false)
        );
    }

    public Location(World world, Map<String, Object> map) {
        super(world,
                getFloat(map, X, true), getFloat(map, Y, true), getFloat(map, Z, true),
                getFloat(map, YAW, false), getFloat(map, PITCH, false)
        );
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new THashMap<>();

        if (!getDefaultWorld().equals(getWorld())) {
            serialized.put(WORLD, getWorld().getName());
        }

        serialized.put(X, getX());
        serialized.put(Y, getY());
        serialized.put(Z, getZ());

        if (getYaw() != 0) {
            serialized.put(YAW, getYaw());
        }

        if (getPitch() != 0) {
            serialized.put(PITCH, getPitch());
        }

        return serialized;
    }

    public static List<Location> parseList(List<Map<?, ?>> list) {
        List<Location> locations = new LinkedList<>();

        if (list != null) {
            for (Map<?, ?> map : list) {
                Location location = new Location(parseMap(map));
                locations.add(location);
            }
        }

        return locations;
    }

    public static List<Location> parseList(World world, List<Map<?, ?>> list) {
        List<Location> locations = new LinkedList<>();

        if (list != null) {
            for (Map<?, ?> map : list) {
                Location location = new Location(world, parseMap(map));
                locations.add(location);
            }
        }

        return locations;
    }

    private static World getDefaultWorld() {
        return Bukkit.getServer().getWorlds().get(0);
    }

    private static World getWorld(String name) {
        World defaultWorld = getDefaultWorld();
        World world = name != null ? Bukkit.getServer().getWorld(name) : null;

        if (world == null || defaultWorld.equals(world)) {
            return defaultWorld;
        }

        return world;
    }

    private static float getFloat(Map<String, Object> map, String key, boolean ex) {
        Object obj = map.get(key);

        if (!(obj instanceof Number)) {
            if (ex) {
                throw new NoSuchElementException(map + " does not contain a number with key " + key);
            }

            return 0;
        }

        return ((Number) obj).floatValue();
    }

    private static Map<String, Object> parseMap(Map<?, ?> map) {
        return new MemoryConfiguration().createSection("tmp", (Map) map).getValues(false);
    }

}
