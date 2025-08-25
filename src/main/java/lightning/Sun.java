package lightning;

import java.nio.ByteBuffer;

import org.joml.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryStack;

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
        Matrix4f lightProjection = new Matrix4f().ortho(-50, 50, -50, 50, 1, 200);
        Matrix4f lightView = new Matrix4f().lookAt(Position, new Vector3f(0, 0,0), new Vector3f(0, 1, 0));
        Matrix4f lightSpaceMatrix = new Matrix4f();
        lightProjection.mul(lightView, lightSpaceMatrix);
        return lightSpaceMatrix;
    }
    public void UpdateShadows()
    {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowFBO);
        GL11.glViewport(0, 0, 1024, 1024);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void GenerateShadowBuffer()
    { 
        shadowFBO = GL30.glGenFramebuffers();
        shadowMap = GL11.glGenTextures();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, shadowMap);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, 1024, 1024, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer)null);

        // Filters
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_BORDER);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_BORDER);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var border = stack.floats(1f, 1f, 1f, 1f);
            GL11.glTexParameterfv(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_BORDER_COLOR, border);
        }

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_MODE, GL11.GL_NONE);
        
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowFBO);
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, shadowMap, 0);
        
        GL11.glDrawBuffer(GL11.GL_NONE);
        GL11.glReadBuffer(GL11.GL_NONE);
        
        int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        if(status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Shadow FBO incomplete: 0x" + Integer.toHexString(status));
        }
        
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }
}