package net.respawn.pointrunner;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GamePlugin;
import com.respawnnetwork.respawnlib.network.command.Command;
import com.respawnnetwork.respawnlib.network.command.CommandManager;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;


public class PointRunnerPlugin extends GamePlugin<PointRunner, PRMap> {
    public PointRunnerPlugin() {
    }

    @Override
    public void onEnable() {
        super.onEnable();

        loadMessages("pointrunner.yml");

        // Register commands
        CommandManager commandManager = getCommandManager();
        if (commandManager != null) {
            commandManager.registerCommands(this);
        }
    }

    @Override
    protected PointRunner createGame(PRMap pointRunnerMap, ConfigurationSection configurationSection) {
        return new PointRunner(this, pointRunnerMap, configurationSection);
    }

    @Override
    protected PRMap createMap(String worldname, ConfigurationSection configurationSection) {
        return new PRMap(worldname, configurationSection);
    }

    @Command(name="game next", permission = "respawn.game.pointrunner.game.next", consoleCmd = true)
    public void onCommand(CommandSender sender, String[] args){
        Game currentGame = getCurrentGame();

        if (currentGame != null) {
            currentGame.nextState();
        }
    }

}
