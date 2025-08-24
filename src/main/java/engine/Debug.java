package engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import obj.Entity;
import texture.Texture;

public class Debug {
    Renderer renderer;
    List<Entity> DebugBoxes = new ArrayList<>();

    public Debug(Renderer renderer) {
        this.renderer = renderer;
    }

    public int AddDebugBox(Vector3f position, Vector3f scale, Vector3f color){
        Entity debugBox = new Entity(position, color, scale, renderer);
        debugBox.LoadMesh("assets/obj/basics/Cube.obj");
        //renderer.initCustomRender(debugBox.mesh, debugBox.vbo, debugBox.vao);
        renderer.Entites.add(debugBox);
        renderer.initEntity(debugBox);
        //if(debugBox.vao == 0) debugBox.vao = GL15.glGenBuffers();
//
        //GL30.glBindVertexArray(debugBox.vao);
        DebugBoxes.add(debugBox);

        return debugBox.vao;
    }
    public void RemoveDebugBox(int vao)
    {
        Entity target = null;
        for(Entity entity : renderer.Entites)
        {
            if(entity.vao == vao)
            {
                // found it! kill it
                target = entity;
                break;
            }
        }
        if(target == null)
        {
            System.out.println("WARNING: didnt found the entity!");
            return;
        }
        GL30.glDeleteVertexArrays(target.vao);
        GL15.glDeleteBuffers(target.vbo);

        renderer.Entites.remove(target);
        vao = 0;
    }

    public int AddDebugMesh(Triangle[] mesh, Vector3f position, Vector3f scale, Vector3f color)
    {
        Entity debugMesh = new Entity(position, color, scale, renderer);
        debugMesh.mesh = mesh;

        renderer.Entites.add(debugMesh);
        renderer.initEntity(debugMesh);

        return debugMesh.vao;
    }
}
