package com.respawnnetwork.respawnlib.network.messages;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class ChatMessageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onChatMessage(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        // Set the chat format
        event.setFormat("%1$s: %2$s");
    }

}
