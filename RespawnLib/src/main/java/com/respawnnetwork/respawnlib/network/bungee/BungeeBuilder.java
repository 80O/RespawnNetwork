package com.respawnnetwork.respawnlib.network.bungee;

import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a builder for bungee messages.
 * <p />
 * Can only be used once.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
class BungeeBuilder implements BungeeMessager {
    /** The list holding the receivers */
    @Getter
    private final List<CommandSender> receivers;


    public BungeeBuilder() {
        receivers = new LinkedList<>();
    }

    @Override
    public BungeeMessager receivers(CommandSender... array) {
        return receivers(Arrays.asList(array));
    }

    @Override
    public BungeeMessager receivers(List<CommandSender> list) {
        receivers.addAll(list);

        return this;
    }

}
