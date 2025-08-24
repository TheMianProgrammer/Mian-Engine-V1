package debug;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GLMemoryTracker {
    static class BufferInfo { int id; int size; String name; }
    static Map<Integer, BufferInfo> vbos = new ConcurrentHashMap<>();
    static Map<Integer, BufferInfo> textures = new ConcurrentHashMap<>();

    public static void trackVBO(int vboId, int size, String name) {
        if (vboId == 0 || size <= 0) return;
        vbos.put(vboId, new BufferInfo(){{
            id = vboId; this.size = size; this.name = name != null ? name : "unnamed";
        }});
    }

    public static void untrackVBO(int vboId) {
        vbos.remove(vboId);
    }

    public static void printSummary() {
        int total = vbos.values().stream().mapToInt(b -> b.size).sum()
                  + textures.values().stream().mapToInt(t -> t.size).sum();
        System.out.println("=== VRAM usage ===");
        vbos.values().forEach(b -> System.out.println("VBO id="+b.id+" "+b.name+": "+b.size+" bytes"));
        textures.values().forEach(t -> System.out.println("Tex id="+t.id+" "+t.name+": "+t.size+" bytes"));
        System.out.println("Total approx VRAM: "+total+" bytes");
    }
}
