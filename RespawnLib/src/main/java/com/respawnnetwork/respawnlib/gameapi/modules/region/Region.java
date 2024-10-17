package com.respawnnetwork.respawnlib.gameapi.modules.region;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a cuboid as defined by two locations.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
@Getter
public abstract class Region {
    private final String name;
    private final World world;
    private final Vector3i minimum;
    private final Vector3i maximum;
    private final Vector3i size;
    private final TMap<String, Object> options;
    private final List<GamePlayer> playersInRegion;


    public Region(String name, World world, Vector3i first, Vector3i second) {
        this.name = name;
        this.world = world;

        this.minimum = new Vector3i();
        this.maximum = new Vector3i();
        this.size = new Vector3i();

        this.options = new THashMap<>();
        this.playersInRegion = new LinkedList<>();

        calcVectors(first, second);
    }

    /**
     *
     */
    protected abstract void onPlayerEnterRegion(GamePlayer player);

    /**
     *
     */
    protected abstract void onPlayerLeaveRegion(GamePlayer player);

    /**
     * Clears specific blocks from the region.
     *
     * @param materials The materials to clear
     */
    public void clearBlocks(Material... materials) {
        clearBlocks(Arrays.asList(materials));
    }

    /**
     * Clears specific blocks from the region.
     *
     * @param materials The collection of materials to clear
     */
    public void clearBlocks(Collection<Material> materials) {
        for (int x = minimum.x; x <= maximum.x; ++x) {
            for (int y = minimum.y; y <= maximum.y; ++y) {
                for (int z = minimum.z; z <= maximum.z; ++z) {
                    Block block = world.getBlockAt(x, y, z);

                    // Ignore the block if it's nil, this shouldn't really happen, but eh...
                    if (block == null) {
                        continue;
                    }

                    // Skip if we don't have the block in the "clear list"
                    if (!materials.contains(block.getType())) {
                        continue;
                    }

                    // Set the block to air
                    block.setType(Material.AIR);
                }
            }
        }
    }

    /**
     * Tests if the specified block is located in this region.
     *
     * @param block The block to test for
     * @return True if the block is located in this region, false if not
     * @since 1.0.1
     */
    public boolean isInRegion(Block block) {
        Location l = block.getLocation();

        return (l.getBlockX() >= minimum.x && l.getBlockY() >= minimum.y && l.getBlockZ() >= minimum.z) &&
                (l.getBlockX() < maximum.x && l.getBlockY() < maximum.y && l.getBlockZ() < maximum.z);
    }

    private void calcVectors(Vector3i... vectors) {
        if (vectors.length < 2) {
            return;
        }

        minimum.set(vectors[0].x, vectors[0].y, vectors[0].z);
        maximum.set(minimum);

        for (Vector3i v : vectors) {
            // Check minimums
            if (v.x < minimum.x) {
                minimum.x = v.x;
            }

            if (v.y < minimum.y) {
                minimum.y = v.y;
            }
            
            if (v.z < minimum.z) {
                minimum.z = v.z;
            }

            // Check maximums
            if (v.x > maximum.x) {
                maximum.x = v.x;
            }

            if (v.y > maximum.y) {
                maximum.y = v.y;
            }

            if (v.z > maximum.z) {
                maximum.z = v.z;
            }
        }

        // Calc total size
        this.size.set(
                Math.abs(minimum.x - maximum.x),
                Math.abs(minimum.y - maximum.y),
                Math.abs(minimum.z - maximum.z)
        );
    }


    /**
     * A default implementation of a region
     *
     * @author spaceemotion
     * @version 1.0
     * @since 1.0.1
     */
    public static class Default extends Region {

        public Default(String name, World world, Vector3i first, Vector3i second) {
            super(name, world, first, second);
        }

        @Override
        protected void onPlayerEnterRegion(GamePlayer player) {}

        @Override
        protected void onPlayerLeaveRegion(GamePlayer player) {}

    }

}
