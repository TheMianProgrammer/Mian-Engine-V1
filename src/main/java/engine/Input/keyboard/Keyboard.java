package engine.Input.keyboard;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

public class Keyboard
{
    private long window;

    private final Map<Integer, Boolean> keys = new HashMap<>();
    private final Map<Integer, Boolean> keysLastFrame = new HashMap<>();

    public Keyboard(long window) {
        this.window = window;
    }

    public void update() {
        // vorherige Tasten speichern
        keysLastFrame.clear();
        keysLastFrame.putAll(keys);

        // alle bekannten Keys aktualisieren
        for (int key : keys.keySet()) {
            keys.put(key, GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS);
        }
    }
    public void RegisterKeyboard()
    {
        for(KeyCode key : KeyCode.values())
        {
            registerKey(key);
        }
    }

    public void registerKey(KeyCode key){
        keys.put(key.glfwKey, false);
        keysLastFrame.put(key.glfwKey, false);
    }

    public boolean getKey(KeyCode key) {
        return keys.getOrDefault(key.glfwKey, false);
    }

    public boolean getKeyDown(KeyCode key) {
        return getKey(key) && !keysLastFrame.getOrDefault(key.glfwKey, false);
    }

    public boolean getKeyUp(KeyCode key) {
        return !getKey(key) && keysLastFrame.getOrDefault(key.glfwKey, false);
    }
}
//Test message