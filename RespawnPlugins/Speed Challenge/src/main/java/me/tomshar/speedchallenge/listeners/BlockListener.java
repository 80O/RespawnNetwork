package me.tomshar.speedchallenge.listeners;

import me.tomshar.speedchallenge.Participate;
import me.tomshar.speedchallenge.SpeedChallenge;
import me.tomshar.speedchallenge.Team;
import me.tomshar.speedchallenge.util.Announcement;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Created by Tom on 11/03/14.
 */
public class BlockListener implements Listener {

	private final SpeedChallenge instance = SpeedChallenge.getInstance();

	@EventHandler
	public void breakEvent(BlockBreakEvent e) {
		Player p = e.getPlayer();
		Block b = e.getBlock();

		Participate ptc = instance.participates.get(p.getName());
		if(ptc == null) return;

		Team team = ptc.getTeam();
		if(team == null) return;
		if(team.getShrine() == null) return;

		// Cancel event if block is in the shrine region.
		if(team.getShrine().getRegion().contains(b.getLocation())) {
			Announcement.DANGER.setRecipient(p.getName()).send("You cannot break blocks by the Shrine.");
			e.setCancelled(true);
		}

	}

	@EventHandler
	public void placeEvent(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		Block b = e.getBlock();

		Participate ptc = instance.participates.get(p.getName());
		if(ptc == null) return;

		Team team = ptc.getTeam();
		if(team == null) return;
		if(team.getShrine() == null) return;

		// Cancel event if block is in the shrine region.
		if(team.getShrine().getRegion().contains(b.getLocation())) {
			Announcement.DANGER.setRecipient(p.getName()).send("You cannot place blocks by the Shrine.");
			e.setCancelled(true);
		}

	}

	@EventHandler
	public void igniteEvent(BlockIgniteEvent e) {
		Player p = e.getPlayer();
		Block b = e.getBlock();

		Participate ptc = instance.participates.get(p.getName());
		if(ptc == null) return;

		Team team = ptc.getTeam();
		if(team == null) return;
		if(team.getShrine() == null) return;

		// Cancel event if block is in the shrine region.
		if(team.getShrine().getRegion().contains(b.getLocation())) {
			Announcement.DANGER.setRecipient(p.getName()).send("You cannot ignite blocks by the Shrine.");
			e.setCancelled(true);
		}

	}

}
