package obj.objects.data;

import engine.render.texture.TextureRegestry;

public enum BiomeRegestry {
    PLAINS(TextureRegestry.GRASS, TextureRegestry.DIRT, TextureRegestry.STONE, 0.2f, 1, 0.5f),
    SWAMP(TextureRegestry.GRASS, TextureRegestry.DIRT, TextureRegestry.STONE, 0.4f, 0.8f, 0.8f);

    private final TextureRegestry topLayer;
    private final TextureRegestry dirt;
    private final TextureRegestry stone;

    private final float mountainAmp;
    private final float plainsAmp;
    private final float detailAmp;

    BiomeRegestry(TextureRegestry topLayer, TextureRegestry dirt, TextureRegestry stone, float mountainAmp, float plainsAmp, float detailAmp)
    {
        this.topLayer = topLayer;
        this.dirt = dirt;
        this.stone = stone;
        this.mountainAmp = mountainAmp;
        this.plainsAmp = plainsAmp;
        this.detailAmp = detailAmp;
    }

    public TextureRegestry getTopLayer() { return topLayer; }
    public TextureRegestry getDirt() { return dirt; }
    public TextureRegestry getStone() { return stone; }
    public float getMontainsAmp() { return mountainAmp; }
    public float getPlainsAmp() { return plainsAmp; }
    public float getDetailAmp() { return detailAmp; }
}
