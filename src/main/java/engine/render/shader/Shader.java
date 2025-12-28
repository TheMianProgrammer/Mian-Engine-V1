package engine.render.shader;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL20;

public class Shader {
    private int programId;

    private String vertexPath;
    private String fragmentPath;

    boolean isMarkedForReload = false;
    public Shader(String vertexPath, String fragmentPath) throws IOException {
        String vertexSrc = Files.readString(Paths.get(vertexPath));
        String fragmentSrc = Files.readString(Paths.get(fragmentPath));
        this.vertexPath = vertexPath;
        this.fragmentPath = fragmentPath;

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
    public void markForReload()
    {
        isMarkedForReload = true;
    }
    public void reload()
    {
        isMarkedForReload = false;
        try{
            String vertexSrc = Files.readString(Paths.get(vertexPath));
            String fragmentSrc=Files.readString(Paths.get(fragmentPath));

            int vertexShader = compileShader(vertexSrc, GL20.GL_VERTEX_SHADER);
            int fragmentShader = compileShader(fragmentSrc, GL20.GL_FRAGMENT_SHADER);

            if(programId != 0) GL20.glDeleteProgram(programId);

            programId = GL20.glCreateProgram();
            GL20.glAttachShader(programId, vertexShader);
            GL20.glAttachShader(programId, fragmentShader);
            GL20.glLinkProgram(programId);
    
            if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
                throw new RuntimeException("Shader linking failed: " + GL20.glGetProgramInfoLog(programId));
            }
    
            GL20.glDeleteShader(vertexShader);
            GL20.glDeleteShader(fragmentShader);
        } catch(Exception e) {
            System.out.println("Failed to reload shader: " + e);
        }
    }

    public void setUniform1i(String name, int value) {
        int loc = GL20.glGetUniformLocation(programId, name);
        GL20.glUniform1i(loc, value);
    }

    public String getVertexPath() {
        return vertexPath;
    }
    public String getFragmentPath() {
        return fragmentPath;
    }

    public void setUniform1f(String name, float value) {
        int loc = GL20.glGetUniformLocation(programId, name);
        GL20.glUniform1f(loc, value);
    }

    public void setUniform2f(String name, float x, float y) {
        int loc = GL20.glGetUniformLocation(programId, name);
        GL20.glUniform2f(loc, x, y);
    }

    public void setUniform3f(String name, Vector3f pos) {
        int loc = GL20.glGetUniformLocation(programId, name);
        GL20.glUniform3f(loc, pos.x, pos.y, pos.z);
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
        if(isMarkedForReload)
            reload();
    }
}
