package lightning;

import java.nio.ByteBuffer;

import org.joml.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

public class Sun{
    public Vector3f Position;
    public float strength;
    public float attenuation = 1.0F;

    public int shadowFBO;
    public int shadowMap;

    public Sun(Vector3f Pos, float Strength)
    {
        this.strength = Strength;
        this.Position = Pos;
    }

    public float getStrength(Vector3f object)
    {
        float distance = Vector3f.distance(object.x, object.y, object.z, Position.x, Position.y, Position.z);
        float att = attenuation / (distance * distance); // stärker fallend
        float result = strength * att;
        return result;
    }

    public Matrix4f getLightSpaceMatrix(){
        //Matrix4f lightProjection = new Matrix4f().ortho(-20, 20, -20, 20, 1, 50);
        //Matrix4f lightView = new Matrix4f().lookAt(Position, new Vector3f(0, 0,0), new Vector3f(0, 1, 0));
        Matrix4f lightSpaceMatrix = new Matrix4f();
        //lightProjection.mul(lightView, lightSpaceMatrix);
        return lightSpaceMatrix;
    }
    public void UpdateShadows()
    {
        //GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowFBO);
        //GL11.glViewport(0, 0, 1024, 1024);
        //GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void GenerateShadowBuffer()
    {
        //shadowFBO = GL30.glGenFramebuffers();
        //shadowMap = GL11.glGenTextures();
//
        //GL11.glBindTexture(GL11.GL_TEXTURE_2D, shadowMap);
        //GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, 1024, 1024, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer)null);
        //GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        //GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        //GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        //GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
//
        //GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowFBO);
        //GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, shadowMap, 0);
        //GL11.glDrawBuffer(GL11.GL_NONE);
        //GL11.glReadBuffer(GL11.GL_NONE);
        //GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }
}