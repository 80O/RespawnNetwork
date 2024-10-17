package com.respawnnetwork.respawnlib.gameapi.modules.shop;

import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

/**
 * Represents an item shop that can be opened by right clicking on an item frame.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class ItemFrameShop extends GameShop implements Listener {

    public ItemFrameShop(ShopModule module) {
        super(module);
    }

    @Override
    public Listener getListener() {
        return this;
    }

    @Override
    public void reset() {
        getModule().unregisterListener(this);

        super.reset();
    }

    @EventHandler
    public void onClickFrame(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame)) {
            return;
        }

        event.setCancelled(true);
        openShop(getModule().getGame().getPlayer(event.getPlayer()));
    }

}
