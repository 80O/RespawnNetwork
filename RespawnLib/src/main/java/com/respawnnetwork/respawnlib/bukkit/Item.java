package com.respawnnetwork.respawnlib.bukkit;

import com.google.common.collect.ImmutableList;
import com.respawnnetwork.respawnlib.lang.ParseException;
import gnu.trove.map.hash.THashMap;
import org.apache.commons.lang.WordUtils;
import org.atteo.evo.inflector.English;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author spaceemotion
 * @version 1.0.1
 */
public final class Item {
    private static final Map<Material, String> MATERIAL_NAMES = new THashMap<>();
    private static final List<Material> NO_PLURAL_NAMES_ARRAY = ImmutableList.<Material>builder()
            .add(Material.SANDSTONE)
            .add(Material.COBBLESTONE).build();


    private Item() {
        // Private constructor...
    }
/*
    public static ItemStack addGlow(ItemStack item) {
        net.minecraft.server.v1_7_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = null;

        if (!nmsStack.hasTag()) {
            tag = new NBTTagCompound();
            nmsStack.setTag(tag);
        }

        if (tag == null) {
            tag = nmsStack.getTag();
        }

        NBTTagList ench = new NBTTagList();
        tag.set("ench", ench);
        nmsStack.setTag(tag);

        return CraftItemStack.asCraftMirror(nmsStack);
    }*/

    public static String getPluralizedMaterialName(Material material, int size) {
        String name = getHumanReadableName(material);

        if (NO_PLURAL_NAMES_ARRAY.contains(material)) {
            return name;
        }

        return English.plural(name, size);
    }

    public static ItemStack getFor(Material mat, String title) {
        ItemStack is = new ItemStack(mat);
        setDisplayName(is, title);

        return is;
    }

    public static void setDisplayName(ItemStack is, String title) {
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(title);
        is.setItemMeta(meta);
    }

    public static void setDescription(ItemStack is, List<String> lines) {
        setDescription(is, lines.toArray(new String[lines.size()]));
    }

    public static void setDescription(ItemStack stack, String... lines) {
        if (stack == null) {
            return;
        }

        ItemMeta meta = stack.getItemMeta();
        meta.setLore(null);

        addLore(meta, Arrays.asList(lines));

        stack.setItemMeta(meta);
    }

    public static void addDescription(ItemStack stack, String... lines) {
        addDescription(stack, true, lines);
    }

    public static void addDescription(ItemStack stack, boolean divider, String... lines) {
        if (stack == null) {
            return;
        }

        ItemMeta meta = stack.getItemMeta();

        LinkedList<String> lore = new LinkedList<>();

        if (divider && meta.hasLore()) {
            lore.add("");
        }

        Collections.addAll(lore, lines);
        addLore(meta, lore);

        stack.setItemMeta(meta);
    }

    public static ItemStack createBook(String title, String author, List<String> pages) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta meta = book.getItemMeta();

        if (meta instanceof BookMeta) {
            BookMeta bookMeta = (BookMeta) meta;

            if (title != null) {
                bookMeta.setTitle(ChatColor.translateAlternateColorCodes('&', title));
            }

            if (author != null) {
                bookMeta.setAuthor(author);
            }

            for (String page : pages) {
                bookMeta.addPage(page); //WordUtils.wrap(page, 26));
            }

            book.setItemMeta(bookMeta);
        }

        return book;
    }

    public static ItemStack parseItem(Logger logger, Object obj) {
        ItemStack itemStack = null;

        try {
            if (obj instanceof Map) {
                // It's a map, so lets create a section from that
                itemStack = parseConfig(logger, createSection((Map) obj));

            } else if (obj instanceof ConfigurationSection) {
                // We have a more detailed item
                itemStack = parseConfig(logger, (ConfigurationSection) obj);

            } else if (obj instanceof String) {
                String str = (String) obj;
                String[] split = str.split(" ", 2);

                int amount = 1;
                String materialString = null;
                Material material;

                if (split.length > 1) {
                    String amntString = split[0].trim();

                    // Check if we even have a number
                    if (amntString.matches("\\d+")) {
                        try {
                            amount = Integer.parseInt(amntString);
                            materialString = split[1];

                        } catch (NumberFormatException ex) {
                            logger.log(Level.WARNING, "Invalid item amount: " + amntString, ex);
                        }
                    }
                }

                // Just use the old string then
                if (materialString == null) {
                    materialString = str;
                }

                // Parse single item name
                material = Material.matchMaterial(materialString);

                if (material == null) {
                    throw new ParseException("Invalid item material");
                }

                itemStack = new ItemStack(material, amount);

            } else if (obj instanceof List) {
                throw new ParseException("Item config cannot be a list, please check if you entered it correctly!");

            } else {
                throw new ParseException("Invalid item config type (expected either a text or a section)");
            }

        } catch (ParseException ex) {
            logger.log(Level.WARNING, "Could not parse item: '" + obj + '\'', ex);
        }

        return itemStack;
    }

    public static boolean parseInventory(Logger logger, List<?> items, List<ItemStack> inventory) {
        if (logger == null || items == null || inventory == null) {
            return false;
        }

        boolean success = true;

        for (Object obj : items) {
            ItemStack itemStack = parseItem(logger, obj);

            if (itemStack == null) {
                success = false;
                continue;
            }

            inventory.add(itemStack);
        }

        return success;
    }

    public static Map<Enchantment, Integer> parseEnchantments(Logger logger, List<String> strings) {
        Map<Enchantment, Integer> map = new THashMap<>();

        if (strings != null && !strings.isEmpty()) {
            for (String enchant : strings) {
                // Parse string (like "fire 2" or "protection fall 1"
                int index = enchant.lastIndexOf(" ") + 1;
                int level = 0;

                if (index > 0) {
                    String lvlString = enchant.substring(index);

                    try {
                        level = Integer.parseInt(lvlString);

                    } catch (NumberFormatException ex) {
                        logger.log(Level.WARNING, "Invalid enchantment level: " + lvlString, ex);
                        continue;
                    }
                }

                String enchantmentName = enchant.substring(0, index).trim().replace(' ', '_').toUpperCase();
                Enchantment enchantment = Enchantment.getByName(enchantmentName);

                if (enchantment == null) {
                    logger.warning("Unknown enchantment: '" + enchantmentName + '\'');
                    continue;
                }

                // Add to list
                map.put(enchantment, level);
            }
        }

        return map;
    }

    public static ItemStack dyeLeatherArmor(ItemStack itemStack, Color color) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (!(itemMeta instanceof LeatherArmorMeta)) {
            return itemStack;
        }

        LeatherArmorMeta meta = (LeatherArmorMeta) itemMeta;
        meta.setColor(color);

        itemStack.setItemMeta(meta);

        return itemStack;
    }


    /**
     * Returns true if the material is a wood, stone, iron, gold or diamond tool.
     *
     * @param mat The material to check
     *
     * @return True or false
     */
    public static boolean isTool(Material mat) {
        switch (mat) {
            case WOOD_HOE:
            case WOOD_PICKAXE:
            case WOOD_SWORD:
            case WOOD_AXE:
            case STONE_HOE:
            case STONE_PICKAXE:
            case STONE_SWORD:
            case STONE_AXE:
            case IRON_HOE:
            case IRON_PICKAXE:
            case IRON_SWORD:
            case IRON_AXE:
            case GOLD_HOE:
            case GOLD_PICKAXE:
            case GOLD_SWORD:
            case GOLD_AXE:
            case DIAMOND_HOE:
            case DIAMOND_PICKAXE:
            case DIAMOND_SWORD:
            case DIAMOND_AXE:
                return true;
        }

        return false;
    }

    public static boolean isArmor(Material material) {
        switch (material) {
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
            case IRON_HELMET:
            case IRON_CHESTPLATE:
            case IRON_LEGGINGS:
            case IRON_BOOTS:
            case GOLD_HELMET:
            case GOLD_CHESTPLATE:
            case GOLD_LEGGINGS:
            case GOLD_BOOTS:
            case DIAMOND_HELMET:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case DIAMOND_BOOTS:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
            case CHAINMAIL_BOOTS:
                return true;
        }

        return false;
    }

    public static int getArmorSlot(Material material) {
        switch (material) {
            case LEATHER_HELMET:
            case IRON_HELMET:
            case GOLD_HELMET:
            case DIAMOND_HELMET:
            case CHAINMAIL_HELMET:
                return 3;

            case LEATHER_CHESTPLATE:
            case IRON_CHESTPLATE:
            case GOLD_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case CHAINMAIL_CHESTPLATE:
                return 2;

            case LEATHER_LEGGINGS:
            case IRON_LEGGINGS:
            case GOLD_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
                return 1;

            case LEATHER_BOOTS:
            case IRON_BOOTS:
            case GOLD_BOOTS:
            case DIAMOND_BOOTS:
            case CHAINMAIL_BOOTS:
                return 0;
        }

        return -1;
    }

    public static boolean fitsInto(ItemStack item, Inventory inventory) {
        int freeSpace = 0;

        for (ItemStack i : inventory) {
            if (i == null) {
                freeSpace += item.getType().getMaxStackSize();

            } else if (i.getType() == item.getType()) {
                freeSpace += i.getType().getMaxStackSize() - i.getAmount();
            }
        }

        return item.getAmount() <= freeSpace;
    }

    public static String getHumanReadableName(ItemStack item) {
        if (item == null) {
            return "<unknown>";
        }

        if (item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();

            // Use the display name if we can
            if (itemMeta.hasDisplayName()) {
                return itemMeta.getDisplayName();
            }
        }

        return getHumanReadableName(item.getType());
    }

    public static String getHumanReadableName(Material material) {
        String humanName = MATERIAL_NAMES.get(material);

        if (humanName != null) {
            return humanName;
        }

        String name = material.name();
        StringBuilder sb = new StringBuilder();

        boolean uppercase = true;
        for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);

            if (c == '_') {
                sb.append(" ");
                uppercase = true;
                continue;
            }

            if (uppercase) {
                sb.append(Character.toUpperCase(c));
                uppercase = false;

            } else {
                sb.append(Character.toLowerCase(c));
            }
        }

        // Put into registry
        humanName = sb.toString().replace(" And ", " and ");
        MATERIAL_NAMES.put(material, humanName);

        return humanName;
    }

    private static void addLore(ItemMeta itemMeta, List<String> lines) {
        List<String> lore;

        if (itemMeta.hasLore()) {
            lore = itemMeta.getLore();

        } else {
            lore = new LinkedList<>();
        }

        for(String s : lines) {
            if (s == null) {
                lore.add("");
                continue;
            }

            lore.addAll(Arrays.asList(WordUtils.wrap(s, 24, "\r", true).split("\r")));
        }

        itemMeta.setLore(lore);
    }


    private static ConfigurationSection createSection(Map<?, ?> map) {
        MemoryConfiguration memory = new MemoryConfiguration();

        return memory.createSection("tmp", (Map) map);
    }

    private static ItemStack parseConfig(Logger logger, ConfigurationSection section) throws ParseException {
        // Get item material
        String materialName = section.getString("material");
        if (materialName == null) {
            throw new ParseException("No 'material' given");
        }

        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            throw new ParseException("Item material does not exist: " + materialName);
        }

        // Handle potions a bit different
        ItemStack item;
        boolean potion = false;

        if (material == Material.POTION) {
            item = parsePotion(section);
            potion = true;

        } else {
            // Create normal item
            item = new ItemStack(material);
        }

        ItemMeta meta = item.getItemMeta();
        boolean changed = false;

        // Set amount
        item.setAmount(getAmount(section));

        // Set item name
        String name = section.getString("name");
        if (name != null) {
            meta.setDisplayName(name);
            changed = true;
        }

        // Description
        List<String> description = section.getStringList("description");
        if (description != null && !description.isEmpty()) {
            addLore(meta, description);
            changed = true;
        }

        // Already stop here when we have a potion
        if (potion) {
            // Only apply changes when we did something
            if (changed) {
                item.setItemMeta(meta);
            }

            return item;
        }

        // Enchantments
        Map<Enchantment, Integer> enchants = parseEnchantments(logger, section.getStringList("enchantments"));

        if (!enchants.isEmpty()) {
            item.addUnsafeEnchantments(enchants);
        }

        // Check meta data
        if (item.getItemMeta() instanceof LeatherArmorMeta) {
            // ------------------------------------------------------------------------ LEATHER ARMOR
            LeatherArmorMeta armorMeta = (LeatherArmorMeta) item.getItemMeta();

            // Color
            Color color = section.getColor("color");
            if (color != null) {
                armorMeta.setColor(color);
                changed = true;
            }

        } else if (item.getItemMeta() instanceof BookMeta) {
            // ------------------------------------------------------------------------ BOOKS
            BookMeta bookMeta = (BookMeta) item.getItemMeta();

            // Title
            String title = section.getString("title");
            if (title != null) {
                bookMeta.setTitle(title);
                changed = true;
            }

            // Author
            String author = section.getString("author");
            if (author != null) {
                bookMeta.setAuthor(author);
                changed = true;
            }

            // Pages
            List<String> pages = section.getStringList("pages");
            if (pages != null && !pages.isEmpty()) {
                bookMeta.addPage(pages.toArray(new String[pages.size()]));
                changed = true;
            }

        } else if (item.getItemMeta() instanceof SkullMeta) {
            // ------------------------------------------------------------------------ PLAYER HEADS
            SkullMeta skullMeta = (SkullMeta) item.getItemMeta();

            // Skull owner
            String owner = section.getString("owner");
            if (owner != null) {
                skullMeta.setOwner(owner);
                changed = true;
            }

        } else if (item.getItemMeta() instanceof FireworkMeta) {
            // ------------------------------------------------------------------------ Fireworks
            FireworkMeta fireworkMeta = (FireworkMeta) meta;

            // Power
            int power = section.getInt("power");
            if (power > 0) {
                fireworkMeta.setPower(power);
                changed = true;
            }

            // Effects
            List<?> fireworks = section.getList("effects");
            if (fireworks != null && !fireworks.isEmpty()) {
                for (Object obj : fireworks) {
                    if (!(obj instanceof FireworkEffect)) {
                        continue;
                    }

                    // Add effect
                    fireworkMeta.addEffect((FireworkEffect) obj);
                }

                changed = true;
            }
        }

        // Only apply changes when we did something
        if (changed) {
            item.setItemMeta(meta);
        }

        return item;
    }

    private static ItemStack parsePotion(ConfigurationSection section) throws ParseException {
        String fxName = section.getString("effect", "none").trim().toUpperCase().replace(' ', '_');
        PotionEffectType potionEffectType = PotionEffectType.getByName(fxName);

        if (potionEffectType == null) {
            throw new ParseException("Unknown potion effect type: " + fxName);
        }

        // Create potion
        Potion potion = new Potion(PotionType.getByEffect(potionEffectType));

        potion.setLevel(section.getInt("level", 1));
        potion.setSplash(section.getBoolean("splash", false));

        if (!potionEffectType.isInstant()) {
            potion.setHasExtendedDuration(section.getBoolean("extended", false));
        }

        // Create item stack
        return potion.toItemStack(getAmount(section));
    }

    private static int getAmount(ConfigurationSection section) {
        int amount = section.getInt("amount");

        return amount > 0 ? amount: 1;
    }

}
