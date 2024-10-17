package com.respawnnetwork.respawnlib.gameapi.maps;

import java.util.List;

/**
 * Represents a constructor for map cycles.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public interface MapCycleConstructor<M extends GameMap> {

    /**
     * Creates a new map cycle using the given map list.
     *
     * @param maps The maps to cycle through
     * @return The constructed map cycle
     */
    MapCycle<M> construct(List<M> maps);

}
