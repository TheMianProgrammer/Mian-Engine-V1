package server.main;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector2f;

import obj.entity.Entity;
import obj.entity.player.Player;
import obj.objects.block.Block;
import server.client.Client;
import server.physics.JBullet;
import engine.Main;
import engine.render.Renderer;
import engine.render.texture.Texture;

public class GameServer {
    List<Entity> entities = new ArrayList<>();
    public List<Main> Clients = new ArrayList<>();
    public Player player;
    public EntityLoader entityLoader;
    public Texture GrassTexture;

    Entity Terrain;
    Entity TestCube;

    JBullet physics = new JBullet();

    public void init()
    {
        physics.InitJBullet();
        entityLoader = new EntityLoader(this);

        LoadImages();
        //Terrain = entityLoader.LoadEntity("assets/obj/terrain/VoxelMap2.obj", new Vector3f(0, -30, 0), new Vector3f(10, 10, 10), GrassTexture, true);
        //entityLoader.initEntityCollider(Terrain, true);
        //Terrain.TillTexture(new Vector2f(100, 100));
        //ActivateEntity(Terrain);
        
        TestCube = entityLoader.LoadEntity("assets/obj/basics/Cube.obj", new Vector3f(0, 5, 0), new Vector3f(1, 1, 1), GrassTexture, false);
        entityLoader.initEntityCollider(TestCube, false);
        ActivateEntity(TestCube);

        for(int y = 0; y < 20; y++){
            Entity cube = entityLoader.LoadEntity("assets/obj/basics/Cube.obj", new Vector3f(0, (y)+y, 0), new Vector3f(1, 1, 1), GrassTexture, false);
            entityLoader.initEntityCollider(cube, false);
            ActivateEntity(cube);
        }
        for(int x = 0; x < 20; x++)
        {
            for(int z = 0; z < 20; z++)
            {
                Block newBlock = new Block(new Vector3f(x, 0, z), 0);
                newBlock.LoadEntity(entityLoader);
                ActivateEntity(newBlock.getEntity());
                // Entity box = entityLoader.LoadEntity("assets/obj/basics/Cube.obj", new Vector3f(x*2, 0, z*2), new Vector3f(1, 1, 1), GrassTexture, true);
                // entityLoader.initEntityCollider(box, false);
                // ActivateEntity(box);
                // for(Entity side : newBlock.sides)
                // {
                //     ActivateEntity(side);
                // }
            }
        }

        physics.initPlayer(player);
    }

    void ActivateEntity(Entity e){
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
    public void initMeshEntity(Entity e)
    {
        entities.add(e);
        physics.AddRigidbodyMesh(e, e.isStatic);
    }
    
    public void tick()
    {
        physics.UpdatePhysics(entities, player);
    }
}
