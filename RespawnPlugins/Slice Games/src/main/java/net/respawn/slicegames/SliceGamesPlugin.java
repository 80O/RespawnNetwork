package net.respawn.slicegames;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.GamePlugin;
import com.respawnnetwork.respawnlib.network.command.Command;
import com.respawnnetwork.respawnlib.network.command.CommandManager;
import net.respawn.slicegames.events.ChatListener;
import net.respawn.slicegames.modules.SliceGamesModule;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Created by spaceemotion on 02/05/14.
 */
public class SliceGamesPlugin extends GamePlugin<SliceGames, SGMap> {

    @Override
    public void onEnable() {
        registerListener(new ChatListener(this));

        loadMessages("slicegames.yml");

        // Register commands
        CommandManager commandManager = getCommandManager();
        if (commandManager != null) {
            commandManager.registerCommands(this);
        }

        super.onEnable();
    }

    @Override
    protected SliceGames createGame(SGMap map, ConfigurationSection gameConfig) {
        return new SliceGames(this, map, gameConfig);
    }

    @Override
    protected SGMap createMap(String name, ConfigurationSection mapConfig) {
        return new SGMap(name, mapConfig);
    }

    @Override
    public boolean usesInventoryMenuAPI() {
        return true;
    }

    @Override
    public boolean usesCustomChatMessages() {
        // We refer to the libraries custom messages here
        return false;
    }

    @Command(name="game refill", permission = "respawn.game.slicegames.game.refill", consoleCmd = true)
    public void onGameRefill(CommandSender sender, String[] args) {
        SliceGames currentGame = getCurrentGame();

        if (currentGame != null) {
            SliceGamesModule module = currentGame.getModule(SliceGamesModule.class);
            if (module == null) {
                return;
            }

            module.refillInventories(true);
        }
    }

    @Command(name="game next", permission = "respawn.game.slicegames.game.next", consoleCmd = true)
    public void onGameNext(CommandSender sender, String[] args) {
        Game currentGame = getCurrentGame();

        if (currentGame != null) {
            currentGame.nextState();
        }
    }

    @Command(name="game secretspectator", minArgs = 1, consoleCmd = true)
    public void onSecretSpectator(CommandSender sender, String[] args) {
        Game currentGame = getCurrentGame();

        if (currentGame != null && sender instanceof Player) {
            GamePlayer player = currentGame.getPlayer(((Player) sender));

            if (player != null) {
                player.setSpectator(Boolean.getBoolean(args[0]));
            }
        }
    }

}
