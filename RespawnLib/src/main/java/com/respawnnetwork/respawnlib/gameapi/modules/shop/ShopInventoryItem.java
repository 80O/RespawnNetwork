package com.respawnnetwork.respawnlib.gameapi.modules.shop;

import com.respawnnetwork.respawnlib.bukkit.Item;
import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.currency.Currency;
import com.respawnnetwork.respawnlib.network.menu.InventoryMenuItem;
import org.bukkit.entity.Player;


class ShopInventoryItem extends InventoryMenuItem {
    private final Game game;
    private final ShopItem item;

    private final String displayName;


    ShopInventoryItem(Game game, ShopItem item, GamePlayer player) {
        super(item.getDisplayItem(player));

        this.game = game;
        this.item = item;

        this.displayName = Item.getHumanReadableName(getItemStack());
    }

    @Override
    protected void onClick(Player player, boolean isRightClick, boolean isShiftClick) {
        GamePlayer gamePlayer = game.getPlayer(player);

        // Skip non-game-players
        if (gamePlayer == null) {
            return;
        }

        Currency currency = item.getShop().getCurrency();

        // Check if the player has la monata
        int price = item.getPrice(gamePlayer);

        if (!currency.canBuy(gamePlayer, price)) {
            GameShop.MESSAGE
                    .provide("price", price)
                    .provide("currency", item.getShop().getCurrency().getDisplayName(price))
                    .provide("count", getAmount())
                    .provide("item", displayName)
                    .sendKey(player, GameShop.MSG_NOT_ENOUGH_MONEY);
            return;
        }

        // Buy the item
        if (item.onBuyItem(gamePlayer)) {
            // Take the money
            currency.take(gamePlayer, price);

            // Update the menu
            setItemStack(item.getDisplayItem(gamePlayer));
        }
    }

}
