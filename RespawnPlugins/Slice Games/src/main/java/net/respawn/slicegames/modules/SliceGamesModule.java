package net.respawn.slicegames.modules;

import com.respawnnetwork.respawnlib.bukkit.Item;
import com.respawnnetwork.respawnlib.bukkit.Location;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import com.respawnnetwork.respawnlib.gameapi.GameState;
import com.respawnnetwork.respawnlib.gameapi.events.PlayerJoinGameEvent;
import com.respawnnetwork.respawnlib.gameapi.states.InGameState;
import com.respawnnetwork.respawnlib.gameapi.statistics.PlayerStatistics;
import com.respawnnetwork.respawnlib.gameapi.statistics.Statistic;
import com.respawnnetwork.respawnlib.math.MersenneTwisterFast;
import com.respawnnetwork.respawnlib.network.menu.InventoryMenu;
import com.respawnnetwork.respawnlib.network.menu.InventoryMenuItem;
import com.respawnnetwork.respawnlib.network.messages.Message;
import gnu.trove.map.hash.THashMap;
import lombok.Data;
import lombok.Getter;
import net.respawn.slicegames.Loot;
import net.respawn.slicegames.SGPlayer;
import net.respawn.slicegames.SliceGames;
import net.respawn.slicegames.states.DeathMatchCountdown;
import net.respawn.slicegames.states.DeathMatchState;
import net.respawn.slicegames.states.InSGState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Furnace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents a core mechanics module for Slice games.
 *
 * @author spaceemotion
 * @author TomShar
 * @version 1.0
 */
public class SliceGamesModule extends GameModule<SliceGames> implements Listener {
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("mm:ss");
    public static final MersenneTwisterFast RANDOM = new MersenneTwisterFast();

    /** The collection holding all loot items */
    private final List<Loot> loot;

    /** The locations for podiums for the tributes */
    @Getter
    private final List<Location> podiums;

    /** The list containing all random chest attributes */
    private final List<String> chestAttributes;

    /** The list containing all random chest types */
    private final List<String> chestTypes;

    /** A collection holding all inventory holders */
    private final Map<InventoryHolder, SGInventory> inventories;

    @Getter
    private Location deathMatchCenter;

    /** The total weight, dunno, ask Tom about this ... */
    private double totalWeight;

    /** The number of players in a death match */
    private int playersInDeathMatch;

    /** The number of maximum random items per inventory (includes chests, brewing stands, ...) */
    private int randomItemsPerInventory;


    public SliceGamesModule(SliceGames game) {
        super(game);

        loot = new LinkedList<>();
        podiums = new LinkedList<>();
        chestAttributes = new LinkedList<>();
        chestTypes = new LinkedList<>();

        inventories = new THashMap<>();
    }

    /**
     * Refills all chests with loot in this map.
     *
     * @param announceRefill True if we should announce the refill
     */
    public void refillInventories(boolean announceRefill) {
        // Only make the announcement when needed
        if (announceRefill) {
            Message.ANNOUNCE.sendKey("sg.chestsRefilled");
        }

        // Just clear the list
        inventories.clear();
    }

    @Override
    protected boolean onEnable() {
        randomItemsPerInventory = getConfig().getInt("randomItemsPerInventory", 5);
        playersInDeathMatch = getConfig().getInt("playersInDeathMatch", 3);

        // Get death match center
        ConfigurationSection centerSection = getConfig().getConfigurationSection("deathMatchCenter");

        if (centerSection != null) {
            deathMatchCenter = new Location(getGame().getMap().getWorld(), centerSection.getValues(false));
        }

        // Get random chest properties
        chestAttributes.addAll(getConfig().getStringList("chestTitles.attributes"));
        chestTypes.addAll(getConfig().getStringList("chestTitles.types"));

        // Add defaults if the list was empty
        if (chestAttributes.isEmpty()) {
            chestAttributes.add("Treasure");
        }

        if (chestTypes.isEmpty()) {
            chestTypes.add("Chest");
        }

        // Get loot
        List<Map<?, ?>> lootList = getConfig().getMapList("loot");
        if (lootList == null) {
            getLogger().warning("No loot specified, game might not work properly!");

        } else {
            for (Map<?, ?> map : lootList) {
                ConfigurationSection lootCfg = new MemoryConfiguration().createSection("tmp", map);

                String materialName = lootCfg.getString("material", "");
                Material material = Material.matchMaterial(materialName);

                if (material == null) {
                    getLogger().info("No material with name " + materialName + " found!");
                    continue;
                }

                // Create entry
                loot.add(new Loot(
                        material,
                        Math.min(lootCfg.getInt("min"), 0),
                        Math.min(lootCfg.getInt("max", 64), material.getMaxStackSize()),
                        lootCfg.getDouble("weight", 1)
                ));
            }
        }

        // Calculate total "weight"
        totalWeight = 0.0d;

        for (Loot entry : loot) {
            totalWeight += entry.getWeight();
        }

        // Load podiums
        List<Map<?, ?>> mapList = getConfig().getMapList("podiums");
        if (mapList == null) {
            getLogger().severe("No podiums specified, will not be able to use map");
            return false;
        }

        podiums.addAll(Location.parseList(getGame().getMap().getWorld(), mapList));

        return true;
    }

    @Override
    public String getDisplayName() {
        return "Slice Games";
    }

    @Override
    public String getName() {
        return "slice-games";
    }

    // -------------------------------- ENTITY MOVEMENT --------------------------------

    @EventHandler
    public void onEntityThrow(ProjectileLaunchEvent e) {
        if(e.getEntity() instanceof ThrownExpBottle) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();

        // Do not cancel other reasons like plugin, ...
        if(e.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }

        // Only cancel them in death matches
        if(getGame().getCurrentState() instanceof DeathMatchState && getGame().getPlayer(player) != null) {
            e.setCancelled(true);
        }
    }

    // -------------------------------- PLAYER SPECIFIC STUFF --------------------------------

    @EventHandler
    public void onPlayerRegen(EntityRegainHealthEvent event) {
        Entity entity = event.getEntity();
        if(!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        double maxHealth = player.getMaxHealth();
        double total = player.getHealth() + event.getAmount();
        double rest = (maxHealth / 2) - total;

        // Clamp the amount to half of the maximum health
        if (rest < 0) {
            event.setAmount(Math.max(event.getAmount() - Math.abs(rest), 0));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        // Don't do stuff when the damage is zero
        if (e.getDamage() <= 0) {
            return;
        }

        // There is a spectator check in the game engine already, so we just need to check
        // for the state here
        if(!(getGame().getCurrentState() instanceof InGameState)) {
            e.setCancelled(true);
            return;
        }

        // Increase damage for block explosion
        if(e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            e.setDamage(e.getDamage() * 1.3d);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Make sure it is a right-click action
        if (item == null || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
            return;
        }

        // Give custom experience
        if (Material.EXP_BOTTLE.equals(item.getType())) {
            player.giveExp(RANDOM.nextInt(10) + 5);
            return;
        }

        // Get player and do nothing if hes not a spectator
        SGPlayer sgPlayer = getGame().getPlayer(player);
        if (sgPlayer == null) {
            return;
        }

        if (Material.SPECKLED_MELON.equals(item.getType())) {
            // Remove item
            player.getInventory().removeItem(new ItemStack(Material.SPECKLED_MELON, 1));

            // Increase statistic
            sgPlayer.getStatistics().increase(SliceGames.MELONS_EATEN);

            // "You gained half a heart."
            Message.INFO.sendKey(player, "sg.melonEaten");

            // We manually set this cause this is one of the only ways to get more health
            player.setHealth(Math.min(player.getHealth() + 1d, player.getMaxHealth()));
            return;
        }

        // Handle compass navigation
        if (sgPlayer.isSpectator() && Material.COMPASS.equals(item.getType())) {
            InventoryMenu menu = new InventoryMenu("Select player", InventoryMenu.calcSize(getGame().getNumberOfRealPlayers()));

            int i = 0;
            for (SGPlayer realPlayer : getGame().getRealPlayers()) {
                final Player bukkitPlayer = realPlayer.getPlayer();
                if (bukkitPlayer == null) {
                    continue;
                }

                // Create item
                ItemStack skullItem = Item.getFor(Material.PAPER, ChatColor.GREEN + realPlayer.getName());
                Item.setDescription(skullItem,
                        ChatColor.RED + "Hearts: " + Math.round(bukkitPlayer.getHealth()),
                        ChatColor.GOLD + "Kills: " + (int)realPlayer.getStatistics().get(Statistic.KILLS)
                );

                // Add inventory item
                menu.addItem(new InventoryMenuItem(skullItem) {
                    @Override
                    protected void onClick(Player player, boolean rightClick, boolean shiftClick) {
                        player.teleport(bukkitPlayer);
                    }
                }, i++);
            }

            menu.openMenu(player);
        }
    }

    // -------------------------------- JOIN, QUITS, DEATHS & RESPAWNS --------------------------------

    @EventHandler
    public void onJoin(PlayerJoinGameEvent event) {
        if (!(event.getGamePlayer() instanceof SGPlayer)) {
            return;
        }

        SGPlayer player = (SGPlayer) event.getGamePlayer();

        // Make sure the player is setup correctly
        player.reset();
        player.setSpectator(true);
        player.giveCompass();

        teleportToGame(player);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if(!(getGame().getCurrentState() instanceof InGameState)) {return;}

        Player player = e.getPlayer();
        SGPlayer sgPlayer = getGame().getPlayer(player);

        if (sgPlayer != null) {
            if (!sgPlayer.isSpectator()) {
                handlePlayerDeath(sgPlayer, player, true);
            }

            setTimeOfDeath(sgPlayer);

            getGame().updateDatabase(sgPlayer);
            getGame().removePlayer(sgPlayer);
        }

        if(getGame().getCurrentState() instanceof InSGState) {
            checkGameEnd();
        }

        if(getGame().getCurrentState() instanceof DeathMatchCountdown) {
            checkGameEnd();
        }

        if(getGame().getCurrentState() instanceof DeathMatchState) {
            checkGameEnd();
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent e) {
        if(getGame().getCurrentState() instanceof InSGState) {
            checkGameEnd();
        }

        if(getGame().getCurrentState() instanceof DeathMatchCountdown) {
            checkGameEnd();
        }

        if(getGame().getCurrentState() instanceof DeathMatchState) {
            checkGameEnd();
        }
    }

    private void checkGameEnd() {
        // Otherwise just go to the next state
        if (getGame().getNumberOfRealPlayers() <= 1) {
            getGame().nextState();
        }
    }

    @EventHandler
    public void playerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Player killerPlayer = player.getKiller();

        SGPlayer dead = getGame().getPlayer(player);
        SGPlayer killer = getGame().getPlayer(killerPlayer);

        if (dead == null) {
            return;
        }

        // Set time of death for player
        setTimeOfDeath(dead);
        // dead.setTimeOfDeath(new Date(System.currentTimeMillis()));

        Timestamp timeAlive = new Timestamp(dead.getTimeOfDeath().getTime() - getGame().getMap().getStartTime().getTime());
        String afterTime = TIME_FORMAT.format(timeAlive);

        if(killer != null) {
            PlayerStatistics killerStats = killer.getStatistics();
            double kills = killerStats.increase(Statistic.KILLS);

            e.setDeathMessage(Message.ANNOUNCE
                    .provide("player", dead.getName())
                    .provide("killer", killer.getName())
                    .provide("time", afterTime)
                    .parseKey("sg.death"));

            double amount = Math.min(kills, 3);
            killerPlayer.setHealth(Math.min(killerPlayer.getHealth() + (amount * 2), killerPlayer.getMaxHealth()));
            Message.CUSTOM.provide("health", amount).sendKey(killerPlayer, "sg.regainedHealth");

        } else {
            // Append how long they lasted.
            e.setDeathMessage(e.getDeathMessage() + " (after " + afterTime + ")");
        }

        // Handle player death
        for (ItemStack drop : e.getDrops()) {
            if(drop.getType() == Material.COMPASS) {continue;}
            player.getWorld().dropItemNaturally(player.getLocation(), drop);
        }

        // Clear drops
        e.getDrops().clear();

        handlePlayerDeath(dead, player, false);
    }

    // -------------------------------- INVENTORY ACTIONS --------------------------------

    @EventHandler
    public void dropItem(PlayerDropItemEvent e) {
        cancelEvent(e.getPlayer(), e);
    }

    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent e) {
        cancelEvent(e.getPlayer(), e);
    }

    @EventHandler
    public void onPlayerEntityInteract(PlayerInteractEntityEvent e) {
        Player clicker = e.getPlayer();
        Entity clickedEntity = e.getRightClicked();

        // Ensure this is a player we clicked
        if(!(clickedEntity instanceof Player)) {
            return;
        }

        Player clicked = (Player) clickedEntity;

        // Show the clicker the clicked players inventory if the clicked player is not a spectator as well
        if(getGame().isSpectator(clicker) && !getGame().isSpectator(clicked)) {
            InventoryMenu inv = new InventoryMenu(clicked.getName(), clicked.getInventory().getSize() / InventoryMenu.ROW_SIZE);
            inv.getInventory().setContents(clicked.getInventory().getContents());

            inv.openMenu(clicker);
        }
    }

    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent event){
        if (event.isCancelled() || !(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = ((Player) event.getPlayer());
        InventoryHolder inventoryHolder = event.getInventory().getHolder();

        // Don't do stuff to inventory menus and player inventories
        if (inventoryHolder == null || inventoryHolder instanceof Player || inventoryHolder instanceof InventoryMenu) {
            return;
        }

        boolean isChest = (inventoryHolder instanceof Chest || inventoryHolder instanceof DoubleChest);
        SGInventory info = inventories.get(inventoryHolder);

        // Refill inventory if we haven't already
        if (info == null) {
            // Double chests are "bugged", since they seem to create a new holder each time again.
            // Just "lock" them for now
            if (inventoryHolder instanceof DoubleChest) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.DOOR_CLOSE, 1, 1);
                Message.CUSTOM.sendKey(player, "sg.chestLocked");
                return;

            } else {
                // Normally refill inventories
                info = refillInventory(inventoryHolder, isChest);
            }
        }

        if (isChest && info != null) {
            // Always cancel chests, since we're using a custom inventory for them
            event.setCancelled(true);

        } else {
            // Do not go further if we don't have a chest
            return;
        }

        // Create custom inventory
        player.openInventory(info.getInventory());

        // Why do I even need to do this ... *sighs* ...
        // ((Player) event.getPlayer()).updateInventory();

        // Play chest sound when necessary
        if (inventoryHolder instanceof Chest && !getGame().isSpectator(player)) {
            player.getWorld().playSound(((Chest) inventoryHolder).getLocation(), Sound.CHEST_OPEN, 1, 1);
        }
    }

    private SGInventory refillInventory(InventoryHolder inventoryHolder, boolean isChest) {
        if (inventoryHolder == null) {
            return null;
        }

        // We inline this, since the same instance cant be shared over different threads.
        MersenneTwisterFast random = new MersenneTwisterFast();

        // Create custom inventory before generating items
        Inventory inventory;

        if (isChest) {
            // Create random chest title
            String attribute = chestAttributes.get(random.nextInt(chestAttributes.size()));
            String type = chestTypes.get(random.nextInt(chestTypes.size()));

            // Get randomized inventory Size
            int invSize = (random.nextInt(4) + 1) * InventoryMenu.ROW_SIZE;

            // Create and assign bukkit inventory
            inventory = Bukkit.createInventory(null, invSize, attribute + " " + type);

        } else {
            inventory = inventoryHolder.getInventory();
        }

        // Add to inventory list
        SGInventory sgInventory = new SGInventory(inventory);
        inventories.put(inventoryHolder, sgInventory);

        // Generate random amount of items
        for (int n = 0, max = Math.min(random.nextInt(randomItemsPerInventory) + 1, inventory.getSize()); n < max; n++) {
            // Handle furnace separately, only fill in the fuel
            if (inventoryHolder instanceof Furnace) {
                FurnaceInventory furnace = ((Furnace) inventoryHolder).getInventory();
                if (furnace.getFuel() == null) {
                    furnace.setFuel(new ItemStack(Material.COAL));
                }

                continue;
            }

            // Calculate weight magic
            double r = random.nextFloat() * totalWeight;
            int randomIndex = -1;

            for (int i = 0; i < loot.size(); ++i) {
                r -= loot.get(i).getWeight();

                if (r <= 0d){
                    randomIndex = i;
                    break;
                }
            }

            // Get random loot
            Loot randLoot = loot.get(randomIndex);
            ItemStack item = new ItemStack(randLoot.getItem(), 1 + random.nextInt(randLoot.getMax()) + randLoot.getMin());

            // Get random, not occupied slot
            int slot = -1;

            while (slot < 0 || inventory.getItem(slot) != null) {
                slot = random.nextInt(inventory.getSize());
            }

            // Normal inventory (also contains droppers, hoppers, minecarts ...)
            inventory.setItem(slot, item);
        }

        return sgInventory;
    }

    /**
     * Cancels the event only if the player could not be found or is a spectator.
     *
     * @param player The player to check
     * @param event The event to cancel
     */
    private void cancelEvent(Player player, Cancellable event) {
        if (getGame().isSpectator(player)) {
            event.setCancelled(true);
        }
    }

    private void setTimeOfDeath(SGPlayer player) {
        player.setTimeOfDeath(new Date(System.currentTimeMillis()));
    }

    private void handlePlayerDeath(final SGPlayer sgPlayer, Player player, boolean leave) {
        GameState currentState = getGame().getCurrentState();

        if (!leave) {
            // Lighting effect (no damage).
            player.getWorld().strikeLightningEffect(player.getLocation());

            // Make the dead a flying zombie
            sgPlayer.reset();
            sgPlayer.setSpectator(true);

            teleportToGame(sgPlayer);

            new BukkitRunnable() {
                @Override
                public void run() {
                    sgPlayer.giveCompass();
                }
            }.runTaskLater(getGame().getPlugin(), 20);
        }

        // Get how many "real" people are left
        int remaining = getGame().getNumberOfRealPlayers();

        if (remaining > playersInDeathMatch) {
            getGame().createMessage().provide("count", remaining - playersInDeathMatch).sendKey("sg.tilDeathMatch");

        } else if (currentState instanceof InSGState) {
            // Go to death match when only x tributes are remaining
            getGame().nextState();
        }

        if (remaining > 1) {
            getGame().createMessage().provide("count", remaining).sendKey("sg.tributesRemaining");

        } else {
            // Announce last player as winner
            SGPlayer winner = getGame().getRealPlayers()[0];
            winner.setTimeOfDeath(new Date());

            getGame().createMessage().provide("player", winner.getName()).sendKey("sg.winner");

            getGame().getMap().setWinner(winner);
            getGame().nextState();
        }
    }

    private void teleportToGame(SGPlayer player) {
        GameState currentState = getGame().getCurrentState();

        if (currentState instanceof DeathMatchState || currentState instanceof DeathMatchCountdown) {
            player.teleportTo(deathMatchCenter);

        } else {
            player.teleportTo(getGame().getMap().getSpawnLocation());
        }
    }


    /**
     * Represents a custom slice games inventory holder.
     */
    @Data
    private static class SGInventory implements InventoryHolder {
        private final Inventory inventory;
    }

}
