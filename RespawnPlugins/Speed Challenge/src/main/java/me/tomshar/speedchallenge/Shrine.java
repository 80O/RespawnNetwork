package me.tomshar.speedchallenge;

import me.tomshar.speedchallenge.util.Region;
import org.bukkit.Location;
import org.bukkit.block.Chest;

/**
 * Created by Tom on 10/03/14.
 */
public class Shrine {

	private final Location chestLocation;
	private final Region region;

	public Shrine(Location chestLocation, Location p1, Location p2) {
		this.chestLocation = chestLocation;
		this.region = new Region(p1, p2);
	}

	public Location getHopperLocation() {
		return chestLocation.add(0, 1, 0);
	}

	public Location getChestLocation() {
		return chestLocation;
	}

	public Chest getChest() {
		return (Chest) chestLocation.getBlock().getState();
	}

	public Region getRegion() {
		return region;
	}

}
