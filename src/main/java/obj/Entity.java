package obj;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.crypto.dsig.Transform;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import engine.Renderer;
import engine.Triangle;
import texture.Texture;

public class Entity{


    public Vector3f Position;
    public Vector3f Scale;
    public Vector3f Color;
    public float Specular = 16;

    String MeshPath;
    Renderer Renderer;

    public int vao;
    public int vbo;
    public int colorVbo;
    public int lightVbo;
    public int normalVbo;
    public int exposureVbo;
    public int uvVbo;

    public Triangle[] mesh;
    public float[] vertecies;

    public Entity(Vector3f position, Vector3f Color, Vector3f Scale, Renderer renderer)
    {
        this.Position = position;
        this.Color = Color;
        this.Scale = Scale;
        this.Renderer = renderer;
    }
    public void update()
    {
        
    }
    void InitVertecies()
    {
        vertecies = getFlattenVertecies();
    }
    public void Translate(Vector3f pos)
    {
        Position.add(pos);
        Renderer.UpdateModel(this);
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
        Renderer.UpdateEntityExposure(this);
        float[] exposureArray = new float[mesh.length*3];
        for(int i=0;i<exposureArray.length;i++) exposureArray[i] = Renderer.getExposure(i, this); // Beispielwert
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, exposureVbo);
        FloatBuffer exposureBuffer = BufferUtils.createFloatBuffer(exposureArray.length);
        exposureBuffer.put(exposureArray).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, exposureBuffer, GL15.GL_STATIC_DRAW);
        GL20.glEnableVertexAttribArray(3); // location 3
        GL20.glVertexAttribPointer(3, 1, GL11.GL_FLOAT, false, Float.BYTES, 0);
    }

    public void initGLBuffers() {
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        colorVbo = GL15.glGenBuffers();
        normalVbo = GL15.glGenBuffers();
        uvVbo = GL15.glGenBuffers();
        exposureVbo = GL15.glGenBuffers();

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
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }
    public void TillTexture(Vector3f TillFactor)
    {
        for(Triangle t : mesh)
        {
            t.uv1.mul(TillFactor.x);
            t.uv2.mul(TillFactor.y);
            t.uv3.mul(TillFactor.z);
        }
    }

    public void Activate(Texture texture)
    {
        texture.bind();
        for(Triangle tri : mesh)
        {
            Triangle coloredTriangle = new Triangle(tri.v1, tri.v2, tri.v3, tri.uv1, tri.uv2, tri.uv3, new Vector3f(1));
            Renderer.triangles.add(coloredTriangle);
        }
        Renderer.Entites.add(this);
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