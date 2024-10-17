package net.respawn.pointrunner.modules;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.modules.TokenModule;
import gnu.trove.map.hash.THashMap;
import net.respawn.pointrunner.PointRunner;
import org.jetbrains.annotations.Nullable;

import java.util.Map;


public class PRTokenModule extends TokenModule<PointRunner> {

    public PRTokenModule(PointRunner game) {
        super(game);
    }

    @Nullable
    @Override
    protected Map<String, Integer> getAdditionalTokensForPlayer(GamePlayer player) {
        if (!player.equals(getGame().getMap().getWinner())) {
            return null;
        }

        Map<String, Integer> map = new THashMap<>();
        map.put("winning the game", getConfig().getInt("pr.winning"));

        return map;
    }

}
