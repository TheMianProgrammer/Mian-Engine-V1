package obj.entity.data;

import org.joml.Vector3f;
import javax.vecmath.Quat4f;
import java.io.Serializable;

public class EntityData implements Serializable {
    public String meshPath;
    public Vector3f position;
    public Vector3f scale;
    public Quat4f rotation;
    public boolean isStatic;

    public EntityData(String meshPath, Vector3f pos, Vector3f scale, Quat4f rot, boolean isStatic) {
        this.meshPath = meshPath;
        this.position = pos;
        this.scale = scale;
        this.rotation = rot;
        this.isStatic = isStatic;
    }
}
