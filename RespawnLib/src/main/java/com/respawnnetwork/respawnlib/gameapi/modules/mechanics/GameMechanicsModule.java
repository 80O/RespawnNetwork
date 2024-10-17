package com.respawnnetwork.respawnlib.gameapi.modules.mechanics;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

/**
 * Represents a module that adds a few general game mechanics to the game.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
@Getter
public class GameMechanicsModule<G extends Game> extends GameModule<G> implements Listener {
    @Setter
    private boolean weatherChanges;


    public GameMechanicsModule(G game) {
        super(game);
    }

    @Override
    protected boolean onEnable() {
        weatherChanges = getConfig().getBoolean("weatherChanges", false);

        return true;
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(!weatherChanges);
    }

    @Override
    public String getDisplayName() {
        return "Game mechanics";
    }

    @Override
    public String getName() {
        return "mechanics";
    }

}
