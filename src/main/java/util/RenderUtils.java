package util;

import java.util.List;

import javax.vecmath.Matrix4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import obj.entity.Entity;

public class RenderUtils {
    public static void drawEntity(Entity entity){
        GL30.glBindVertexArray(entity.vao);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, entity.mesh.length*3);
    }
}
