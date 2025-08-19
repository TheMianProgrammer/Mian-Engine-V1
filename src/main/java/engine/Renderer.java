package engine;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import lightning.Ray;
import lightning.Sun;
import obj.Entity;
import shader.Shader;
import text.Text;

public class Renderer {
    Shader shader;
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
        sun = new Sun(new Vector3f(20, 50, 20), 50);
        sun.attenuation = 40;
        this.EngineWindow = window;
    }
    /// Caculate all vertecies 
    /// and put them into the Buffer
    ///
    /// ```
    /// VertexPosition: 0
    /// Type: Vector3
    /// ```
    void CaculateVerteciesBuffer(Entity entity)
    {
        List<Float> allVertices = new ArrayList<>();
        for(Triangle t : entity.mesh)
            for(float v : t.getVertices()) allVertices.add(v);

        float[] verts = new float[allVertices.size()];
        for (int i = 0; i < allVertices.size(); i++) verts[i] = allVertices.get(i);

        vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(verts.length);
        vertexBuffer.put(verts).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);

        // location=0 → positions
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
    }

    /// Caculate all Colors
    /// and put them into the Buffer
    /// ```
    /// VertexPosition: 1
    /// Type: Vector3
    /// ```
    void CaculateColorBuffer(Entity entity)
    {
        List<Float> allColors = new ArrayList<>();
        for (Triangle tri : entity.mesh) {
            Vector3f c = tri.color;
            // normalize from 0..255 to 0..1 if user used 255 values
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

        // layout(location = 1) vec3 color
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
    }
    /// Caculate the Lights
    /// and put them into the Buffer
    /// ```
    /// VertexPosition: 3
    /// Type: Float
    /// ```
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

        // GL30.glBindVertexArray(entity.vao);
// 
        // // Mach platz auf der GPU
        // entity.lightVbo = GL15.glGenBuffers();
        // GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, entity.lightVbo);
        // FloatBuffer buffer = BufferUtils.createFloatBuffer(strengthArray.length);
        // buffer.put(strengthArray).flip();
        // GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
// 
        // // VertexAttribPointer, location = 3
        // GL20.glEnableVertexAttribArray(3);
        // GL20.glVertexAttribPointer(3, 1, GL11.GL_FLOAT, false, Float.BYTES, 0);
    }

    /// Caculate the Normals
    /// and put them into the Buffer
    /// ```
    /// VertexPosition: 2
    /// Type: Vector3
    /// ```
    void CaculateNormalBuffer(Entity entity)
    {
        Map<Vector3f, Vector3f> vertexNormals = new HashMap<>(new IdentityHashMap<>()); // IdentityMap für richtige Keys
        for (Triangle t : entity.mesh) {
            // Bekomme die kanten
            Vector3f edge1 = new Vector3f(t.v2).sub(t.v1);
            Vector3f edge2 = new Vector3f(t.v3).sub(t.v1);
            // Bekomme die richtung der Kanten
            Vector3f triNormal = edge1.cross(edge2, new Vector3f()).normalize();

            // Mach es für die Liste verwendbar
            vertexNormals.putIfAbsent(t.v1, new Vector3f());
            vertexNormals.putIfAbsent(t.v2, new Vector3f());
            vertexNormals.putIfAbsent(t.v3, new Vector3f());

            // Pack es in die Liste
            vertexNormals.get(t.v1).add(triNormal);
            vertexNormals.get(t.v2).add(triNormal);
            vertexNormals.get(t.v3).add(triNormal);
        }
        // Normalen normalisieren
        for (Vector3f n : vertexNormals.values()) n.normalize();

        // Normale für VBO in Reihenfolge der Dreiecke
        List<Float> allNormals = new ArrayList<>();
        for (Triangle t : entity.mesh) {
            Vector3f n1 = vertexNormals.get(t.v1);
            Vector3f n2 = vertexNormals.get(t.v2);
            Vector3f n3 = vertexNormals.get(t.v3);

            allNormals.add(n1.x); allNormals.add(n1.y); allNormals.add(n1.z);
            allNormals.add(n2.x); allNormals.add(n2.y); allNormals.add(n2.z);
            allNormals.add(n3.x); allNormals.add(n3.y); allNormals.add(n3.z);
        }

        // ---- Normals VBO ----
        float[] normalFloats = new float[allNormals.size()];
        for (int i = 0; i < allNormals.size(); i++) normalFloats[i] = allNormals.get(i);

        entity.normalVbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, entity.normalVbo);
        FloatBuffer buffer1 = BufferUtils.createFloatBuffer(normalFloats.length);
        buffer1.put(normalFloats).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer1, GL15.GL_STATIC_DRAW);

        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 3*Float.BYTES, 0);
    }
    void CaculateUVBuffer(Entity entity) {
        List<Float> allUVs = new ArrayList<>();
        for (Triangle t : entity.mesh) {
            allUVs.add(t.uv1.x); allUVs.add(t.uv1.y);
            allUVs.add(t.uv2.x); allUVs.add(t.uv2.y);
            allUVs.add(t.uv3.x); allUVs.add(t.uv3.y);
        }

        float[] uvs = new float[allUVs.size()];
        for (int i = 0; i < allUVs.size(); i++) uvs[i] = allUVs.get(i);

        int uvVbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, uvVbo);
        FloatBuffer uvBuffer = BufferUtils.createFloatBuffer(uvs.length);
        uvBuffer.put(uvs).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, uvBuffer, GL15.GL_STATIC_DRAW);

        // location=3 → uv
        GL20.glEnableVertexAttribArray(4);
        GL20.glVertexAttribPointer(4, 2, GL11.GL_FLOAT, false, 2 * Float.BYTES, 0);
    }

    void CaculateFontBuffer()
    {
        int VertecieCount = 0;
        List<float[]> Fonts = new ArrayList<>();
        for(Text text : texts)
        {
            Fonts.add(text.getVertecies());
            VertecieCount += text.getVertecies().length;
        }

        float[] FontVertecies = new float[VertecieCount];
        for(int i = 0; i < Fonts.size(); i++)
        {
            for(float Vertecie : Fonts.get(i))
            {
                FontVertecies[i] = Vertecie;
            }
        }
        fontVbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, fontVbo);
        FloatBuffer fontBuffer = BufferUtils.createFloatBuffer(VertecieCount);
        fontBuffer.put(FontVertecies).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fontBuffer, GL15.GL_STATIC_DRAW);

        GL20.glEnableVertexAttribArray(3);
        GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, Float.BYTES, 0);
    }

    public void init()
    {
        try {
            shader = new Shader("assets/shader/basic/vertex.vert", "assets/shader/basic/fragment.frag");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1); // stop program if shader can't be loaded
        }
        // texts.add(new Text("Hey"));

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glEnable(GL11.GL_CULL_FACE); 
        GL11.glCullFace(GL11.GL_FRONT);
        GL11.glFrontFace(GL11.GL_CCW);


        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao); // ← vor jedem Buffer-Aufruf
        
        for(Entity entity : Entites)
        {
            // GL30.glBindVertexArray(entity.vao);
            // CaculateVerteciesBuffer(entity);
            // CaculateColorBuffer(entity);
            // CaculateLightBuffer(entity);
            // CaculateNormalBuffer(entity);
            // CaculateUVBuffer(entity);
            // GL30.glBindVertexArray(0);
            if(entity.vao == 0) entity.initGLBuffers();
        }
       // CaculateFontBuffer();
        
        // unbind array buffer and VAO (good hygiene)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    public void render(Long window)
    {
        shader.use();
        for(Entity entity : Entites)
        {
            InitShaderVariables(entity);
            // CaculateLightBuffer(entity);
        }

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

        Matrix4f model = new Matrix4f();
        model
            .translate(entity.Position)
            .scale(entity.Scale);
        Matrix4f view = camera.getViewMatrix(); 
        Matrix4f projection = camera.getProjectionMatrix((float)Math.toRadians(60), EngineWindow.getWidth()/EngineWindow.getHeight(), 0.1f, 2000f);

        try(MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);

            shader.setUniformMat4("model", model.get(fb));
            shader.setUniformMat4("view", view.get(fb));
            shader.setUniformMat4("projection", projection.get(fb));
        }
        
        GL30.glBindVertexArray(entity.vao);        
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, entity.mesh.length * 3);
    }

    public void UpdateModel(Entity entity)
    {
        Matrix4f model = new Matrix4f().translate(entity.Position).scale(entity.Scale);
    }
    public void UpdateEntityExposure(Entity entity)
    {
        CaculateLightBuffer(entity);
    }
    public float getExposure(int i, Entity entity) {
        return ExposureEntities.get(entity)[i];    
    }
}
