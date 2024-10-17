package com.respawnnetwork.respawnlib.network.messages;

import gnu.trove.map.hash.THashMap;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;

/**
 * A basic builder for messages.
 *
 * @author spaceemotion
 * @version 1.0.1
 */
public class MessageBuilder implements MessageCreator {
    /** The list holding the receivers */
    @Getter
    private final List<CommandSender> receivers;

    /** Provided data for message */
    private final Map<String, Object> providedData;

    /** The message display */
    @Getter
    private MessageDisplay display;

    /** The format to use when sending the message */
    @Getter
    private String format;


    /**
     * Creates a new message builder.
     *
     * @param format The default format to use
     */
    public MessageBuilder(String format) {
        this.receivers = new LinkedList<>();
        this.providedData = new THashMap<>();

        this.display = MessageDisplay.CHAT;
        this.format = format;
    }

    @Override
    public MessageCreator receivers(CommandSender... list) {
        receivers.addAll(Arrays.asList(list));

        return this;
    }

    @Override
    public MessageCreator receivers(List<CommandSender> list) {
        receivers.addAll(list);

        return this;
    }

    @Override
    public MessageCreator format(String format) {
        this.format = format;

        return this;
    }

    @Override
    public MessageCreator provide(String key, Object value) {
        this.providedData.put(key, value);

        return this;
    }

    @Override
    public MessageCreator provide(Map<String, Object> data) {
        this.providedData.putAll(data);

        return this;
    }

    @Override
    public MessageCreator setDisplay(MessageDisplay display) {
        if (display != null) {
            this.display = display;
        }

        return this;
    }

    @Override
    public void send(String message) {
        if (display == null) {
            return;
        }

        // Add all online players if we haven't specified any
        if (Message.addOnlineByDefault() && receivers.isEmpty()) {
            Collections.addAll(receivers, Bukkit.getServer().getOnlinePlayers());
        }

        String messageToSend = parse(message);

        for (CommandSender participant : receivers) {
            display.sendMessage(participant, messageToSend);
        }
    }

    @Override
    public void send(String message, Object... args) {
        send(String.format(message, args));
    }

    @Override
    public void send(CommandSender sender, String message) {
        if (sender == null) {
            return;
        }

        receivers.add(sender);

        send(message);
    }

    @Override
    public void send(CommandSender sender, String message, Object... args) {
        receivers.add(sender);

        send(message, args);
    }

    @Override
    public void sendKey(String message) {
        send(getMessage(message));
    }

    @Override
    public void sendKey(String message, Object... args) {
        send(getMessage(message), args);
    }

    @Override
    public void sendKey(CommandSender sender, String message) {
        send(sender, getMessage(message));
    }

    @Override
    public void sendKey(CommandSender sender, String message, Object... args) {
        send(sender, getMessage(message), args);
    }

    @Override
    public String parseKey(String message) {
        return parse(getMessage(message));
    }

    @Override
    public String parse(String message) {
        String messageToSend = ChatColor.translateAlternateColorCodes(COLOR_CODE, String.format(format, message));

        for(Map.Entry<String, Object> entry : providedData.entrySet()) {
            messageToSend = messageToSend.replace('{' + entry.getKey() + '}', entry.getValue().toString());
        }

        return messageToSend;
    }

    private String getMessage(String message) {
        String msg = Message.DEFAULTS.get(message);

        if (msg == null) {
            return message;
        }

        return msg;
    }

}
