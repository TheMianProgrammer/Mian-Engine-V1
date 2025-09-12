package obj.entity.hitbox;

import org.joml.Vector3f;

public class MeshHitbox extends Hitbox {
    public Vector3f[] vertices;

    public MeshHitbox(Vector3f[] vertices, Vector3f offset) {
        super(null, offset);
        this.vertices = vertices;
    }


    // Bounding Box der Mesh
    public Vector3f getMin(Vector3f entityPos) {
        Vector3f min = new Vector3f(Float.MAX_VALUE);
        for(Vector3f v : vertices)
            min.min(new Vector3f(v).add(entityPos).add(offset));
        return min;
    }

    public Vector3f getMax(Vector3f entityPos) {
        Vector3f max = new Vector3f(Float.MIN_VALUE);
        for(Vector3f v : vertices)
            max.max(new Vector3f(v).add(entityPos).add(offset));
        return max;
    }
}
