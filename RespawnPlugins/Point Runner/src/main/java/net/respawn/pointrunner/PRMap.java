package net.respawn.pointrunner;

import com.respawnnetwork.respawnlib.gameapi.maps.GameMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;


public class PRMap extends GameMap{
    @Getter
    @Setter
    private PRPlayer winner;


    public PRMap(String worldName, ConfigurationSection config) {
        super(worldName, config);
    }

}
