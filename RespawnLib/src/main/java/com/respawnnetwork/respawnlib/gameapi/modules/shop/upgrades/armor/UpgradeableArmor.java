package com.respawnnetwork.respawnlib.gameapi.modules.shop.upgrades.armor;

import com.respawnnetwork.respawnlib.bukkit.Item;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.GameShop;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.upgrades.UpgradeTypeLoader;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.upgrades.UpgradeableShopItem;
import gnu.trove.map.hash.THashMap;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Represents a purchasable armor upgrade.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class UpgradeableArmor extends UpgradeableShopItem<ArmorUpgrade> {

    public UpgradeableArmor(GameShop shop, UpgradeTypeLoader type) {
        super(shop, type);
    }


    public static class Loader implements UpgradeTypeLoader<ArmorUpgrade, UpgradeableArmor> {
        private static final String ENCHANTMENTS = "enchantments";

        @Nullable
        @Override
        public UpgradeableArmor createShopItem(GameShop shop, ConfigurationSection config) {
            return new UpgradeableArmor(shop, this);
        }

        @Override
        public ArmorUpgrade loadUpgrade(UpgradeableArmor shop, ArmorUpgrade prev, int price, ConfigurationSection config) {
            Logger logger = shop.getShop().getModule().getLogger();

            ArmorUpgrade upgrade = null;
            ItemStack icon = null;

            if (config.isSet("icon")) {
                // Load custom icon if set
                icon = Item.parseItem(logger, config.get("icon"));
            }

            if (config.isList(ENCHANTMENTS)) {
                // Okay we have a list of enchants, so its a full armor upgrade
                Map<Enchantment, Integer> enchantments = Item.parseEnchantments(logger, config.getStringList(ENCHANTMENTS));

                // Build icon
                if (icon == null) {
                    icon = buildIcon(shop, true, FullArmorUpgrade.SLOTS, enchantments, config);
                }

                // Create upgrade and add all enchantments
                upgrade = new FullArmorUpgrade(prev, icon, price);
                upgrade.getEnchantments().putAll(enchantments);

            } else if (config.isConfigurationSection(ENCHANTMENTS)) {
                // We specify the parts we want to enchant
                ConfigurationSection section = config.getConfigurationSection(ENCHANTMENTS);
                Map<Integer, Map<Enchantment, Integer>> enchantments = new THashMap<>();

                for (String key : section.getKeys(false)) {
                    int slot;

                    switch (key.toLowerCase()) {
                        case "helmet":
                            slot = 0;
                            break;

                        case "chestpiece":
                            slot = 1;
                            break;

                        case "leggings":
                            slot = 2;
                            break;

                        case "boots":
                            slot = 3;
                            break;

                        default:
                            logger.warning("Unknown armor slot type: " + key);
                            continue;
                    }

                    if (section.isList(key)) {
                        logger.warning("Armor type config is not a list:" + key);
                        continue;
                    }

                    // Parse enchantments
                    enchantments.put(slot, Item.parseEnchantments(logger, section.getStringList(key)));
                }

                // Build icon
                if (icon == null) {
                    // Create combined map
                    Map<Enchantment, Integer> combined = new THashMap<>();
                    for (Map<Enchantment, Integer> entry : enchantments.values()) {
                        combined.putAll(entry);
                    }

                    icon = buildIcon(shop, false, enchantments.keySet(), combined, config);
                }

                upgrade = new PartialArmorUpgrade(prev, icon, price);

            } else {
                // Unknown garbage, show error ...
                logger.warning("Invalid armor upgrade, no enchants given!");
            }

            return upgrade;
        }

        @Override
        public String getName() {
            return "armor";
        }

        private ItemStack buildIcon(UpgradeableArmor shop, boolean full, Collection<Integer> parts,
                                      Map<Enchantment, Integer> enchants, ConfigurationSection config) {
            String type = config.getString("iconType", "book");
            ItemStack icon;

            switch (type) {
                default:
                case "book":
                    icon = buildBookIcon(full ? null : parts, enchants);
                    break;

                case "inherit":
                case "armor":
                    icon = buildInheritArmorIcon(shop, enchants);
                    break;
            }

            Item.setDisplayName(icon, ChatColor.GOLD + (full ? "Full" : "Partial") + " Armor Upgrade");

            return icon;
        }

        private ItemStack buildInheritArmorIcon(UpgradeableArmor shop, Map<Enchantment, Integer> enchantments) {
            ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);

            // Apply all inherited enchantments
            for (ArmorUpgrade upgrade : shop.getUpgrades()) {
                item.addEnchantments(upgrade.getEnchantments());
            }

            // Add new ones
            item.addEnchantments(enchantments);

            return item;
        }

        private ItemStack buildBookIcon(Collection<Integer> parts, Map<Enchantment, Integer> enchantments) {
            ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

            // Add description
            if (parts != null && !parts.isEmpty()) {
                List<String> partNames = new LinkedList<>();
                partNames.add("Applies to:");

                for (int part : parts) {
                    switch (part) {
                        default:
                            partNames.add("- (Unknown)");
                            break;

                        case 0:
                            partNames.add("- Helmet");
                            break;

                        case 1:
                            partNames.add("- Chestplate");
                            break;

                        case 2:
                            partNames.add("- Leggings");
                            break;

                        case 3:
                            partNames.add("- Boots");
                            break;
                    }
                }

                Item.setDescription(book, partNames);
            }

            // Add enchantments
            ItemMeta meta = book.getItemMeta();

            if (meta instanceof EnchantmentStorageMeta) {
                EnchantmentStorageMeta enchantmentMeta = (EnchantmentStorageMeta) meta;

                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    enchantmentMeta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
                }

                book.setItemMeta(meta);
            }

            return book;
        }

    }

}
