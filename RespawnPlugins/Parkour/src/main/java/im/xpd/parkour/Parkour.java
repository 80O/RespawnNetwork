package im.xpd.parkour;

import im.xpd.parkour.SpecialCondition.SpecialCondition;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.*;

public class Parkour {
	private Main main;
	
	private Lobby lobby;
	private Map<String, Course> courses;
	private Map<String, PPlayer> pPlayers;
	public int totalPoints;
	private List<Flair> flair;
	private boolean tokensEnabled = true;
	
	public Parkour(Main main) {
		this.main = main;
		this.pPlayers = new HashMap<>();
		this.flair = new ArrayList<>();
		load();
	}
	
	public Lobby getLobby() {
		return lobby;
	}
	
	public boolean tokensEnabled() {
		return tokensEnabled;
	}
	
	public Collection<Course> getCourses() {
		return courses.values();
	}
	
	public Course getCourseByLobbySign(Location lobbySign) {
		for (Course course : courses.values()) {
			if (lobbySign.getWorld() == main.getServer().getWorlds().get(0) && course.getLobbySign().getBlockX() == lobbySign.getBlockX() && course.getLobbySign().getBlockY() == lobbySign.getBlockY() && course.getLobbySign().getBlockZ() == lobbySign.getBlockZ()) {
				return course;
			}
		}
		return null;
	}
	
	public Course getCourseByWorldName(String worldName) {
		return courses.get(worldName.toLowerCase());
	}
	
	public PPlayer getPPlayer(String name) {
		return pPlayers.get(name.toLowerCase());
	}
	
	public PPlayer loadPPlayer(String name) {
        if(name == null){
            main.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Parkour] Player name was null! Aborting player load...");
        }
		PPlayer pPlayer = main.database.getPPlayer(name);
		if (pPlayer != null) {
			pPlayers.put(name.toLowerCase(), pPlayer);
			return pPlayer;
		}
		return null;
	}
	
	public void unloadPPlayer(String name) {
		PPlayer pPlayer = getPPlayer(name);
		if (pPlayer != null) {
			main.database.savePlayer(pPlayer);
		}
		pPlayers.remove(name.toString());
	}
	
	public int getTotalPoints() {
		return totalPoints;
	}
	
	@SuppressWarnings("deprecation")
	public void load() {
		// Load lobby
		File lobbyFile = new File(main.getServer().getWorldContainer() + File.separator + main.getConfig().getString("lobbyWorld") + File.separator + "lobby.yml");
		YamlConfiguration lobbyYml = YamlConfiguration.loadConfiguration(lobbyFile);
		
		Location lobbySpawn = new Location(main.getServer().getWorlds().get(0), lobbyYml.getDouble("spawn.x"), lobbyYml.getDouble("spawn.y"), lobbyYml.getDouble("spawn.z"));
		lobbySpawn.setPitch((float) lobbyYml.getDouble("spawn.pitch"));
		lobbySpawn.setYaw((float) lobbyYml.getDouble("spawn.yaw"));
		int lobbyBackToSpawnY = lobbyYml.getInt("back-to-spawn");
	    tokensEnabled = main.getConfig().getBoolean("tokensEnabled");
		main.getServer().getWorlds().get(0).setSpawnLocation(lobbySpawn.getBlockX(), lobbySpawn.getBlockY(), lobbySpawn.getBlockZ());
		
		lobby = new Lobby(lobbySpawn, lobbyBackToSpawnY);
		
		// Load parkour courses
		courses = new HashMap<>();
		totalPoints = 0;
		for (File file : main.getServer().getWorldContainer().listFiles()) {
			if (file.isDirectory() && file.getName().startsWith("course_")) {
				File courseFile = new File(file + File.separator + "course.yml");
				if (courseFile.exists()) {
					// Load course world
					World courseWorld = main.getServer().getWorld(file.getName());
					if (courseWorld == null) {
						courseWorld = main.getServer().createWorld(new WorldCreator(file.getName()));
					}
                    courseWorld.setGameRuleValue("doDaylightCycle","false");

					YamlConfiguration courseYml = YamlConfiguration.loadConfiguration(courseFile);
					
					Location courseStart = new Location(courseWorld, courseYml.getDouble("start.x"), courseYml.getDouble("start.y"), courseYml.getDouble("start.z"));
					courseStart.setPitch((float) courseYml.getDouble("start.pitch"));
					courseStart.setYaw((float) courseYml.getDouble("start.yaw"));
					courseWorld.setSpawnLocation(courseStart.getBlockX(), courseStart.getBlockY(), courseStart.getBlockZ());

					Location courseLobbySign = new Location(main.getServer().getWorlds().get(0), courseYml.getInt("lobby-sign.x"), courseYml.getInt("lobby-sign.y"), courseYml.getInt("lobby-sign.z"));
					
					String courseName = courseYml.getString("name");
					String courseAuthor = courseYml.getString("author");
                    String permission = courseYml.getString("permission");
					
					//Load blacklisted/whitelisted blocks
					String mode = null;
					List<String> modeBlocks = new ArrayList<>();
					try{
					for(String id : courseYml.getStringList("modeBlocks")){
						modeBlocks.add(id);
					}
					mode = courseYml.getString("mode");
					}catch(Exception ex){
					}
					
					int checkpointYTrigger = courseYml.getInt("checkpoint-y-trigger");
					
					List<ParkourBlock> blocks = new ArrayList<>();
					for (String b : courseYml.getStringList("blocks")) {
						String[] bD = b.split(" ");
						if (bD.length == 2) {
							ParkourBlockType type = ParkourBlockType.valueOf(bD[1]);
							if (type != null) {
								int id;
								byte data;
								if (!bD[0].contains(":")) {
									try {
										id = Integer.parseInt(bD[0]);
										data = 0;
									} catch (Exception e) {
										continue;
									}
								} else {
									String[] bDD = bD[0].split(":");
									try {
										id = Integer.parseInt(bDD[0]);
										data = (byte) Integer.parseInt(bDD[1]);
									} catch (Exception e) {
										continue;
									}
								}
								main.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[Parkour] ("+courseWorld.getName()+") " + id + ":" + data + " = "+type+".");
								blocks.add(new ParkourBlock(id, data, type));
							}
						}
					}
					
					int courseTotalPoints = courseYml.getInt("points");
					totalPoints += courseTotalPoints;
					
					List<SpecialCondition> courseSpecialConditions = new ArrayList<>();
					for (String ssc : courseYml.getStringList("special")) {
						SpecialCondition sc = SpecialCondition.fromString(ssc);
						if (sc != null) {
							courseSpecialConditions.add(sc);
						} else {
							main.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Parkour] Failed to parse special condition \""+ChatColor.WHITE+ssc+ChatColor.RED+"\".");
						}
					}
					
					List<CoursePotionEffect> coursePotionEffects = new ArrayList<>();
					for (String potion : courseYml.getStringList("potions")) {
						String[] potionD = potion.split(" ");
						if (potionD.length == 2) {
							PotionEffectType pet = PotionEffectType.getByName(potionD[0]);
							int level = 0;
							try {
								level = Integer.parseInt(potionD[1])-1;
							} catch (Exception e) {
								
							}
							coursePotionEffects.add(new CoursePotionEffect(pet, level));
						}
					}
					
					Course course = new Course(courseWorld.getName(), courseStart, courseLobbySign, courseName, courseAuthor, checkpointYTrigger, blocks, courseTotalPoints, courseSpecialConditions, coursePotionEffects, mode, modeBlocks, permission);
					courses.put(courseWorld.getName(), course);
					course.updateCourseSign();
				} else {
					main.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Parkour] Can't load course \""+file.getName()+"\" because course.yml is missing.");
				}
			}
		}
		
		// Load boots
		for (String b : main.getConfig().getConfigurationSection("boots").getKeys(false)) {
			int percentage;
			try {
				percentage = Integer.parseInt(b);
			} catch (Exception e) {
				main.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Parkour] Failed to load boot. \""+b+"\" is an invalid integer.");
				continue;
			}
			
			int id;
			if (main.getConfig().contains("boots."+b+".id")) {
				id = main.getConfig().getInt("boots."+b+".id");
			} else {
				main.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Parkour] Failed to load boot. \""+b+"\" does not have an item ID.");
				continue;
			}
			
			ItemStack itemStack = new ItemStack(Material.getMaterial(id));
			
			if (main.getConfig().contains("boots."+b+".leather-rgb")) {
				if (itemStack.getType() == Material.LEATHER_BOOTS) {
					if (main.getConfig().getIntegerList("boots."+b+".leather-rgb").size() == 3) {
						List<Integer> rgb = main.getConfig().getIntegerList("boots."+b+".leather-rgb");
						Color leatherColor = Color.fromRGB(rgb.get(0), rgb.get(1), rgb.get(2));
						LeatherArmorMeta lam = (LeatherArmorMeta) itemStack.getItemMeta();
						lam.setColor(leatherColor);
						itemStack.setItemMeta(lam);
					}
				}
			}
			
			if (main.getConfig().contains("boots."+b+".enchantments")) {
				for (String e : main.getConfig().getStringList("boots."+b+".enchantments")) {
					String[] eD = e.split(" ");
					if (eD.length == 2) {
						int lvl;
						try {
							lvl = Integer.parseInt(eD[1]);
						} catch (Exception ex) {
							main.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Parkour] Failed to enchantment \""+e+"\" on boot \""+b+"\". Invalid integer \""+eD[1]+"\".");
							continue;
						}
						Enchantment ench = Enchantment.getByName(eD[0]);
						if (ench != null) {
							itemStack.addUnsafeEnchantment(ench, lvl);
						} else {
							main.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Parkour] Failed to enchantment \""+e+"\" on boot \""+b+"\". Invalid enchantment \""+eD[0]+"\".");
						}
					} else {
						main.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Parkour] Failed to enchantment \""+e+"\" on boot \""+b+"\".");
					}
				}
			}
			
			flair.add(new Flair(percentage, itemStack));
		}
		//Load Inventory Teleporter
	    FileConfiguration config = main.getConfig();
	    int rows = config.getInt("menu.rows") * 9;
		Inventory i = Bukkit.createInventory(null,rows,ChatColor.DARK_RED + main.getConfig().getString("menu.title"));
		main.in = i;
		HashMap<String,String> invicons = new HashMap<>();
		for(String worldname : config.getConfigurationSection("menu.items").getKeys(false)){
			int slot = Integer.parseInt(config.getString("menu.items." + worldname + ".slot"));
			ItemStack is = new ItemStack(Material.getMaterial(config.getInt("menu.items." + worldname + ".materialid")));
			ItemMeta im = is.getItemMeta();
			String displayname = config.getString("menu.items." + worldname + ".displayname");
			im.setDisplayName(ChatColor.RESET + "" + ChatColor.GREEN + displayname);
			List<String> lores = new ArrayList<>();
			lores.add(ChatColor.RESET + "Total Points: " + config.getString(("menu.items." + worldname + ".points")));
			lores.add(ChatColor.RESET + "Author: " + config.getString(("menu.items." + worldname + ".author")));
			lores.add(ChatColor.RESET + "Difficulty: " + config.getString(("menu.items." + worldname + ".diff")));
			im.setLore(lores);
			is.setItemMeta(im);
			main.in.setItem(slot,is);
			invicons.put(worldname, displayname);
		}
		main.inicons = invicons;
	}
	
	public Flair getFlair(int percentage) {
		for (Flair f : flair) {
			if (percentage >= f.getPercentage()) {
				return f;
			}
		}
		
		return new Flair(0, null);
	}
}
