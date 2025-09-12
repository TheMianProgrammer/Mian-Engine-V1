package obj.entity.player;


import org.joml.Vector2f;
import org.joml.Vector3f;

import com.bulletphysics.dynamics.RigidBody;

import engine.Input.keyboard.KeyCode;
import engine.Input.keyboard.Keyboard;

public class Player {
    public Vector2f velocity;

    public Vector3f position;
    public RigidBody body;

    public float yaw;
    public float pitch;

    public Camera camera;

    public float inputX;
    public float inputZ;

    public float JumpStrenght = 0;
    static final float SPEED = 10F;
    static final float SENSIVITY=100F;

    public boolean isGrounded;

    public Keyboard Input;

    public Player(Vector3f startPos)
    {
        this.position = startPos;
        this.camera = new Camera();
        this.velocity = new Vector2f();
    }
    public void setPosition(Vector3f pos)
    {
        this.position = pos;
        this.camera.position = pos;
        this.camera.target = pos;
    }
    public void update()
    {
        // camera.translate(new Vector3f(0, -2, 0));
        if(JumpStrenght >= 0)
            JumpStrenght -= 0.1F;
        else
            JumpStrenght = 0;

        this.velocity.mul(0.1F);
        HandleInputs();
    }

    public void Jump(float force)
    {
        if(isGrounded)
            JumpStrenght = force;
    }
    public void translate(Vector2f position)
    {
        this.position.add(new Vector3f(position.x, 0, position.y));
        camera.translateForward(position.x);
        camera.translateRight(position.y);
        camera.translate(new Vector3f(0, position.y, 0));

        // rotate input into world space
        double rad = Math.toRadians(camera.yaw);
        inputX = (float) (Math.cos(rad) * position.x - Math.sin(rad) * position.y);
        inputZ = (float) (Math.sin(rad) * position.x + Math.cos(rad) * position.y);
    }

    public void setInput(Keyboard input){
        this.Input = input;
    }

    public void rotate(float pitch, float yaw)
    {
        this.pitch += pitch;
        this.yaw += yaw;
        camera.rotate(pitch, yaw);
    }

    public void HandleInputs()
    {
        if(Input.getKey(KeyCode.W))
            velocity.add(new Vector2f(SPEED, 0));
        if(Input.getKey(KeyCode.A))
            velocity.add(new Vector2f(0, -SPEED));
        if(Input.getKey(KeyCode.S))
            velocity.add(new Vector2f(-SPEED, 0));
        if(Input.getKey(KeyCode.D))
            velocity.add(new Vector2f(0, SPEED));

        if(Input.getKey(KeyCode.SPACE))
            Jump(SPEED);
        // if(Input.getKey(KeyCode.C))
        //     player.translate(new Vector2f(0, 0));

        translate(velocity);

        if(Input.getKey(KeyCode.J))
            rotate(0, -SENSIVITY);
        if(Input.getKey(KeyCode.K))
            rotate(-SENSIVITY, 0);
        if(Input.getKey(KeyCode.L))
            rotate(0, SENSIVITY);
        if(Input.getKey(KeyCode.I))
            rotate(SENSIVITY, 0);
    }
}
