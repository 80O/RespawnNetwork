package net.respawn.pointrunner;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.modules.*;
import com.respawnnetwork.respawnlib.gameapi.modules.mechanics.GameMechanicsModule;
import com.respawnnetwork.respawnlib.gameapi.modules.mechanics.PlayerMechanicsModule;
import com.respawnnetwork.respawnlib.gameapi.states.DefaultInGameState;
import com.respawnnetwork.respawnlib.gameapi.statistics.Statistic;
import net.respawn.pointrunner.modules.PRTokenModule;
import net.respawn.pointrunner.modules.PointRunnerModule;
import net.respawn.pointrunner.states.EndCountdownState;
import net.respawn.pointrunner.states.PrepareGameState;
import net.respawn.pointrunner.states.StartGameCountdown;
import net.respawn.pointrunner.states.WaitForPRPlayersState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;


public class PointRunner extends Game<PRPlayer, PointRunnerPlugin, PRMap>{
    /** The collected points statistic */
    public static final Statistic POINTS = new Statistic("pr", "points", "Points collected", 0);


    public PointRunner(PointRunnerPlugin plugin, PRMap map, ConfigurationSection config) {
        super(plugin, map, config);

        getStatistics().track(POINTS);
    }

    @Override
    protected void addStates() {
        addState(new WaitForPRPlayersState(this));
        addState(new StartGameCountdown(this));
        addState(new PrepareGameState(this));
        addState(new DefaultInGameState<>(this));
        addState(new EndCountdownState(this));
    }

    @Override
    protected void addModules() {
        addModule(new ScoreModule<>(this));
        addModule(new MOTDStatusModule<>(this));
        addModule(new HandbookModule<>(this));
        addModule(new BlockProtectionModule<>(this));
        addModule(new PlayerMechanicsModule<>(this));
        addModule(new GameMechanicsModule<>(this));
        addModule(new PointRunnerModule(this));
        addModule(new BungeeModule<>(this));
        addModule(new PRTokenModule(this));
    }

    @NotNull
    @Override
    public PRPlayer addPlayer(PRPlayer gamePlayer) {
        PRPlayer player = super.addPlayer(gamePlayer);
        player.resetPlayer();

        return player;
    }

    @NotNull
    @Override
    public PRPlayer createPlayer(Player player) {
        return new PRPlayer(this, player);
    }

    @Override
    protected PRPlayer[] convertPlayerArray(Collection<PRPlayer> collection) {
        return collection.toArray(new PRPlayer[collection.size()]);
    }
}
