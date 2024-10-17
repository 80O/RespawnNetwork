package net.respawn.slicegames.modules;

import com.google.common.collect.ImmutableList;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import net.respawn.slicegames.SliceGames;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Represents a very basic block protection module.
 *
 * @author spaceemotion
 * @author TomShar
 * @version 1.0
 */
public class SGProtectionModule extends GameModule<SliceGames> implements Listener {
    public static final List<Material> REDSTONE_MATERIALS = ImmutableList.<Material>builder()
        .add(Material.LEVER)
        .add(Material.WOOD_BUTTON)
        .add(Material.STONE_BUTTON)
        .add(Material.GOLD_PLATE)
        .add(Material.IRON_PLATE)
        .add(Material.WOOD_PLATE)
        .add(Material.STONE_PLATE)
        .add(Material.WOOD_DOOR)
        .add(Material.WOODEN_DOOR)
        .add(Material.TRAP_DOOR)
        .add(Material.IRON_DOOR)
        .add(Material.IRON_DOOR_BLOCK)
        .add(Material.ENDER_CHEST).build();


    public SGProtectionModule(SliceGames game) {
        super(game);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void blockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();

        // Allow all things in creative
        if (GameMode.CREATIVE.equals(player.getGameMode())) {
            return;
        }

        Block block = e.getBlock();
        ItemStack item = player.getItemInHand();

        // Cancel redstone stuff
        if(REDSTONE_MATERIALS.contains(e.getBlockAgainst().getType())) {
            e.setCancelled(true);
            return;
        }

        // Prevent fire and cobweb placement
        if(block.getType() == Material.WEB || block.getType() == Material.FIRE) {
            return;
        }

        // Always cancel the event
        e.setCancelled(true);

        // Activate TNT instantly
        if(block.getType() == Material.TNT && item.getType() == Material.TNT) {
            TNTPrimed primed = (TNTPrimed) player.getWorld().spawnEntity(block.getLocation(), EntityType.PRIMED_TNT);
            primed.setFuseTicks(40);

            // Remove TNT item from inventory
            player.getInventory().removeItem(new ItemStack(Material.TNT, 1));
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent e) {
        if (e.getBlock().getType().equals(Material.WEB)) {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void blockBurn(BlockBurnEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void blockDamage(BlockDamageEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void blockExplosion(EntityExplodeEvent e) {
        e.setCancelled(true);
    }

//    @EventHandler
//    public void blockSpread(BlockSpreadEvent e) {
//        e.setCancelled(true);
//    }

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event){
        event.setCancelled(true);
    }

   @EventHandler
    public void playerBlockDamage(EntityDamageByBlockEvent e) {
       e.setCancelled(true);
    }

    @EventHandler
    public void onStructureGrow(StructureGrowEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onCreatureSpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Monster) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onRedstoneMaterialInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        //Check if GamePlayer is spectator
        if (!getGame().isSpectator(event.getPlayer())) {
            return;
        }

        //Cancel all redstone stuff
        if (REDSTONE_MATERIALS.contains(event.getClickedBlock().getType())) {
            event.setCancelled(true);
        }
    }

    @Override
    public String getDisplayName() {
        return "SliceGames Protection";
    }

    @Override
    public String getName() {
        return "sg-protection";
    }

}
