package engine.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3i;

import debug.Dir;
import debug.Direction;
import engine.render.block.BlockRenderer;
import obj.objects.World;
import obj.objects.block.Block;

public class WorldRenderer {
    public World world;

    public void AddWorld(World world)
    {
        this.world = world;
        this.world.onDestroyBlock.add(pos ->
            {
                OnDestroyBlock(pos);
        });
        this.world.onPlaceBlock.add(pos ->
            {
                OnPlaceBlock(pos);
        });
    }
    void OnDestroyBlock(Vector3i Position)
    {
        UpdateBlock(Position);
    }
    void OnPlaceBlock(Vector3i Position)
    {
        UpdateBlock(Position);
    }

    public void UpdateBlock(Vector3i Pos)
    {
        Block centerBlock = world.blockList.get(Pos);
        BlockRenderer center = null;
        if(centerBlock != null)
            center = centerBlock.renderer;

        for (Direction dir : Direction.values())
        {
            Vector3i neighborPos  = new Vector3i(Pos).add(new Vector3i(dir.offset));
            Block neighborBlock = world.blockList.get(neighborPos);
            BlockRenderer neighbor = null;
            if(neighborBlock != null)
                neighbor = neighborBlock.renderer;
            
            if (center == null) {
                if (neighbor != null) {
                    neighbor.enable(dir);
                }
                continue;
            }

            if(neighbor != null)
            {
                center.disable(dir);
                neighbor.disable(Dir.opposite(dir));
            } else
            {
                center.enable(dir);
            }
        }
    }
}
