package com.respawnnetwork.respawnlobby;

import com.respawnnetwork.respawnlib.math.MersenneTwisterFast;
import com.respawnnetwork.respawnlib.network.menu.InventoryMenu;
import com.respawnnetwork.respawnlib.network.menu.InventoryMenuItem;
import com.respawnnetwork.respawnlobby.network.Bungee;
import gnu.trove.map.hash.THashMap;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class NavigationListener extends LobbyListener {
    private final static MersenneTwisterFast RANDOM = new MersenneTwisterFast();
    private final Map<Integer, NavigationItem> navigationItems;

    private Material navigationMaterial;
    private String title;
    private int rows;


    public NavigationListener(RespawnLobby plugin) {
        super(plugin);

        navigationItems = new THashMap<>();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!player.getItemInHand().getType().equals(navigationMaterial)) {
            return;
        }

        event.setCancelled(true);

        InventoryMenu menu = new InventoryMenu(title, rows);
        for (Map.Entry<Integer, NavigationItem> entry : navigationItems.entrySet()) {
            menu.addItem(entry.getValue(), entry.getKey());
        }

        menu.openMenu(player);
    }

    public void loadConfig(ConfigurationSection config) {
        if (config == null) {
            getPlugin().getPluginLog().warning("No navigation config set, will skip navigation stuff!");
            navigationMaterial = null;
            return;
        }

        navigationMaterial = Material.matchMaterial(config.getString("material", "ender pearl"));
        title = e(config.getString("title", "RespawnNetwork"));

        // Get items
        ConfigurationSection menuCfg = config.getConfigurationSection("menu");
        int maxSlot = 0;

        if (menuCfg != null) {
            for (String slotName : menuCfg.getKeys(false)) {
                int slot;

                try {
                    slot = Integer.parseInt(slotName) - 1;

                } catch (NumberFormatException ex) {
                    getPlugin().getPluginLog().warning("Invalid compass menu slot number: " + slotName);
                    continue;
                }

                ConfigurationSection itemCfg = menuCfg.getConfigurationSection(slotName);
                if (itemCfg == null) {
                    getPlugin().getPluginLog().warning("Invalid menu slot config: " + slotName);
                    continue;
                }

                if (!itemCfg.getBoolean("enabled", true)) {
                    getPlugin().getPluginLog().info("Skipping slot " + slotName + " since it was disabled");
                    continue;
                }

                String itemName = itemCfg.getString("item");
                Material itemMaterial = Material.matchMaterial(itemName);
                if (itemMaterial == null) {
                    getPlugin().getPluginLog().warning("Invalid item for slot " + slotName + ", skipping item");
                    continue;
                }

                NavigationItem item = new NavigationItem(
                        e(itemCfg.getString("title", "Click here")),
                        itemMaterial,
                        itemCfg.getStringList("servers"));

                ItemMeta meta = item.getItemStack().getItemMeta();

                List<String> lore = itemCfg.getStringList("description");
                List<String> encLore = new LinkedList<>();

                for (String s : lore) {
                    encLore.add(e(s));
                }

                meta.setLore(encLore);
                item.getItemStack().setItemMeta(meta);

                navigationItems.put(slot, item);

                if (maxSlot < slot) {
                    maxSlot = slot;
                }
            }
        }

        rows = InventoryMenu.calcSize(maxSlot + 1);
    }

    private String e(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private class NavigationItem extends InventoryMenuItem {
        private final List<String> bungeeServers;


        private NavigationItem(String text, Material icon, List<String> bungeeServers) {
            super(text, icon);

            this.bungeeServers = bungeeServers;
        }

        @Override
        protected void onClick(Player player, boolean isRightClick, boolean isShiftClick) {
            Bungee.teleport(getPlugin(), player, bungeeServers.get(RANDOM.nextInt(bungeeServers.size())));
            getMenu().closeMenu();
        }

    }

}
