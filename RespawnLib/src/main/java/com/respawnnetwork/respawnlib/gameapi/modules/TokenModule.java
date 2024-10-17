package com.respawnnetwork.respawnlib.gameapi.modules;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.events.StateChangeEvent;
import com.respawnnetwork.respawnlib.gameapi.maps.TeamGameMap;
import com.respawnnetwork.respawnlib.gameapi.modules.team.Team;
import com.respawnnetwork.respawnlib.gameapi.statistics.Statistic;
import com.respawnnetwork.respawnlib.network.database.Database;
import com.respawnnetwork.respawnlib.network.messages.Message;
import com.respawnnetwork.respawnlib.network.tokens.TokenReward;
import com.respawnnetwork.respawnlib.network.tokens.Tokens;
import gnu.trove.map.hash.THashMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.logging.Level;

/**
 * Represents a module for the token system.
 * <p />
 * The token system will announce and give the tokens before the bungee module state kicks in.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class TokenModule<G extends Game> extends GameModule<G> implements Listener {
    private final Tokens tokens;
    private boolean given;


    public TokenModule(G game) {
        super(game);

        // Create instance of token system
        Database databaseManager = getGame().getPlugin().getDatabaseManager();
        if (databaseManager != null) {
            tokens = new Tokens(databaseManager);

        } else {
            getLogger().warning("Could not access database, will not be able to use the token system!");
            tokens = null;
        }
    }

    /**
     * Returns a map of additional tokens to give the player. This can return null if no extra tokens should be given.
     *
     * @param player The player to get the additional tokens for
     * @return A map containing additional tokens by their display string
     */
    @Nullable
    protected Map<String, Integer> getAdditionalTokensForPlayer(GamePlayer player) {
        return null;
    }

    @EventHandler
    public void onStateChange(StateChangeEvent event) {
        if (tokens == null || given || event.isCancelled() || !(/*event.getNext() instanceof BungeeModule.BungeeState ||*/ event.getNext() == null)) {
            return;
        }

        // Collect all statistics we need to check
        Map<Statistic, Double> statisticsToUse = new THashMap<>();

        for (Statistic statistic : getGame().getStatistics().getTracking()) {
            ConfigurationSection statSection = getConfig().getConfigurationSection(statistic.getIdentifier().replace(':', '.'));
            if (statSection == null) {
                getLogger().info("Tracked statistic " + statistic.getIdentifier() + " isn't in the config, will skip tokens");
                continue;
            }

            boolean enabled = statSection.getBoolean("enabled", true);
            double modifier = statSection.getDouble("modifier", 0);

            if (modifier == 0) {
                getLogger().info("Modifier for " + statistic.getIdentifier() + " is 0 - will skip");
                continue;
            }

            // If it's enabled give them the tokens
            if (enabled) {
                statisticsToUse.put(statistic, modifier);
            }
        }

        // Iterate over players and give the tokens
        for (GamePlayer gamePlayer : getGame().getPlayers()) {
            Map<String, Integer> tokensToGive = new THashMap<>();
            int points = 0;

            for (Map.Entry<Statistic, Double> statEntry : statisticsToUse.entrySet()) {
                // The key is the statistic, the value the modifier
                Statistic statistic = statEntry.getKey();
                int get = (int) Math.floor(gamePlayer.getStatistics().get(statistic) * statEntry.getValue());

                getLogger().info("Will give " + gamePlayer.getName() + " " + get + " token(s) for " + statistic.getIdentifier());
                points += get;

                // Add to give list
                tokensToGive.put(statistic.getDisplayName(), get);
            }

            // Add additional tokens
            Map<String, Integer> additionalTokens = getAdditionalTokensForPlayer(gamePlayer);
            if (additionalTokens != null && !additionalTokens.isEmpty()) {
                for (Map.Entry<String, Integer> additionalEntry : additionalTokens.entrySet()) {
                    getLogger().info(
                            "Will give " + gamePlayer.getName() + " additional " + additionalEntry.getValue() + " token(s) for " +
                                    additionalEntry.getKey()
                    );

                    points += additionalEntry.getValue();
                }

                // Add all additional tokens
                tokensToGive.putAll(additionalTokens);
            }

            // Notify the player
            for (Map.Entry<String, Integer> notifyEntry : tokensToGive.entrySet()) {
                Message.INFO
                        .provide("tokens", notifyEntry.getValue())
                        .provide("tokenStr", notifyEntry.getValue() == 1 ? "diamond" : "diamonds")
                        .provide("statistic", notifyEntry.getKey())
                        .sendKey(gamePlayer.getPlayer(), "game.tokens.received");
            }

            // Only give the tokens when we need to
            if (points > 0) {
                try {
                    tokens.give(new TokenReward(gamePlayer.getUuid(), points));

                } catch (NullPointerException ex) {
                    // We have to catch this ...
                    getLogger().log(Level.WARNING, "Could not give tokens to player", ex);
                }
            }
        }

        // Do not give them the next time
        given = true;
    }

    @Override
    public String getDisplayName() {
        return "Tokens";
    }

    @Override
    public String getName() {
        return "tokens";
    }


    public static class WinningTeam<G extends Game> extends TokenModule<G> {

        public WinningTeam(G game) {
            super(game);
        }

        @Nullable
        @Override
        protected Map<String, Integer> getAdditionalTokensForPlayer(GamePlayer player) {
            if (!(getGame().getMap() instanceof TeamGameMap)) {
                return null;
            }

            Team winningTeam = ((TeamGameMap) getGame().getMap()).getWinningTeam();
            if (winningTeam == null || !winningTeam.getPlayers().contains(player)) {
                return null;
            }

            Map<String, Integer> map = new THashMap<>();
            map.put("being in the winning team", getConfig().getInt("lib.winning"));

            return map;
        }

    }

}
