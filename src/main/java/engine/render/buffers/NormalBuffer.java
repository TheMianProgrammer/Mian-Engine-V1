package engine.render.buffers;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import engine.render.Triangle;
import obj.entity.Entity;

public class NormalBuffer implements BufferBuilder {
    @Override
    public void build(Entity entity)
    {
        List<Float> allNormals = new ArrayList<>();
        for (Triangle t : entity.mesh)
        {
            for (int i = 0; i < 3; i++)
            {
                allNormals.add(t.normal.x);
                allNormals.add(t.normal.y);
                allNormals.add(t.normal.z);
            }
        }

        float[] normals = new float[allNormals.size()];
        for (int i = 0; i < allNormals.size(); i++) normals[i] = allNormals.get(i);

        entity.normalVbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, entity.normalVbo);
        FloatBuffer buf = BufferUtils.createFloatBuffer(normals.length);
        buf.put(normals).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW);

        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 3*Float.BYTES, 0);   
    }
}
