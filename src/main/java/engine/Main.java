package engine;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import keyboard.KeyCode;
import keyboard.Keyboard;
import obj.Entity;
import obj.Hitbox;
import obj.MeshHitbox;
import obj.Physics;
import texture.Texture;

class Main{
    static Window window;
    static Camera camera;
    static Renderer renderer;
    static Keyboard Input;

    static Entity Monkey;
    static Hitbox MonkeyHitbox;
    static Physics MonkeyPhysics;
    static Entity Terrain;
    static MeshHitbox TerrainHitbox;
    static Entity Water;
    static Hitbox WaterHitbox;

    static Texture GrassTexture;

    static Entity Player;
    static float deltaTime;

    static long lastTime = System.nanoTime();

    static float speed = 10F;
    static float sensitivty=100F;
    public static void main(String[] args) {
        window = new Window();
        window.init();
        camera = new Camera();
        renderer = new Renderer(camera, window);
        LoadImages();
        renderer.init();

        RegisterEntitys();

        Input = new Keyboard(window.getWindow());
        RegisterKeyboard();

        while (!window.shouldClose()) {
            renderer.clear();
            renderer.render(window.getWindow());
            window.update();
            Input.update();
            // Monkey.update();
            UpdatePhysics();

            HandleKeyboardEvents();
        }

        window.destroy();
    }
    static void UpdatePhysics()
    {
        long now = System.nanoTime();
        deltaTime = (now - lastTime) / 1_000_000_000f; // ns → s
        lastTime = now;
        //MonkeyPhysics.update(deltaTime);
        //MonkeyHitbox.updatePosition(Monkey.Position);
    }
    static void LoadImages()
    {
        GrassTexture = new Texture("assets/texture/grass/grass.jpg");
    }
    static void RegisterEntitys()
    {
        Monkey = new Entity(new Vector3f(0, 15,0), new Vector3f(255, 0, 0), new Vector3f(1, 1, 1), renderer);
        Monkey.LoadMesh("assets/obj/Monkey/monkey.obj");
        Monkey.Activate(GrassTexture);
        MonkeyHitbox = new Hitbox(new Vector3f(5, 5, 5), Monkey.Position);
        MonkeyPhysics = new Physics(Monkey, MonkeyHitbox);

        //Player = new Entity(new Vector3f(0, 10, 0), new Vector3f(255, 255,255), new Vector3f(1, 1, 1));
        //Player.LoadMesh("assets/obj/basics/Cube.obj");
        //Player.Activate(renderer, GrassTexture);
        //Player.TillTexture(new Vector3f(100, 100, 100));
//
        Terrain = new Entity(new Vector3f(0, -10, 0), new Vector3f(255, 255, 255), new Vector3f(10, 10, 10), renderer);
        Terrain.LoadMesh("assets/obj/terrain/VoxelMap.obj");
        Terrain.Activate(GrassTexture);
        Terrain.TillTexture(new Vector3f(100, 100, 100));
        TerrainHitbox = new MeshHitbox(Terrain.getMeshVertecies(), new Vector3f(0, -10, 0));

        //Water = new Entity(new Vector3f(100, 100, 100), new Vector3f(1, 1, 1), new Vector3f(100, 100, 100));
        //Water.LoadMesh("assets/obj/basics/plane.obj");
        //Water.Activate(renderer, GrassTexture);
        //WaterHitbox = new Hitbox(Water.Scale, Water.Position);
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
        camera.deltaTime = deltaTime;

        if(Input.getKey(KeyCode.W))
            camera.translateForward(speed);
        if(Input.getKey(KeyCode.A))
            camera.translateRight(-speed);
        if(Input.getKey(KeyCode.S))
            camera.translateForward(-speed);
        if(Input.getKey(KeyCode.D))
            camera.translateRight(speed);

        if(Input.getKey(KeyCode.SPACE))
            camera.translate(new Vector3f(0, speed, 0));
        if(Input.getKey(KeyCode.C))
            camera.translate(new Vector3f(0, -speed, 0));


        if(Input.getKey(KeyCode.J))
            camera.rotate(0, -sensitivty);
        if(Input.getKey(KeyCode.K))
            camera.rotate(-sensitivty, 0);
        if(Input.getKey(KeyCode.L))
            camera.rotate(0, sensitivty);
        if(Input.getKey(KeyCode.I))
            camera.rotate(sensitivty, 0);
    }
}