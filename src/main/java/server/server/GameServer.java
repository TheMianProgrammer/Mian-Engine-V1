package server.server;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector2f;

import engine.Renderer;
import obj.Entity;
import obj.Player;
import server.physics.JBullet;
import texture.Texture;
import engine.Client;
import engine.Main;

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
        Terrain = entityLoader.LoadEntity("assets/obj/terrain/VoxelMap2.obj", new Vector3f(0, -30, 0), new Vector3f(10, 10, 10), GrassTexture, true);
        entityLoader.initEntityCollider(Terrain, true);
        Terrain.TillTexture(new Vector2f(100, 100));
        ActivateEntity(Terrain);
        
        TestCube = entityLoader.LoadEntity("assets/obj/basics/Cube.obj", new Vector3f(0, 5, 0), new Vector3f(1, 1, 1), GrassTexture, false);
        entityLoader.initEntityCollider(TestCube, false);
        ActivateEntity(TestCube);

        for(int y = 0; y < 10; y++){
            Entity cube = entityLoader.LoadEntity("assets/obj/basics/Cube.obj", new Vector3f(0, (y)+y, 0), new Vector3f(1, 1, 1), GrassTexture, false);
            entityLoader.initEntityCollider(cube, false);
            ActivateEntity(cube);
        }
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
