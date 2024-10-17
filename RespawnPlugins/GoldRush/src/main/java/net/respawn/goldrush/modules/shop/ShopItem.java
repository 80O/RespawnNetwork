package net.respawn.goldrush.modules.shop;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.lang.Displayable;
import lombok.Data;


@Data
public abstract class ShopItem implements Displayable {
    private final int price;


    /**
     * Gets executed whenever a player buys this item.
     *
     * @param gamePlayer The game player that buys this item
     * @return True on a successful buy, false if something went wrong
     */
    public abstract boolean onBuyItem(GamePlayer gamePlayer);

}
