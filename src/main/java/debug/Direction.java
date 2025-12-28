package debug;

import org.joml.Vector3i;

public enum Direction {
    LEFT (-1, 0, 0),
    RIGHT( 1, 0, 0),
    DOWN ( 0,-1, 0),
    UP   ( 0, 1, 0),
    BACK ( 0, 0,-1),
    FRONT( 0, 0, 1);

    public final Vector3i offset;

    Direction(int x, int y, int z) {
        offset = new Vector3i(x, y, z);
    }
}