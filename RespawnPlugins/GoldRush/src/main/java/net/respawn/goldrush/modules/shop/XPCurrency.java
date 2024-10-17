package net.respawn.goldrush.modules.shop;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.currency.Currency;
import org.bukkit.entity.Player;


public enum XPCurrency implements Currency {
    INSTANCE;


    @Override
    public boolean matches(String name) {
        return name.equalsIgnoreCase("xp");
    }

    @Override
    public boolean canBuy(GamePlayer gamePlayer, int amount) {
        Player player = gamePlayer.getPlayer();
        return player != null && player.getLevel() >= amount;
    }

    @Override
    public boolean give(GamePlayer gamePlayer, int amount) {
        Player player = gamePlayer.getPlayer();

        if (player != null) {
            player.setLevel(player.getLevel() + amount);

            return true;
        }

        return false;
    }

    @Override
    public boolean take(GamePlayer gamePlayer, int amount) {
        Player player = gamePlayer.getPlayer();

        if (player != null) {
            player.setLevel(player.getLevel() - amount);

            return true;
        }

        return false;
    }

    @Override
    public String getDisplayName(int amount) {
        return getDisplayName();
    }

    @Override
    public String getDisplayName() {
        return "XP";
    }

}
