package com.respawnnetwork.respawnlib.gameapi.modules.shop;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Represents a shop opened by sign clicks.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class SignShop extends GameShop implements Listener {
    public static final String SIGN_LABEL = "[Shop]";


    public SignShop(ShopModule module) {
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
    public void onClickSignEvent(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();

        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();

            if (!sign.getLine(0).contains(SIGN_LABEL)) {
                return;
            }

            event.setCancelled(true);
            openShop(getModule().getGame().getPlayer(event.getPlayer()));
        }
    }

}
