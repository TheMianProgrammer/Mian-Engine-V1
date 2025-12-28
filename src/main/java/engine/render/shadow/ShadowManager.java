package engine.render.shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import engine.render.lightning.Sun;
import engine.render.shader.Shader;
import obj.entity.Entity;
import util.RenderUtils;

public class ShadowManager {
    private final int SHADOW_RES = 1024;

    private int windowWidth=-1;
    private int windowHeight=-1;

    private Shader depthShader;

    Sun sun;
    List<Entity> Entities = new ArrayList<>();
    public void init()
    {
        try{
            depthShader = new Shader("assets/shader/basic/depth/depth.vert", "assets/shader/basic/depth/depth.frag");  
        } catch(Exception e)
        {
            System.out.println(e);
        }
    }
    public IntConsumer setWidth = w -> windowWidth = w; // SUPER SMALL function
    public IntConsumer setHeight= h -> windowHeight= h;
    public void render()
    {
        if(sun == null)
        {
            throw new RuntimeException("There is no sun, for the shadows!");
        }
        GL30.glViewport(0, 0, SHADOW_RES, SHADOW_RES);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, sun.shadowFBO);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(2.0f, 4.0f);
        GL11.glCullFace(GL11.GL_FRONT);;
    
        sun.UpdateShadows();
        depthShader.use();

        for (Entity entity : Entities){
            entity.renderDepth(depthShader, sun.getLightSpaceMatrix());

            RenderUtils.drawEntity(entity);
        }

        GL11.glCullFace(GL11.GL_BACK);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);


        GL11.glViewport(0, 0, windowWidth, windowHeight);
        clear();
    }
    public void setSun(Sun sun)
    {
        this.sun = sun;
    }
    public void clear() {
        GL11.glClearColor(0.2f, 0.3f, 0.3f, 0.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }
    public void addEntity(Entity entity)
    {
        this.Entities.add(entity);
    }
}
