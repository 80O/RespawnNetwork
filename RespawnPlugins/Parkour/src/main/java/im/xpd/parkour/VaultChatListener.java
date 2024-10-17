package im.xpd.parkour;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredServiceProvider;


public class VaultChatListener implements Listener {
    private final Main plugin;
    private Permission permission;
    private Chat chat;


    public VaultChatListener(Main plugin) {
        this.plugin = plugin;

        // Get permissions
        RegisteredServiceProvider<Permission> permissionProvider = getProvider(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }

        // Get chat
        RegisteredServiceProvider<Chat> chatProvider = getProvider(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled() || permission == null || chat == null) {
            return;
        }

        String group = permission.getPrimaryGroup(event.getPlayer());
        String prefix = null, suffix = null;

        if (group != null) {
            for(World w : Bukkit.getWorlds()) {
                prefix = escape(chat.getGroupPrefix(w, group));
                suffix = escape(chat.getGroupSuffix(w, group));
            }
        } else {
            prefix = "";
            suffix = "§r";
        }

        // Translate color codes for players that have the permission
        if (event.getPlayer().hasPermission("respawn.color")) {
            event.setMessage(escape(event.getMessage()));
        }

        // Set format
        event.setFormat(prefix + "%1$s" + suffix + ": %2$s");
    }

    private <T> RegisteredServiceProvider<T> getProvider(Class<T> aClass) {
        return plugin.getServer().getServicesManager().getRegistration(aClass);
    }

    private String escape(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

}
