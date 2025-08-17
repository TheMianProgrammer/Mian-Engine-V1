package engine;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import lightning.Sun;
import shader.Shader;

public class Renderer {
    Shader shader;
    Sun sun;

    public List<Triangle> triangles = new ArrayList<>();
    int vao;
    int vbo;
    int colorVbo;
    int lightVbo;
    int normalVbo;

    Camera camera;
    public Renderer(Camera cam)
    {
        this.camera = cam;
        sun = new Sun(new Vector3f(5, 5, 5), 20);
    }
    /// Caculate all vertecies 
    /// and put them into the Buffer
    ///
    /// ```
    /// VertexPosition: 0
    /// Type: Vector3
    /// ```
    void CaculateVerteciesBuffer()
    {
        List<Float> allVertices = new ArrayList<>();
        for(Triangle t : triangles)
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
    void CaculateColorBuffer()
    {
        List<Float> allColors = new ArrayList<>();
        for (Triangle tri : triangles) {
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

        colorVbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorVbo);
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
    /// VertexPosition: 2
    /// Type: Float
    /// ```
    void CaculateLightBuffer()
    {
        List<Float> strength = new ArrayList<>();
        for(Triangle t : triangles){
            float[] verticies = t.getVertices();
            for (int i = 0; i < verticies.length; i+=3)
            {
                Vector3f vertex = new Vector3f(verticies[i], verticies[i+1], verticies[i+2]);
                strength.add(sun.getStrength(vertex));
            }
        }

        float[] strengthArray = new float[strength.size()];
        for(int i = 0; i < strength.size(); i++) strengthArray[i] = strength.get(i);

        // Mach platz auf der GPU
        lightVbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lightVbo);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(strengthArray.length);
        buffer.put(strengthArray).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        // VertexAttribPointer, location = 2 (z.B.)
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, Float.BYTES, 0);
    }

    /// Caculate the Normals
    /// and put them into the Buffer
    /// ```
    /// VertexPosition: 3
    /// Type: Vector3
    /// ```
    void CaculateNormalBuffer()
    {
        Map<Vector3f, Vector3f> vertexNormals = new HashMap<>(new IdentityHashMap<>()); // IdentityMap für richtige Keys
        for (Triangle t : triangles) {
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
        for (Triangle t : triangles) {
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

        normalVbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalVbo);
        FloatBuffer buffer1 = BufferUtils.createFloatBuffer(normalFloats.length);
        buffer1.put(normalFloats).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer1, GL15.GL_STATIC_DRAW);

        GL20.glEnableVertexAttribArray(3);
        GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, 3*Float.BYTES, 0);
    }


    public void init()
    {
        try {
            shader = new Shader("assets/shader/vertex.vert", "assets/shader/fragment.frag");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1); // stop program if shader can't be loaded
        }
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LESS);

        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao); // ← vor jedem Buffer-Aufruf
        
        CaculateVerteciesBuffer();
        CaculateColorBuffer();
        CaculateLightBuffer();
        CaculateNormalBuffer();
        
        // unbind array buffer and VAO (good hygiene)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

    }

    public void render(Long window)
    {
        shader.use();
        InitShaderVariables();
    }
    public void clear() {
        GL11.glClearColor(0.2f, 0.3f, 0.3f, 0.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }
    void InitShaderVariables()
    {
        shader.setUniform1f("time", (float)GLFW.glfwGetTime());
        shader.setUniform2f("res", 800, 600);

    

        Matrix4f model = new Matrix4f();
        Matrix4f view = camera.getViewMatrix(); 
        Matrix4f projection = camera.getProjectionMatrix((float)Math.toRadians(60f), 800/600, 0.1f, 100f);

        try(MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            shader.setUniformMat4("model", model.get(fb));
            shader.setUniformMat4("view", view.get(fb));
            shader.setUniformMat4("projection", projection.get(fb));
        }

        GL30.glBindVertexArray(vao);        
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, triangles.size() * 3);

        // 1. Alle Vector3f in ein Float-Array packen
        shader.setUniform3f("viewSource", camera.position.x, camera.position.y, camera.position.z);
    }
}
