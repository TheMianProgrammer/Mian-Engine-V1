package obj.objects.block;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Quat4f;

import org.joml.Vector3f;

import engine.render.Triangle;
import engine.render.texture.Texture;
import obj.entity.Entity;
import server.main.EntityLoader;
import util.QuaternionUtil;

public class Block {
    public Entity sides[] = new Entity[6];

    Entity entity;
    int texID;
    Vector3f position;
    Texture texture;

    public void setTexture(Texture texture)
    {
        this.texture = texture;
    }

    public Block(Vector3f position, int texID){
        this.texID = texID;
        this.position = position;
        this.texID = texID;
    }

    public void LoadEntity(EntityLoader entityLoader)
    {
        this.entity = entityLoader.LoadEntity("assets/obj/basics/Cube.obj", position.mul(2), new Vector3f(1, 1, 1), texture, true);
        entityLoader.initEntityCollider(this.entity, false);
        // LoadSides(entityLoader);
    }

    void LoadSides(EntityLoader entityLoader)
    {
        List<Triangle>[] triSides = new List[6];

        for(Triangle tri : this.entity.mesh){
            if(tri.normal.equals(new Vector3f(1,0,0))) triSides[0].add(tri);
            if(tri.normal.equals(new Vector3f(-1,0,0))) triSides[1].add(tri);
            if(tri.normal.equals(new Vector3f(0,1,0))) triSides[2].add(tri);
        } 
    }

    public Entity getEntity()
    {
        return this.entity;
    }
}
