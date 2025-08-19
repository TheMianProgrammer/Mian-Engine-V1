package lightning;

import org.joml.Vector3f;

public class Ray {
    public Vector3f origin, direction;

    public Ray(Vector3f origin, Vector3f direction) {
        this.origin = new Vector3f(origin);
        this.direction = new Vector3f(direction).normalize();
    }

    // returns distance t or -1 if no hit
    public float intersectTriangle(Vector3f v0, Vector3f v1, Vector3f v2) {
        final float EPSILON = 0.000001f;

        Vector3f edge1 = new Vector3f(v1).sub(v0);
        Vector3f edge2 = new Vector3f(v2).sub(v0);
        Vector3f h = new Vector3f(direction).cross(edge2);
        float a = edge1.dot(h);

        if (a > -EPSILON && a < EPSILON) return -1; // parallel

        float f = 1.0f / a;
        Vector3f s = new Vector3f(origin).sub(v0);
        float u = f * s.dot(h);
        if (u < 0.0 || u > 1.0) return -1;

        Vector3f q = new Vector3f(s).cross(edge1);
        float v = f * direction.dot(q);
        if (v < 0.0 || u + v > 1.0) return -1;

        float t = f * edge2.dot(q);
        return t > EPSILON ? t : -1;
    }
}
