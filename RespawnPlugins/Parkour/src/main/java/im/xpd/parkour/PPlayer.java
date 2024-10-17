package im.xpd.parkour;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PPlayer {
	private String id;
	private String name;
	private int points;
	private Map<String, Integer> coursePoints;
	private List<LogBlock> blockLog;
	private List<LogBlock> blockLogCache;
	private Map<String, Checkpoint> checkPoints;
	private boolean firstJoin = false;

	public PPlayer(String id, String name, int points, Map<String, Integer> coursePoints, List<LogBlock> blockLog, Map<String, Checkpoint> checkPoints) {
		this.id = id;
		this.name = name;
		this.points = points;
		this.coursePoints = coursePoints;
		this.blockLog = blockLog;
		this.blockLogCache = new ArrayList<>();
		this.checkPoints = checkPoints;
	}
	
	public String getId() {
		String uuid = Bukkit.getPlayer(name).getUniqueId().toString();
		return uuid;
	}
	
	public boolean isFirstJoin() {
		return firstJoin;
	}
	
	public void setFirstJoin(boolean firstJoin) {
		this.firstJoin = firstJoin;
	}
	
	public void sendMessage(String msg) {
		Bukkit.getPlayer(name).sendMessage(msg);
	}
	
	public String getName() {
		return name;
	}
	
	public String getUuid() {
		String uuid = Bukkit.getPlayer(name).getUniqueId().toString();
		return uuid;
	}
	
	public int getPoints() {
		return points;
	}
	
	public Map<String, Integer> getCoursePoints() {
		return coursePoints;
	}
	
	public int getCoursePoints(String world) {
		if (coursePoints.containsKey(world.toLowerCase())) {
			return coursePoints.get(world.toLowerCase());
		} else {
			return 0;
		}
	}
	
	public List<LogBlock> getBlockLog() {
		return blockLog;
	}
	
	public List<LogBlock> getBlockLogCache() {
		return blockLogCache;
	}
	
	public boolean hasTakenBlock(Block block) {
		for (LogBlock b : blockLog) {
			if (block.getX() == b.getBlock().getX() && block.getY() == b.getBlock().getY() && block.getZ() == b.getBlock().getZ()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean takeBlock(Block block, ParkourBlockType type) {
		if (!hasTakenBlock(block)) {
			LogBlock lBlock = new LogBlock(block, type);
			blockLog.add(lBlock);
			blockLogCache.add(lBlock);
			return true;
		}
		return false;
	}
	
	public void clearBlockLog(String world, ParkourBlockType type) {
		List<LogBlock> newBlockList = new ArrayList<LogBlock>();
		for (LogBlock lBlock : blockLog) {
			if (!lBlock.getBlock().getWorld().getName().equalsIgnoreCase(world) && lBlock.getType() != type) {
				newBlockList.add(lBlock);
			}
		}
		blockLog = newBlockList;
		
		List<LogBlock> newBlockCacheList = new ArrayList<LogBlock>();
		for (LogBlock lBlock : blockLogCache) {
			if (!lBlock.getBlock().getWorld().getName().equalsIgnoreCase(world) && lBlock.getType() != type) {
				newBlockCacheList.add(lBlock);
			}
		}
		blockLogCache = newBlockCacheList;
	}
	
	public void awardPoints(int points, Course course) {
		Player player = Bukkit.getPlayerExact(name);
		if (player != null && course != null) {
			this.points += points;
			if (coursePoints.containsKey(course.getWorldName().toLowerCase())) {
				coursePoints.put(course.getWorldName().toLowerCase(), coursePoints.get(course.getWorldName().toLowerCase())+points);
			} else {
				coursePoints.put(course.getWorldName().toLowerCase(), points);
			}
		}
	}
	
	public void updateExpBar(Parkour parkour, Course course) {
		Player player = Bukkit.getPlayerExact(name);
		if (player != null) {
			float percentage;
			if (course == null) {
				player.setLevel(points);
				percentage = (float) ((double)points/(double)parkour.getTotalPoints());
				player.setExp(percentage);
			} else {
				player.setLevel(getCoursePoints(course.getWorldName()));
				percentage = (float) ((double)getCoursePoints(course.getWorldName())/(double)course.getTotalPoints());
				player.setExp(percentage);
			}
			ItemStack boots = parkour.getFlair((int) ((double)points/(double)parkour.getTotalPoints()*100)).getItemStack();
			ItemMeta bootmeta = boots.getItemMeta();
			//TO DO: Edit boot meta
			int percentmin = parkour.getFlair((int) ((double)points/(double)parkour.getTotalPoints()*100)).getPercentage();
			List<String> lore = new ArrayList<String>();
			String percentlore = percentmin + "% Total Progress";
			lore.add(ChatColor.GOLD + percentlore);
			bootmeta.setLore(lore);
			boots.setItemMeta(bootmeta);
			player.getInventory().setBoots(boots);
		}
	}
	
	public void setCheckpoint(Block block, Location location) {
		checkPoints.put(block.getWorld().getName().toLowerCase(), new Checkpoint(block, location));
	}
	
	public Checkpoint getCheckPoint(Course course) {
		if (checkPoints.containsKey(course.getWorldName().toLowerCase())) {
			return checkPoints.get(course.getWorldName().toLowerCase());
		} else {
			return new Checkpoint(course.getStart().clone().add(0, -1, 0).getBlock(), course.getStart());
		}
	}
	
	public Collection<Checkpoint> getCheckPoints() {
		return checkPoints.values();
	}
	
	public void updatePotions(Course course) {
		Player player = Bukkit.getPlayer(name);
		if (player != null) {
			for (PotionEffect pe : player.getActivePotionEffects()) {
				player.removePotionEffect(pe.getType());
			}
			if (course != null) {
				for (CoursePotionEffect cpe : course.getPotionEffects()) {
					player.addPotionEffect(cpe.getPotionEffect());
				}
			}
		}
	}
}
