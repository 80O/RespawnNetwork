package com.respawnnetwork.respawnlib.gameapi.modules.region;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.events.EndGameEvent;
import gnu.trove.map.hash.THashMap;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.khelekore.prtree.MBRConverter;
import org.khelekore.prtree.PRTree;
import org.khelekore.prtree.SimpleMBR;

import java.util.*;
import java.util.logging.Level;

/**
 * Represents a module that handles regions.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class RegionModule<G extends Game> extends GameModule<G> implements Listener {
    /** The default tree branch factor */
    public static final int DEFAULT_BRANCH_FACTOR = 30;

    /** The mbr converter for regions */
    private static final MBRConverter<Region> CONVERTER = new RegionMBRConverter();

    /** The region map, holding all key -> region relations */
    private final Map<String, Region> regions;

    /** Holds the last seen location of a player, using the UUID */
    private final Map<UUID, Location> lastSeenLocation;

    /** The branch factor to use */
    private final int factor;

    /** The PR Tree instance that handles all regions */
    @Getter
    private PRTree<Region> tree;


    protected RegionModule(G game) {
        this(game, DEFAULT_BRANCH_FACTOR);
    }

    protected RegionModule(G game, int factor) {
        super(game);

        this.factor = factor;
        this.regions = new THashMap<>();
        this.lastSeenLocation = new THashMap<>();
    }

    /**
     * Creates a new region.
     *
     * @param name The region name
     * @param min The minimum location
     * @param max The maximum location
     * @return The created region instance
     */
    protected Region createRegion(String name, World world, Vector3i min, Vector3i max) {
        return new Region.Default(name, world, min, max);
    }

    @Override
    protected boolean onEnable() {
        ConfigurationSection list = getConfig().getConfigurationSection("list");

        if (list == null) {
            getLogger().info("No regions specified");
            return true;
        }

        for (String name : list.getKeys(false)) {
            if (!list.isConfigurationSection(name)) {
                getLogger().warning(name + " is an invalid region");
                continue;
            }

            ConfigurationSection regionSection = list.getConfigurationSection(name);

            Vector3i min;
            Vector3i max;

            try {
                min = parseLocation(regionSection.getString("min", ""));
                max = parseLocation(regionSection.getString("max", ""));

            } catch (IllegalArgumentException ex) {
                getLogger().log(Level.WARNING, "Error reading region: " + name, ex);
                continue;
            }

            World world = null;
            String worldName = list.getString("world");

            if (worldName != null) {
                world = getGame().getPlugin().getServer().getWorld(worldName);
            }

            if (world == null) {
                world = getGame().getMap().getWorld();
            }

            Region region = createRegion(name, world, min, max);
            regions.put(region.getName(), region);

            // Add optional "meta data"
            if (regionSection.isConfigurationSection("options")) {
                region.getOptions().putAll(regionSection.getConfigurationSection("options").getValues(false));
            }
        }

        // Load all regions into the tree
        reloadTree();

        return true;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        handleMovement(null, event.getRespawnLocation(), event.getPlayer());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) {
            return;
        }

        handleMovement(event.getFrom(), event.getTo(), event.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (getGame().hasEnded()) {
            return;
        }

        handleMovement(event.getFrom(), event.getTo(), event.getPlayer());
    }

    @EventHandler
    public void onEndGame(EndGameEvent event) {
        // Clear all "players in region"
        for (Region region : getRegions()) {
            region.getPlayersInRegion().clear();
        }

        // Clear last seen locations
        lastSeenLocation.clear();
    }

    /**
     * Adds a region to this module.
     *
     * @param region The region to add
     */
    public void addRegion(Region region) {
        regions.put(region.getName(), region);

        reloadTree();
    }

    /**
     * Gets a region by his name.
     *
     * @param name The region name
     * @return The found region or null if the region is not present
     * @since 1.0.1
     */
    public Region getRegion(String name) {
        return regions.get(name);
    }

    /**
     * Returns a collection of all loaded regions.
     *
     * @return The loaded regions
     */
    public Collection<Region> getRegions() {
        return regions.values();
    }

    private void reloadTree() {
        tree = new PRTree<>(CONVERTER, factor);

        tree.load(regions.values());
    }

    @Override
    public String getDisplayName() {
        return "Regions";
    }

    @Override
    public String getName() {
        return "regions";
    }

    private void handleMovement(Location from, Location to, Player player) {
        if (!getGame().isRunning()) {
            return;
        }

        if (from == null) {
            from = lastSeenLocation.get(player.getUniqueId());
        }

        // Just ignore the event when we didn't move from the block
        if (!changedBlock(from, to) || getTree() == null) {
            return;
        }

        // Save last seen location
        lastSeenLocation.put(player.getUniqueId(), to);

        // Lookup cuboids at the old position
        List<Region> old = new ArrayList<>();
        getTree().find(new SimpleMBR(
                from.getX(), from.getX(), from.getY(), from.getY(), from.getZ(), from.getZ()
        ), old);

        // Lookup cuboids at the new position
        Iterator<Region> iterator = getTree().find(new SimpleMBR(
                to.getX(), to.getX(), to.getY(), to.getY(), to.getZ(), to.getZ()
        )).iterator();

        // We did not trigger any changes, so just cancel here
        if (old.isEmpty() && !iterator.hasNext()) {
            return;
        }

        // Get game player
        GamePlayer gamePlayer = getGame().getPlayer(player);
        if (gamePlayer == null) {
            return;
        }

        while (iterator.hasNext()) {
            Region region = iterator.next();
            int otherIdx = old.indexOf(region);

            if (otherIdx == -1) {
                // He just entered the region
                region.getPlayersInRegion().add(gamePlayer);
                region.onPlayerEnterRegion(gamePlayer);

            } else {
                // The player was in that region before so we can just
                // remove it from the "old" list
                old.remove(otherIdx);
            }
        }

        // We now only have a list containing all old cuboids
        // That the player has exited
        for (Region region : old) {
            region.getPlayersInRegion().remove(gamePlayer);
            region.onPlayerLeaveRegion(gamePlayer);
        }
    }

    private boolean changedBlock(Location a, Location b) {
        return !(a == null | b == null) &&
                (a.getBlockX() != b.getBlockX() || a.getBlockY() != b.getBlockY() || a.getBlockZ() != b.getBlockZ());
    }

    private Vector3i parseLocation(String location) throws IllegalArgumentException {
        String[] split = location.split(",", 3);

        if (split.length != 3) {
            throw new IllegalArgumentException("Location does not have 3 positions (X, Y and Z)");
        }

        return new Vector3i(
                Integer.parseInt(split[0].trim()),
                Integer.parseInt(split[1].trim()),
                Integer.parseInt(split[2].trim())
        );
    }

}
