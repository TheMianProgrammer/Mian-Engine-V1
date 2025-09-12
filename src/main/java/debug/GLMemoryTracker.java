// Test
package debug;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class GLMemoryTracker {
    static class BufferInfo { int id; int size; String name; }
    static Map<Integer, BufferInfo> vbos = new ConcurrentHashMap<>();
    static Map<Integer, BufferInfo> textures = new ConcurrentHashMap<>();
    static Map<Integer, Integer> Triangles = new ConcurrentHashMap<>();
    static int totalTriangles = 0;
    static int debugViewMode = 0;

    public static void trackVBO(int vboId, int size, String name) {
        if (vboId == 0 || size <= 0) return;
        vbos.put(vboId, new BufferInfo(){{
            id = vboId; this.size = size; this.name = name != null ? name : "unnamed";
        }});
    }

    public static void AddTriangle(int objectVBO){
        if(Triangles.get(objectVBO) == null) Triangles.put(objectVBO, 0);
        Triangles.put(objectVBO, Triangles.get(objectVBO)+1);
    }
    public static void SetTriangle(int objectVBO, int num) {
        Triangles.put(objectVBO, num);
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
        totalTriangles = 0;
        int[] index = {0};
        int[] largestObject = {0};
        Triangles.values().forEach(t -> {
            System.out.println("Object VBO=" + index[0] + " Triangles: " + t);
            if(largestObject[0] < t)
                largestObject[0] = t;
            totalTriangles += t;
            index[0]++;
        });
        System.out.println("Total approx VRAM: "+total+" bytes");
        System.out.println("Total Triangles: " + totalTriangles + " Largest: " + largestObject[0]);
    }
}
