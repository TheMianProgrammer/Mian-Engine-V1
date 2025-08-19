package engine;

import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

public class Window {
    private long window;

    public int getWidth()
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            GLFW.glfwGetWindowSize(window, width, height);
            return width.get(0);
        }
    }

    public int getHeight()
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            GLFW.glfwGetWindowSize(window, width, height);
            return height.get(0);
        }
    }

    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        window = GLFW.glfwCreateWindow(2000, 1200, "LWJGL Window", 0, 0);
        if (window == 0)
            throw new RuntimeException("Failed to create the GLFW window");

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1); // V-Sync enabled → FPS capped auf Monitor Refresh (~60Hz)
        GLFW.glfwSwapInterval(0); // V-Sync disabled → keine Cap
        GL.createCapabilities();
    }

    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(window);
    }

    public void update() {
        GLFW.glfwSwapBuffers(window);
        GLFW.glfwPollEvents();
    }

    public void destroy() {
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    public Long getWindow()
    {
        return window;
    }
}
