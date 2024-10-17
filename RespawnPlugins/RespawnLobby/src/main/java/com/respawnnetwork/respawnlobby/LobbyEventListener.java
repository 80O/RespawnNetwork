package com.respawnnetwork.respawnlobby;

import com.respawnnetwork.respawnlib.bukkit.Item;
import com.respawnnetwork.respawnlib.network.database.Database;
import com.respawnnetwork.respawnlib.network.messages.Message;
import com.respawnnetwork.respawnlib.network.tokens.Tokens;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.jooq.DSLContext;

import java.util.LinkedList;
import java.util.List;

import static com.respawnnetwork.respawnlib.network.database.generated.Tables.USERS;

/**
 * Full event handler / listener for the lobby plugin.
 *
 * @author spaceemotion
 * @author SonarBerserk
 * @author TomShar
 * @version 1.0.1
 */
public class LobbyEventListener extends LobbyListener {
    /** The token instance */
    private final Tokens tokens;

    /** The list of players that wants to hide others */
    private final List<Player> hidePlayersEnabled;


    public LobbyEventListener(RespawnLobby plugin) {
        super(plugin);

        this.hidePlayersEnabled = new LinkedList<>();

        Database db = plugin.getDatabaseManager();

        if (db != null) {
            tokens = new Tokens(db);

        } else {
            plugin.getPluginLog().info("Could not get database, will not be able to use tokens");
            tokens = null;
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // Cancel join message
        e.setJoinMessage(null);

        // Get player information
        Player p = e.getPlayer();
        String ip = p.getAddress().getAddress().getHostAddress();

        // Teleport to spawn location
        p.teleport(getPlugin().getSpawnLocation());
        p.setFallDistance(0f);
        p.getInventory().clear();
        p.setWalkSpeed(getPlugin().getWalkSpeed());

        // Send join messages
        for(String line : getPlugin().getWelcomeMessages()) {
            Message.CUSTOM.send(p, line);
        }

        // Update database if specified in config
        String uuid = p.getUniqueId().toString().replace("-", "");

        if (getPlugin().isUpdateUUIDs()) {
            Database db = getPlugin().getDatabaseManager();

            if (db == null) {
                getPlugin().getPluginLog().warning("Couldn't get a database instance.");

            } else {
                if (!db.connected()) {
                    db.open();
                }

                DSLContext ctx = db.getContext();

                ctx.insertInto(USERS).set(USERS.UUID, uuid)
                        .set(USERS.LAST_SEEN_USERNAME, p.getName())
                        .set(USERS.LAST_SEEN_IP, ip)
                        .onDuplicateKeyUpdate()
                        .set(USERS.LAST_SEEN_USERNAME, p.getName())
                        .set(USERS.LAST_SEEN_IP, ip)
                        .execute();
            }
        }

        // Get tokens
        int softTokens = tokens.getSoft(uuid);

        // Give the player the lobby items
        ItemStack pearl = Item.getFor(Material.ENDER_PEARL, "§6§lGame Selection");
        pearl.addUnsafeEnchantment(Enchantment.LUCK, 1);
        p.getInventory().setItem(0, pearl);

        ItemStack hider = Item.getFor(Material.NAME_TAG, "§6§lHide players");
        hider.addUnsafeEnchantment(Enchantment.LUCK, 1);
        p.getInventory().setItem(7, hider);

        ItemStack diamond = Item.getFor(Material.DIAMOND, String.format("§6§lYou have %d diamonds.", softTokens));
        diamond.addUnsafeEnchantment(Enchantment.LUCK, 1);
        p.getInventory().setItem(8, diamond);

        // Hide newly joined player
        for (Player player : hidePlayersEnabled) {
            player.hidePlayer(e.getPlayer());
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof ItemFrame) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {

        Player player = e.getPlayer();

        // Always cancel event if player is not in creative
        boolean isCreative = player.getGameMode().equals(GameMode.CREATIVE);

        if (!isCreative) {
            cancelEvent(e);
        }

        // Handle show/hide players
        if (player.getItemInHand().getType() == Material.NAME_TAG) {
            if (getPlugin().getCooldownsTask().isOnCooldown(player.getUniqueId())) {
                Message.INFO.send(player, "Please wait some time before using that item again!");
                return;
            }

            if (hidePlayersEnabled.contains(player)) {
                Item.setDisplayName(player.getItemInHand(), "§6§lHide players");
                hidePlayersEnabled.remove(player);

            } else {
                Item.setDisplayName(player.getItemInHand(), "§6§lShow players");
                hidePlayersEnabled.add(player);
            }

            refreshVision(player);
            getPlugin().getCooldownsTask().addUUID(player.getUniqueId());
            return;
        }

        if (e.getClickedBlock() == null) {
            return;
        }

        if (e.getClickedBlock().getType() != Material.SIGN_POST && e.getClickedBlock().getType() != Material.SIGN && e.getClickedBlock().getType() != Material.WALL_SIGN) {
            return;
        }

        // Get sign from clicked block
        Sign sign = getPlugin().getSign(e.getClickedBlock());
        if (sign == null) {
            return;
        }

        // Check if we're right clicking
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        for(GameServer gameServer : getPlugin().getGameServers()) {
            if (!gameServer.getSigns().contains(sign)) {
                continue;
            }

            if (gameServer.getOnline() == gameServer.getMax()) {
                Message.INFO.send(player, "This server is full, please try again later!");
                return;
            }

            // Do the creative check on on game server signs
            if (isCreative) {
                Message.WARNING.send(player, "Please switch to survival or adventure mode to use signs.");
                return;
            }

            if (gameServer.isPremiumOnly() && !player.hasPermission("respawn.premium")) {
                Message.INFO.sendKey(player, "lobby.premiumOnly");
                return;
            }

            if (gameServer.isInvestorOnly() && !player.hasPermission("respawn.investor")) {
                Message.INFO.sendKey(player, "lobby.investorOnly");
                return;
            }

            if (gameServer.isVipOnly() && !player.hasPermission("respawn.vip")) {
                Message.INFO.sendKey(player, "lobby.vipOnly");
                return;
            }

            if (gameServer.isDisabled()) {
                Message.INFO.sendKey(e.getPlayer(), "lobby.disabled");
                return;
            }

            if (gameServer.isOffline()) {
                Message.INFO.sendKey(e.getPlayer(), "lobby.offline");
                return;
            }

            // Teleport player
            gameServer.teleport(getPlugin(), player);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo().getY() < 0) {
            event.getPlayer().teleport(getPlugin().getSpawnLocation());
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        checkBuildingPermissions(e.getPlayer(), e);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        checkBuildingPermissions(e.getPlayer(), e);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        hidePlayersEnabled.remove(e.getPlayer());
        e.setQuitMessage(null);
    }

    @EventHandler
    public void onItemMove(InventoryDragEvent e) {
        cancelEvent(e);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        cancelEvent(e);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        cancelEvent(e);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {
        cancelEvent(e);
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        cancelEvent(e);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        cancelEvent(e);
    }

    @EventHandler
    public void onTarget(EntityTargetEvent e) {
        cancelEvent(e);
    }

    private void checkBuildingPermissions(Player player, Cancellable e) {
        if (e.isCancelled() || player == null) {
            return;
        }

        if (player.hasPermission("respawn.building") && (player.getGameMode() == GameMode.CREATIVE)) {
            return;
        }

        cancelEvent(e);
    }

    private void cancelEvent(Cancellable event) {
        event.setCancelled(true);
    }

    private void refreshVision(Player player) {
        boolean isBlind = hidePlayersEnabled.contains(player);

        for (Player onlinePlayer : player.getServer().getOnlinePlayers()) {
            if (player == onlinePlayer) {
                continue;
            }

            if (isBlind) {
                player.hidePlayer(onlinePlayer);

            } else {
                player.showPlayer(onlinePlayer);
            }
        }
    }

}
