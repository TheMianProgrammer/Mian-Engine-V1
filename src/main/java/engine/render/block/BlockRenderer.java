package engine.render.block;

import org.joml.Vector3f;

import debug.Direction;
import obj.entity.Entity;
import obj.objects.block.Block;
import server.main.EntityLoader;
import server.main.GameServer;

public class BlockRenderer {
    Entity front;
    Entity back;
    Entity left;
    Entity right;
    Entity top;
    Entity bottom;

    Block block;
    public BlockRenderer(Block block)
    {
        this.block = block;
    }
    public Block GetBlock()
    {
        return block;
    }

    public void UnloadBlock(EntityLoader loader)
    {
        for(Direction dir : Direction.values())
        {
            disable(dir);
        }
    }
    public void InitzilizeBlock(EntityLoader loader, Block block)
    {
        Vector3f defaultScale = new Vector3f(1, 1, 1);

        front = loader.LoadEntity("assets/obj/basics/Plane.obj", new Vector3f(block.GetPosition()).add(new Vector3f(Direction.FRONT.offset)), defaultScale, new Vector3f(90, 0, 0), block.GetTexture(), true);
        back = loader.LoadEntity("assets/obj/basics/Plane.obj",  new Vector3f(block.GetPosition()).add(new Vector3f(Direction.BACK.offset)), defaultScale, new Vector3f(-90, 0, 0), block.GetTexture(), true);
        left = loader.LoadEntity("assets/obj/basics/Plane.obj",  new Vector3f(block.GetPosition()).add(new Vector3f(Direction.LEFT.offset)), defaultScale, new Vector3f(0, 0, 90), block.GetTexture(), true);
        right = loader.LoadEntity("assets/obj/basics/Plane.obj", new Vector3f(block.GetPosition()).add(new Vector3f(Direction.RIGHT.offset)), defaultScale, new Vector3f(0, 0, -90), block.GetTexture(), true);
        top = loader.LoadEntity("assets/obj/basics/Plane.obj",   new Vector3f(block.GetPosition()).add(new Vector3f(Direction.UP.offset)), defaultScale, new Vector3f(0, 0, 0), block.GetTexture(), true);
        bottom = loader.LoadEntity("assets/obj/basics/Plane.obj",new Vector3f(block.GetPosition()).add(new Vector3f(Direction.DOWN.offset)), defaultScale, new Vector3f(-180, 0, 0), block.GetTexture(), true);
    
        GameServer server = GetBlock().GetWorld().GetServer();
        server.ActivateEntity(front); loader.initEntityCollider(front, true);
        server.ActivateEntity(back);  loader.initEntityCollider(back, true);
        server.ActivateEntity(left);  loader.initEntityCollider(left, true);
        server.ActivateEntity(right); loader.initEntityCollider(right, true);
        server.ActivateEntity(top);   loader.initEntityCollider(top, true);
        server.ActivateEntity(bottom);loader.initEntityCollider(bottom, true);
    }

    public void enable(Direction dir) {
        switch (dir) {
            case FRONT -> {
                front.Enable();
            }
            case BACK  -> {
                back.Enable();
            }
            case LEFT  -> {
                left.Enable();
            }
            case RIGHT -> {
                right.Enable();
            }
            case UP    -> {
                top.Enable();
            }
            case DOWN  -> {
                bottom.Enable();
            }
        }
    }
    public void disable(Direction dir) {
        switch (dir) {
            case FRONT -> {
                front.Disable();
            }
            case BACK  -> {
                back.Disable();
            }
            case LEFT  -> {
                left.Disable();
            }
            case RIGHT -> {
                right.Disable();
            }
            case UP    -> {
                top.Disable();
            }
            case DOWN  -> {
                bottom.Disable();
            }
        }
    }
}
