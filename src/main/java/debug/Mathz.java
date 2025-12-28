package debug;

import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import org.joml.Vector3f;

public class Mathz {
    public static Quat4f eulerToQuat(Vector3f euler) {
    // Euler in Radians
    float pitch = (float) Math.toRadians(euler.x);
    float yaw   = (float) Math.toRadians(euler.y);
    float roll  = (float) Math.toRadians(euler.z);

    // Rotationsmatrizen
    Matrix3f rx = new Matrix3f();
    rx.rotX(pitch);

    Matrix3f ry = new Matrix3f();
    ry.rotY(yaw);

    Matrix3f rz = new Matrix3f();
    rz.rotZ(roll);

    // Gesamtrotation: R = rz * ry * rx
    Matrix3f r = new Matrix3f();
    r.mul(rz, ry);
    r.mul(rx);

    Quat4f quat = new Quat4f();
    quat.set(r);
    return quat;
}
}
