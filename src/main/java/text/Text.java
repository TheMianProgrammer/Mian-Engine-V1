package text;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.stb.STBTTVertex;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

public class Text {
    public String content;
    public Font font;
    float[] vertecies;

    void InitVertecies() {
        List<Float> verts = new ArrayList<>();
        STBTTFontinfo fontInfo = font.getFontInfo();
        float scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, 32);
        float x = 0;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer advance = stack.mallocInt(1);
            IntBuffer lsb = stack.mallocInt(1);
            STBTTPackedchar.Buffer cdata = STBTTPackedchar.malloc(1); // nur Dummy, wir brauchen PointerBuffer
            PointerBuffer pvertices = stack.mallocPointer(1);

            for (char c : content.toCharArray()) {
                STBTruetype.stbtt_GetCodepointHMetrics(fontInfo, c, advance, lsb);

                int numVertices = STBTruetype.stbtt_GetCodepointShape(fontInfo, c, pvertices); // liefert int Anzahl
                long ptr = pvertices.get(0);

                STBTTVertex.Buffer buf = STBTTVertex.create(ptr, numVertices); // Buffer aus Pointer

                for (int i = 0; i < buf.limit(); i++) {
                    STBTTVertex v = buf.get(i);
                    verts.add(v.x() * scale + x);
                    verts.add(v.y() * scale);
                    verts.add(0f);
                }

                x += advance.get(0) * scale;
            }
        }

    vertecies = new float[verts.size()];
    for (int i = 0; i < verts.size(); i++) vertecies[i] = verts.get(i);
}




    public Text(String text)
    {
        this.content = text;
        try{
            this.font = new Font("assets/font/Roboto.ttf");
        } catch(Exception e)
        {
            System.err.println("Failed to laod font: " + e);
        }
        InitVertecies();
    }

    public float[] getVertecies()
    {
        return vertecies;
    }
}
