package com.respawnnetwork.respawnlib.network.tokens;

import com.respawnnetwork.respawnlib.network.database.Database;
import org.jooq.DSLContext;

import static com.respawnnetwork.respawnlib.network.database.generated.Tables.USERS;

public class Tokens {
    private final Database db;


    public Tokens(Database db) {
        this.db = db;
    }

    /**
     * Returns true if the tokens where successfully
     * given to the specified UUID given from the
     * TokenReward object, otherwise false if the
     * query failed to execute.
     *
     * @param reward    TokenReward object containing UUID, Amount of tokens and Multiplier.
     * @return          TRUE if tokens are given, FALSE if the tokens aren't given.
     * @see             TokenReward
     */
    public boolean give(TokenReward reward) {
        DSLContext context = getContext();

        return context != null &&context.update(USERS)
                .set(USERS.TOKENS_SOFT, USERS.TOKENS_SOFT.add(reward.getAmount() * reward.getMultiplier()))
                .set(USERS.TOKENS_HARD, USERS.TOKENS_HARD.add(reward.getAmount())).where(USERS.UUID.eq(reward.getUuid()))
                .execute() == 1;
    }

    /**
     * Returns the amount of (soft) tokens the user with the specified UUID currently has.
     *
     * @param uuid The player UUID of whom you want to find amount of tokens
     * @return The amount of tokens
     */
    public int getSoft(String uuid) {
        DSLContext context = getContext();
        if (context == null) {
            return -1;
        }

        return context.select(USERS.TOKENS_SOFT).from(USERS).where(USERS.UUID.eq(uuid)).fetchAny().value1();
    }

    /**
     * Returns the amount of (hard) tokens the user with the specified UUID currently has.
     *
     * @param uuid The player UUID of whom you want to find amount of tokens
     * @return The amount of tokens
     */
    public int getHard(String uuid) {
        DSLContext context = getContext();
        if (context == null) {
            return -1;
        }

        return context.select(USERS.TOKENS_HARD).from(USERS).where(USERS.UUID.eq(uuid)).fetchAny().value1();
    }

    private DSLContext getContext() {
        if (db == null) {
            return null;
        }

        if (!db.connected()) {
            db.open();
        }

        return db.getContext();
    }

}
