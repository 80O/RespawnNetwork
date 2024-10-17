package me.tomshar.speedchallenge.listeners;

import me.tomshar.speedchallenge.Participate;
import me.tomshar.speedchallenge.SpeedChallenge;
import me.tomshar.speedchallenge.Team;
import me.tomshar.speedchallenge.challenge.ChestChallenge;
import me.tomshar.speedchallenge.util.Announcement;
import me.tomshar.speedchallenge.util.ItemName;
import me.tomshar.speedchallenge.util.ItemUtils;
import org.bukkit.block.Chest;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Tom on 11/03/14.
 */
public class InventoryListener implements Listener {

	private final SpeedChallenge instance = SpeedChallenge.getInstance();

	@EventHandler
	public void inventoryPickup(InventoryPickupItemEvent e) {
		Item item = e.getItem();

		for(Team team : instance.teams.values()) {
			if(team.getChallenge() instanceof ChestChallenge) {
				ChestChallenge c = (ChestChallenge) team.getChallenge();

				if(team.getShrine().getRegion().contains(item.getLocation())) {
					boolean passed = false;

					for(ItemStack need : c.getRemaining()) {
						if(need.isSimilar(item.getItemStack())) {
							Announcement.EVENT.sendGrouped(team.getName() + " just added {groupBy} " + ItemName.convert(item.getItemStack()) + " to their Shrine.", item.getItemStack().getAmount(), team);
							passed = true; break;
						}
					}

					if(!passed) {
						List<String> recip = new ArrayList<>();

						for(Participate ptc : team.getMemebers())
							recip.add(ptc.getName());

						Announcement.WARNING.setRecipients(recip).send("The item: " + ItemName.convert(item.getItemStack()) + " is not for the Shrine.");
						e.setCancelled(true);

						Random r = new Random();
						double v = r.nextDouble() * 0.3 - 0.15;
						item.setVelocity(new Vector(v, 0.3, v));
					}

				}

			}
		}

	}

	@EventHandler
	public void inventoryItemMove(InventoryMoveItemEvent e) {
		Inventory to = e.getDestination();

		if(to.getHolder() instanceof Chest) {
			Chest chest = (Chest) to.getHolder();
			for(Team team : instance.teams.values()) {
				if(chest.equals(team.getShrine().getChest())) {
					new CheckWinCondition(team).runTaskLaterAsynchronously(instance, 10);
				}
			}
		}

	}

	class CheckWinCondition extends BukkitRunnable {

		private final Team team;

		public CheckWinCondition(Team team) {
			this.team = team;
		}

		@Override
		public void run() {
			if(team.getChallenge().checkWinCondition()) {
				Announcement.EVENT.send(team.getName() + " have completed the challenge.", team);
			}
		}
	}


	/**
	 * If in lobby world then don't let people drop items etc.
	 */

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getWhoClicked().getWorld().getName().equals("world")) {
			e.setCancelled(true);
			Player p = (Player) e.getWhoClicked();
			ItemStack clicked = e.getCurrentItem();

			if(ItemUtils.CHALLENGE_MODE.getProduct().equals(clicked)) {
				Inventory inv = instance.getServer().createInventory(p, 9, "§lConfiguration: Mode");

				inv.setItem(0, ItemUtils.CHALLENGE_MODE.getProduct());
				inv.setItem(1, ItemUtils.CHALLENGE_VARIABLES.getProduct());
				inv.setItem(2, ItemUtils.CHALLENGE_SETUP.getProduct());

				p.openInventory(inv);
				return;
			}

			for(ItemUtils custom : ItemUtils.values()) {
				if(custom.getProduct().equals(clicked)) {
					Team team = instance.teams.get(custom.getData());

					if(team != null) {
						Participate ptc = instance.participates.get(p.getName());

						if(ptc != null) {
							if(ptc.hasTeam())
								ptc.getTeam().removeMember(ptc, false);

							team.addMember(ptc);
							instance.participates.put(ptc.getName(), ptc);
						} else {
							ptc = new Participate(p.getName(), team);
							instance.participates.put(ptc.getName(), ptc);
						}

						break;
					}
				}
			}

		}
	}

	@EventHandler
	public void onDrag(InventoryDragEvent e) {
		if(e.getWhoClicked().getWorld().getName().equals("world"))
			e.setCancelled(true);
	}

	@EventHandler
	public void onPickup(PlayerPickupItemEvent e) {
		if(e.getPlayer().getWorld().getName().equals("world"))
			e.setCancelled(true);
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		if(e.getPlayer().getWorld().getName().equals("world"))
			e.setCancelled(true);
	}

}
