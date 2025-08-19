package texture;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;

public class Texture {
    private int id;
    private int width, height;

    public Texture(String path)
    {
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        STBImage.stbi_set_flip_vertically_on_load(true);
        ByteBuffer image = STBImage.stbi_load(path, w, h, channels, 4);
        if (image == null) throw new RuntimeException("Failed to laod image: " + STBImage.stbi_failure_reason());

        width = w.get();
        height = h.get();

        id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

        STBImage.stbi_image_free(image);
    }

    public void bind(){
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
    }

    public void delete(){
        GL11.glDeleteTextures(id);
    }
}
