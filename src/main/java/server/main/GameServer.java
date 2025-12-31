package server.main;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2i;
import org.joml.Vector3f;

import engine.Main;
import engine.render.WorldRenderer;
import engine.render.texture.Texture;
import obj.entity.Entity;
import obj.entity.player.Player;
import obj.objects.Chunk;
import obj.objects.World;
import server.main.worldgen.WorldGen;
import server.physics.JBullet;

public class GameServer {
    List<Entity> entities = new ArrayList<>();
    public List<Main> Clients = new ArrayList<>();
    public Player player;
    public EntityLoader entityLoader;
    public Texture GrassTexture;
    WorldRenderer renderer;
    WorldGen worldGen;

    Entity Terrain;
    Entity TestCube;
    int seed = 51234;
    final int startGenDistance = 4;

    World world; 

    JBullet physics = new JBullet();

    public void init()
    {
        physics.InitJBullet();
        entityLoader = new EntityLoader(this);
        world = new World(this);
        renderer = new WorldRenderer();
        renderer.AddWorld(world);
        worldGen = new WorldGen(seed);
        world.onLazyInitChunks.add((b)->onChunksGenerated(b));

        LoadImages();
        
        TestCube = entityLoader.LoadEntity("assets/obj/basics/Cube.obj", new Vector3f(0, 5, 0), new Vector3f(1, 1, 1), GrassTexture, false);
        entityLoader.initEntityCollider(TestCube, false);
        ActivateEntity(TestCube);

        PrepareWorldGeneration();

        physics.initPlayer(player);
    }

    void PrepareWorldGeneration(){
        for (int x = 0; x <= startGenDistance; x++)
        {
            for (int z = 0; z < startGenDistance; z++)
            {
                Vector2i ChunkPos = new Vector2i(x-16, z-16);

                world.InitChunk(ChunkPos);
            }
        }
    }

    public void UpdatePlayerChunks()
    {
        world.PrepareChunkGeneration();

        for(int x = 0; x <= player.RenderDistance*2; x++)
        {
            for(int z = 0; z <= player.RenderDistance*2; z++)
            {
                Vector2i PlayerOffset = new Vector2i(
                    Math.round(player.position.x/16),
                    Math.round(player.position.z/16)
                );
                Vector2i ChunkPos = new Vector2i(x+PlayerOffset.x-player.RenderDistance, z+PlayerOffset.y-player.RenderDistance);
                world.GenChunk(ChunkPos);
            }
        }
    }

    void onChunksGenerated(boolean idkwhyitshere_cantremoveit)
    {
        world.loadedChunks.forEach((Vector2i pos, Chunk chunk) -> {
            world.UpdateChunk(chunk);
        });
        world.CleanupChunks();
    }

    public void UpdatePlayerColliders()
    {
        world.loadedChunks.forEach((Vector2i Pos, Chunk chunk) -> {
            chunk.CheckColliders(new javax.vecmath.Vector3f(player.position.x, player.position.y, player.position.z));
        });
    }

    public void ActivateEntity(Entity e){
        for(Main c : Clients){
            c.ActivateEntity(e, GrassTexture);
        }
    }

    void LoadImages()
    {
        GrassTexture = new Texture("assets/texture/grass/VoxelGrass.png");
    }

    public void initEntity(Entity e)
    {
        entities.add(e);
        physics.AddRigidbody(e);
    }
    public void Shutdown()
    {
        world.Shutdown();
        physics.Shutdown();
    }
    public void initMeshEntity(Entity e)
    {
        entities.add(e);
        physics.AddRigidbodyMesh(e, e.isStatic);
    }
    public void removeEntityColl(Entity e)
    {
        physics.RemoveRigidbody(e);
    }
    
    boolean isPlayerStartLocked = true;
    public void tick()
    {
        physics.UpdatePhysics(entities, player);
        world.TickChunkGeneration();
        
        // for start
        if(!world.isBussyLoadingChunks() && isPlayerStartLocked)
        {
            isPlayerStartLocked = false;
            player.position = new Vector3f(0, 20, 0);
            UpdatePlayerChunks();
            Main.GetRenderer().PackageEntityRenderers();
            System.out.println("Done Generating Chunks!");
        }
    }

    public WorldGen GetWorldGen()
    {
        return worldGen;
    }
    public WorldRenderer GetWorldRenderer()
    {
        return renderer;
    }
}
