package engine.render;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.system.MemoryStack;

import engine.Window;
import engine.Input.keyboard.Keyboard;
import engine.render.buffers.BufferBuilder;
import engine.render.buffers.ColorBuffer;
import engine.render.buffers.LightBuffer;
import engine.render.buffers.NormalBuffer;
import engine.render.buffers.UVBuffer;
import engine.render.buffers.VertexBuffer;
import engine.render.lightning.Ray;
import engine.render.lightning.Sun;
import engine.render.shader.Shader;
import engine.render.shader.ShaderWatchService;
import engine.render.shadow.ShadowManager;
import engine.render.texture.Texture;
import jdk.jshell.spi.ExecutionControl;
import obj.entity.Entity;
import obj.entity.player.Camera;
import text.Text;
import util.RenderUtils;

public class Renderer {
    private final List<BufferBuilder> bufferBuilders = new ArrayList<>();

    Shader shader;
    ShaderWatchService shaderWatchService;
    Sun sun;
    ShadowManager shadowManager = new ShadowManager();

    public Map<Entity, float[]> ExposureEntities = new HashMap<>();

    private final FloatBuffer matBuf = BufferUtils.createFloatBuffer(16);

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

    int currentRenderFrame = 0;
    int shadowRenderUpdate = 100;

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
    public void addEntity(Entity entity)
    {
        this.Entites.add(entity);
        shadowManager.addEntity(entity);
    }
    public void RecaculateMesh()
    {
        Set<Triangle> uniqueTriangles = new LinkedHashSet<>(triangles);
        triangles = new ArrayList<>(uniqueTriangles);
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
        if (true) //needed for compiler :/
            throw new RuntimeException("Performance o7");
        
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
            shadowManager.init();
            shaderWatchService = new ShaderWatchService(shader);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1); // stop program if shader can't be loaded
        }

        if(sun != null)
            sun.GenerateShadowBuffer();
        else {
            sun = new Sun(new Vector3f(20, 50, 25), 1);
            sun.GenerateShadowBuffer();
        }

        shadowManager.setSun(sun);
        shadowManager.setHeight.accept(EngineWindow.getHeight());
        shadowManager.setWidth.accept(EngineWindow.getWidth());

        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, sun.shadowMap);

        GL.createCapabilities();
        GL11.glEnable(GL43.GL_DEBUG_OUTPUT);
        GL11.glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);
        GL43.glDebugMessageCallback((source, type, id, severity, length, message, userParam) -> {
            String msg = GLDebugMessageCallback.getMessage(length, message);
            System.err.println("GL DEBUG: " + msg);
        }, 0);

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
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        mat.get(fb);
        return fb;
    }
    public void render(Long window)
    {
        currentRenderFrame++;
        if(currentRenderFrame%shadowRenderUpdate != 0)
            // Shadows
            shadowManager.render();

        // Main Shader
        shader.use();

        shader.setUniform1f("time", (float)GLFW.glfwGetTime());
        shader.setUniform1i("texture1", 0);
        shader.setUniform1f("sunPower", sun.strength);
        shader.setUniform1f("exposure", 1);
        shader.setUniform2f("res", EngineWindow.getWidth(), EngineWindow.getHeight());
        shader.setUniform2f("shadowMapSize", 1024, 1024);
        shader.setUniform3f("sunPos", sun.Position);
        shader.setUniform3f("viewSource", camera.position);
        shader.setUniformMat4("lightSpaceMatrix", Renderer.toFloatBuffer(sun.getLightSpaceMatrix()));
        shader.setUniform1i("shadowMap", 1);

        for (Entity entity : Entites)
        {
            if(!entity.isEnabled) continue;
            setModelMatrix(entity);
            RenderUtils.drawEntity(entity);
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }
    private void bindTexture(Entity entity)
    {
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        if(entity.texture != null)
            entity.texture.bind();
        else
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }
    public void clear() {
        GL11.glClearColor(0.2f, 0.3f, 0.3f, 0.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }
    void setModelMatrix(Entity entity)
    {
        bindTexture(entity);

        shader.setUniform1f("fSpecular", entity.Specular);
        
        
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

        shader.setUniformMat4("model", model.get(matBuf));
        shader.setUniformMat4("view", view.get(matBuf));
        shader.setUniformMat4("projection", projection.get(matBuf));
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
