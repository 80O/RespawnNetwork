package com.respawnnetwork.respawnlib.network.messages;

import org.bukkit.command.CommandSender;

/**
 * Represents the display "slot" for a message.
 * <p />
 * This has been added for the upcoming 1.8 minecraft release, where the
 * action bar has been introduced.
 *
 * @author spaceemotion
 * @version 1.0
 * @version 1.0.1
 */
public enum MessageDisplay {
    /** Sends the message to the chat */
    CHAT {
        @Override
        void sendMessage(CommandSender receiver, String message) {
            receiver.sendMessage(message);
        }

        @Override
        void sendMessages(CommandSender receiver, String... messages) {
            receiver.sendMessage(messages);
        }
    },

    /** Sends the message to the action bar */
    ACTION_BAR {
        @Override
        void sendMessage(CommandSender receiver, String message) {
            // Not yet implemented...
        }

        @Override
        void sendMessages(CommandSender receiver, String... messages) {
            // Not yet implemented...
        }
    };

    abstract void sendMessage(CommandSender receiver, String message);

    abstract void sendMessages(CommandSender receiver, String... messages);

}
