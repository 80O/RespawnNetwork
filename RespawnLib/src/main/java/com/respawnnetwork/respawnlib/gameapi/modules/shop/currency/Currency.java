package com.respawnnetwork.respawnlib.gameapi.modules.shop.currency;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.lang.Displayable;

/**
 * Represents a currency for shop items and upgrades.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public interface Currency extends Displayable {

    /**
     * Checks if the given string matches this currency.
     *
     * @param name The name to check
     * @return True if it does, false if not
     */
    boolean matches(String name);

    /**
     * Indicates whether or not the player can afford the specified money.
     *
     * @param player The player in context
     * @param amount The amount to check
     * @return True if he can, false if not
     */
    boolean canBuy(GamePlayer player, int amount);

    /**
     * Tries to give a specified amount of money to the player.
     *
     * @param player The player in context
     * @param amount The amount to give
     * @return True if we were able to give the money
     */
    boolean give(GamePlayer player, int amount);

    /**
     * Tries to take a specified amount of money from the player.
     *
     * @param player The player in context
     * @param amount The amount to take
     * @return True if we were able to take the money
     */
    boolean take(GamePlayer player, int amount);

    /**
     * Returns a human-readable name for this currency.
     *
     * @param amount The amount to get the name for
     * @return The display name of this object depending on the
     * given amount
     * @since 1.0.1
     */
    String getDisplayName(int amount);

}
