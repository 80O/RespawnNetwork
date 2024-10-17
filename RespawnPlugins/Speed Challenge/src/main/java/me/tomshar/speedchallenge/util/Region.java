package me.tomshar.speedchallenge.util;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 11/03/14.
 */
public class Region {

	private double x1, y1, z1;
	private double x2, y2, z2;

	private World world;

	private List<Location> region = new ArrayList<>();

	public Region(Location p1, Location p2) {
		assert p1.getWorld() == p2.getWorld();

		world = p1.getWorld();
		x1 = Math.min(p1.getX(), p2.getX()); x2 = Math.max(p1.getX(), p2.getX());
		y1 = Math.min(p1.getY(), p2.getY()); y2 = Math.max(p1.getY(), p2.getY());
		z1 = Math.min(p1.getZ(), p2.getZ()); z2 = Math.max(p1.getZ(), p2.getZ());

		this.load();
	}

	public Location getMinimumPoint() {
		return new Location(world, x1, y1, z1);
	}

	public Location getMaximumPoint() {
		return new Location(world, x2, y2, z2);
	}

	public boolean load() {
		int xlen = (int) (x2 - x1);
		int ylen = (int) (y2 - y1);
		int zlen = (int) (z2 - z1);

		for(int x = 0; x < xlen; x++) {
			for(int y = 0; y < ylen; y++) {
				for(int z = 0; z < zlen; z++) {
					region.add(new Location(world, x + x1, y + y1, z + z1));
				}
			}
		}

		return false;
	}

	public boolean contains(Location l) {
		double x = l.getX(); double y = l.getY(); double z = l.getZ();
		return (x <= x2 && x >= x1 && y <= y2 && y >= y1 && z <= z2 && z >= z1);
	}

}
