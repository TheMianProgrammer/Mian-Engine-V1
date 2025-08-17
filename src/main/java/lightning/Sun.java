package lightning;

import org.joml.Vector3f;

public class Sun{
    public Vector3f Position;
    public float strength;
    public float attenuation = 1.0F;

    public Sun(Vector3f Pos, float Strength)
    {
        this.strength = Strength;
        this.Position = Pos;
    }

    public float getStrength(Vector3f object)
    {
        float distance = Vector3f.distance(object.x, object.y, object.z, Position.x, Position.y, Position.z);
        float att = attenuation / (distance * distance); // stärker fallend
        float result = strength * att;
        return result;
    }
}