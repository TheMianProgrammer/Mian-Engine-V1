package obj.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private ExecutorService lazyInitChunkPool;

    public List<Consumer<Boolean>> onLazyInitChunks = new ArrayList<>();

    private final AtomicInteger loadingChunksCount = new AtomicInteger(0);
    private final Set<Vector2i> initzilisingChunks = new HashSet<>();

    public boolean isBussyLoadingChunks()
    {
        return loadingChunksCount.get() > 0
            || !lazyReadyInitChunks.isEmpty()
            || !lazyReadyLoadChunks.isEmpty();
    }

    public World(GameServer server)
    {
        this.server = server;
        lazyInitChunkPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
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
        if(blockList.containsKey(Pos)) {
            System.out.println("Warning: Skipped duplicate");
            return;
        }
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
    public void Shutdown()
    {
        lazyInitChunkPool.shutdown();
    }
    void CreateChunkData(Vector2i ChunkPos)
    {
        ChunkData data = generateChunkData(ChunkPos);
        Chunk chunk = new Chunk(this);
        chunk.data = data;
        chunk.Position = ChunkPos;
    
        /*List<Block> blocks = Arrays.stream(data.positions)
            .parallel()
            .map(p -> new Block(new Vector3f(p), 1, this))
            .toList(); */
        List<Block> blocks = Arrays.stream(data.positions)
            .map(p -> new Block(p, 1, this))
            .toList();
    
        for (int i = 0; i < data.positions.length; i++) {
            Block block = blocks.get(i);
            chunk.AddBlock(data.positions[i], block);
        }
    
        synchronized (initzilisingChunks) {
            if (generatedChunks.containsKey(chunk.Position)){
                initzilisingChunks.remove(chunk.Position);
                loadingChunksCount.decrementAndGet();
                return;
            }
            synchronized (lazyReadyInitChunks) {
                lazyReadyInitChunks.add(chunk);
            }
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
    }
    public void InitChunk(Vector2i ChunkPos)
    {
        synchronized (initzilisingChunks) {
            if (generatedChunks.containsKey(ChunkPos) || initzilisingChunks.contains(ChunkPos))
            {
                return;
            }
            initzilisingChunks.add(ChunkPos);
        }
        loadingChunksCount.incrementAndGet();
        //Thread initThread = new Thread(() -> {
        //     CreateChunkData(ChunkPos);
        //});
        //initThread.setDaemon(true);
        lazyInitChunkPool.submit(() -> CreateChunkData(ChunkPos));
        // initThread.start();
    }
    public void TickChunkGeneration()
    {
        synchronized (lazyReadyInitChunks) {
            for (Chunk chunk : lazyReadyInitChunks)
            {
                if (generatedChunks.containsKey(chunk.Position)) continue;
                for (int i = 0; i < chunk.data.positions.length; i++)
                {
                    Vector3i pos = chunk.data.positions[i];
                    Block block = chunk.Blocks.get(pos);
                    if (block == null) continue;
                    PlaceBlockSilent(pos, block);
                    block.InitRendering(GetServer().entityLoader);
                }
                loadingChunksCount.decrementAndGet();
                generatedChunks.put(chunk.Position, chunk);
            }
            onLazyInitChunks.forEach(c -> c.accept(true));
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

            data.positions[i] = new Vector3i(wx, y, wz);
            data.textures[i] = server.GetWorldGen().GetTexture(wx, y, wz);
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
