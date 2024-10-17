package net.respawn.skybridgewars.modules;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.modules.mechanics.TeamMechanicsModule;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.ShopItem;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.ShopModule;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.upgrades.UpgradeableShopItem;
import com.respawnnetwork.respawnlib.gameapi.modules.team.Team;
import net.respawn.skybridgewars.SkyBridgeWars;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class SBWMechanicsModule extends TeamMechanicsModule<SkyBridgeWars> {
    private ShopModule shopModule;


    public SBWMechanicsModule(SkyBridgeWars game) {
        super(game);
    }

    @Override
    protected boolean onEnable() {
        shopModule = getGame().getModule(ShopModule.class);

        if (shopModule == null || !shopModule.isLoaded()) {
            getLogger().warning("Shop module does not exist or is not loaded! Please rearrange load order!");
            shopModule = null;
        }

        return super.onEnable();
    }

    public void resetShopItems(GamePlayer player) {
        if (shopModule == null || shopModule.getShop() == null) {
            return;
        }

        for (ShopItem shopItem : shopModule.getShop().getStock()) {
            if (!(shopItem instanceof UpgradeableShopItem)) {
                continue;
            }

            UpgradeableShopItem upgradeableShopItem = (UpgradeableShopItem) shopItem;
            upgradeableShopItem.reset(player);
        }
    }

    @Override
    protected void onResetPlayer(final GamePlayer gamePlayer, Team team) {
        super.onResetPlayer(gamePlayer, team);

        Player player = gamePlayer.getPlayer();
        if (player == null) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(getGame().getPlugin(), new Runnable() {
            @Override
            public void run() {
                resetShopItems(gamePlayer);
            }
        }, 1L);
    }

}
