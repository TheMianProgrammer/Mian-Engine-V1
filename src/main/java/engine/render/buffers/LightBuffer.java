package engine.render.buffers;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import engine.render.Renderer;
import engine.render.Triangle;
import obj.entity.Entity;

public class LightBuffer implements BufferBuilder{
    public Renderer renderer;

    @Override
    public void build(Entity entity) {
        entity.exposureVbo = GL15.glGenBuffers();

        renderer.UpdateEntityExposure(entity);
        float[] exposureArray = new float[entity.mesh.length*3];
        for(int i=0;i<exposureArray.length;i++) exposureArray[i] = renderer.getExposure(i, entity); // Beispielwert
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, entity.exposureVbo);
        FloatBuffer exposureBuffer = BufferUtils.createFloatBuffer(exposureArray.length);
        exposureBuffer.put(exposureArray).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, exposureBuffer, GL15.GL_STATIC_DRAW);
        GL20.glEnableVertexAttribArray(3); // location 3
        GL20.glVertexAttribPointer(3, 1, GL11.GL_FLOAT, false, Float.BYTES, 0);
    }
    
}
