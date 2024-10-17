package com.respawnnetwork.respawnlib.gameapi.maps;

import com.respawnnetwork.respawnlib.math.MersenneTwisterFast;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Defines the next map returned by {@link #getNext()}.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 * @param <M> The map type
 */
@AllArgsConstructor
public abstract class MapCycle<M extends GameMap> {
    /** The name of the default map cycle */
    public static final String DEFAULT_NAME = "linear";

    /** The list of maps to cycle through */
    @Getter(AccessLevel.PROTECTED)
    private final List<M> maps;


    /**
     * Returns the next map that should be played.
     * If no next map should be started this will return null.
     *
     * @return The next map, otherwise null.
     */
    public abstract M getNext();

    /**
     * Returns the number of maps this cycle needs at least.
     * <p />
     * This takes into account that the server at least have one map (If no maps has been
     * configured the server will stop long beforehand).
     *
     * @return The least amount of maps needed
     */
    public abstract int getMinimumNeededMaps();


    /**
     * Represents a map cycle that is almost the same as linear but will not jump back to the
     * beginning (no endless mode)
     *
     * @author spaceemotion
     * @version 1.0
     * @since 1.0.1
     * @param <M> The map type
     */
    public static class Once<M extends GameMap> extends MapCycle<M> {
        private Iterator<M> iterator;


        public Once(List<M> maps) {
            super(maps);

            iterator = maps.iterator();
        }

        @Override
        public M getNext() {
            return iterator.hasNext() ? iterator.next() : null;
        }

        @Override
        public int getMinimumNeededMaps() {
            return 1;
        }

    }

    /**
     * Represents a map cycle that just chooses chooses the next map in the map list when another is done.
     *
     * @author spaceemotion
     * @version 1.0
     * @since 1.0.1
     * @param <M> The map type
     */
    public static class Linear<M extends GameMap> extends MapCycle<M> {
        private Iterator<M> iterator;


        public Linear(List<M> maps) {
            super(maps);
        }

        @Override
        public M getNext() {
            // We cannot start a game with 0 maps
            if (getMaps().size() == 0) {
                return null;
            }

            // Create new iterator whenever we need one
            if (iterator == null) {
                iterator = getMaps().iterator();
            }

            // Get next map
            M map = iterator.next();

            // Delete iterator when we're done
            if (!iterator.hasNext()) {
                iterator = null;
            }

            return map;
        }

        @Override
        public int getMinimumNeededMaps() {
            // We always need one map to switch to
            return 2;
        }

    }

    /**
     * Represents a map cycle that chooses a random map as the next one.
     *
     * @author spaceemotion
     * @version 1.0
     * @since 1.0.1
     * @param <M> The map type
     */
    public static class Randomized<M extends GameMap> extends MapCycle<M> {
        private MersenneTwisterFast r = new MersenneTwisterFast();
        private int before = 0;


        public Randomized(List<M> maps) {
            // We convert the list to an array map for faster get access
            super(new ArrayList<>(maps));
        }

        @Override
        public M getNext() {
            int next = getNextMapId();
            before = next;

            return getMaps().get(next);
        }

        private int getNextMapId() {
            int next = 0;

            // Get random number until we get a different map than before
            while (next == before) {
                next = r.nextInt(getMaps().size());
            }

            return next;
        }

        @Override
        public int getMinimumNeededMaps() {
            // (like linear)
            return 2;
        }

    }

}
