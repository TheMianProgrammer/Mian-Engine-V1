package engine.render;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import engine.Triangle;
import obj.Entity;

public class UVBuffer implements BufferBuilder {

    @Override
    public void build(Entity entity) {
        entity.uvVbo = GL15.glGenBuffers();

        float[] uvs = entity.getFlattenedUVs();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, entity.uvVbo);
        FloatBuffer uvBuffer = BufferUtils.createFloatBuffer(uvs.length);
        uvBuffer.put(uvs).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, uvBuffer, GL15.GL_STATIC_DRAW);
        GL20.glEnableVertexAttribArray(4);
        GL20.glVertexAttribPointer(4, 2, GL11.GL_FLOAT, false, 2 * Float.BYTES, 0);
    }
    
}
