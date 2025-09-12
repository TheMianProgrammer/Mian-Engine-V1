package engine;

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

        while (!window.shouldClose()) {
            renderer.clear();
            renderer.render(window.getWindow());
            window.update();
            server.tick();
            player.update();
            inputManager.update();

            UpdateDeltaTime();
            debug.CheckForKeyboard();
            player.camera.deltaTime = deltaTime;
        }

        window.destroy();
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
}