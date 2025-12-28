package obj.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

import obj.objects.block.Block;
import obj.objects.data.ChunkData;
import server.main.GameServer;

public class World {
    public GameServer server;

    public Map<Vector3i, Block> blockList = new HashMap<>();

    public List<Consumer<Vector3i>> onPlaceBlock = new ArrayList<>();
    public List<Consumer<Vector3i>> onDestroyBlock = new ArrayList<>();

    public Map<Vector2i, Chunk> loadedChunks = new HashMap<>();
    public Map<Vector2i, Chunk> generatedChunks = new HashMap<>();
    public Map<Vector2i, Chunk> lastLoadedChunks = new HashMap<>();

    private final List<Chunk> lazyReadyInitChunks = new ArrayList<>();
    private final List<Chunk> lazyReadyLoadChunks = new ArrayList<>();
    private final List<Vector2i> lazyToLoadChunks = new ArrayList<>();

    private final AtomicInteger loadingChunksCount = new AtomicInteger(0);

    public boolean isBussyLoadingChunks()
    {
        return loadingChunksCount.get() > 0
            || !lazyReadyInitChunks.isEmpty()
            || !lazyReadyLoadChunks.isEmpty();
    }

    public World(GameServer server)
    {
        this.server = server;
    }

    public GameServer GetServer()
    {
        return server;
    }

    public void DestroyBlock(Vector3i Position)
    {
        blockList.remove(Position);
        onDestroyBlock.forEach(e -> e.accept(Position));

    }

    public void PlaceBlock(Vector3i Position, Block block)
    {
        blockList.put(Position, block);
        onPlaceBlock.forEach(e -> e.accept(Position));
    }
    public void PlaceBlockSilent(Vector3i Pos, Block block)
    {
        blockList.put(Pos, block);
    }

    public void CheckColliders()
    {
        
    }

    public void UnloadChunk(Vector2i ChunkPos)
    {
        if(!generatedChunks.containsKey(ChunkPos)) throw new RuntimeException("The Chunk needs to be generated in order to be unloaded!");
        if(!loadedChunks.containsKey(ChunkPos)) throw new RuntimeException("The Chunk needs to be loaded in order to be unloaded!");
        generatedChunks.get(ChunkPos).Blocks.forEach((Vector3i pos, Block block) ->
        {
            block.DisableRendering(GetServer().entityLoader);
        });
        loadedChunks.remove(ChunkPos, generatedChunks.get(ChunkPos));
    }
    public void LoadChunk(Vector2i ChunkPos)
    {
        if(!generatedChunks.containsKey(ChunkPos)) throw new RuntimeException("The Chunk needs to be generated in order to be loaded!");
        generatedChunks.get(ChunkPos).Blocks.forEach((Vector3i pos, Block block) ->
        {
            block.ActivateRendering(GetServer().entityLoader);
        });
        loadedChunks.put(ChunkPos, generatedChunks.get(ChunkPos));
        lastLoadedChunks.put(ChunkPos, generatedChunks.get(ChunkPos));
    }
    public void UpdateChunk(Chunk chunk)
    {
        chunk.Blocks.forEach((Vector3i pos, Block block) -> {
            GetServer().GetWorldRenderer().UpdateBlock(pos);
        });
    }
    public void InitChunk(Vector2i ChunkPos)
    {
        loadingChunksCount.incrementAndGet();
        new Thread(() -> {
            try {
                ChunkData data = generateChunkData(ChunkPos);
                Chunk chunk = new Chunk(this);
                chunk.data = data;
                chunk.Position = ChunkPos;
        
                /*List<Block> blocks = Arrays.stream(data.positions)
                    .parallel()
                    .map(p -> new Block(new Vector3f(p), 1, this))
                    .toList(); */
                List<Block> blocks = Arrays.stream(data.positions)
                    .map(p -> new Block(new Vector3f(p), 1, this))
                    .toList();
        
                for (int i = 0; i < data.positions.length; i++) {
                    Block block = blocks.get(i);
                    chunk.AddBlock(data.positions[i], block);
                }
    
                synchronized (lazyReadyInitChunks) {
                    lazyReadyInitChunks.add(chunk);
                }
                synchronized (lazyToLoadChunks) {
                    if(lazyToLoadChunks.contains(ChunkPos))
                    {
                        if (lazyToLoadChunks.remove(ChunkPos)){
                            synchronized (lazyReadyLoadChunks) {
                                lazyReadyLoadChunks.add(chunk);
                            }
                        }
                    }
                }
            } finally {
                loadingChunksCount.decrementAndGet();
            }
        }).start();
    }
    public void TickChunkGeneration()
    {
        synchronized (lazyReadyInitChunks) {
            for (Chunk chunk : lazyReadyInitChunks)
            {
                int i = 0;
                for (Block block : chunk.Blocks.values())
                {
                    PlaceBlockSilent(chunk.data.positions[i], block);
                    block.InitRendering(GetServer().entityLoader);
                    i++;
                }
                generatedChunks.put(chunk.Position, chunk);
            }
            lazyReadyInitChunks.clear();
        }
        synchronized (lazyReadyLoadChunks) {
            for (Chunk chunk : lazyReadyLoadChunks)
            {
                LoadChunk(chunk.Position);
            }
            lazyReadyLoadChunks.clear();
        }
    }
    public ChunkData generateChunkData(Vector2i chunkPos)
    {
        ChunkData data = new ChunkData();

        IntStream.range(0, 16*16).parallel().forEach(i -> {
            int x = i % 16;
            int z = i / 16;

            int wx = x + chunkPos.x * 16;
            int wz = z + chunkPos.y * 16;
            int y = server.GetWorldGen().GetOverworldY(wx, wz);

            data.positions[i] = new Vector3i(wx*2, y*2, wz*2);
        });

        return data;
    }
    public void GenChunk(Vector2i ChunkPos)
    {
        if(loadedChunks.containsKey(ChunkPos)) {
            lastLoadedChunks.put(ChunkPos, loadedChunks.get(ChunkPos));
            return;
        }

        Vector3f PlayerPos = GetServer().player.position;
        Vector3f ChunkPosition = new Vector3f(
            ChunkPos.x*16,
            PlayerPos.y,
            ChunkPos.y*16
        );
        float dx = ChunkPosition.x - PlayerPos.x;
        float dz = ChunkPosition.z - PlayerPos.z;
        float sqDist = dx*dx + dz*dz;

        float renderDistance = GetServer().player.RenderDistance*16;
        if(generatedChunks.containsKey(ChunkPos))
        {
            if (sqDist <= renderDistance*renderDistance)
                LoadChunk(ChunkPos);
            return;
        }
        InitChunk(ChunkPos);
        if (sqDist <= renderDistance*renderDistance)
            synchronized (lazyToLoadChunks) {
                lazyToLoadChunks.add(ChunkPos);
            }
            //LoadChunk(ChunkPos);
    }
    public void PrepareChunkGeneration()
    {
        lastLoadedChunks.clear();
    }
    public void CleanupChunks()
    {
        List<Vector2i> toUnload = new ArrayList<>();

        loadedChunks.forEach((pos, chunk) -> {
            if (!lastLoadedChunks.containsKey(pos))
                toUnload.add(pos);
        });

        toUnload.forEach(this::UnloadChunk);
    }
}
