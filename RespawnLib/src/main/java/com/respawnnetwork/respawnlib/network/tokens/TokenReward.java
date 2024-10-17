package com.respawnnetwork.respawnlib.network.tokens;

import lombok.Data;

@Data
public class TokenReward {
    private final String uuid;
    private final int amount;
    private final double multiplier;


    public TokenReward(String uuid, int amount) {
        this.uuid = uuid;
        this.amount = amount;
        this.multiplier = 1d;
    }

    public TokenReward(String uuid, int amount, double multiplier) {
        this.uuid = uuid;
        this.amount = amount;
        this.multiplier = multiplier;
    }

}
