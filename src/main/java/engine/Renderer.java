package engine;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import engine.render.BufferBuilder;
import engine.render.ColorBuffer;
import engine.render.LightBuffer;
import engine.render.NormalBuffer;
import engine.render.UVBuffer;
import engine.render.VertexBuffer;
import lightning.Ray;
import lightning.Sun;
import obj.Entity;
import shader.Shader;
import text.Text;
import texture.Texture;

public class Renderer {
    private final List<BufferBuilder> bufferBuilders = new ArrayList<>();
    
    public Texture defaulTexture;

    Shader shader;
    Shader depthShader;
    Sun sun;

    public Map<Entity, float[]> ExposureEntities = new HashMap<>();

    public List<Entity> Entites = new ArrayList<>();
    public List<Text> texts = new ArrayList<>();
    public List<Triangle> triangles = new ArrayList<>();
    int vao;
    int vbo;
    int colorVbo;
    int lightVbo;
    int normalVbo;
    int fontVbo;
    int entityVbo;

    Window EngineWindow;
    Camera camera;
    public Renderer(Camera cam, Window window)
    {
        this.camera = cam;
        sun = new Sun(new Vector3f(20, 50, 20), 1);
        sun.attenuation = 1;
        this.EngineWindow = window;
        
        bufferBuilders.add(new VertexBuffer());
        bufferBuilders.add(new ColorBuffer());
        bufferBuilders.add(new NormalBuffer());
        bufferBuilders.add(new UVBuffer());

        // LightBuffer lightBuffer = new LightBuffer();
        // lightBuffer.renderer = this;
        // bufferBuilderas.add(lightBuffer);
    }
    public void initEntity(Entity entity)
    {
        if(entity.vao == 0)
            entity.vao = GL30.glGenVertexArrays();

        // CaculateLightBuffer(entity);

        GL30.glBindVertexArray(entity.vao);

        for(BufferBuilder builder : bufferBuilders)
        {
            builder.build(entity);
        }

        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }
    void CaculateLightBuffer(Entity entity)
    {
        List<Float> strength = new ArrayList<>();
        for(Triangle t : entity.mesh){
            float[] verticies = t.getVertices();
            for (int i = 0; i < verticies.length; i+=3)
            {
                Vector3f vertex = new Vector3f(verticies[i], verticies[i+1], verticies[i+2]);
                // Shoot ray
                Vector3f rayDir = new Vector3f(vertex).sub(sun.Position).normalize();
                Ray ray = new Ray(sun.Position, rayDir);

                boolean blocked = false;
                for (Triangle other : triangles) {
                    float[] ov = other.getVertices();
                    float dist = ray.intersectTriangle(
                        new Vector3f(ov[0], ov[1], ov[2]), 
                        new Vector3f(ov[3], ov[4], ov[5]), 
                        new Vector3f(ov[6], ov[7], ov[8])
                    );

                    if (dist > 0 && dist < vertex.distance(sun.Position))
                    {
                        blocked = true;
                        break;
                    }
                }

                if(!blocked) 
                    strength.add(sun.getStrength(vertex));
                else
                    strength.add(0F);
            }
        }

        float[] strengthArray = new float[strength.size()];
        for(int i = 0; i < strength.size(); i++) strengthArray[i] = strength.get(i);
        ExposureEntities.put(entity, strengthArray);
    }
    public void init()
    {
        try {
            shader = new Shader("assets/shader/basic/vertex.vert", "assets/shader/basic/fragment.frag");
            depthShader = new Shader("assets/shader/basic/depth/depth.vert", "assets/shader/basic/depth/depth.frag");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1); // stop program if shader can't be loaded
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glEnable(GL11.GL_CULL_FACE); 
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glFrontFace(GL11.GL_CCW);

        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao); // ← vor jedem Buffer-Aufruf
        
        for(Entity entity : Entites)
        {
            if(entity.vao == 0) entity.initGLBuffers();
        }
        
        // unbind array buffer and VAO (good hygiene)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }
    public static FloatBuffer toFloatBuffer(Matrix4f mat) {
        FloatBuffer fb = FloatBuffer.allocate(16);
        mat.get(fb);
        fb.flip();
        return fb;
    }
    public void render(Long window)
    {
        shader.use();
        for(Entity entity : Entites)
        {
            InitShaderVariables(entity);
            GL30.glBindVertexArray(entity.vao);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, entity.mesh.length*3);
            // CaculateLightBuffer(entity);
        }

        // Shadows
        sun.UpdateShadows();
        depthShader.use();
        for (Entity e : Entites){
            e.renderDepth(depthShader, sun.getLightSpaceMatrix());
        }
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }
    public void clear() {
        GL11.glClearColor(0.2f, 0.3f, 0.3f, 0.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }
    void InitShaderVariables(Entity entity)
    {
        shader.setUniform1f("time", (float)GLFW.glfwGetTime());
        shader.setUniform1f("texture1", 0);
        shader.setUniform1f("sunPower", sun.strength);
        shader.setUniform1f("fSpecular", entity.Specular);
        shader.setUniform2f("res", EngineWindow.getWidth(), EngineWindow.getHeight());
        shader.setUniform3f("sunPos", sun.Position);
        shader.setUniform3f("viewSource", camera.position);
        shader.setUniform1f("exposure", 1);

        Matrix4f model = new Matrix4f();

        Quaternionf jq = new Quaternionf(
            entity.Rotation.x,
            entity.Rotation.y,
            entity.Rotation.z,
            entity.Rotation.w
        );

        model
        .identity()
        .translate(new Vector3f(
                entity.Position.x + entity.RenderOffset.x,
                entity.Position.y + entity.RenderOffset.y,
                entity.Position.z + entity.RenderOffset.z
                ))
            .rotate(jq)
            .scale(entity.Scale);
        Matrix4f view = camera.getViewMatrix(); 
        float aspect = (float)EngineWindow.getWidth() / (float)EngineWindow.getHeight();
        Matrix4f projection = camera.getProjectionMatrix((float)Math.toRadians(77), aspect, 0.1f, 2000f);

        try(MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);

            shader.setUniformMat4("model", model.get(fb));
            shader.setUniformMat4("view", view.get(fb));
            shader.setUniformMat4("projection", projection.get(fb));
        }
    }

    public void UpdateModel(Entity entity)
    {
        // Matrix4f model = new Matrix4f().translate(entity.Position).scale(entity.Scale);
    }
    public void UpdateEntityExposure(Entity entity)
    {
        CaculateLightBuffer(entity);
    }
    public float getExposure(int i, Entity entity) {        
        return ExposureEntities.get(entity)[i];    
    }
}
