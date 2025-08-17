package engine;

import org.joml.Vector3f;

import keyboard.KeyCode;
import keyboard.Keyboard;
import objects.Entity;

class Main{
    static Window window;
    static Camera camera;
    static Renderer renderer;
    static Keyboard Input;

    static float speed = 0.1F;
    static float sensitivty=1F;
    public static void main(String[] args) {
        window = new Window();
        window.init();
        camera = new Camera();
        renderer = new Renderer(camera);

        RegisterEntitys();

        renderer.init();
        Input = new Keyboard(window.getWindow());
        RegisterKeyboard();

        while (!window.shouldClose()) {
            renderer.clear();
            renderer.render(window.getWindow());
            window.update();
            Input.update();

            HandleKeyboardEvents();
        }

        window.destroy();
    }
    static void RegisterEntitys()
    {
        Entity Monkey = new Entity(new Vector3f(0,0,0), new Vector3f(255, 0, 0));
        Monkey.LoadMesh("assets/obj/Monkey/monkey.obj");
        Monkey.Activate(renderer);

        Entity MonkeySmooth = new Entity(new Vector3f(3,0,0), new Vector3f(0, 255, 0));
        MonkeySmooth.LoadMesh("assets/obj/Monkey/monkeysmooth.obj");
        MonkeySmooth.Activate(renderer);

        Entity MonkeySubdivide = new Entity(new Vector3f(6,0,0), new Vector3f(0, 0, 255));
        MonkeySubdivide.LoadMesh("assets/obj/Monkey/monkeysubdivide.obj");
        MonkeySubdivide.Activate(renderer);

        Entity Plane = new Entity(new Vector3f(0, 0, 0), new Vector3f(255, 255, 255));
        Plane.LoadMesh("assets/obj/basics/Plane.obj");
        Plane.Activate(renderer);

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