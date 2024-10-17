package net.respawn.skybridgewars.modules;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.events.StateChangeEvent;
import com.respawnnetwork.respawnlib.gameapi.modules.region.Region;
import com.respawnnetwork.respawnlib.gameapi.modules.region.TeamRegionModule;
import com.respawnnetwork.respawnlib.gameapi.modules.region.Vector3i;
import com.respawnnetwork.respawnlib.gameapi.modules.team.Team;
import com.respawnnetwork.respawnlib.gameapi.states.InGameState;
import com.respawnnetwork.respawnlib.gameapi.statistics.PlayerStatistics;
import lombok.AllArgsConstructor;
import net.respawn.skybridgewars.SkyBridgeWars;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.khelekore.prtree.SimpleMBR;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SBWRegionModule extends TeamRegionModule<SkyBridgeWars> {
    private BukkitTask task;


    public SBWRegionModule(SkyBridgeWars game) {
        super(game);
    }

    @Override
    protected TeamRegion createRegion(Team team, World world, Vector3i min, Vector3i max) {
        return new SBWRegion(team, world, min, max);
    }

    @Override
    protected void onDisable() {
        super.onDisable();

        cancelTask();
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {
        if (event.isCancelled() || event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (!allowsBuilding(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) {
        if (event.isCancelled() || event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (!allowsBuilding(event.getBlockPlaced())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Iterator<Block> it = event.blockList().iterator();

        while (it.hasNext()) {
            Block block = it.next();

            // Remove blocks we don't allow to break
            if (!allowsBuilding(block)) {
                it.remove();
            }
        }
    }

    @EventHandler
    public void onSwitchState(StateChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getNext() instanceof InGameState) {
            createTask();

        } else {
            cancelTask();
        }
    }

    private void createTask() {
        task = new DamageTask(this).runTaskTimer(getGame().getPlugin(), 20, 20);
    }

    private void cancelTask() {
        if (task == null) {
            return;
        }

        task.cancel();
    }

    private boolean allowsBuilding(Block block) {
        if (getTree() == null) {
            return true;
        }

        Iterable<Region> it = getTree().find(new SimpleMBR(
                block.getX(), block.getX(), block.getY(), block.getY(), block.getZ(), block.getZ()
        ));

        for (Region region : it) {
            Object obj = region.getOptions().get("allowBuild");

            if (!(obj instanceof Boolean)) {
                continue;
            }

            boolean allowBuild = (Boolean) obj;

            if (allowBuild) {
                return true;
            }
        }

        return false;
    }

    public List<Region> getBuildingRegions() {
        List<Region> regions = new LinkedList<>();

        for (Region region : getRegions()) {
            Object obj = region.getOptions().get("allowBuild");

            if ((obj instanceof Boolean) && (Boolean) obj) {
                regions.add(region);
            }
        }

        return regions;
    }


    @AllArgsConstructor
    private static class DamageTask extends BukkitRunnable {
        private final SBWRegionModule module;


        @Override
        public void run() {
            if (module.getTeamModule() == null) {
                return;
            }

            for (Region region : module.getRegions()) {
                if (!(region instanceof SBWRegion)) {
                    continue;
                }

                SBWRegion sbwRegion = (SBWRegion) region;
                Team sbwRegionTeam = sbwRegion.getTeam();
                int decrease = 0;

                for (GamePlayer player : sbwRegion.getPlayersInRegion()) {
                    Team team = module.getTeamModule().getTeam(player);

                    if (team == null) {
                        continue;
                    }

                    if (team != sbwRegionTeam) {
                        decrease++;

                        // Track statistics
                        PlayerStatistics statistics = player.getStatistics();

                        if (statistics.getGameStatistics().isTracking(SkyBridgeWars.TEAM_DAMAGE)) {
                            statistics.increase(SkyBridgeWars.TEAM_DAMAGE);
                        }
                    }
                }

                // Just continue if we don't need to decrease anything
                if (decrease < 1) {
                    continue;
                }

                sbwRegionTeam.decreaseScore(decrease);

                // Go to next state when we're done
                if (sbwRegionTeam.getScore() <= 0) {
                    module.getGame().nextState();
                }
            }
        }

    }

    private static class SBWRegion extends TeamRegion {

        private SBWRegion(Team team, World world, Vector3i first, Vector3i second) {
            super(team, world, first, second);
        }

        @Override
        protected void onPlayerEnterRegion(GamePlayer gamePlayer) {
            // He is on the same team
            if (!checkPlayerAndState(gamePlayer) || getTeam().getPlayers().contains(gamePlayer)) {
                return;
            }

            // Add poison effect
            Player player = gamePlayer.getPlayer();

            if (player != null) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, Integer.MAX_VALUE, 0));
            }
        }

        @Override
        protected void onPlayerLeaveRegion(GamePlayer gamePlayer) {
            if (!checkPlayerAndState(gamePlayer)) {
                return;
            }

            // Remove poison effect
            Player player = gamePlayer.getPlayer();

            if (player != null) {
                player.removePotionEffect(PotionEffectType.POISON);
            }
        }

        private boolean checkPlayerAndState(GamePlayer player) {
            return player != null && player.getGame().getCurrentState() instanceof InGameState;
        }

    }

}
