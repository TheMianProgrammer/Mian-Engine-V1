package obj;


import org.joml.Vector3f;

import com.bulletphysics.dynamics.RigidBody;

import engine.Camera;

public class Player {
    public Vector3f position;
    public RigidBody body;

    public float yaw;
    public float pitch;

    public Camera camera;

    public Player(Vector3f startPos)
    {
        this.position = startPos;
        this.camera = new Camera();
    }
    public void setPosition(Vector3f pos)
    {
        this.position = pos;
        this.camera.position = pos;
        this.camera.target = pos;
    }

    public void translate(Vector3f position)
    {
        this.position.add(position);
        camera.translateForward(position.x);
        camera.translateRight(position.z);
        camera.translate(new Vector3f(0, position.y, 0));
    }

    public void rotate(float pitch, float yaw)
    {
        this.pitch += pitch;
        this.yaw += yaw;
        camera.rotate(pitch, yaw);
    }
}
