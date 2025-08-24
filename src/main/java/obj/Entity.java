package obj;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Quat4f;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.bulletphysics.dynamics.RigidBody;

import debug.GLMemoryTracker;
import engine.Renderer;
import engine.Triangle;
import shader.Shader;
import texture.Texture;

public class Entity{

    public RigidBody body;

    public Vector3f Position;
    public Vector3f RenderOffset;
    public Quat4f Rotation;
    public Vector3f Scale;
    public Vector3f Color;
    public float Specular = 16;

    String MeshPath;
    public Renderer ERenderer;

    public int vao;
    public int vbo;
    public int colorVbo;
    public int lightVbo;
    public int normalVbo;
    public int exposureVbo;
    public int uvVbo;

    public Triangle[] mesh;
    public float[] vertecies;
    private float[] baseUVs;

    public boolean isStatic = false;

    public Entity(Vector3f position, Vector3f Color, Vector3f Scale, Renderer Renderer)
    {
        this.Position = position;
        this.Rotation = new Quat4f(0, 0, 0, 1);
        this.Color = Color;
        this.Scale = Scale;
        this.RenderOffset = new Vector3f(0, 0, 0);
        this.ERenderer = Renderer;
    }
    public void renderDepth(Shader depthShader, Matrix4f lightSpaceMatrix) {
        depthShader.setUniformMat4("model", Renderer.toFloatBuffer(getModelMatrix()));
        depthShader.setUniformMat4("lightSpaceMatrix", Renderer.toFloatBuffer(lightSpaceMatrix));

        GL30.glBindVertexArray(vao);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, mesh.length * 3);
        GL30.glBindVertexArray(0);
    }
    public Matrix4f getModelMatrix() {
        Matrix4f model = new Matrix4f().identity();

        model.translate(Position);
        model.rotate(new org.joml.Quaternionf(Rotation.x, Rotation.y, Rotation.z, Rotation.w));
        model.scale(Scale);

        return model;
    }

    public void update()
    {
        
    }
    void InitVertecies()
    {
        vertecies = getFlattenVertecies();
        baseUVs = getFlattenedUVs();
    }
    public void Translate(Vector3f pos)
    {
        Position.add(pos);
        ERenderer.UpdateModel(this);
    }
    public void LoadMesh(String path)
    {
        MeshPath = path;
        try{
            List<Triangle> LMesh = new ArrayList<>();

            if (!new File(path).exists()) path = "assets/obj/Error.obj";
            for(Triangle tri : Loader.loadOBJ(path)){
                Triangle loadedTriangle = new Triangle(tri.v1, tri.v2, tri.v3, tri.uv1, tri.uv2, tri.uv3, tri.color);
                loadedTriangle.uv1 = tri.uv1;
                loadedTriangle.uv2 = tri.uv2;
                loadedTriangle.uv3 = tri.uv3;
                LMesh.add(loadedTriangle); // nur lokal
            }
            mesh = LMesh.toArray(new Triangle[0]);
            for(int i = 0; i < mesh.length; i++) mesh[i] = LMesh.get(i);
        } catch (Exception e)
        {
            System.err.println("Failed to load the following model path: " + path + " ERROR: " + e);
        }
        InitVertecies();
        initGLBuffers();
    }
    public float[] getFlattenedNormals() {
        float[] normals = new float[mesh.length * 9];
        int idx = 0;
        for (Triangle t : mesh) {
            // Triangle hat t.normal (berechnet in Konstruktor)
            for (int i = 0; i < 3; i++) {
                normals[idx++] = t.normal.x;
                normals[idx++] = t.normal.y;
                normals[idx++] = t.normal.z;
            }
        }
        return normals;
    }
    public float[] getFlattenedUVs() {
        float[] uvs = new float[mesh.length * 3 * 2]; // 3 vertices * 2 floats
        int idx = 0;
        for (Triangle t : mesh) {
            // hier müsstest du pro Vertex UVs aus dem OBJ haben!
            uvs[idx++] = t.uv1.x; uvs[idx++] = t.uv1.y;
            uvs[idx++] = t.uv2.x; uvs[idx++] = t.uv2.y;
            uvs[idx++] = t.uv3.x; uvs[idx++] = t.uv3.y;
        }
        return uvs;
    }

    public float[] getFlattenVertecies() {
        float[] verts = new float[mesh.length*9]; // 3 verts * 3 floats
        int idx = 0;
        for(Triangle t : mesh) {
            verts[idx++] = t.v1.x; verts[idx++] = t.v1.y; verts[idx++] = t.v1.z;
            verts[idx++] = t.v2.x; verts[idx++] = t.v2.y; verts[idx++] = t.v2.z;
            verts[idx++] = t.v3.x; verts[idx++] = t.v3.y; verts[idx++] = t.v3.z;
        }
        return verts;
    }
    public float[] getFlattenedColorsFixed(Vector3f meshColor) {
        float[] colors = new float[mesh.length * 3 * 3]; // 3 vertices * 3 floats
        int idx = 0;
        for (Triangle t : mesh) {
            for (int i = 0; i < 3; i++) {
                colors[idx++] = t.color.x > 1f ? t.color.x / 255f : t.color.x;
                colors[idx++] = t.color.y > 1f ? t.color.y / 255f : t.color.y;
                colors[idx++] = t.color.z > 1f ? t.color.z / 255f : t.color.z;
            }
        }
        return colors;
    }
    void CaculateExposureBuffer()
    {
        // --- Exposure Buffer ---
        ERenderer.UpdateEntityExposure(this);
        float[] exposureArray = new float[mesh.length*3];
        for(int i=0;i<exposureArray.length;i++) exposureArray[i] = ERenderer.getExposure(i, this); // Beispielwert
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, exposureVbo);
        FloatBuffer exposureBuffer = BufferUtils.createFloatBuffer(exposureArray.length);
        exposureBuffer.put(exposureArray).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, exposureBuffer, GL15.GL_STATIC_DRAW);
        GL20.glEnableVertexAttribArray(3); // location 3
        GL20.glVertexAttribPointer(3, 1, GL11.GL_FLOAT, false, Float.BYTES, 0);
    }

    public void initGLBuffers() {
        /*
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        colorVbo = GL15.glGenBuffers();
        normalVbo = GL15.glGenBuffers();
        uvVbo = GL15.glGenBuffers();
        exposureVbo = GL15.glGenBuffers();
        /*
        GL30.glBindVertexArray(vao);

        // Vertex buffer
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertecies.length);
        vertexBuffer.put(vertecies).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);

        // Color buffer
        float[] colors = getFlattenedColorsFixed(Color);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorVbo);
        FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(colors.length);
        colorBuffer.put(colors).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorBuffer, GL15.GL_STATIC_DRAW);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);

        // Normal Buffer
        float[] normals = getFlattenedNormals();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalVbo);
        FloatBuffer normalBuffer = BufferUtils.createFloatBuffer(normals.length);
        normalBuffer.put(normals).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalBuffer, GL15.GL_STATIC_DRAW);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);

        CaculateExposureBuffer();
    
        // uv Buffer
        float[] uvs = getFlattenedUVs();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, uvVbo);
        FloatBuffer uvBuffer = BufferUtils.createFloatBuffer(uvs.length);
        uvBuffer.put(uvs).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, uvBuffer, GL15.GL_STATIC_DRAW);
        GL20.glEnableVertexAttribArray(4);
        GL20.glVertexAttribPointer(4, 2, GL11.GL_FLOAT, false, 2 * Float.BYTES, 0);

        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);*/
    }
    public void TillTexture(Vector2f tilling)
    {
        if(baseUVs == null)
        {
            baseUVs = getFlattenedUVs();
        }

        float[] newUVs = new float[baseUVs.length];
        for(int i = 0; i < baseUVs.length; i+=2){
            newUVs[i + 0] = baseUVs[i + 0] * tilling.x;
            newUVs[i + 1] = baseUVs[i + 1] * tilling.y;
        }

        applyUVsToMesh(newUVs);
        updateUVBuffer(newUVs);
    }

    private void applyUVsToMesh(float[] uvs)
    {
        int idx = 0;
        for(int t = 0; t < mesh.length; t++ ){
            mesh[t].uv1.x = uvs[idx++]; mesh[t].uv1.y = uvs[idx++];
            mesh[t].uv2.x = uvs[idx++]; mesh[t].uv2.y = uvs[idx++];
            mesh[t].uv3.x = uvs[idx++]; mesh[t].uv3.y = uvs[idx++];
        }
    }
    private void updateUVBuffer(float[] uvs)
    {
        if(uvVbo == 0) 
            uvVbo = GL15.glGenBuffers();
        
        GL30.glBindVertexArray(vao);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, uvVbo);
        FloatBuffer buf = BufferUtils.createFloatBuffer(uvs.length);
        buf.put(uvs).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW);

        GL30.glEnableVertexAttribArray(4);
        GL30.glVertexAttribPointer(4, 2, GL11.GL_FLOAT, false, 2 * Float.BYTES, 0);
        
        GLMemoryTracker.trackVBO(uvVbo, buf.capacity() * Float.BYTES, "uvVBO");
    }

    public void Activate(Texture texture)
    {
        texture.bind();
        for(Triangle tri : mesh) {
            ERenderer.triangles.add(new Triangle(tri.v1, tri.v2, tri.v3, tri.uv1, tri.uv2, tri.uv3, tri.color));
        }
        ERenderer.Entites.add(this);
        ERenderer.initEntity(this);
    }
    public Vector3f[] getMeshVertecies() {
        Vector3f[] Verticies = new Vector3f[mesh.length * 3];
        for(int i = 0; i < mesh.length; i++)
        {
            Verticies[i+0] = mesh[i].v1;
            Verticies[i+1] = mesh[i].v2;
            Verticies[i+2] = mesh[i].v3;
        }
        return Verticies;
    }
}