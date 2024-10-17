package me.tomshar.speedchallenge;

import me.tomshar.speedchallenge.listeners.BlockListener;
import me.tomshar.speedchallenge.listeners.ConnectionListener;
import me.tomshar.speedchallenge.listeners.InventoryListener;
import me.tomshar.speedchallenge.listeners.PlayerListener;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tom on 10/03/14.
 */
public class SpeedChallenge extends JavaPlugin {

	private static SpeedChallenge instance;
	public static SpeedChallenge getInstance() { return instance; }

	public Map<Integer, Team> teams = new HashMap<>();
	public Map<String, Participate> participates = new HashMap<>();

	@Override
	public void onEnable() {
		this.instance = this;

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new BlockListener(), this);
		pm.registerEvents(new InventoryListener(), this);
		pm.registerEvents(new ConnectionListener(), this);
		pm.registerEvents(new PlayerListener(), this);

		teams.put(14, new Team("Red Team", ChatColor.RED));
		teams.put(11, new Team("Blue Team", ChatColor.BLUE));
		teams.put(4, new Team("Yellow Team", ChatColor.YELLOW));
		teams.put(5, new Team("Green Team", ChatColor.DARK_GREEN));

		/*
		World w = getServer().getWorld("world");

		List<ItemStack> map = new ArrayList<>();

		map.add(new ItemStack(Material.WOOL, 10, (short) 14));
		map.add(new ItemStack(Material.APPLE, 3));
		map.add(new ItemStack(Material.ICE, 3));

		Shrine s = new Shrine(new Location(w, -38.5, 97, 1.5), new Location(w, -41.5, 95, 4.5), new Location(w, -35.5, 104, -1.5));
		ChestChallenge cc = new ChestChallenge(Challenges.WOOL_RACE, s, map);

		Team team = new Team("Red Team", ChatColor.LIGHT_PURPLE);
		Participate tomshar = new Participate("TomShar");
		team.setShrine(s);
		team.setChallenge(cc);
		team.addMember(tomshar);

		teams.put(team.getName(), team);
		participates.put("TomShar", tomshar);
		*/

	}

	@Override
	public void onDisable() {

	}

}
