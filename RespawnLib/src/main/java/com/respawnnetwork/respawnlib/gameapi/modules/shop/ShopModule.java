package com.respawnnetwork.respawnlib.gameapi.modules.shop;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import com.respawnnetwork.respawnlib.network.menu.InventoryMenuEventListener;
import gnu.trove.map.hash.THashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Represents a shop functionality for games.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class ShopModule<G extends Game> extends GameModule<G> {
    /** The name of the messages configuration */
    public static final String MESSAGES_CONFIG = "shop.yml";

    private final Map<String, ShopConstructor> registry;

    /** The shop we're using */
    private GameShop shop;


    /**
     * Creates a new shop module instance.
     *
     * @param game The game we're running in
     */
    public ShopModule(G game) {
        super(game);

        this.registry = new THashMap<>();

        // Add default shop types
        addSupport("sign", new ShopConstructor() {
            @Override
            public GameShop createShop(ShopModule<?> module) {
                return new SignShop(module);
            }
        });

        addSupport("frame", new ShopConstructor() {
            @Override
            public GameShop createShop(ShopModule<?> module) {
                return new ItemFrameShop(module);
            }
        });
    }

    @Override
    protected boolean onEnable() {
        // Register listener if needed
        if (!getGame().getPlugin().usesInventoryMenuAPI()) {
            registerListener(new InventoryMenuEventListener());
        }

        // Load shop messages
        getGame().getPlugin().loadMessages(MESSAGES_CONFIG);

        // Get correct shop type
        String type = getConfig().getString("type", "sign");
        ShopConstructor constructor = registry.get(type);

        if (constructor == null) {
            getLogger().warning("Unknown shop type specified: " + type);
            return false;
        }

        shop = constructor.createShop(this);

        if (shop != null) {
            // Add custom supports
            addShopSupports(shop);

            // Load config
            shop.loadConfig();

            // Register shop-specific listener
            registerListener(shop.getListener());
        }

        return true;
    }

    /**
     * A method to add custom shop functionality to the shop.
     */
    protected void addShopSupports(GameShop shop) {
        // Nothing for the base class
    }

    @Override
    protected void onDisable() {
        // Reset the shop
        if (getShop() != null) {
            getShop().reset();
        }
    }

    protected final void addSupport(String name, ShopConstructor shopConstructor) {
        registry.put(name, shopConstructor);
    }

    @Override
    public String getDisplayName() {
        return "Shop";
    }

    @Override
    public String getName() {
        return "shop";
    }

    /**
     * Returns the shop we're using.
     *
     * @return The shop instance
     */
    @Nullable
    public GameShop getShop() {
        return shop;
    }


    protected static interface ShopConstructor {
        GameShop createShop(ShopModule<?> module);
    }

}
