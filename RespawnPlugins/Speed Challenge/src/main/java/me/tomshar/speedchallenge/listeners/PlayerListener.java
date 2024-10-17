package me.tomshar.speedchallenge.listeners;

import me.tomshar.speedchallenge.SpeedChallenge;
import me.tomshar.speedchallenge.util.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

/**
 * Created by Tom on 12/03/14.
 */
public class PlayerListener implements Listener {

	private final SpeedChallenge instance = SpeedChallenge.getInstance();

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();

		if(e.getItem() == null) return;

		if(e.getItem().equals(ItemUtils.TEAM_SELECTION.getProduct()))
		{
			Inventory inv = instance.getServer().createInventory(p, 9, "§lSelect a team");

			inv.setItem(0, ItemUtils.RED_TEAM.getProduct());
			inv.setItem(1, ItemUtils.BLUE_TEAM.getProduct());
			inv.setItem(2, ItemUtils.YELLOW_TEAM.getProduct());
			inv.setItem(3, ItemUtils.GREEN_TEAM.getProduct());

			p.openInventory(inv);
		}

		if(e.getItem().equals(ItemUtils.GAME_SETUP.getProduct()))
		{
			Inventory inv = instance.getServer().createInventory(p, 9, "§lConfiguration: Home");

			inv.setItem(0, ItemUtils.CHALLENGE_MODE.getProduct());
			inv.setItem(1, ItemUtils.CHALLENGE_VARIABLES.getProduct());
			inv.setItem(2, ItemUtils.CHALLENGE_SETUP.getProduct());

			p.openInventory(inv);
		}

	}

}
