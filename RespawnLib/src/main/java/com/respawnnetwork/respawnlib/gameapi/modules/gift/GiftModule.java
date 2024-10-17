package com.respawnnetwork.respawnlib.gameapi.modules.gift;

import com.respawnnetwork.respawnlib.bukkit.Item;
import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.events.EndGameEvent;
import com.respawnnetwork.respawnlib.gameapi.events.StateChangeEvent;
import com.respawnnetwork.respawnlib.gameapi.states.InGameState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Represents a module that adds items to player inventories based on a configurable frequency.
 * <p />
 * Example of a gift configuration:
 * <pre>
 *     gifts:
 *       20:
 *         items:
 *         - sandstone
 *       1200:
 *         items:
 *         - tnt
 *         - wooden sword
 *         -
 *           material: iron sword
 *           enchantments:
 *           - knockback 2
 * </pre>
 *
 * @author spaceemotion
 * @version 1.0.1
 */
public class GiftModule<G extends Game> extends GameModule<G> implements Listener {
    public static final String KEY_IMMEDIATELY = "immediately";
    public static final String KEY_ITEMS = "items";

    /** The list of gifts to give */
    @Getter
    private final List<Gift> gifts;

    /** The list of registered tasks */
    private final List<GiftTask> tasks;


    /**
     * Creates a new gift module instance.
     *
     * @param game The game instance
     */
    public GiftModule(G game) {
        super(game);

        this.gifts = new ArrayList<>();
        this.tasks = new ArrayList<>();
    }

    @Override
    protected boolean onEnable() {
        // Walk over config list
        for (String key : getConfig().getValues(false).keySet()) {
            int frequency;

            try {
                frequency = Integer.parseInt(key);

            } catch (NumberFormatException ex) {
                getLogger().log(Level.WARNING, "Invalid gift config section: " + key, ex);
                continue;
            }

            // Get section
            ConfigurationSection section = getConfig().getConfigurationSection(key);

            if (section == null) {
                getLogger().warning(key + " is not a configuration section!");
                continue;
            }

            // Immediately gifts will have no start timer
            boolean immediately = section.getBoolean(KEY_IMMEDIATELY, false);

            // Gets and parses the items we give the players
            List<ItemStack> items = new ArrayList<>();

            if (!Item.parseInventory(getLogger(), section.getList(KEY_ITEMS), items)) {
                getLogger().warning("There were problems with your gift configuration! Please fix and restart.");
            }

            // Add gift to list
            Gift gift = new Gift(frequency, items);
            gift.setImmediately(immediately);
            getGifts().add(gift);
        }

        return true;
    }

    @EventHandler
    public void onStateChange(StateChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getNext() instanceof InGameState) {
            for (Gift gift : getGifts()) {
                // Schedule SYNCHRONOUS task since async ones should never access the API
                GiftTask task = new GiftTask(getGame(), gift);
                tasks.add(task);

                task.runTaskTimer(getGame().getPlugin(),
                        gift.isImmediately() ? 0 : gift.getFrequency(),
                        gift.getFrequency()
                );
            }
        }

        if (event.getPrevious() instanceof InGameState) {
            cancelAll();
        }
    }

    @EventHandler
    public void onGameEnd(EndGameEvent event) {
        cancelAll();
    }

    @Override
    protected void onDisable() {
        cancelAll();
    }

    @Override
    public String getDisplayName() {
        return "Gift";
    }

    @Override
    public String getName() {
        return "gift";
    }

    private void cancelAll() {
        for(BukkitRunnable task : tasks) {
            try {
                task.cancel();

            } catch (IllegalStateException ex) {
                getLogger().log(Level.WARNING, "Could not cancel gift task", ex);
            }
        }

        tasks.clear();
    }

    @AllArgsConstructor
    private static class GiftTask extends BukkitRunnable {
        private final Game game;
        private final Gift gift;


        @Override
        @SuppressWarnings("unchecked")
        public void run() {
            for (GamePlayer gamePlayer : game.getRealPlayers()) {
                Player player = gamePlayer.getPlayer();

                if (player == null) {
                    continue;
                }

                for (ItemStack item : gift.getItems()) {
                    player.getInventory().addItem(item.clone());
                }
            }
        }

    }

}
