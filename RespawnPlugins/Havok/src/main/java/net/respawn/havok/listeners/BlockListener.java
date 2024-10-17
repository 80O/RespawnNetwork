package net.respawn.havok.listeners;

import net.respawn.havok.Havok;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

/**
 * Created by Tom on 19/03/14.
 */
public class BlockListener implements Listener {
	private Havok instance;

	public BlockListener(Havok plugin) {
		this.instance = plugin;
	}

	@EventHandler()
	public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(true);
    }

	@EventHandler()
	public void onBlockDamage(BlockDamageEvent event) {
		event.setCancelled(true);
	}

	@EventHandler()
	public void onBlockBreak(BlockBreakEvent event){
        event.setCancelled(true);
	}

	@EventHandler()
	public void onBlockGrow(BlockGrowEvent event) {
		event.setCancelled(true);
	}

	@EventHandler()
	public void onEntityBlockForm(EntityBlockFormEvent event) {
		event.setCancelled(true);
	}

	@EventHandler()
	public void onBlockFromTo(BlockFromToEvent event) {
		event.setCancelled(true);
	}

	@EventHandler()
	public void onBlockForm(BlockFormEvent event) {
		event.setCancelled(true);
	}

	@EventHandler()
	public void onBlockFade(BlockFadeEvent event) {
		event.setCancelled(true);
	}

	@EventHandler()
	public void onBlockIgnite(BlockIgniteEvent event) {
		event.setCancelled(true);
	}

	@EventHandler()
	public void onBlockBurn(BlockBurnEvent event) {
		event.setCancelled(true);
	}

	@EventHandler()
	public void onBlockSpread(BlockSpreadEvent event){
		event.setCancelled(true);
	}

	@EventHandler()
	public void onLeavesDecay(LeavesDecayEvent event){
		event.setCancelled(true);
	}
}