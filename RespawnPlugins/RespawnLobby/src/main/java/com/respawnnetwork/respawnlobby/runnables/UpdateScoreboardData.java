package com.respawnnetwork.respawnlobby.runnables;

import com.respawnnetwork.respawnlib.network.database.Database;
import com.respawnnetwork.respawnlobby.RespawnLobby;
import com.respawnnetwork.respawnlobby.ScoreboardSign;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.exception.DataAccessException;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import static com.respawnnetwork.respawnlib.network.database.generated.Tables.USERS;


public class UpdateScoreboardData extends LobbyRunnable {

    public UpdateScoreboardData(RespawnLobby plugin) {
        super(plugin);
    }

    @Override
    public void run() {

        List<ScoreboardSign> signs = getPlugin().getScoreboardSigns();

        // Don't do stuff when we have an empty list
        if (signs.isEmpty()) {
            return;
        }

        Database database = getPlugin().getDatabaseManager();

        // If we didn't set any database connection, do nothing as well
        if (database == null) {
            return;
        }

        // Check if we're connected and connect if we aren't
        if (!database.connected() && database.open() == null) {
            // We already logged an error in the database class
            return;
        }

        DSLContext context = database.getContext();

        try {
            // Execute query
            Result<Record2<String, Integer>> result = context.select(USERS.LAST_SEEN_USERNAME, USERS.TOKENS_HARD)
                    .from(USERS)
                    .orderBy(USERS.TOKENS_HARD.desc())
                    .limit(signs.size())
                    .fetch();

            // Get records
            Iterator<ScoreboardSign> signIterator = signs.iterator();
            int i = 1;

            for (Record2<String, Integer> record : result) {
                // This shouldn't really happen anyway
                if (!signIterator.hasNext()) {
                    continue;
                }

                // Update sign data
                ScoreboardSign sign = signIterator.next();
                sign.setTopId(i++);
                sign.setName(record.getValue(USERS.LAST_SEEN_USERNAME));
                sign.setDiamonds(record.getValue(USERS.TOKENS_HARD));
            }

        } catch (DataAccessException ex) {
            getPlugin().getPluginLog().log(Level.WARNING, "Could not get latest top players", ex);
        }
    }

}
