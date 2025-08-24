package server.server;

import java.util.ArrayList;
import java.util.List;

import  org.joml.Vector3f;

import engine.Renderer;
import obj.Entity;
import texture.Texture;

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
    public void initEntityCollider(Entity e, boolean isMeshed)
    {
        if(isMeshed)
            server.initMeshEntity(e);
        else
            server.initEntity(e);
    }
}
