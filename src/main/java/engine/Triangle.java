package engine;

import org.joml.Vector3f;

public class Triangle{
    public Vector3f v1, v2, v3;
    public Vector3f color;
    public Vector3f normal;

    public Triangle(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f color)
    {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.color = color;
        calculateNormal();
    }

    private void calculateNormal() {
        // Richtung der Fläche berechnen
        Vector3f edge1 = new Vector3f(v2).sub(v1); // v2 - v1
        Vector3f edge2 = new Vector3f(v3).sub(v1); // v3 - v1
        normal = edge1.cross(edge2, new Vector3f()).normalize();
    }

    public float[] getVertices() {
        return new float[] {
            v1.x, v1.y, v1.z,
            v2.x, v2.y, v2.z,
            v3.x, v3.y, v3.z
        };
    }

    public float[] getNormalArray() {
        // 3x die Normal pro Vertex, damit jeder Vertex die gleiche Normale bekommt
        return new float[] {
            normal.x, normal.y, normal.z,
            normal.x, normal.y, normal.z,
            normal.x, normal.y, normal.z
        };
    }   
}