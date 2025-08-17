package objects;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import engine.Renderer;
import engine.Triangle;
import obj.Loader;

public class Entity{
    public Vector3f Position;
    public Vector3f Color;

    public Triangle[] mesh;
    public Entity(Vector3f position, Vector3f Color)
    {
        this.Position = position;
        this.Color = Color;
    }
    public void LoadMesh(String path)
    {
        try{
            List<Triangle> LMesh = new ArrayList<>();

            if (!new File(path).exists()) path = "assets/obj/Error.obj";
            for(Triangle tri : Loader.loadOBJ(path)){
                Vector3f v1 = new Vector3f(tri.v1.x + Position.x, tri.v1.y + Position.y, tri.v1.z + Position.z);
                Vector3f v2 = new Vector3f(tri.v2.x + Position.x, tri.v2.y + Position.y, tri.v2.z + Position.z);
                Vector3f v3 = new Vector3f(tri.v3.x + Position.x, tri.v3.y + Position.y, tri.v3.z + Position.z);

                LMesh.add(new Triangle(v1, v2, v3, tri.color));
            }
            mesh = new Triangle[LMesh.size()];
            for(int i = 0; i < mesh.length; i++) mesh[i] = LMesh.get(i);
        } catch (Exception e)
        {
            System.err.println("Failed to load the following model path: " + path + " ERROR: " + e);
        }
    }
    public void Activate(Renderer renderer)
    {
        for(Triangle tri : mesh)
        {
            Triangle coloredTriangle = new Triangle(tri.v1, tri.v2, tri.v3, Color);
            renderer.triangles.add(coloredTriangle);
        }
    }
}