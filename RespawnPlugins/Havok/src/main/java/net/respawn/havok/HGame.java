package net.respawn.havok;

import com.respawnnetwork.respawnlib.network.tokens.TokenReward;
import com.respawnnetwork.respawnlib.network.tokens.Tokens;
import gnu.trove.map.hash.THashMap;
import net.respawn.havok.util.BungeeUtils;
import net.respawn.havok.util.GameState;
import net.respawn.havok.util.PlayerUtils;
import net.respawn.havok.util.WeaponEnchantment;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.*;

/**
 * Created by Tom on 19/03/14.
 */
public class HGame {

	private final String name;
	public final World world;
	private Map<UUID, HPlayer> players = new HashMap<>();
	private List<Location> spawns = new ArrayList<>();
	private List<Weapon> weapons = new ArrayList<>();
	private List<PotionEffect> potionEffects = new ArrayList<>();
	private GameState currentState = GameState.PRE_GAME;
	private List<LightningHit> lightningLocales = new ArrayList<>();

    private Map<UUID, Integer> randWeaponCache;

    private ArrayList<String> playerNameCache;

    private Tokens tokens;

	public HGame(String name) {
		this.name = name;
		this.world = Havok.instance.getServer().getWorld(name);

        if(world == null) {
            Havok.instance.getLogger().severe("Failed to load game world specified!");
            return;
        }

        world.setWeatherDuration(0);
        world.setThundering(false);

        world.setTime(Havok.instance.getConfig().getInt("startTime"));
        world.setGameRuleValue("doDaylightCycle", "false");

        if(!(Havok.instance.getConfig().get("spawns") == null) && !(Havok.instance.getConfig().getStringList("spawns").size() == 0)) {
            for (String spawn : Havok.instance.getConfig().getStringList("spawns")) {
                String[] parts = spawn.split(" ");
                if (parts.length == 4) {
                    //Turned pitch to 0 to dissalow vertical rotation of spawn. Not allowing player to look at sky.
                    this.spawns.add(new Location(world, Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Float.parseFloat(parts[3]), 0));
                    Havok.instance.getLogger().info("Added spawn point: " + spawn);
                } else {
                    Havok.instance.getLogger().severe("Error adding spawn: " + spawn);
                }
            }
        } else {
            Havok.instance.getLogger().warning("No random spawns specified in configuration, defaulting to world spawn");
        }

        if(!(Havok.instance.getConfig().get("potions") == null) && !(Havok.instance.getConfig().getStringList("potions").size() == 0)) {
            for (String effect : Havok.instance.getConfig().getStringList("potions")) {
                String[] parts = effect.split(" ");
                if (parts.length == 2) {
                    this.potionEffects.add(new PotionEffect(PotionEffectType.getByName(parts[0]), 9999999, Integer.parseInt(parts[1])));
                    Havok.instance.getLogger().info("Added potion effect: " + PotionEffectType.getByName(parts[0]) + " " + parts[1]);
                } else {
                    Havok.instance.getLogger().severe("Error adding potion: " + effect);
                }
            }
        } else {
            Havok.instance.getLogger().warning("No potion effects found in configuration");
        }

        List<Weapon> premadeWeps = Arrays.asList(
                new Weapon(Material.BLAZE_ROD, 1, "Zeus", new String[]{"Fires lightning bolts", "that rivals the power of Zeus."}, 0.5, 20.0D),
                new Weapon(Material.COAL, 1, "Shaman", new String[]{"Channels the power", "and decimation of wither skulls."}, 1, 20.0D, null),
                new Weapon(Material.FIREBALL, 1, "Blast", new String[]{"Guarantees both you and your enemy will have a blast."}, 2.0, 20.0D, null),
                new Weapon(Material.SNOW_BALL, 16, "Snow Bomb", new String[]{"Offers your enemy the fun of winter", "but with more explosions and death."}, 0.20, 20.0D, null),
                new Weapon(Material.DIAMOND, 1, "Icicle", new String[]{"Left Click: Freezes the player you hit.", "Right Click: Fires arrows rapidly."}, 0.1, 20.0D, null),
                new Weapon(Material.STONE_SWORD, 1, "Spike", new String[]{"A Pretty Sharp Sword (Sharpness II)."}, 0.0, -30000.0D, new WeaponEnchantment(Enchantment.DAMAGE_ALL, 2)),
                new Weapon(Material.WOOD_AXE, 1, "Virus", new String[]{"Viciously poisons the player hit."}, 0.0, -30000.0D)
        );

        int slotCounter = 0;

        while(getWeapons().size() < premadeWeps.size()) {
            Random random = new Random();
            int randInt = random.nextInt(premadeWeps.size());

            if(!getWeapons().contains(premadeWeps.get(randInt))) {
                getWeapons().add(slotCounter, premadeWeps.get(randInt));
                slotCounter++;
            }
        }

        randWeaponCache = new THashMap<UUID, Integer>();
        playerNameCache = new ArrayList<String>();

        if(!(Havok.instance.database == null)) {

            tokens = new Tokens(Havok.instance.database);
        }
	}

    /**
     * Returns the name of the map
     * @return the name of the map
     */
	public String getMapName() { return name; }

    /**
     * returns the current state of the game
     * @return the current state of the game
     */
	public GameState getCurrentState() {
		return currentState;
	}

    /**
     *Sets the current state of the game
     * @param currentState the game state to set it to
     */
	public void setCurrentState(GameState currentState) {
		this.currentState = currentState;
	}

    /**
     * Returns a map of the current game players. Structure is <player name, HPlayer entry>
     * @return a map of the current game players. Structure is <player name, HPlayer entry>
     */
	public Map<UUID, HPlayer> getPlayers() {
		return players;
	}

    /**
     * returns a list of the all the map's spawn points
     * @return a list of the all the map's spawn points
     */
	public List<Location> getSpawns() { return spawns; }

    /**
     * Returns a random spawn
     * @return a random spawn
     */
	public Location getRandomSpawn() {
        if(spawns.size() == 0) {
            return world.getSpawnLocation();
        }

		Random r = new Random();
		return spawns.get(r.nextInt(spawns.size()));
	}

    /**
     * Returns a list of all the weapons
     * @return a list of all the weapons
     */
	public List<Weapon> getWeapons() {
		return weapons;
	}

    /**
     * Returns a random spawn, uses UUID to prevent repeating
     * @param UUID the uuid of the player to randomize for, used to prevent repeating
     * @return a random spawnm uses UUID to prevent repeating
     */
	public Weapon getRandomWeapon(UUID UUID) {
		Random r = new Random();
        int weaponPos = r.nextInt(weapons.size());

        if(randWeaponCache.keySet().contains(UUID)) {

            while(weaponPos == randWeaponCache.get(UUID)) {

                weaponPos = r.nextInt(weapons.size());
            }

            randWeaponCache.put(UUID, weaponPos);

        } else if(!randWeaponCache.keySet().contains(UUID)) {

            randWeaponCache.put(UUID, weaponPos);
        }

		return weapons.get(weaponPos);
	}

    /**
     * Returns a list of the potions effects being applied to the players
     * @return a list of the potions effects being applied to the players
     */
	public List<PotionEffect> getPotionEffects() {
		return potionEffects;
	}

    /**
     * Returns the amount of kills required to win
     * @return the amount of kills required to win
     */
    public int getRequiredKills() {

        if(Havok.instance.getConfig().get("killsToWin") == null) {return -1;}

        int requiredKills = Havok.instance.getConfig().getInt("killsToWin");

        return requiredKills;
    }

    /**
     * Starts the game
     */
	public void start() {

        world.setTime(Havok.instance.getConfig().getInt("startTime"));

        if(!Havok.instance.getConfig().getBoolean("allowTimeChange")) {
            world.setGameRuleValue("doDaylightCycle", "false");
        } else {
            world.setGameRuleValue("doDaylightCycle", "true");
        }

        for(Player player: Havok.instance.getServer().getOnlinePlayers()) {

            Havok.instance.game.getPlayers().put(player.getUniqueId(), new HPlayer(player.getUniqueId()));
        }

        Havok.instance.objective.setDisplayName("§aHavok Kills");
        Havok.instance.scoreboard.resetScores("Players:");

        setCurrentState(GameState.IN_GAME);

		for(HPlayer hPlayer : players.values()) {
			Player player = hPlayer.getPlayer();
			PlayerUtils.reset(player);
			player.setAllowFlight(false);

            Havok.instance.whitelistUUID(player.getUniqueId());

            refreshPotionEffects(hPlayer.getPlayer());
			player.teleport(getRandomSpawn());
			player.setFallDistance(0f);

            hPlayer.getPlayer().setAllowFlight(false);
            hPlayer.getPlayer().setGameMode(GameMode.ADVENTURE);

            Weapon wep = getRandomWeapon(player.getUniqueId());
            hPlayer.getPlayer().getInventory().clear();
            hPlayer.getPlayer().getInventory().setItem(0, wep.getItem());
            hPlayer.getPlayer().getInventory().setHeldItemSlot(0);
            hPlayer.setCurrentWeapon(wep);
            hPlayer.getPlayer().sendMessage(ChatColor.GREEN + "You have been given: " + wep.getName());

            playerNameCache.add(hPlayer.getPlayer().getName());

            Havok.instance.objective.getScore(hPlayer.getPlayer().getName()).setScore(-1);
            Havok.instance.objective.getScore(hPlayer.getPlayer().getName()).setScore(hPlayer.getKills());
            hPlayer.getPlayer().setScoreboard(Havok.instance.scoreboard);
		}
	}

    /**
     * Ends the game, may be passed a player or null
     * @param winner the player to declare the winner
     */
	public void end(Player winner) {
        setCurrentState(GameState.END_GAME);

        if(Havok.instance.getServer().getOnlinePlayers().length == 0) {

            for(String nameToClean: playerNameCache) {

                Havok.instance.scoreboard.resetScores(nameToClean);
            }

            Havok.instance.clearUUIDs();

            Havok.instance.game.getPlayers().clear();
            Havok.instance.game.setCurrentState(GameState.PRE_GAME);
            return;
        }

        for(HPlayer hPlayer : Havok.instance.game.getPlayers().values()) {

            if(hPlayer == null || hPlayer.getPlayer() == null) {continue;}

            if(Havok.instance.isWhitelisted(hPlayer.getUniqueId())) {
                Havok.instance.unWhitelistUUID(hPlayer.getUniqueId());

                Havok.instance.unWhitelistUUID(hPlayer.getUniqueId());

                if(winner == null) {continue;}

                hPlayer.getPlayer().sendMessage(ChatColor.RED + "Game is finished! The winner is " + String.valueOf(winner.getDisplayName()) + "!");
            }
        }

        if(Havok.instance.database != null && winner != null) {

            tokens.give(new TokenReward(String.valueOf(winner.getUniqueId()).replace("-", ""), Havok.instance.getConfig().getInt("winnerTokens")));

            if(Havok.instance.getConfig().getInt("winnerTokens") == 1) {
                winner.getPlayer().sendMessage(ChatColor.GREEN + "You have earned " + Havok.instance.getConfig().getInt("winnerTokens") + " " + Havok.instance.getConfig().getString("tokenNameSingular") + " for winning Havok!");
            } else if(Havok.instance.getConfig().getInt("winnerTokens") > 1) {
                winner.getPlayer().sendMessage(ChatColor.GREEN + "You have earned " + Havok.instance.getConfig().getInt("winnerTokens") + " " +Havok.instance.getConfig().getString("tokenNamePlural") + " for winning Havok!");
            }
        }

        for(Player player: Havok.instance.getServer().getOnlinePlayers()) {

            if(winner == null) {
                player.sendMessage(ChatColor.GREEN + "Being sent back to the lobby in " + Havok.instance.getConfig().getInt("waitBeforeReturningPlayers") + " seconds...");
                player.getInventory().clear();
                player.setLevel(0);
                continue;
            }

            player.sendMessage(ChatColor.GREEN + "Being sent back to the lobby in " + Havok.instance.getConfig().getInt("waitBeforeReturningPlayers") + " seconds...");
            player.getInventory().clear();
            player.setLevel(0);
        }

        Havok.instance.game.getPlayers().clear();

        Havok.instance.getServer().getScheduler().runTaskLater(Havok.instance, new Runnable() {
            @Override
            public void run() {
                for(Player p : Havok.instance.getServer().getOnlinePlayers()) {
                    BungeeUtils.returnPlayer(Havok.instance.getConfig().getString("lobbyServerName"), p);
                }

                for(String nameToClean: playerNameCache) {

                    Havok.instance.scoreboard.resetScores(nameToClean);
                }

                if(Havok.instance.getConfig().getBoolean("stopOnEnd")) {
                    Havok.instance.getServer().dispatchCommand(Havok.instance.getServer().getConsoleSender(), "stop");
                }

                Havok.instance.game.setCurrentState(GameState.PRE_GAME);
                world.setTime(Havok.instance.getConfig().getInt("startTime"));
                world.setGameRuleValue("doDaylightCycle", "false");
            }
        }, Havok.instance.getConfig().getInt("waitBeforeReturningPlayers") * 20);
    }

	/**
	 * Gets the lightning attacker that last shot a lightning bolt at that location.
	 * @param locale
	 * @return
	 */
	public HPlayer getLightningAttacker(Location locale) {
		for(LightningHit hit: lightningLocales){
			if(hit.validateAttacker(locale)){
				return hit.attacker;
			}
		}
		return null;
	}

	/**
	 * Creates the lightning hit and effect in the world.
	 * @param p
	 */
	public void createLightningHit(Player p) {
		Block target = PlayerUtils.getTargetBlock(p, 50);
		Location location = target.getLocation();
		World world = p.getWorld();
		world.strikeLightning(location);
		lightningLocales.add(new LightningHit(location, getPlayers().get(p.getUniqueId())));
	}

	/**
	 * Class that saves each lightning hit, not sure if i need to check local area around hit for players since i havent been able to test it yet.
	 * @author Laake-Mac
	 *
	 */
	private class LightningHit {
		private HPlayer attacker;
		private Location locale;

		public LightningHit(Location locale, HPlayer attacker) {
			this.locale = locale;
			this.attacker = attacker;
		}
		public boolean validateAttacker(Location locale2) {
			if(locale.getBlockX() == locale2.getBlockX()  && locale.getBlockY() == locale2.getBlockY() && locale.getBlockZ() == locale.getBlockZ()) {
				return true;
			} else {
				return false;
			}
		}
	}

    /**
     * Refreshes the potion effects on a player
     * @param player the player to refresh
     */
    public void refreshPotionEffects(Player player) {
        for(PotionEffect effect : potionEffects) {
            player.addPotionEffect(effect);
        }
    }
}
