package obj.objects;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.vecmath.Vector3f;

import org.joml.Vector2i;
import org.joml.Vector3i;

import obj.objects.block.Block;
import obj.objects.data.ChunkData;

public class Chunk {
    public Vector2i Position;
    World world;
    public ChunkData data;

    public Map<Vector3i, Block> Blocks = new LinkedHashMap<>();

    public Chunk(World world)
    {
        this.world = world;
    }

    public void AddBlock(Vector3i Position, Block block)
    {
        Blocks.put(Position, block);
    }
    
    public void CheckColliders(Vector3f Position)
    {
        Blocks.entrySet()
            .parallelStream()
            .forEach(entry -> {
                Vector3i pos = entry.getKey();
                Block block = entry.getValue();
                if (org.joml.Vector3f.distance(pos.x, pos.y, pos.z, Position.x, Position.y, Position.z) <= 5)
                    block.getEntity().EnableCollider(world);
                else
                    block.getEntity().DisableCollider(world);
            });
    }

    public void enable()
    {
        Blocks.forEach((Vector3i pos, Block block) -> {
            block.getEntity().Enable();
        });
    }

    public void disable()
    {
        Blocks.forEach((Vector3i pos, Block block) -> {
            block.getEntity().Disable();
            block.getEntity().DisableCollider(world);
        });
    }
}
