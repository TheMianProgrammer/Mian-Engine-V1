package engine.render.texture;

public enum TextureRegestry {
    STONE("stone/stone.png"),
    DIRT("dirt/dirt.png"),
    GRASS("grass/grass.png");

    private final String path;

    private TextureRegestry(String path) {
        this.path = path;
    }

    public String path()
    {
        return path;
    }
}
