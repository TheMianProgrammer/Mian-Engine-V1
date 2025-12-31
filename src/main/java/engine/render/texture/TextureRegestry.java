package engine.render.texture;

public enum TextureRegestry {
    STONE("stone/stone.png"),
    DIRT("dirt/dirt.png"),
    GRASS("grass/grass_top.png");

    private final String path;
    private Texture texture;

    private TextureRegestry(String path) {
        this.path = path;
    }

    public String path()
    {
        return "./assets/texture/" + path;
    }

    public Texture texture()
    {
        return texture;
    }

    public static void init()
    {
        for (TextureRegestry t : values()) {
            t.texture = new Texture(t.path());
        }
    }
}
