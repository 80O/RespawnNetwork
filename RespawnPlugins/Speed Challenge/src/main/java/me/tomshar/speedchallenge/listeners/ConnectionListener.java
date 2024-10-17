package me.tomshar.speedchallenge.listeners;

import me.tomshar.speedchallenge.SpeedChallenge;
import me.tomshar.speedchallenge.util.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;

/**
 * Created by Tom on 12/03/14.
 */
public class ConnectionListener implements Listener {

	private final SpeedChallenge instance = SpeedChallenge.getInstance();

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		Inventory inv = p.getInventory();

		if(!instance.participates.containsKey(p.getName())) {
			inv.clear();

			inv.setItem(0, ItemUtils.TEAM_SELECTION.getProduct());

			if(p.isOp()) {
				inv.setItem(2, ItemUtils.GAME_SETUP.getProduct());
			}
		}

	}

}
