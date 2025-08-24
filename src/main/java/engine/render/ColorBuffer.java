package engine.render;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import engine.Triangle;
import obj.Entity;

public class ColorBuffer implements BufferBuilder {
    @Override
    public void build(Entity entity)
    {
        List<Float> allColors = new ArrayList<>();
        for (Triangle tri : entity.mesh)
        {
            Vector3f c = tri.color;
            float rx = c.x > 1f ? c.x / 255f : c.x;
            float ry = c.y > 1f ? c.y / 255f : c.y;
            float rz = c.z > 1f ? c.z / 255f : c.z;
            for (int i = 0; i < 3; i++) {
                allColors.add(rx);
                allColors.add(ry);
                allColors.add(rz);
            }
        }

        float[] colors = new float[allColors.size()];
        for (int i = 0; i < allColors.size(); i++) colors[i] = allColors.get(i);

        entity.colorVbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, entity.colorVbo);
        FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(colors.length);
        colorBuffer.put(colors).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorBuffer, GL15.GL_STATIC_DRAW);

        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
    }
}
