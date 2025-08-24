package engine;

import java.nio.FloatBuffer;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.xml.sax.SAXException;

import debug.GLMemoryTracker;
import keyboard.KeyCode;
import keyboard.Keyboard;
import obj.Entity;
import obj.Hitbox;
import obj.MeshHitbox;
import obj.Physics;
import obj.Player;
import texture.Texture;
import server.server.GameServer;
import server.server.Server;

public class Main{
    static Window window;
    static Camera camera;
    static Player player;
    static GameServer server;
    static Renderer renderer;
    static Keyboard Input;
    static Debug debug;

    static int D_MonkeyHitboxVao;
    static int D_TerrainHitboxVao;

    static Entity Monkey;
    static Entity Terrain;
    static Entity Water;

    static Texture GrassTexture;

    static float deltaTime;

    static long lastTime = System.nanoTime();

    static float speed = 10F;
    static float sensitivty=100F;

    static int viewMode = 0;
    public static void main(String[] args) {
        window = new Window();
        window.init();
        server = new GameServer();
        server.Clients.add(new Main());
        player = new Player(new Vector3f(0, 10, 0));
        server.player = player;
        renderer = new Renderer(player.camera, window);
        server.init();
        // LoadImages();
        
        //RegisterEntitys();
        renderer.init();

        debug = new Debug(renderer);

        Input = new Keyboard(window.getWindow());
        RegisterKeyboard();

        while (!window.shouldClose()) {
            renderer.clear();
            renderer.render(window.getWindow());
            window.update();
            Input.update();
            server.tick();

            UpdateDeltaTime();
            HandleKeyboardEvents();
        }

        window.destroy();
    }
    public void ActivateEntity(Entity e, Texture texture){
        e.ERenderer = renderer;
        e.Activate(texture);
    }
    static void UpdateDeltaTime()
    {
        long now = System.nanoTime();
        deltaTime = (now - lastTime) / 1_000_000_000f; // ns → s
        lastTime = now;
    }
    static void LoadImages()
    {
        GrassTexture = new Texture("assets/texture/grass/VoxelGrass.png");
    }
    static void RegisterEntitys()
    {
        Monkey = new Entity(new Vector3f(0, 30,0), new Vector3f(255, 0, 0), new Vector3f(1, 1, 1), renderer);
        Monkey.LoadMesh("assets/obj/Monkey/monkey.obj");
        Monkey.Activate(GrassTexture);
        server.initEntity(Monkey);

        Entity box = new Entity(new Vector3f(0, 20, 0), new Vector3f(), new Vector3f(3, 3, 3), renderer);
        box.LoadMesh("assets/obj/basics/cube.obj");
        box.Activate(GrassTexture);
        server.initEntity(box);

        Terrain = new Entity(new Vector3f(0, -10, 0), new Vector3f(255, 255, 255), new Vector3f(10, 10, 10), renderer);
        Terrain.RenderOffset = new Vector3f(0, -21, 0);
        Terrain.LoadMesh("assets/obj/terrain/VoxelMap2.obj");
        Terrain.TillTexture(new Vector2f(100, 100));
        Terrain.Activate(GrassTexture);
        Terrain.isStatic = true;
        server.initMeshEntity(Terrain);
    }
    static void RegisterKeyboard()
    {
        for(KeyCode key : KeyCode.values())
        {
            Input.registerKey(key);
        }
    }
    
    static void HandleKeyboardEvents()
    {
        player.camera.deltaTime = deltaTime;

        if(Input.getKey(KeyCode.W))
            player.translate(new Vector3f(speed, 0, 0));
        if(Input.getKey(KeyCode.A))
            player.translate(new Vector3f(0, 0, -speed));
        if(Input.getKey(KeyCode.S))
            player.translate(new Vector3f(-speed, 0, 0));
        if(Input.getKey(KeyCode.D))
            player.translate(new Vector3f(0, 0, speed));

        if(Input.getKey(KeyCode.SPACE))
            player.translate(new Vector3f(0, speed, 0));
        if(Input.getKey(KeyCode.C))
            player.translate(new Vector3f(0, -speed, 0));


        if(Input.getKey(KeyCode.J))
            player.rotate(0, -sensitivty);
        if(Input.getKey(KeyCode.K))
            player.rotate(-sensitivty, 0);
        if(Input.getKey(KeyCode.L))
            player.rotate(0, sensitivty);
        if(Input.getKey(KeyCode.I))
            player.rotate(sensitivty, 0);
        if(Input.getKeyDown(KeyCode.G))
        {
            /*if(D_MonkeyHitboxVao == 0)
            {
                D_MonkeyHitboxVao = debug.AddDebugBox(Monkey.Position, Monkey.Scale, new Vector3f(255, 255, 255));
            }
            else
            {
                debug.RemoveDebugBox(D_MonkeyHitboxVao);
                D_MonkeyHitboxVao = 0;
            }*/
            GLMemoryTracker.printSummary();

            viewMode++;
            switch (viewMode) {
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
                    viewMode = 0;
                    GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE);
                    break;
            }
        }
    }
}