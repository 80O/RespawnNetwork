package net.respawn.goldrush.modules;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.modules.region.Region;
import com.respawnnetwork.respawnlib.gameapi.modules.region.TeamRegionModule;
import com.respawnnetwork.respawnlib.gameapi.modules.region.Vector3i;
import com.respawnnetwork.respawnlib.gameapi.modules.team.Team;
import com.respawnnetwork.respawnlib.gameapi.states.InGameState;
import com.respawnnetwork.respawnlib.network.messages.Message;
import net.respawn.goldrush.GoldRush;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;


public class GRRegionModule extends TeamRegionModule<GoldRush> {
    private int neededScore;


    public GRRegionModule(GoldRush game) {
        super(game);
    }

    @Override
    protected boolean onEnable() {
        neededScore = getGame().getMap().getConfig().getInt("score.needed", 10);

        return super.onEnable();
    }

    @Override
    protected TeamRegion createRegion(Team team, World world, Vector3i min, Vector3i max) {
        return new GRRegion(team, world, min, max);
    }

    @EventHandler
    public void onPlayerCollectsGold(PlayerInteractEvent event) {
        if (event.isCancelled() || getTeamModule() == null || !(getGame().getCurrentState() instanceof InGameState)) {
            return;
        }

        // Get player from game
        GamePlayer player = getGame().getPlayer(event.getPlayer());
        if (player == null) {
            return;
        }

        // Get team by player
        Team team = getTeamModule().getTeam(player);
        if (team == null) {
            return;
        }

        if (event.getClickedBlock().getState() instanceof Sign) {
            for (Region region : getRegions()) {
                if (!region.isInRegion(event.getClickedBlock())) {
                    continue;
                }

                if (region instanceof TeamRegion && !((TeamRegion) region).getTeam().equals(team)) {
                    event.setCancelled(true);
                }
            }

            return;
        }

        // Check if we clicked a dropper
        if (!event.getClickedBlock().getType().equals(Material.DROPPER)) {
            return;
        }

        // Cancel in all cases
        event.setCancelled(true);

        // Get region based on team name
        Region region = getRegion(team.getName());
        if (region == null || !region.isInRegion(event.getClickedBlock())) {
            return;
        }

        // Go through all items and remove them
        int points = 0;

        Player bukkitPlayer = player.getPlayer();
        if (bukkitPlayer == null) {
            return;
        }

        PlayerInventory inventory = bukkitPlayer.getInventory();
        for(int i = 0, max = inventory.getSize(); i < max; ++i) {
            ItemStack itemStack = inventory.getItem(i);
            if(itemStack == null || !itemStack.getType().equals(Material.GOLD_INGOT)) {
                continue;
            }

            points += itemStack.getAmount();

            // Remove item
            inventory.setItem(i, null);
        }

        // AAARGH bukkit!!!
        bukkitPlayer.updateInventory();

        // Don't do anything when we couldn't find any gold
        if (points == 0) {
            return;
        }

        // Increase score for player
        player.getStatistics().increase(GoldRush.GOLD_COLLECTED, points);
        Message.INFO.provide("gold", points).sendKey(player.getPlayer(), "goldrush.collected");

        // increase the score
        team.increaseScore(points);

        // check if we reached the needed score
        if (team.getScore() >= neededScore) {
            getGame().getMap().setWinningTeam(team);

            // Go to next state
            getGame().nextState();
        }
    }


    private static class GRRegion extends TeamRegion {

        private GRRegion(Team team, World world, Vector3i first, Vector3i second) {
            super(team, world, first, second);
        }

        @Override
        protected void onPlayerEnterRegion(GamePlayer player) {
            if (getTeam().getPlayers().contains(player) || player.isSpectator()) {
                return;
            }

            Player bukkitPlayer = player.getPlayer();

            if (bukkitPlayer != null) {
                bukkitPlayer.addPotionEffect(PotionEffectType.WITHER.createEffect(Integer.MAX_VALUE, 3), true);
            }
        }

        @Override
        protected void onPlayerLeaveRegion(GamePlayer player) {
            Player bukkitPlayer = player.getPlayer();

            if (bukkitPlayer != null) {
                bukkitPlayer.removePotionEffect(PotionEffectType.WITHER);
            }
        }

    }

}
