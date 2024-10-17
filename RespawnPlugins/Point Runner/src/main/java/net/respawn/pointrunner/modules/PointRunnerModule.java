package net.respawn.pointrunner.modules;

import com.respawnnetwork.respawnlib.bukkit.Location;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.GameState;
import com.respawnnetwork.respawnlib.gameapi.events.PlayerJoinGameEvent;
import com.respawnnetwork.respawnlib.gameapi.events.StateChangeEvent;
import com.respawnnetwork.respawnlib.gameapi.modules.ScoreModule;
import com.respawnnetwork.respawnlib.gameapi.states.InGameState;
import com.respawnnetwork.respawnlib.gameapi.states.PrepareGameState;
import com.respawnnetwork.respawnlib.plugin.PluginDependency;
import gnu.trove.map.hash.THashMap;
import lombok.Getter;
import net.respawn.pointrunner.PRPlayer;
import net.respawn.pointrunner.PointRunner;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class PointRunnerModule extends GameModule<PointRunner> implements Listener {
    private final boolean useBarAPI;

    private ScoreModule scoreModule;

    private SafetyCountdown safetyCountdown;

    @Getter
    private List<Location> spawnlocations = new LinkedList<>();

    private Map<Material, Integer> points = new THashMap<>();
    private Material deathBlock;

    private Map<GamePlayer, MultiplierTask> multiplierTasks = new THashMap<>();
    private Material multiplierBlock;
    private short multiplierAmount;
    private short multiplierCooldown;

    private List<Block> collectedBlocks = new LinkedList<>();

    private boolean allowJumps;

    private int maxPoints = 1;

    private int safetyCooldown;


    public PointRunnerModule(PointRunner game) {
        super(game);

        PluginDependency pluginDependency = getGame().getPlugin().getPluginDependency();
        useBarAPI = pluginDependency != null && pluginDependency.isInstalled("BarAPI");

        if (!useBarAPI) {
            getLogger().info("Could not find BarAPI, will not display uber fancy, fancy stuffz");

        } else {
            getLogger().info("BarAPI hooked and loaded. Prepare for uber fancy, fancy stuffz");
        }
    }

    @Override
    protected boolean onEnable() {

        scoreModule = getGame().getModule(ScoreModule.class);
        if (scoreModule != null) {
            if(!scoreModule.isLoaded()) {
                getLogger().warning("ScoreModule is not loaded!");

            } else {
                scoreModule.getScoreboards().clear();
            }
        } else {
            getLogger().warning("Could not get ScoreModule, has it been added?");
        }

        // load spawn locations
        spawnlocations = Location.parseList(getGame().getMap().getWorld(), getConfig().getMapList("spawnLocations"));

        ConfigurationSection section = getConfig().getConfigurationSection("blocks");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Material material = Material.matchMaterial(key);
                if (material == null) {
                    getLogger().warning("Unknown material: blocks." + key);
                    continue;
                }

                int pointValue = section.getInt(key);

                if (pointValue > maxPoints) {
                    maxPoints = pointValue;
                }

                points.put(material, pointValue);
            }
        }

        getLogger().info("Loaded " + points.size() + " block type(s)");

        deathBlock = Material.matchMaterial(getConfig().getString("deathBlock", "bedrock"));

        // Multiplier stuffz
        multiplierBlock = Material.matchMaterial(getConfig().getString("multiplier.block", "lapis block"));
        multiplierAmount = (short) getConfig().getInt("multiplier.amount", 2);
        multiplierCooldown = (short) getConfig().getInt("multiplier.cooldown", 10);

        allowJumps = getConfig().getBoolean("allowJumps", false);

        safetyCooldown = getConfig().getInt("safetyCooldown", 10);

        return true;
    }

    @Override
    protected void onDisable() {
        if (!getGame().getPlugin().isEnabled()) {
            return;
        }

        // Cancel safety countdown
        if (safetyCountdown != null) {
            safetyCountdown.cancel();
        }

        // Cancel multiplier tasks
        for (MultiplierTask task : multiplierTasks.values()) {
            task.cancel();
        }
    }

    @Override
    public String getDisplayName() {
        return "PointRunner";
    }

    @Override
    public String getName() {
        return "point-runner";
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        GameState currentState = getGame().getCurrentState();
        if (!(currentState instanceof InGameState) || currentState instanceof PrepareGameState) {
            return;
        }

        // Get player and ignore spectators
        PRPlayer player = getGame().getPlayer(event.getPlayer());
        if (player == null  || player.isSpectator()) {
            return;
        }

        // Check the block beneath the player
        Player bukkitPlayer = event.getPlayer();
        Block foot = event.getTo().getBlock().getRelative(BlockFace.DOWN);

        if (foot == null || foot.getType().equals(Material.AIR)) {
            return;
        }

        if (foot.getType().equals(deathBlock)) {
            getGame().createMessage().provide("player", player.getName()).sendKey("game.death");
            callEvent(new PlayerDeathEvent(bukkitPlayer, new LinkedList<ItemStack>(), 0, ""));
            return;
        }

        if (collectedBlocks.contains(foot)) {
            return;
        }

        if (foot.getType().equals(multiplierBlock)) {
            MultiplierTask task = multiplierTasks.get(player);

            if (task == null) {
                task = new MultiplierTask(player);
                multiplierTasks.put(player, task);

                task.runTaskTimer(getGame().getPlugin(), 20, 20);

            } else {
                task.setCounter(0);
            }

        } else {
            Integer point = points.get(foot.getType());

            if (point != null) {
                if (multiplierTasks.containsKey(player)) {
                    point *= multiplierAmount;
                }

                int totalPoints = (int) player.getStatistics().increase(PointRunner.POINTS, point);
                event.getPlayer().setLevel(totalPoints);

                if (scoreModule != null) {
                    scoreModule.setScore(player.getName(), totalPoints);
                }

                // Play scaled sound
                Sound sound = point < 0 ? Sound.BLAZE_HIT: Sound.ORB_PICKUP;
                bukkitPlayer.playSound(bukkitPlayer.getLocation(), sound, 1.5f, ((float) point / (float) maxPoints * 2f));
            }
        }

        collectedBlocks.add(foot);
        removeBlockLater(foot, 10);

        // just for "safety" reasons, ha ha ha ...
        createSafetyCountdown();
        safetyCountdown.counter = 0;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        GamePlayer player = getGame().getPlayer(event.getEntity());
        if (player == null) {
            return;
        }

        getLogger().info(player.getName() + " died with " + (int)player.getStatistics().get(PointRunner.POINTS) + " point(s)");
        setSpectator(player);

        // Now do the end game check
        checkGameEnd();
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinGameEvent event) {
        GamePlayer gamePlayer = event.getGamePlayer();
        gamePlayer.clearPotionEffects();
        setSpectator(gamePlayer);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (useBarAPI) {
            me.confuser.barapi.BarAPI.removeBar(event.getPlayer());
        }

        getGame().removePlayer(getGame().getPlayer(event.getPlayer()));

        if (getGame().getCurrentState() instanceof InGameState) {
            checkGameEnd();
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent e) {
        if(getGame().getCurrentState() instanceof InGameState) {
            checkGameEnd();
        }
    }

    @EventHandler
    public void onStateChange(StateChangeEvent event) {
        if (event.isCancelled() || !(event.getNext() instanceof InGameState)) {
            return;
        }

        createSafetyCountdown();

        for (PRPlayer player : getGame().getPlayers()) {
            Player bukkitPlayer = player.getPlayer();
            if (bukkitPlayer == null || allowJumps) {
                continue;
            }

            bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128));
        }

        if (scoreModule != null) {
            scoreModule.getScoreboards().displaySlot(DisplaySlot.SIDEBAR);
        }
    }

    private void removeBlockLater(final Block block, int after) {
        getGame().getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getGame().getPlugin(), new Runnable() {
            @Override
            public void run() {
                block.setType(Material.AIR);
                collectedBlocks.remove(block);
            }
        }, after);
    }

    private void setSpectator(GamePlayer player) {
        player.heal();
        player.setSpectator(true);
        player.teleportTo(getGame().getMap().getSpawnLocation());
    }

    private void checkGameEnd() {
        // Otherwise just go to the next state
        if (getGame().getNumberOfRealPlayers() <= 1) {
            getGame().nextState();
        }
    }

    private void createSafetyCountdown() {
        if (safetyCountdown != null) {
            return;
        }

        safetyCountdown = new SafetyCountdown();
        safetyCountdown.runTaskTimer(getGame().getPlugin(), 20, 20);
    }


    private class SafetyCountdown extends BukkitRunnable {
        private int counter;


        @Override
        public void run() {
            counter++;

            // Go to next state when we reached the safety countdown
            if (counter >= safetyCooldown) {
                getLogger().info("Safety countdown kicking in, will go to next state automatically ...");
                cancel();
                getGame().nextState();
            }
        }
    }

    private class MultiplierTask extends BukkitRunnable {
        private final GamePlayer player;
        private float counter;
       // private int secCounter;


        private MultiplierTask(GamePlayer player) {
            this.player = player;

            setCounter(0);
        }

        @Override
        public void run() {
           // secCounter++;

            //if (secCounter % 10 == 0) {
                setCounter(counter + 1);

                if (counter >= multiplierCooldown) {
                    playSound(Sound.FIZZ);

                    if (useBarAPI) {
                        me.confuser.barapi.BarAPI.removeBar(player.getPlayer());
                    }

                    multiplierTasks.remove(player);
                    cancel();
                }

           //     secCounter = 0;
           // }
        }

        private void playSound(Sound sound) {
            Player bukkitPlayer = player.getPlayer();
            if (bukkitPlayer == null) {
                return;
            }

            bukkitPlayer.playSound(bukkitPlayer.getLocation(), sound, 2, 1.5f);
        }

        public void setCounter(float counter) {
            // Play eat sound whenever we renew the multiplier
            if (counter == 0) {
                playSound(Sound.EAT);
            }

            this.counter = counter;

            // Also use the BarAPI
            if (useBarAPI) {
                me.confuser.barapi.BarAPI.setMessage(player.getPlayer(),
                        "Multiplier §lx" + multiplierAmount,
                        100f - (100f * (counter / (float)multiplierCooldown))
                );
            }
        }
    }

}
