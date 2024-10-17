package com.respawnnetwork.respawnlobby;

import com.respawnnetwork.respawnlib.network.signs.SignBuilder;
import lombok.Data;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;

/**
 * Represents a scoreboard sign
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
@Data
public class ScoreboardSign {
    private final Sign sign;
    private final Skull skull;
    private int topId;
    private String name;
    private int diamonds;


    /**
     * Updates this scoreboard sign alongside with the skull.
     */
    public void update() {
        SignBuilder signBuilder = new SignBuilder(getSign());
        signBuilder.line(0, "Top " + (topId) + " Diamonds");
        signBuilder.line(1, "collected:");
        signBuilder.line(2, name);
        signBuilder.line(3, "" + diamonds);
        signBuilder.apply();

        // Update skull
        Skull skull = getSkull();
        skull.setOwner(name);
        skull.update(true);
    }

}
