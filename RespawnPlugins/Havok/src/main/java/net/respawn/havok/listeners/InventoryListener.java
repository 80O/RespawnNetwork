package net.respawn.havok.listeners;

import net.respawn.havok.Havok;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Created by Tom on 19/03/14.
 */
public class InventoryListener implements Listener {
	private Havok plugin;

	public InventoryListener(Havok plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		event.setCancelled(true);
		event.setResult(Event.Result.DENY);
	}
}
