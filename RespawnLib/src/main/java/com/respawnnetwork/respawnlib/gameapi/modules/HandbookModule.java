package com.respawnnetwork.respawnlib.gameapi.modules;

import com.respawnnetwork.respawnlib.bukkit.Item;
import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameDescriptor;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import com.respawnnetwork.respawnlib.gameapi.events.PlayerJoinGameEvent;
import com.respawnnetwork.respawnlib.utils.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a module that gives the players a handbook to read through.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class HandbookModule<G extends Game> extends GameModule<G> implements Listener {
    /** The book item we'll be giving the players */
    private ItemStack book;


    /**
     * Creates a new handbook module instance.
     *
     * @param game The game instance
     */
    public HandbookModule(G game) {
        super(game);
    }

    @Override
    protected boolean onEnable() {
        // Build main / info page
        List<String> pages = new LinkedList<>();
        GameDescriptor descriptor = getGame().getDescriptor();

        StringBuilder infoPage = new StringBuilder(ChatColor.BOLD.toString()).append(descriptor.getName()).append('\n');

        // Add creators
        if (!descriptor.getAuthors().isEmpty()) {
            infoPage.append(ChatColor.BOLD).append("\nCreated by: \n");
            infoPage.append(ChatColor.RESET).append(StringUtils.implodeProperEnglish(descriptor.getAuthors()));
            infoPage.append(ChatColor.RESET).append("\n");
        }

        // Add developers
        if (!descriptor.getDevelopers().isEmpty()) {
            infoPage.append(ChatColor.BOLD).append("\nDeveloped by: \n");
            infoPage.append(ChatColor.RESET).append(StringUtils.implodeProperEnglish(descriptor.getDevelopers()));
            infoPage.append(ChatColor.RESET).append("\n");
        }

        // Create page from string builder
        pages.add(infoPage.toString());

        // Get the user-written pages
        if (getConfig().isList("pages")) {
            pages.addAll(getConfig().getStringList("pages"));
        }

        // Create book item
        book = Item.createBook("&6-= &lHandbook / Manual&r&6 =-", "RespawnNetwork", pages);

        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinGameEvent event) {
        giveBook(event.getGamePlayer().getPlayer());
    }

    @Override
    public String getDisplayName() {
        return "Handbook";
    }

    @Override
    public String getName() {
        return "handbook";
    }


    private void giveBook(Player player) {
        PlayerInventory inventory = player.getInventory();

        // Don't re-add that, should we?
        if (!inventory.contains(book)) {
            inventory.addItem(book.clone());
        }
    }

}
