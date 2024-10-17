package com.respawnnetwork.respawnlib.gameapi;

import com.respawnnetwork.respawnlib.gameapi.maps.GameMap;
import com.respawnnetwork.respawnlib.gameapi.states.InGameState;
import com.respawnnetwork.respawnlib.plugin.Plugin;
import com.respawnnetwork.respawnlib.plugin.PluginConfig;
import gnu.trove.map.hash.THashMap;
import org.bukkit.Server;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.UUID;
import java.util.logging.Logger;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Represents a unit test suite for the GameAPI.
 *
 * @author spaceemotion
 * @version 1.0.1
 */
@RunWith(PowerMockRunner.class)
@Ignore
public class GameAPITest {
    private TestGame game;


    @Before
    @PrepareForTest({Plugin.class})
    public void setUp() throws Exception {
        // Create dummy player
        UUID playerUUID = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerUUID);

        // Create dummy server
        Server server = mock(Server.class);
        when(server.getUpdateFolder()).thenReturn("");
        when(server.getLogger()).thenReturn(Logger.getGlobal());
        when(server.getPluginManager()).thenReturn(new SimplePluginManager(server, mock(SimpleCommandMap.class)));
        when(server.getOnlinePlayers()).thenReturn(new Player[] {player});

        // Create map
        GameMap map = new GameMap("dummy-world", new MemoryConfiguration());

        // Set up the plugin
        GamePlugin plugin = mock(GamePlugin.class);

        MemberModifier.field(JavaPlugin.class, "server").set(plugin, server);
        MemberModifier.field(JavaPlugin.class, "description").set(plugin, new PluginDescriptionFile("DemoPlugin", "1.0", getClass().getName()));
        MemberModifier.field(JavaPlugin.class, "dataFolder").set(plugin, new File("./src/test/resources/"));
        MemberModifier.field(JavaPlugin.class, "configFile").set(plugin, new File(plugin.getDataFolder(), "config.yml"));
        MemberModifier.field(JavaPlugin.class, "logger").set(plugin, new PluginLogger(plugin));
        MemberModifier.field(Plugin.class, "configCache").set(plugin, new THashMap<String, PluginConfig>());
        MemberModifier.field(GamePlugin.class, "gameConfig").set(plugin, new PluginConfig(plugin, "game.yml"));
        when(plugin.getPluginLog()).thenReturn(Logger.getLogger("PluginLogger"));
        when(plugin, "buildGameConfiguration", map).thenCallRealMethod();
        // when(plugin, "getConfig", "game.yml").thenCallRealMethod();
        when(plugin.getGameConfig()).thenCallRealMethod();

        // Create game
        game = new TestGame(plugin, map, plugin.buildGameConfiguration(map));
        MemberModifier.field(Game.class, "logger").set(game, Logger.getLogger("GameLogger"));
    }

    @Test
    public void testStateChanges() throws Exception {
        game.startGame();

        // Just go to the next state
        Assert.assertTrue("Game is not in IN-GAME state!", game.getCurrentState() instanceof InGameState);
        game.nextState();

        Assert.assertTrue("Game should not be running anymore!", game.hasEnded());
    }

    @Test
    public void testShutdown() throws Exception {
        game.startGame();
        game.shutdown();
    }

}
