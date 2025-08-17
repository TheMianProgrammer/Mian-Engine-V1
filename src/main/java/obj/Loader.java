package obj;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import engine.Triangle;

public class Loader{
    public static List<Triangle> loadOBJ(String path) throws IOException {
        List<Vector3f> vertices = new ArrayList<>();
        List<Triangle> triangles = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("v ")) {
                    String[] parts = line.split("\\s+");
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    float z = Float.parseFloat(parts[3]);
                    vertices.add(new Vector3f(x, y, z));
                } else if (line.startsWith("f ")) {
                    String[] parts = line.split("\\s+");
                    // Face hat >=3 Vertices
                    int[] indices = new int[parts.length - 1];
                    for (int i = 1; i < parts.length; i++)
                        indices[i-1] = Integer.parseInt(parts[i].split("/")[0]) - 1;

                    // Fan-Triangulation
                    for (int i = 1; i < indices.length - 1; i++) {
                        Vector3f v1 = vertices.get(indices[0]);
                        Vector3f v2 = vertices.get(indices[i]);
                        Vector3f v3 = vertices.get(indices[i+1]);
                        triangles.add(new Triangle(v1, v2, v3, new Vector3f(i, i, i)));
                    }
                }
            }
        }
        return triangles;
    }
}