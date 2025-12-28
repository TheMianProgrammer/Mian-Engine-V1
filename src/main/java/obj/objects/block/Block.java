package obj.objects.block;

import org.joml.Vector3f;

import engine.render.block.BlockRenderer;
import engine.render.texture.Texture;
import obj.entity.Entity;
import obj.objects.World;
import server.main.EntityLoader;

/// TODO: Convert every Vector3f to Vector3i
public class Block {
    public engine.render.block.BlockRenderer renderer;

    Entity entity;
    int texID;
    Vector3f position;
    Texture texture;
    World world;

    public World GetWorld()
    {
        return world;
    }
    public void InitRendering(EntityLoader loader)
    {
        renderer = new BlockRenderer(this);
        renderer.InitzilizeBlock(loader, this);
    }

    public void ActivateRendering(EntityLoader loader)
    {
        renderer.ActivateRendering(loader);
    }
    public void DisableRendering(EntityLoader loader)
    {
        renderer.UnloadBlock(loader);
    }

    public void setTexture(Texture texture)
    {
        this.texture = texture;
    }

    public Block(Vector3f position, int texID, World world){
        this.texID = texID;
        this.position = position;
        this.texID = texID;
        this.world = world;
    }

    public void LoadEntity(EntityLoader entityLoader)
    {
        this.entity = entityLoader.LoadEntity("assets/obj/basics/Cube.obj", position.mul(2), new Vector3f(1, 1, 1), texture, true);
        entityLoader.initEntityCollider(this.entity, false);
    }

    public Entity getEntity()
    {
        return this.entity;
    }

    public Vector3f GetPosition()
    {
        return this.position;
    }

    public Texture GetTexture()
    {
        return texture;
    }
}
