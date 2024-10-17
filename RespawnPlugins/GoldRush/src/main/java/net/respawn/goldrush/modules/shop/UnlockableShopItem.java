package net.respawn.goldrush.modules.shop;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.GameShop;
import com.respawnnetwork.respawnlib.gameapi.modules.team.Team;
import com.respawnnetwork.respawnlib.gameapi.modules.team.TeamModule;
import net.respawn.goldrush.GoldRush;

import java.util.LinkedList;
import java.util.List;


public abstract class UnlockableShopItem extends ShopItem {
    private final TeamModule<GoldRush> teamModule;
    private final List<Team> unlocked;


    public UnlockableShopItem(TeamModule<GoldRush> teamModule, int price) {
        super(price);

        this.teamModule = teamModule;
        this.unlocked = new LinkedList<>();
    }

    @Override
    public boolean onBuyItem(GamePlayer gamePlayer) {
        Team team = teamModule.getTeam(gamePlayer);
        if (team == null) {
            return false;
        }

        if (isUnlocked(team)) {
            GameShop.MESSAGE.sendKey(gamePlayer.getPlayer(), GameShop.ERROR + "alreadyUnlocked");
            return false;
        }

        if (onUnlockItem(gamePlayer, team)) {
            gamePlayer.getGame().createMessage()
                    .provide("player", gamePlayer.getName())
                    .provide("item",  getDisplayName())
                    .sendKey(GameShop.SUCCESS + "unlocked");

            unlock(team);
            return true;
        }

        return false;
    }

    protected abstract boolean onUnlockItem(GamePlayer player, Team team);

    /**
     *
     * @param team
     * @return
     */
    public boolean isUnlocked(Team team) {
        return unlocked.contains(team);
    }

    /**
     *
     * @param team
     */
    public void unlock(Team team) {
        unlocked.add(team);
    }

}
