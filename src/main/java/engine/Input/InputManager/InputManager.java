package engine.Input.InputManager;

import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;

import debug.Debug;
import debug.GLMemoryTracker;
import engine.Input.keyboard.Keyboard;

public class InputManager {
    Keyboard Input;
    
    public InputManager(InputManagerContext context){
        this.Input = new Keyboard(context.window);
        this.Input.RegisterKeyboard();
    }

    public Keyboard getInput(){
        return this.Input;
    }

    public void update()
    {
        this.Input.update();
    }
}
