package engine.render;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import debug.GLMemoryTracker;
import engine.Triangle;
import obj.Entity;

public class VertexBuffer implements BufferBuilder{

    @Override
    public void build(Entity entity) {
        List<Float> allVertices = new ArrayList<>();
        for(Triangle t : entity.mesh)
            for(float v : t.getVertices()) allVertices.add(v);

        float[] verts = new float[allVertices.size()];
        for (int i = 0; i < allVertices.size(); i++) verts[i] = allVertices.get(i);

        entity.vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, entity.vbo);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(verts.length);
        vertexBuffer.put(verts).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);

        // location=0 → positions
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
    
        GLMemoryTracker.trackVBO(entity.vbo, vertexBuffer.capacity()*Float.BYTES, "EntityVertVBo");
    }
}
