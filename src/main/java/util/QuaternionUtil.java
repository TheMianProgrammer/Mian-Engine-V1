package util;

import javax.vecmath.Quat4f;

import org.joml.Vector3f;

public class QuaternionUtil {
    public static Quat4f fromAxisAngle(Vector3f axis, float angle)
    {
        axis.normalize();
        float sinHalf = (float)Math.sin(angle/2);
        float cosHalf = (float)Math.cos(angle/2f);
        return new Quat4f(
            axis.x * sinHalf,
            axis.y * sinHalf,
            axis.z * sinHalf,
            cosHalf
        );
    }
}
