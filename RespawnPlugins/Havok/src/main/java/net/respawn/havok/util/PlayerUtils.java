package net.respawn.havok.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Tom on 19/03/14.
 */
public class PlayerUtils {

	public static void reset(Player p) {
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
		p.setHealth(20d);
		p.setSaturation(20f);
		p.setFoodLevel(20);
		p.setExhaustion(0f);
	}

	public static String getUuidByName(String name) {
		return Bukkit.getPlayer(name).getUniqueId().toString();
	}

	public static Block getTargetBlock(Player player, int maxDistance)
	{
		return (Block) getLineOfSight(player, maxDistance, 1).get(0);
	}

	public static List<Block> getLineOfSight(Player player, int maxDistance, int maxLength)
	{
		if (maxDistance > 120) {
			maxDistance = 120;
		}
		List<Block> blocks = new ArrayList();
		Iterator<Block> itr = new BlockIterator(player, maxDistance);
		while (itr.hasNext())
		{
			Block block = (Block)itr.next();

			blocks.add(block);
			if ((maxLength != 0) &&
					(blocks.size() > maxLength)) {
				blocks.remove(0);
			}
			Material material = block.getType();
			if (material != Material.AIR) {
				break;
			}
		}
		return blocks;
	}

}
