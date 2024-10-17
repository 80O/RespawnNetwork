package com.respawnnetwork.respawnlib.gameapi.modules;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a module that protects all blocks in a map.
 * <p />
 * Block materials can either be whitelisted or blacklisted.
 *
 * @author spaceemotion
 * @version 1.0.1
 */
public class BlockProtectionModule<G extends Game> extends GameModule<G> implements Listener {
    @Getter
    private final List<Material> materials;

    @Getter
    @Setter
    private boolean whitelist;


    public BlockProtectionModule(G game) {
        super(game);

        this.materials = new LinkedList<>();
    }

    @Override
    protected boolean onEnable() {
        whitelist = getConfig().getBoolean("whitelist", true);

        for (String name : getConfig().getStringList("materials")) {
            Material material = Material.matchMaterial(name);

            if (material == null) {
                getLogger().info("Unknown material to protect: " + name);
                continue;
            }

            getLogger().info((whitelist ? "Not protecting '" : "Protecting '") + material + '\'');
            materials.add(material);
        }

        return true;
    }

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) {
        handleBlockEvent(event, event.getPlayer(), event.getBlock());
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {
        handleBlockEvent(event, event.getPlayer(), event.getBlock());
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Iterator<Block> blocks = event.blockList().iterator();

        while (blocks.hasNext()) {
            Block block = blocks.next();

            if (!isAllowed(block)) {
                blocks.remove();
            }
        }
    }

    /**
     * Indicates if a block is allowed to be broken or placed.
     *
     * @param block The block in context
     * @return True if breaking and placing is allowed, false it not
     */
    public boolean isAllowed(Block block) {
        boolean contains = materials.contains(block.getType());

        // Prevent the block from breaking
        return !(!whitelist && contains || whitelist && !contains);
    }

    @Override
    public String getDisplayName() {
        return "Block Protection";
    }

    @Override
    public String getName() {
        return "block-protection";
    }

    private void handleBlockEvent(Cancellable event, Player player, Block block) {
        if (event.isCancelled() || player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Prevent the block from breaking (includes spectators)
        if (getGame().isSpectator(player) || !isAllowed(block)) {
            event.setCancelled(true);

            // *sigh* bukkit, oh bukkit. why don't you update that crap yourself ...
            player.updateInventory();
        }
    }

}
