package engine.Input.keyboard;

import org.lwjgl.glfw.GLFW;

public enum KeyCode {
    SPACE(GLFW.GLFW_KEY_SPACE),
    W(GLFW.GLFW_KEY_W),
    A(GLFW.GLFW_KEY_A),
    S(GLFW.GLFW_KEY_S),
    D(GLFW.GLFW_KEY_D),
    C(GLFW.GLFW_KEY_C),
    J(GLFW.GLFW_KEY_J),
    K(GLFW.GLFW_KEY_K),
    L(GLFW.GLFW_KEY_L),
    I(GLFW.GLFW_KEY_I),
    G(GLFW.GLFW_KEY_G),
    ESC(GLFW.GLFW_KEY_ESCAPE);

    public final int glfwKey;

    KeyCode(int key) {
        this.glfwKey = key;
    }
}
