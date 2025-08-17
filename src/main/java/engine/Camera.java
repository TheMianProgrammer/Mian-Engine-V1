package engine;

import org.joml.Matrix4f;
import org.joml.Vector3f;

class Camera{
    public Vector3f position = new Vector3f(0, 0, 3); 
    public Vector3f target = new Vector3f(0, 0, 0);
    public Vector3f up = new Vector3f(0, 1, 0);

    private float pitch = 0;
    private float yaw = -90;

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(position, target, new Vector3f(0, 1, 0));
    }

    public Matrix4f getProjectionMatrix(float fov, float aspect, float near, float far)
    {
        return new Matrix4f().perspective(fov, aspect, near, far);
    }

    public void translate (Vector3f position)
    {
        this.position.add(position);
        this.target.add(position);
    }
    public void translateForward(float distance) {
        float radYaw = (float)Math.toRadians(yaw);
        Vector3f dir = new Vector3f(
            (float)Math.cos(radYaw) * distance,
            0,
            (float)Math.sin(radYaw) * distance
        );
        position.add(dir);
        target.add(dir);
    }

    public void translateRight(float distance) {
        float radYaw = (float)Math.toRadians(yaw + 90);
        Vector3f dir = new Vector3f(
            (float)Math.cos(radYaw) * distance,
            0,
            (float)Math.sin(radYaw) * distance
        );
        position.add(dir);
        target.add(dir);
    }


    public void rotate(float deltaPitch, float deltaYaw)
    {
        pitch += deltaPitch;
        yaw += deltaYaw;

        if(pitch > 89) pitch = 89;
        // if(yaw < -89) yaw = -89;

        Vector3f direction = new Vector3f();
        direction.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        direction.y = (float) Math.sin(Math.toRadians(pitch));
        direction.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));

        direction.normalize();
        target = new Vector3f(position).add(direction);
    }
}