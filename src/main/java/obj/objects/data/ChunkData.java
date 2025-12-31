package obj.objects.data;

import org.joml.Vector3i;

import engine.render.texture.Texture;

public class ChunkData {
    public Vector3i[] positions = new Vector3i[16*16];
    public Texture[] textures = new Texture[16*16];
}
