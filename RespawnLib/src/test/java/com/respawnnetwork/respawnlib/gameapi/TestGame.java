package com.respawnnetwork.respawnlib.gameapi;

import com.respawnnetwork.respawnlib.gameapi.maps.GameMap;
import com.respawnnetwork.respawnlib.gameapi.states.DefaultInGameState;
import com.respawnnetwork.respawnlib.gameapi.states.PrepareGameState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Represents a basic unit tested game.
 *
 * @author spaceemotion
 * @version 1.0
 */
public class TestGame extends Game<GamePlayer, GamePlugin, GameMap> {

    public TestGame(GamePlugin plugin, GameMap map, ConfigurationSection cfg) {
        super(plugin, map, cfg);
    }

    @Override
    protected void addStates() {
        addState(new PrepareTestGameState(this));
        addState(new TestInGameState(this));
    }

    @Override
    protected void addModules() {

    }

    @Override
    protected void onStartGame() {
        for (GamePlayer gamePlayer : getPlayers()) {
            getLogger().info("Player " + gamePlayer.getName() + " will be participating in the game!");
        }
    }

    @Override
    protected void onEndGame(boolean forcedStop) {

    }

    @NotNull
    @Override
    public GamePlayer createPlayer(Player player) {
        return new TestPlayer(this, player);
    }

    @Override
    protected GamePlayer[] convertPlayerArray(Collection<GamePlayer> collection) {
        return collection.toArray(new GamePlayer[collection.size()]);
    }


    private static class PrepareTestGameState extends PrepareGameState<TestGame> {

        private PrepareTestGameState(TestGame game) {
            super(game);
        }

        @Override
        public void onEnter() {
            // Nothing to prepare, go to the next state
            getGame().nextState();
        }

    }

    private static class TestInGameState extends DefaultInGameState<TestGame> {
        private TestInGameState(TestGame game) {
            super(game);
        }

        @Override
        public void onEnter() {
            // Do nothing
        }
    }

    private static class TestPlayer extends GamePlayer<TestGame> {

        private TestPlayer(TestGame game, Player player) {
            super(game, player);
        }

        @Nullable
        @Override
        public Player getPlayer() {
            return mock(Player.class);
        }

    }

}
