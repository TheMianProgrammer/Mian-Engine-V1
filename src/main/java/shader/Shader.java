package shader;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.lwjgl.opengl.GL20;

public class Shader {
    private int programId;

    public Shader(String vertexPath, String fragmentPath) throws IOException {
        String vertexSrc = Files.readString(Paths.get(vertexPath));
        var fragmentSrc = Files.readString(Paths.get(fragmentPath));

        int vertexShader = compileShader(vertexSrc, GL20.GL_VERTEX_SHADER);
        int fragmentShader = compileShader(fragmentSrc, GL20.GL_FRAGMENT_SHADER);

        programId = GL20.glCreateProgram();
        GL20.glAttachShader(programId, vertexShader);
        GL20.glAttachShader(programId, fragmentShader);
        GL20.glLinkProgram(programId);

        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Shader linking failed: " + GL20.glGetProgramInfoLog(programId));
        }

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    public void setUniform1f(String name, float value) {
        int loc = GL20.glGetUniformLocation(programId, name);
        GL20.glUniform1f(loc, value);
    }

    public void setUniform2f(String name, float x, float y) {
        int loc = GL20.glGetUniformLocation(programId, name);
        GL20.glUniform2f(loc, x, y);
    }

    public void setUniform3f(String name, float x, float y, float z) {
        int loc = GL20.glGetUniformLocation(programId, name);
        GL20.glUniform3f(loc, x, y, z);
    }    

    public void setUniformMat4(String name, FloatBuffer matrix) {
        int loc = GL20.glGetUniformLocation(programId, name);
        GL20.glUniformMatrix4fv(loc, false, matrix);
    }

    private int compileShader(String src, int type) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, src);
        GL20.glCompileShader(shader);

        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Shader compile failed: " + GL20.glGetShaderInfoLog(shader));
        }
        return shader;
    }

    public void use() {
        GL20.glUseProgram(programId);
    }
}
