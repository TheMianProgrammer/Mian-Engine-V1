package text;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

public class Font{
    STBTTFontinfo font;

    public Font(String fontPath) throws Exception{
        ByteBuffer ttf = ByteBuffer.wrap(Files.readAllBytes(Paths.get(fontPath)));
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ttf = BufferUtils.createByteBuffer(Files.readAllBytes(Paths.get("assets/font/Roboto.ttf")).length);
            ttf.put(Files.readAllBytes(Paths.get("assets/font/Roboto.ttf"))).flip();
        }
    }

    public STBTTFontinfo getFontInfo()
    {
        return font;
    }
}