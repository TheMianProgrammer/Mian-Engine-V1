package obj.entity.player;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera{
    public Vector3f position = new Vector3f(0, 0, 3); 
    public Vector3f target = new Vector3f(0, 0, 0);
    public Vector3f front = new Vector3f(0, 0, -1);
    public Vector3f up = new Vector3f(0, 1, 0);

    public float pitch = 0;
    public float yaw = -90;

    public float deltaTime;

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(
            position,
            new Vector3f(position).add(front), // look into direction of "front"
            up
        );
    }


    public Matrix4f getProjectionMatrix(float fov, float aspect, float near, float far)
    {
        return new Matrix4f().perspective(fov, aspect, near, far);
    }

    public void translate(Vector3f dir) {
        Vector3f move = new Vector3f(dir).mul(deltaTime);
        position.add(move);
    }

    public void translateForward(float distance) {
        Vector3f move = new Vector3f(front).normalize().mul(distance * deltaTime);
        position.add(move);
    }

    public void translateRight(float distance) {
        Vector3f right = new Vector3f(front).cross(up).normalize();
        Vector3f move = right.mul(distance * deltaTime);
        position.add(move);
    }

    public void rotate(float deltaPitch, float deltaYaw) {
        deltaPitch *= deltaTime;
        deltaYaw   *= deltaTime;

        pitch += deltaPitch;
        yaw   += deltaYaw;

        if (pitch > 89) pitch = 89;
        if (pitch < -89) pitch = -89;

        // recalc front vector
        front.x = (float)(Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        front.y = (float)(Math.sin(Math.toRadians(pitch)));
        front.z = (float)(Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        front.normalize();
    }
}