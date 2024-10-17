package net.respawn.skybridgewars;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.maps.TeamGameMap;
import com.respawnnetwork.respawnlib.gameapi.modules.*;
import com.respawnnetwork.respawnlib.gameapi.modules.gift.GiftModule;
import com.respawnnetwork.respawnlib.gameapi.modules.mechanics.GameMechanicsModule;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.ShopModule;
import com.respawnnetwork.respawnlib.gameapi.modules.team.TeamModule;
import com.respawnnetwork.respawnlib.gameapi.states.WaitForTeamPlayersState;
import com.respawnnetwork.respawnlib.gameapi.statistics.Statistic;
import net.respawn.skybridgewars.modules.SBWMechanicsModule;
import net.respawn.skybridgewars.modules.SBWRegionModule;
import net.respawn.skybridgewars.states.InSBWGameState;
import net.respawn.skybridgewars.states.PrepareSBWGameState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;


public class SkyBridgeWars extends Game<SBWPlayer, SkyBridgeWarsPlugin, TeamGameMap> {
    public static final String NAMESPACE = "sbw";
    public static final Statistic TEAM_DAMAGE = new Statistic(NAMESPACE, "teamDamage", "Team damage", 0);


    /**
     * Creates a new sky bridge wars game.
     *
     * @param plugin The plugin
     * @param map The map we're playing on
     * @param config The map configuration
     */
    public SkyBridgeWars(SkyBridgeWarsPlugin plugin, TeamGameMap map, ConfigurationSection config) {
        super(plugin, map, config);

        getStatistics().track(TEAM_DAMAGE);
    }

    @Override
    protected void addStates() {
        addState(new WaitForTeamPlayersState<>(this));
        addState(new PrepareSBWGameState(this));
        addState(new InSBWGameState(this));
    }

    @Override
    protected void addModules() {
        addModule(new BungeeModule<>(this));
        addModule(new MOTDStatusModule<>(this));
        addModule(new YActionModule<>(this));
        addModule(new CommandModule<>(this));
        addModule(new GiftModule<>(this));
        addModule(new BlockProtectionModule<>(this));
        addModule(new ScoreModule<>(this));
        addModule(new TeamModule<>(this));
        addModule(new ShopModule<>(this));
        addModule(new SBWRegionModule(this));
        addModule(new SBWMechanicsModule(this));
        addModule(new GameMechanicsModule<>(this));
        addModule(new HandbookModule<>(this));
        addModule(new TokenModule.WinningTeam<>(this));
    }

    @NotNull
    @Override
    public SBWPlayer addPlayer(SBWPlayer gamePlayer) {
        SBWPlayer player = super.addPlayer(gamePlayer);
        player.heal();

        return player;
    }

    @NotNull
    @Override
    public SBWPlayer createPlayer(Player player) {
        return new SBWPlayer(this, player);
    }

    @Override
    protected SBWPlayer[] convertPlayerArray(Collection<SBWPlayer> sbwPlayers) {
        return sbwPlayers.toArray(new SBWPlayer[sbwPlayers.size()]);
    }

}
