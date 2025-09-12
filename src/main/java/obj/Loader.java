package obj;

import org.joml.Vector2f;
import org.joml.Vector3f;

import engine.render.Triangle;

import java.io.*;
import java.util.*;

public class Loader {
    public static List<Triangle> loadOBJ(String path) throws IOException {
        List<Vector3f> vertices = new ArrayList<>();
        List<Vector2f> uvs = new ArrayList<>();
        List<Triangle> triangles = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("v ")) { // vertex
                    String[] p = line.split("\\s+");
                    vertices.add(new Vector3f(
                        Float.parseFloat(p[1]),
                        Float.parseFloat(p[2]),
                        Float.parseFloat(p[3])
                    ));
                } else if (line.startsWith("vt ")) { // texture coord
                    String[] p = line.split("\\s+");
                    uvs.add(new Vector2f(
                        Float.parseFloat(p[1]),
                        1 - Float.parseFloat(p[2]) // invert Y (OBJ has top=0)
                    ));
                } else if (line.startsWith("f ")) { // face
                    String[] p = line.split("\\s+");
                    // p[i] looks like: vertexIndex/uvIndex/normalIndex
                    int[] vIndex = new int[p.length - 1];
                    int[] uvIndex = new int[p.length - 1];

                    for (int i = 1; i < p.length; i++) {
                        String[] parts = p[i].split("/");
                        vIndex[i-1]  = Integer.parseInt(parts[0]) - 1;
                        uvIndex[i-1] = parts.length > 1 && !parts[1].isEmpty()
                                        ? Integer.parseInt(parts[1]) - 1
                                        : -1;
                    }

                    // triangulate
                    for (int i = 1; i < vIndex.length - 1; i++) {
                        Vector3f v1 = vertices.get(vIndex[0]);
                        Vector3f v2 = vertices.get(vIndex[i]);
                        Vector3f v3 = vertices.get(vIndex[i+1]);

                        Vector2f uv1 = uvIndex[0]  >= 0 ? uvs.get(uvIndex[0])  : new Vector2f();
                        Vector2f uv2 = uvIndex[i]  >= 0 ? uvs.get(uvIndex[i])  : new Vector2f();
                        Vector2f uv3 = uvIndex[i+1]>= 0 ? uvs.get(uvIndex[i+1]): new Vector2f();

                        triangles.add(new Triangle(v1, v2, v3, uv1, uv2, uv3, new Vector3f(1,1,1)));
                    }
                }
            }
        }
        return triangles;
    }
}
