package obj.entity.hitbox;

import org.joml.Vector3f;

public class Hitbox {
    public Vector3f scale;
    public Vector3f offset;

    public Hitbox(Vector3f scale, Vector3f offset){
        this.scale = scale;
        this.offset = offset;
    }

    public Vector3f getMin(Vector3f entityPos){
        return new Vector3f(entityPos).add(offset).sub(new Vector3f(scale).mul(0.5F));
    }

    public Vector3f getMax(Vector3f entityPos) {
        return new Vector3f(entityPos).add(offset).add(new Vector3f(scale).mul(0.5f));
    }

    // AABB vs AABB collision
    public boolean collides(Hitbox other, Vector3f entityPos, Vector3f otherPos) {
        Vector3f minA = getMin(entityPos);
        Vector3f maxA = getMax(entityPos);
        Vector3f minB = other.getMin(otherPos);
        Vector3f maxB = other.getMax(otherPos);

        return (minA.x <= maxB.x && maxA.x >= minB.x) &&
               (minA.y <= maxB.y && maxA.y >= minB.y) &&
               (minA.z <= maxB.z && maxA.z >= minB.z);
    }

    public void updatePosition(Vector3f position) {
        this.offset = position;
    }
}
