package com.respawnnetwork.respawnlib.gameapi.modules;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import com.respawnnetwork.respawnlib.gameapi.events.EndGameEvent;
import com.respawnnetwork.respawnlib.gameapi.events.StartGameEvent;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a module that executes command whenever a game starts or ends.
 *
 * @author spaceemotion
 * @version 1.0.1
 */
public class CommandModule<G extends Game> extends GameModule<G> {
    private final List<String> startGameCommands;
    private final List<String> endGameCommands;


    public CommandModule(G game) {
        super(game);

        this.startGameCommands = new LinkedList<>();
        this.endGameCommands = new LinkedList<>();
    }

    @Override
    protected boolean onEnable() {
        if (getConfig().isList("start")) {
            startGameCommands.addAll(getConfig().getStringList("start"));
        }

        if (getConfig().isList("end")) {
            endGameCommands.addAll(getConfig().getStringList("end"));
        }

        return true;
    }

    @EventHandler
    public void onStartGame(StartGameEvent event) {
        if (event.isCancelled()) {
            return;
        }

        dispatchCommands(event.getGame(), startGameCommands);
    }

    @EventHandler
    public void onEndGame(EndGameEvent event) {
        dispatchCommands(event.getGame(), endGameCommands);
    }

    @Override
    public String getDisplayName() {
        return "Commands";
    }

    @Override
    public String getName() {
        return "commands";
    }

    public List<String> getStartGameCommands() {
        return startGameCommands;
    }

    public List<String> getEndGameCommands() {
        return endGameCommands;
    }

    private void dispatchCommands(Game game, List<String> commands) {
        for (String command : commands) {
            ConsoleCommandSender sender = game.getPlugin().getServer().getConsoleSender();
            game.getPlugin().getServer().dispatchCommand(sender, command);
        }
    }

}
