package net.respawn.goldrush;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.maps.TeamGameMap;
import com.respawnnetwork.respawnlib.gameapi.modules.*;
import com.respawnnetwork.respawnlib.gameapi.modules.mechanics.GameMechanicsModule;
import com.respawnnetwork.respawnlib.gameapi.modules.team.TeamModule;
import com.respawnnetwork.respawnlib.gameapi.states.WaitForTeamPlayersState;
import com.respawnnetwork.respawnlib.gameapi.statistics.Statistic;
import net.respawn.goldrush.modules.GRMechanicsModule;
import net.respawn.goldrush.modules.GRRegionModule;
import net.respawn.goldrush.modules.shop.GRShopModule;
import net.respawn.goldrush.states.InGRGameState;
import net.respawn.goldrush.states.PrepareGameState;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;


public class GoldRush extends Game<GRPlayer, GoldRushPlugin, TeamGameMap> {
    public static final Statistic GOLD_COLLECTED = new Statistic("gr", "goldCollected", "Gold Collected", 0);


    public GoldRush(GoldRushPlugin plugin, TeamGameMap map, ConfigurationSection config) {
        super(plugin, map, config);

        getStatistics().track(GOLD_COLLECTED);
    }

    @Override
    protected void addStates() {
        addState(new WaitForTeamPlayersState<>(this));
        addState(new PrepareGameState(this));
        addState(new InGRGameState(this));
    }

    @Override
    protected void addModules() {
        addModule(new MOTDStatusModule<>(this));
        addModule(new YActionModule<>(this));
        addModule(new ScoreModule<>(this));
        addModule(new TeamModule<>(this));
        addModule(new HandbookModule<>(this));
        addModule(new GameMechanicsModule<>(this));
        addModule(new GRMechanicsModule(this));
        addModule(new GRShopModule(this));
        addModule(new GRRegionModule(this));
        addModule(new BlockProtectionModule<>(this));
        addModule(new BungeeModule<>(this));
        addModule(new TokenModule.WinningTeam<>(this));
    }

    @Override
    protected void onStartGame() {
        super.onStartGame();

        World world = getMap().getWorld();
        if (world == null) {
            return;
        }

        // We clear monsters by this one as well, but we don't want to
        // have them respawn directly after clearing
        world.setDifficulty(Difficulty.PEACEFUL);

        // Clear all xp orbs and creatures
        for (Entity entity : world.getEntitiesByClasses(ExperienceOrb.class, Creature.class, Item.class)) {
            entity.remove();
        }
    }

    @NotNull
    @Override
    public GRPlayer createPlayer(Player player) {
        return new GRPlayer(this, player);
    }

    @Override
    protected GRPlayer[] convertPlayerArray(Collection<GRPlayer> collection) {
        return collection.toArray(new GRPlayer[collection.size()]);
    }

}
