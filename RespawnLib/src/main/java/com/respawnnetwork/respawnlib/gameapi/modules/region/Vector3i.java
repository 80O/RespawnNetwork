package com.respawnnetwork.respawnlib.gameapi.modules.region;

/**
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class Vector3i {
    public int x, y, z;

    public Vector3i() {
        this(0, 0, 0);
    }

    public Vector3i(Vector3i vector) {
        this(vector.x, vector.y, vector.z);
    }

    public Vector3i(int x, int y, int z) {
        set(x, y, z);
    }

    public void set(Vector3i v) {
        set(v.x, v.y, v.z);
    }

    public void set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

}
