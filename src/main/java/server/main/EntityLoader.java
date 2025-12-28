package server.main;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Quat4f;

import  org.joml.Vector3f;

import debug.Mathz;
import engine.render.texture.Texture;
import obj.entity.Entity;

public class EntityLoader {
    public List<Entity> entities = new ArrayList<>();
    public GameServer server;

    public EntityLoader(GameServer server)
    {
        this.server = server;
    }

    public Entity LoadEntity(String mesh, Vector3f pos, Vector3f scale, Texture texture, boolean isStatic)
    {
        Entity e = new Entity(pos, new Vector3f(255,255,255), scale, null);
        e.isStatic = isStatic;
        e.LoadMesh(mesh);
        //e.Activate(texture); // needs renderer! 

        entities.add(e);
        return e;
    }
    public Entity LoadEntity(String mesh, Vector3f pos, Vector3f scale, Vector3f rotation, Texture texture, boolean isStatic)
    {
        Entity e = new Entity(pos, new Vector3f(255,255,255), scale, null);
        Quat4f quat = Mathz.eulerToQuat(rotation);
        e.Rotation = quat;
        e.isStatic = isStatic;
        e.LoadMesh(mesh);
        //e.Activate(texture); // needs renderer! 

        entities.add(e);
        return e;
    }
    public void initEntityCollider(Entity e, boolean isMeshed)
    {
        e.isMeshed = isMeshed; // maybe there is a better way to set it...
        if(isMeshed)
            server.initMeshEntity(e);
        else
            server.initEntity(e);
    }
    public void removeEntityCollider(Entity e)
    {
        server.removeEntityColl(e);
    }
}
