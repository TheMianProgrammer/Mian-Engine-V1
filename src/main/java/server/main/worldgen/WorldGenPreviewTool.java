package server.main.worldgen;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glVertex2i;
import static org.lwjgl.system.MemoryUtil.NULL;

public class WorldGenPreviewTool {

    private long window;
    private final int WIDTH = 1024;
    private final int HEIGHT = 1024;
    private WorldGen wg;

    // Blockgröße für Preview (macht es blockig)
    private final int blockSize = 4;

    // Pan & Zoom
    private double offsetX = 0;
    private double offsetZ = 0;
    private double scale = 4.0; // Start-Zoom weiter raus
    private final double scrollSpeed = 50;
    private final double zoomSpeed = 0.1;

    // Dynamische Min/Max Höhe für Graustufen
    private int yMin = Integer.MAX_VALUE;
    private int yMax = Integer.MIN_VALUE;

    public WorldGenPreviewTool() {
        wg = new WorldGen(12345); // Seed für Test
        calculateYBounds();
    }

    private void calculateYBounds() {
        // kleine grobe Berechnung, um yMin/yMax zu ermitteln
        for (int x = 0; x < 256; x++) {
            for (int z = 0; z < 256; z++) {
                int y = wg.GetOverworldY(x, z);
                if (y < yMin) yMin = y;
                if (y > yMax) yMax = y;
            }
        }
        System.out.println("yMin: " + yMin + " | yMax: " + yMax);
    }

    public void run() {
        init();
        setupInput();
        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        window = glfwCreateWindow(WIDTH, HEIGHT, "WorldGen Preview", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create GLFW window");

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
        GL.createCapabilities();

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, WIDTH, 0, HEIGHT, -1, 1);
        glMatrixMode(GL_MODELVIEW);
    }

    private void setupInput() {
        // Zoom mit Mausrad
        glfwSetScrollCallback(window, (win, xoffset, yoffset) -> {
            scale += yoffset * zoomSpeed;
            if (scale < 0.1) scale = 0.1;
            if (scale > 10) scale = 10;
        });

        // Pan mit WASD
        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                switch (key) {
                    case GLFW_KEY_W: offsetZ -= scrollSpeed / scale; break;
                    case GLFW_KEY_S: offsetZ += scrollSpeed / scale; break;
                    case GLFW_KEY_A: offsetX -= scrollSpeed / scale; break;
                    case GLFW_KEY_D: offsetX += scrollSpeed / scale; break;
                }
            }
        });
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT);

            renderHeightMap();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void renderHeightMap() {
    glBegin(GL_QUADS);
    for (int x = 0; x < WIDTH; x += blockSize) {
        for (int z = 0; z < HEIGHT; z += blockSize) {

            // World-Koordinaten für unendliche Map
            int worldX = (int)((x + offsetX) / scale);
            int worldZ = (int)((z + offsetZ) / scale);

            int y = wg.GetOverworldY(worldX, worldZ);

            // Farben festlegen
            if (y < 0) {
                // Wasser unter 0 = Blau
                glColor3f(0f, 0f, 1f);
            } else if (y < 100) {
                // Hügel 0..100 = Grün
                float factor = (float) y / 100f; // 0..1
                factor = Math.min(factor, 1f);
                glColor3f(0f, factor, 0f);
            } else if (y < yMax) {
                // Berge = Rot-Farbverlauf
                float factor = (float)(y - 100) / (yMax - 100);
                factor = Math.min(factor, 1f);
                glColor3f(factor, 0f, 0f);
            } else {
                // Sehr hohe Berge = Weiß
                glColor3f(1f, 1f, 1f);
            }

            // Block zeichnen
            glVertex2i(x, z);
            glVertex2i(x + blockSize, z);
            glVertex2i(x + blockSize, z + blockSize);
            glVertex2i(x, z + blockSize);
        }
    }
    glEnd();
}



    public static void main(String[] args) {
        new WorldGenPreviewTool().run();
    }
}
