package debug;

public class Dir{
    public static Direction opposite(Direction d) {
    return switch (d) {
        case LEFT  -> Direction.RIGHT;
        case RIGHT -> Direction.LEFT;
        case UP    -> Direction.DOWN;
        case DOWN  -> Direction.UP;
        case FRONT -> Direction.BACK;
        case BACK  -> Direction.FRONT;
    };
}
}