package net.respawn.slicegames;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.modules.BungeeModule;
import com.respawnnetwork.respawnlib.gameapi.modules.MOTDStatusModule;
import com.respawnnetwork.respawnlib.gameapi.modules.ScoreModule;
import com.respawnnetwork.respawnlib.gameapi.modules.TokenModule;
import com.respawnnetwork.respawnlib.gameapi.modules.mechanics.GameMechanicsModule;
import com.respawnnetwork.respawnlib.gameapi.modules.mechanics.PlayerMechanicsModule;
import com.respawnnetwork.respawnlib.gameapi.statistics.PlayerStatistics;
import com.respawnnetwork.respawnlib.gameapi.statistics.Statistic;
import com.respawnnetwork.respawnlib.network.database.Database;
import net.respawn.slicegames.modules.SGProtectionModule;
import net.respawn.slicegames.modules.SliceGamesModule;
import net.respawn.slicegames.runnables.UpdateCompasses;
import net.respawn.slicegames.states.*;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;

import static net.respawn.slicegames.database.generated.Tables.HG_GAMES;
import static net.respawn.slicegames.database.generated.Tables.HG_USERS;

/**
 * Represents the Slice Game mini-game.
 *
 * @author spaceemotion
 * @author TomShar
 * @version 1.0
 */
public class SliceGames extends Game<SGPlayer, SliceGamesPlugin, SGMap> {
    /** The melons eaten statistic */
    public static final Statistic MELONS_EATEN = new Statistic("slice", "melonsEaten", "Melons eaten", 0);

    private BukkitTask compassTask;


    public SliceGames(SliceGamesPlugin plugin, SGMap map, ConfigurationSection config) {
        super(plugin, map, config);

        getStatistics().track(MELONS_EATEN);
    }

    @Override
    protected void onStartGame() {
        super.onStartGame();

        // We just clear all hostile mobs by this
        World world = getMap().getWorld();
        if (world != null) {
            world.setDifficulty(Difficulty.PEACEFUL);
            world.setDifficulty(getMap().getDifficulty());
        }

        compassTask = new UpdateCompasses(this).runTaskTimer(getPlugin(), 20, 20);
    }

    @Override
    protected void onEndGame(boolean forcedStop) {
        super.onEndGame(forcedStop);

        // Disable compass task
        if (compassTask != null) {
            compassTask.cancel();
        }

        // Update database
        Database db = getPlugin().getDatabaseManager();

        if(db == null) {
            // Could not update database, not set up at all
            getLogger().info("Database not set up, could not save match details");
            return;
        }

        if (!db.connected() && db.open() != null) {
            if (getMap().getId() < 1) {
                getLogger().info("No valid Map ID set, will not save match details");

            } else if (getMap().getStartTime() == null) {
                getLogger().info("No Map start time set, will not save match details");

            } else {
                if (getMap().getWinner() == null) {
                    getLogger().info("No winner for map set, will not save match details");

                } else {
                    db.getContext()
                            .insertInto(HG_GAMES).set(HG_GAMES.START, getMap().getStartTime())
                            .set(HG_GAMES.FINISH, new Timestamp(getMap().getStartTime().getTime() + System.currentTimeMillis()))
                            .set(HG_GAMES.MAP, getMap().getId())
                            .set(HG_GAMES.WINNER, getMap().getWinner().getUuid()).execute();
                }
            }
        }
    }

    @Override
    protected void addStates() {
        addState(new WaitForTributesState(this));
        addState(new PrepareGameState(this));
        addState(new InSGState(this));
        addState(new DeathMatchCountdown(this));
        addState(new DeathMatchState(this));
    }

    @Override
    protected void addModules() {
        addModule(new ScoreModule<>(this));
        addModule(new BungeeModule<>(this));
        addModule(new MOTDStatusModule<>(this));
        addModule(new PlayerMechanicsModule<>(this));
        addModule(new GameMechanicsModule<>(this));
        addModule(new SGProtectionModule(this));
        addModule(new TokenModule<>(this));
        addModule(new SliceGamesModule(this));
    }

    /**
     * Updates the database statistics for the given player.
     *
     * @param player The player to update the tables for
     //* @return True on success, false if something went wrong
     */
    public void updateDatabase(final SGPlayer player) {
        final Database db = getPlugin().getDatabaseManager();

        if(db == null) {
            // Could not update database, not set up at all
            getLogger().info("Database not set up, could not update tables for player " + player.getName());
            return;
        }

        if (!db.connected() && db.open() == null) {
            // Tried to connect but failed, return false
            return;
        }

        final long timePlayed = player.getTimeOfDeath().getTime() - getMap().getStartTime().getTime();
        final int wins = player.equals(getMap().getWinner()) ? 1 : 0;

        // Get player statistics
        PlayerStatistics stats = player.getStatistics();

        final BigDecimal dealt = BigDecimal.valueOf(stats.get(Statistic.DAMAGE_DEALT));
        final int kills = (int) stats.get(Statistic.KILLS);
        final int melons = (int) stats.get(MELONS_EATEN);

        // Super ugly queries, YAY!
        new BukkitRunnable() {
            @Override
            public void run() {
                db.getContext()
                        .insertInto(HG_USERS).set(HG_USERS.UUID, player.getUuid())
                        .set(HG_USERS.TIME_PLAYED,  timePlayed)
                        .set(HG_USERS.WINS,         wins)
                        .set(HG_USERS.KILLS,        kills)
                        .set(HG_USERS.DAMAGE_DEALT, dealt)
                        .set(HG_USERS.MELONS_EATEN, melons)
                        .onDuplicateKeyUpdate()
                        .set(HG_USERS.TIME_PLAYED,  HG_USERS.TIME_PLAYED.add(timePlayed))
                        .set(HG_USERS.PLAYED,       HG_USERS.PLAYED.add(1))
                        .set(HG_USERS.WINS, HG_USERS.WINS.add(wins))
                        .set(HG_USERS.KILLS,        HG_USERS.KILLS.add(kills))
                        .set(HG_USERS.DAMAGE_DEALT, HG_USERS.DAMAGE_DEALT.add(dealt))
                        .set(HG_USERS.MELONS_EATEN, HG_USERS.MELONS_EATEN.add(melons))
                        .execute();
            }
        }.runTask(getPlugin());
    }

    @NotNull
    @Override
    public SGPlayer createPlayer(Player player) {
        return new SGPlayer(this, player);
    }

    @Override
    protected SGPlayer[] convertPlayerArray(Collection<SGPlayer> collection) {
        return collection.toArray(new SGPlayer[collection.size()]);
    }

}
