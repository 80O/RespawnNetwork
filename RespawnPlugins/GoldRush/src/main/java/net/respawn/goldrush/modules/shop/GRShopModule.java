package net.respawn.goldrush.modules.shop;

import com.respawnnetwork.respawnlib.bukkit.Location;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.GameShop;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.ShopModule;
import com.respawnnetwork.respawnlib.gameapi.modules.team.TeamModule;
import com.respawnnetwork.respawnlib.gameapi.states.InGameState;
import com.respawnnetwork.respawnlib.lang.ParseException;
import gnu.trove.map.hash.THashMap;
import net.respawn.goldrush.GoldRush;
import net.respawn.goldrush.modules.shop.types.EffectShopType;
import net.respawn.goldrush.modules.shop.types.ItemShopType;
import net.respawn.goldrush.modules.shop.types.ShopType;
import net.respawn.goldrush.modules.shop.types.TeleportShopType;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;


public class GRShopModule extends GameModule<GoldRush> implements Listener {
    private final Map<String, ShopType> typeRegistry;
    private final Map<Location, ShopItem> stock;


    public GRShopModule(GoldRush game) {
        super(game);

        typeRegistry = new THashMap<>();
        stock = new THashMap<>();
    }

    /**
     * Adds support for the specified item type.
     *
     * @param type The item type
     */
    protected final <T extends ShopType> T addSupport(T type) {
        typeRegistry.put(type.getName(), type);

        return type;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean onEnable() {
        // Load default messages from default library shop module
        getGame().getPlugin().loadMessages(ShopModule.MESSAGES_CONFIG);

        // Add item type support
        TeamModule teamModule = getGame().getModule(TeamModule.class);

        addSupport(new ItemShopType());
        registerListener(addSupport(new TeleportShopType(teamModule, getConfig().getInt("teleportDelay", 3))));
        addSupport(new EffectShopType(teamModule));

        // Get list of shop signs
        List<Map<?, ?>> signsConfig = getConfig().getMapList("signs");
        World world = getGame().getMap().getWorld();

        if (signsConfig != null) {
            for (Map<?, ?> map : signsConfig) {
                ConfigurationSection itemCfg = new MemoryConfiguration().createSection("tmp", (Map) map);

                String typeName = itemCfg.getString("type");
                if (typeName == null) {
                    getLogger().warning("No item type given for shop sign");
                    continue;
                }

                // Get correct item type from registry
                ShopType shopType = typeRegistry.get(typeName);
                if (shopType == null) {
                    getLogger().warning("Unknown item type: " + typeName);
                    continue;
                }

                // Get sign locations
                List<Location> locations = Location.parseList(world, itemCfg.getMapList("locations"));
                if (locations.isEmpty()) {
                    getLogger().warning("No sign location specified!");
                    continue;
                }

                // Get item price
                int price = itemCfg.getInt("price");
                if (price <= 0) {
                    getLogger().warning("Price shouldn't be less or equal than zero!");
                    continue;
                }

                // Get the item from the item type
                try {
                    ShopItem item = shopType.parseConfig(getLogger(), price, itemCfg);
                    if (item == null) {
                        continue;
                    }

                    // Add to stock
                    for (Location location : locations) {
                        stock.put(new Location(
                                location.getWorld(),
                                location.getBlockX(),
                                location.getBlockY(),
                                location.getBlockZ()
                        ), item);
                    }

                } catch (ParseException ex) {
                    getLogger().log(Level.WARNING, "Could not parse shop item", ex);
                }
            }

            getLogger().info("Loaded " + stock.size() + " shop items!");

        } else {
            // Log error if we didn't get a proper config
            getLogger().info("Please provide a valid \"signs\" section in the shop config!");
        }

        // Always return true in this case ...
        return true;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignClick(PlayerInteractEvent event) {
        // check if we even have a sign
        if (!Action.RIGHT_CLICK_BLOCK.equals(event.getAction()) || !(event.getClickedBlock().getState() instanceof Sign)) {
            return;
        }

        // Check if the game is running
        if (!(getGame().getCurrentState() instanceof InGameState)) {
            return;
        }

        // Get game player and cancel action for spectators
        GamePlayer gamePlayer = getGame().getPlayer(event.getPlayer());
        if (gamePlayer == null || gamePlayer.isSpectator()) {
            return;
        }

        // Get item from stock
        ShopItem shopItem = stock.get(new Location(event.getClickedBlock().getLocation()));
        if (shopItem == null) {
            return;
        }

        // Check if he can buy the item
        if (!XPCurrency.INSTANCE.canBuy(gamePlayer, shopItem.getPrice())) {
            GameShop.MESSAGE
                    .provide("price", shopItem.getPrice())
                    .provide("currency", XPCurrency.INSTANCE.getDisplayName(shopItem.getPrice()))
                    .provide("count", 1)
                    .provide("item", shopItem.getDisplayName())
                    .sendKey(gamePlayer.getPlayer(), GameShop.MSG_NOT_ENOUGH_MONEY);
            return;
        }

        // First check and give the item
        if (shopItem.onBuyItem(gamePlayer)) {
            // Then take away the money
            XPCurrency.INSTANCE.take(gamePlayer, shopItem.getPrice());
        }
    }

    @Override
    public String getDisplayName() {
        return "GoldRush Shop";
    }

    @Override
    public String getName() {
        return "gr-shop";
    }

}
