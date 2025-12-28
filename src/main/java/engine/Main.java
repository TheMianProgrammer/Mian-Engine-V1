package engine;

import java.sql.Time;

import org.joml.Vector3f;

import debug.Debug;
import engine.Input.InputManager.InputManager;
import engine.Input.InputManager.InputManagerContext;
import engine.render.Renderer;
import engine.render.texture.Texture;
import obj.entity.Entity;
import obj.entity.player.Player;
import server.main.GameServer;

public class Main{
    static Window window;
    static Player player;
    static GameServer server;
    static Renderer renderer;
    static Debug debug;
    static InputManager inputManager;

    static Entity Monkey;
    static Entity Terrain;
    static Entity Water;

    static Texture GrassTexture;

    static float deltaTime;
    static long lastTime = System.nanoTime();

    static double RenderTime, WindowUpdateTime, ServerTickTime, PlayerUpdateTime,InputManagerTime;

    public static void main(String[] args) {
        window = new Window();
        window.init();
        server = new GameServer();
        server.Clients.add(new Main());
        player = new Player(new Vector3f(0, 10, 0));
        server.player = player;
        renderer = new Renderer(player.camera, window);
        server.init();
        renderer.init();
        debug = new Debug(renderer);
        inputManager = new InputManager(new InputManagerContext(window.getWindow()));

        debug.setInput(inputManager.getInput());
        player.setInput(inputManager.getInput());

        double lastRenderTime, lastWindowUpdateTime, lastServerTickTime, lastPlayerUpdateTime, lastInputManagerTime;

        while (!window.shouldClose()) {
            lastRenderTime = System.currentTimeMillis();
            renderer.clear();
            renderer.render(window.getWindow());
            RenderTime = System.currentTimeMillis() - lastRenderTime;

            lastWindowUpdateTime = System.currentTimeMillis();
            window.update();
            WindowUpdateTime = System.currentTimeMillis() - lastWindowUpdateTime;

            lastServerTickTime = System.currentTimeMillis();
            server.tick();
            ServerTickTime = System.currentTimeMillis() - lastServerTickTime;
            
            lastPlayerUpdateTime = System.currentTimeMillis();
            player.update();
            PlayerUpdateTime = System.currentTimeMillis() - lastPlayerUpdateTime;

            lastInputManagerTime = System.currentTimeMillis();
            inputManager.update();
            InputManagerTime = System.currentTimeMillis() - lastInputManagerTime;

            lastWindowUpdateTime = System.currentTimeMillis();
            UpdateDeltaTime();
            debug.CheckForKeyboard();
            player.camera.deltaTime = deltaTime;
            WindowUpdateTime = System.currentTimeMillis() - lastWindowUpdateTime;
        }

        window.destroy();
    }

    public static void LogDebugCacTimes()
    {
        System.out.println("Render Time: " + RenderTime);
        System.out.println("Window Update Time: " + WindowUpdateTime);
        System.out.println("Server Tick Time: " + ServerTickTime);
        System.out.println("Player Update Time: " + PlayerUpdateTime);
        System.out.println("Input Manager Time: " + InputManagerTime);
        System.out.println("Last Window Update Time: " + WindowUpdateTime);
    }

    public void ActivateEntity(Entity entity, Texture texture){
        entity.rendererComponent = renderer;
        entity.Activate(texture);
    }

    static void UpdateDeltaTime() {
        long now = System.nanoTime();
        deltaTime = (now - lastTime) / 1_000_000_000f; // ns → s
        lastTime = now;
    }
    static void LoadImages() {
        GrassTexture = new Texture("assets/texture/grass/VoxelGrass.png");
    }
    public static GameServer GetServer()
    {
        return server;
    }
}