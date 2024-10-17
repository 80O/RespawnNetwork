package net.respawn.goldrush.modules;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.events.PlayerJoinGameEvent;
import com.respawnnetwork.respawnlib.gameapi.modules.mechanics.TeamMechanicsModule;
import com.respawnnetwork.respawnlib.gameapi.modules.team.Team;
import com.respawnnetwork.respawnlib.gameapi.states.InGameState;
import net.respawn.goldrush.GRPlayer;
import net.respawn.goldrush.GoldRush;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;


public class GRMechanicsModule extends TeamMechanicsModule<GoldRush> {

    public GRMechanicsModule(GoldRush game) {
        super(game);
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerClicksArmor(InventoryClickEvent event) {
        if (!(getGame().getCurrentState() instanceof InGameState)) {
            return;
        }

        // Only affect armor slots
        if (!InventoryType.SlotType.ARMOR.equals(event.getSlotType())) {
            return;
        }

        // Check if the cursor was empty
        if (event.getCursor() == null || Material.AIR.equals(event.getCursor().getType())) {
            return;
        }

        // De-Cancel event
        event.setCancelled(false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onResetPlayerOnJoin(PlayerJoinGameEvent event) {
        GamePlayer gamePlayer = event.getGamePlayer();
        gamePlayer.setSpectator(true);
        gamePlayer.teleportTo(getGame().getMap().getSpawnLocation());
        gamePlayer.clearInventory();

        if (gamePlayer instanceof GRPlayer) {
            ((GRPlayer) gamePlayer).reset();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDropItemsOnPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        for (ItemStack itemStack : player.getInventory()) {
            if (itemStack == null) {
                continue;
            }

            // Only drop golden items
            if (Material.GOLD_NUGGET.equals(itemStack.getType()) || Material.GOLD_INGOT.equals((itemStack.getType()))) {
                player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
            }
        }
    }

    @Override
    protected void onResetPlayer(GamePlayer player, Team team) {
        super.onResetPlayer(player, team);

        if (player instanceof GRPlayer) {
            ((GRPlayer) player).addSpeed();
        }
    }

}
