package com.respawnnetwork.respawnlib.gameapi.modules.shop;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.currency.BlockCurrency;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.currency.Currency;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.items.ItemLoader;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.upgrades.UpgradeLoader;
import com.respawnnetwork.respawnlib.gameapi.states.InGameState;
import com.respawnnetwork.respawnlib.lang.Displayable;
import com.respawnnetwork.respawnlib.network.menu.InventoryMenu;
import com.respawnnetwork.respawnlib.network.messages.Message;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import org.atteo.evo.inflector.English;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a game shop.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public abstract class GameShop implements Displayable {
    /** Shop messages */
    public static final Message MESSAGE = new Message("[&4&lSHOP&r] %s");

    public static final String PREFIX = "shop.";
    public static final String ERROR = PREFIX + "error.";
    public static final String SUCCESS = PREFIX + "success.";

    public static final String MSG_NOT_AVAILABLE    = ERROR + "notAvailable";
    public static final String MSG_NOT_ENOUGH_MONEY = ERROR + "notEnoughMoney";
    public static final String MSG_NOT_ENOUGH_SPACE = ERROR + "notEnoughSpace";
    public static final String MSG_IS_EMPTY         = PREFIX + "isEmpty";
    public static final String MSG_SUCCESS          = SUCCESS + "normal";
    public static final String MSG_SUCCESS_UPGRADE  = SUCCESS+ "upgrade";

    /** The game instance */
    private final ShopModule module;

    private final Map<String, ShopLoader> loaders;
    private final List<Currency> currencies;

    private final TIntObjectMap<ShopItem> stock;

    private boolean available;

    private String name;

    private Currency currency;

    private int inventorySize;


    /**
     * Creates a new game shop.
     *
     * @param module The shop module
     */
    protected GameShop(ShopModule module) {
        this.module = module;

        this.loaders = new THashMap<>();
        this.currencies = new ArrayList<>();

        this.stock = new TIntObjectHashMap<>();

        addSupport(new ItemLoader());
        addSupport(new UpgradeLoader());
        addSupport(new BlockCurrency());
    }

    /**
     * Registers the listener for the shop.
     */
    public abstract Listener getListener();

    /**
     * Opens the shop for a specified player.
     *
     * @param gamePlayer The player object
     */
    public void openShop(final GamePlayer gamePlayer) {
        if (gamePlayer == null) {
            return;
        }

        Player player = gamePlayer.getPlayer();

        // Do nothing when the game hasn't really started yet
        if (!(getModule().getGame().getCurrentState() instanceof InGameState)) {
            return;
        }

        if (!isAvailable()) {
            MESSAGE.sendKey(player, MSG_NOT_AVAILABLE);
            return;
        }

        if (stock.isEmpty()) {
            MESSAGE.sendKey(player, MSG_IS_EMPTY);
            return;
        }

        final InventoryMenu menu = new InventoryMenu(getDisplayName(), inventorySize);

        stock.forEachEntry(new TIntObjectProcedure<ShopItem>() {
            @Override
            public boolean execute(int a, ShopItem b) {
                menu.addItem(new ShopInventoryItem(getModule().getGame(), b, gamePlayer), a);

                return true;
            }
        });

        menu.openMenu(player);
    }

    public void reset() {
        // Reset all items
        for (ShopItem item : getStock()) {
            item.reset();
        }

        setAvailable(true);
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    /**
     * Adds support for the given currency.
     *
     * @param currency The currency to add support for
     */
    public void addSupport(Currency currency) {
        currencies.add(currency);
    }

    /**
     * Adds support for the specified shop loader.
     *
     * @param loader The shop loader
     */
    public void addSupport(ShopLoader loader) {
        loaders.put(loader.getName(), loader);
    }

    /**
     * Gets the currency that is used to buy stuff in this shop.
     *
     * @return The used currency
     */
    public Currency getCurrency() {
        return currency;
    }

    /**
     * Returns the stock of this shop.
     *
     * @return The collection of items in this shop
     */
    public Collection<ShopItem> getStock() {
        return stock.valueCollection();
    }

    /**
     * Adds an item to the shop.
     *
     * @param slot The slot id
     * @param item The item to add
     */
    public void addItem(int slot, ShopItem item) {
        stock.put(slot, item);
    }

    /**
     * Indicates whether or not this shop is available.
     *
     * @return True if it's available, false if not
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Sets the available state of this shop.
     *
     * @param available True if it should be available, false if not
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * Gets the module that created this shop.
     *
     * @return The shop module
     */
    public ShopModule getModule() {
        return module;
    }

    /**
     * Loads the shop using the configuration.
     */
    public void loadConfig() {
        Logger logger = getModule().getLogger();
        ConfigurationSection config = getModule().getConfig();

        // Load shop name
        name = config.getString("name", "Shop");

        // Load currency
        String currencyName = config.getString("currency");
        if (currencyName == null) {
            logger.info("No shop currency given! Shop won't be available.");
            return;
        }

        for (Currency entry : currencies) {
            if (!entry.matches(currencyName)) {
                continue;
            }

            logger.info("Using " + entry.getDisplayName() + " as currency.");
            currency = entry;
        }

        // Stop loading when we don't have a currency
        if (currency == null) {
            logger.warning("No currency for '" + currencyName + "' found, making shop not available!");
            available = false;
            return;
        }

        // Load sections
        ConfigurationSection stockSection = config.getConfigurationSection("stock");

        if (stockSection == null) {
            logger.info("No stock config specified, shop won't have any items to sell!");

        } else {
            for (String key : stockSection.getKeys(false)) {
                ShopLoader loader = loaders.get(key);

                if (loader == null) {
                    continue;
                }

                int count = loadSection(stockSection.getConfigurationSection(key), loader);
                logger.info("Loaded " + count + ' ' + English.plural(loader.getDisplayName(), count));
            }
        }

        // Get biggest slot number and set default inventory size that way
        double max = 0;
        for (int i : stock.keys()) {
            // 'i' is the slot number, so we compare that
            if (i > max) {
                max = i;
            }
        }

        // Calc needed inventory size, plus one for the 0-index offset
        inventorySize = InventoryMenu.calcSize(max);

        // Set us to available
        available = true;
    }

    private int loadSection(ConfigurationSection section, ShopLoader loader) {
        Logger logger = module.getLogger();
        int count = 0;

        for (String key : section.getKeys(false)) {
            // Get Item slot
            int slot = getSlotId(key);

            if (slot < 0) {
                continue;
            }

            // Get section
            ConfigurationSection configurationSection = section.getConfigurationSection(key);
            if (configurationSection == null) {
                logger.warning("Invalid " + loader.getDisplayName() + " config section: '" + key + "' is not a section");
                continue;
            }

            // Load the item from the config
            ShopItem item = loader.load(this, configurationSection);
            if (item == null) {
                continue;
            }

            // Add to shop
            addItem(slot, item);

            // Count the item
            count++;
        }

        return count;
    }

    private int getSlotId(String key) {
        try {
            return Integer.parseInt(key);

        } catch (NumberFormatException ex) {
            getModule().getLogger().log(Level.WARNING, "Invalid slot ID: " + key, ex);
        }

        return -1;
    }

}
