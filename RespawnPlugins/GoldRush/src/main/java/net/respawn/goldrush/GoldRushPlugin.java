package net.respawn.goldrush;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GamePlugin;
import com.respawnnetwork.respawnlib.gameapi.maps.TeamGameMap;
import com.respawnnetwork.respawnlib.network.command.Command;
import com.respawnnetwork.respawnlib.network.command.CommandManager;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;


public class GoldRushPlugin extends GamePlugin<GoldRush, TeamGameMap> {

    @Override
    public void onEnable() {
        super.onEnable();

        loadMessages("goldrush.yml");

        // Register commands
        CommandManager commandManager = getCommandManager();
        if (commandManager != null) {
            commandManager.registerCommands(this);
        }
    }

    @Override
    protected GoldRush createGame(TeamGameMap map, ConfigurationSection gameConfig) {
        return new GoldRush(this, map, gameConfig);
    }

    @Override
    protected TeamGameMap createMap(String name, ConfigurationSection mapConfig) {
        return new TeamGameMap(name, mapConfig);
    }

    @Command(name="game next", permission = "respawn.game.goldrush.game.next", consoleCmd = true)
    public void onCommand(CommandSender sender, String[] args){
        Game currentGame = getCurrentGame();

        if (currentGame != null) {
            currentGame.nextState();
        }
    }

}
