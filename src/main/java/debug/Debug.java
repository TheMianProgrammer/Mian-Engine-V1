package debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import engine.Main;
import engine.Input.keyboard.KeyCode;
import engine.Input.keyboard.Keyboard;
import engine.render.Renderer;
import engine.render.Triangle;
import engine.render.texture.Texture;
import obj.entity.Entity;

public class Debug {
    int debugViewMode = 0;

    public Keyboard Input;

    Renderer renderer;
    List<Entity> DebugBoxes = new ArrayList<>();

    public Debug(Renderer renderer) {
        this.renderer = renderer;
    }

    public int AddDebugBox(Vector3f position, Vector3f scale, Vector3f color){
        Entity debugBox = new Entity(position, color, scale, renderer);
        debugBox.LoadMesh("assets/obj/basics/Cube.obj");
        //renderer.initCustomRender(debugBox.mesh, debugBox.vbo, debugBox.vao);
        renderer.Entities.add(debugBox);
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
        for(Entity entity : renderer.Entities)
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

        renderer.Entities.remove(target);
        vao = 0;
    }

    public void setInput(Keyboard input){
        this.Input = input;
    }

    boolean hasPressedH = false;

    public void CheckForKeyboard()
    {
        if(Input.getKeyDown(KeyCode.G))
        {
            GLMemoryTracker.printSummary();

            debugViewMode++;
            switch (debugViewMode) {
                case 0:
                    GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE);
                    break;
                case 1: 
                    GL11.glPolygonMode(GL11.GL_BACK, GL11.GL_LINE);
                    break;
                case 2: 
                    GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
                    break;
                case 3: 
                    GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
                    break;
                default:
                    debugViewMode = 0;
                    GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE);
                    break;
            }
        }
        if(Input.getKeyDown(KeyCode.C))
        {
            renderer.RecaculateMesh();
        }
        if(Input.getKeyDown(KeyCode.Q))
        {
            Main.LogDebugCacTimes();
        }
        if (Input.getKeyDown(KeyCode.H))
        {
            if(!hasPressedH) 
            {
                Main.GetServer().UpdatePlayerChunks();
                Main.GetRenderer().PackageEntityRenderers();
                hasPressedH = true;
            }
        } else
            hasPressedH = false;
    }

    public int AddDebugMesh(Triangle[] mesh, Vector3f position, Vector3f scale, Vector3f color)
    {
        Entity debugMesh = new Entity(position, color, scale, renderer);
        debugMesh.mesh = mesh;

        renderer.Entities.add(debugMesh);
        renderer.initEntity(debugMesh);

        return debugMesh.vao;
    }
}
