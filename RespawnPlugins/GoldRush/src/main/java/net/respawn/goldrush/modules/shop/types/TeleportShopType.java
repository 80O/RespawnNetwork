package net.respawn.goldrush.modules.shop.types;

import com.respawnnetwork.respawnlib.bukkit.Location;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.modules.team.Team;
import com.respawnnetwork.respawnlib.gameapi.modules.team.TeamModule;
import com.respawnnetwork.respawnlib.lang.ParseException;
import com.respawnnetwork.respawnlib.network.messages.Message;
import gnu.trove.map.hash.THashMap;
import net.respawn.goldrush.GRPlayer;
import net.respawn.goldrush.GoldRush;
import net.respawn.goldrush.modules.shop.ShopItem;
import net.respawn.goldrush.modules.shop.UnlockableShopItem;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public class TeleportShopType implements Listener, ShopType {
    private final Map<Team, List<TeleportShopItem>> unlocked;
    private final TeamModule<GoldRush> teamModule;
    private final Map<GamePlayer, BukkitTask> teleportTasks;
    private final int teleportDelay;


    public TeleportShopType(TeamModule<GoldRush> teamModule, int teleportDelay) {
        this.unlocked = new THashMap<>();
        this.teleportTasks = new THashMap<>();

        this.teamModule = teamModule;
        this.teleportDelay = teleportDelay;
    }

    @Override
    public ShopItem parseConfig(Logger logger, int price, ConfigurationSection itemConfig) throws ParseException {
        World world = teamModule.getGame().getMap().getWorld();

        String displayName = itemConfig.getString("name", "<unknown>");
        Map<Location, Location> teleports = new THashMap<>();

        for (Map<?, ?> map : itemConfig.getMapList("teleports")) {
            ConfigurationSection teleportSection = new MemoryConfiguration().createSection("tmp", (Map) map);

            Location from = new Location(world, teleportSection.getConfigurationSection("from").getValues(false));
            Location to = new Location(world, teleportSection.getConfigurationSection("to").getValues(false));

            teleports.put(from, to);
        }

        return new TeleportShopItem(price, displayName, teleports);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Don't do stuff if the player has not moved blocks
        Location a = new Location(event.getFrom().getBlock().getLocation());
        Location b = new Location(event.getTo().getBlock().getLocation());

        if (a.getBlockX() == b.getBlockX() && a.getBlockY() == b.getBlockY() && a.getBlockZ() == b.getBlockZ()) {
            return;
        }

        final Player player = event.getPlayer();
        final GRPlayer grPlayer = teamModule.getGame().getPlayer(player);

        BukkitTask task = teleportTasks.get(grPlayer);
        if (task != null) {
            task.cancel();
            teleportTasks.remove(grPlayer);

            Message.INFO.sendKey(player, "goldrush.teleport.canceled");
            return;
        }

        // Get player team
        Team team = teamModule.getTeam(grPlayer);
        if (team == null) {
            return;
        }

        // Get unlocked teleports for team
        List<TeleportShopItem> shopItems = unlocked.get(team);
        if (shopItems == null) {
            return;
        }

        for (TeleportShopItem shopItem : shopItems) {
            final Location toLocation = shopItem.teleports.get(b);

            // Teleport the player if the block is a teleport
            if (toLocation == null) {
                continue;
            }

            Message.INFO.provide("seconds", teleportDelay).sendKey(player, "goldrush.teleport.commencing");

            teleportTasks.put(grPlayer, new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleport(toLocation);
                    player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 2, 1);

                    // Remove old task
                    teleportTasks.remove(grPlayer);
                }
            }.runTaskLater(teamModule.getGame().getPlugin(), teleportDelay * 20));
        }
    }

    @Override
    public String getName() {
        return "teleport";
    }


    private class TeleportShopItem extends UnlockableShopItem {
        private final String displayName;
        private final Map<Location, Location> teleports;


        private TeleportShopItem(int price, String displayName, Map<Location, Location> teleports) {
            super(teamModule, price);

            this.displayName = displayName;
            this.teleports = teleports;
        }

        @Override
        protected boolean onUnlockItem(GamePlayer player, Team team) {
            List<TeleportShopItem> unlockedItems = unlocked.get(team);

            if (unlockedItems == null) {
                unlockedItems = new LinkedList<>();
                unlocked.put(team, unlockedItems);
            }

            unlockedItems.add(this);

            return true;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }
    }

}
