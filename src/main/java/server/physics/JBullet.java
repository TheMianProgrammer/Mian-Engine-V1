package server.physics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.BroadphaseProxy;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

import obj.entity.Entity;
import obj.entity.player.Player;

public class JBullet {
    CollisionConfiguration config;
    CollisionDispatcher dispatcher;
    BroadphaseInterface broadphase;
    SequentialImpulseConstraintSolver solver;
    DiscreteDynamicsWorld dynamicsWorld;

    List<RigidBody> bodies = new ArrayList<>();

    public void InitJBullet()
    {
        config = new DefaultCollisionConfiguration();
        dispatcher = new CollisionDispatcher(config);
        broadphase = new DbvtBroadphase();
        solver = new SequentialImpulseConstraintSolver();
        dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, config);
        dynamicsWorld.setGravity(new Vector3f(0, -9.81f,0));
    }
    public TriangleIndexVertexArray buildTriangleIndexVertexArray(float[] verts, Vector3f localOffset, Vector3f localScale) {
        // verts als triangle-list [t0.v0.x, t0.v0.y, t0.v0.z, t0.v1.x, ...]
        int numTriangles = verts.length / 9;
        int numVertices  = verts.length / 3;

        int[] indices = new int[numTriangles * 3];
        for (int i = 0; i < indices.length; i++) indices[i] = i;

        int triangleIndexStride = 3 * 4;
        int vertexStride = 3 * 4;

        ByteBuffer indexBuffer = ByteBuffer.allocateDirect(indices.length * 4).order(ByteOrder.nativeOrder());
        for (int idx : indices) indexBuffer.putInt(idx);
        indexBuffer.flip();

        // transformiere Vertices: scale + offset
        float[] transformed = new float[verts.length];
        for (int i = 0; i < verts.length; i += 3) {
            transformed[i + 0] = verts[i + 0] * localScale.x + localOffset.x;
            transformed[i + 1] = verts[i + 1] * localScale.y + localOffset.y;
            transformed[i + 2] = verts[i + 2] * localScale.z + localOffset.z;
        }

        ByteBuffer vertexBuffer = ByteBuffer.allocateDirect(transformed.length * 4).order(ByteOrder.nativeOrder());
        for (float v : transformed) vertexBuffer.putFloat(v);
        vertexBuffer.flip();

        return new TriangleIndexVertexArray(
            numTriangles,
            indexBuffer, triangleIndexStride,
            numVertices,
            vertexBuffer, vertexStride
        );
    }
    public void initPlayer(Player player)
    {
        CollisionShape boxSpahShape = new BoxShape(new Vector3f(1, 2, 1));

        Transform startTransform = new Transform();
        startTransform.setIdentity();
        startTransform.origin.set(player.position.x,player.position.y,player.position.z );

        float mass = 1f;
        Vector3f inertia = new Vector3f();
        boxSpahShape.calculateLocalInertia(mass, inertia);

        DefaultMotionState motion =new DefaultMotionState(startTransform);
        RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, motion, boxSpahShape, inertia);
        RigidBody body = new RigidBody(rbInfo);
        player.body = body;

        dynamicsWorld.addRigidBody(body);
    }
    public void AddRigidbodyMesh(Entity entity, boolean isStatic) {
        float[] verts = entity.getFlattenVertecies();

        if (isStatic) {
            Vector3f offset = entity.RenderOffset != null ? new Vector3f(entity.RenderOffset.x, entity.RenderOffset.y, entity.RenderOffset.z) : new Vector3f(0,0,0);
            TriangleIndexVertexArray meshArray = buildTriangleIndexVertexArray(verts, offset, new Vector3f(entity.Scale.x, entity.Scale.y, entity.Scale.z));
            BvhTriangleMeshShape meshShape = new BvhTriangleMeshShape(meshArray, true);

            Transform startTransform = new Transform();
            startTransform.setIdentity();
            startTransform.origin.set(entity.Position.x + offset.x, entity.Position.y + offset.y, entity.Position.z + offset.z);
            startTransform.setRotation(entity.Rotation);

            DefaultMotionState motion = new DefaultMotionState(startTransform);
            RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(0f, motion, meshShape, new Vector3f());
            RigidBody body = new RigidBody(rbInfo);
            entity.body = body;
            dynamicsWorld.addRigidBody(body);
        } else {
            // dynamic concave meshes: NICHT BvhTriangleMeshShape verwenden!
            // Verwende stattdessen ConvexHullShape (evtl. mit Dekimation) oder teile das Mesh in Konvexe Teile.
        }
    }

    public void AddRigidbody(Entity entity)
    {
        CollisionShape boxSpahShape = new BoxShape(new Vector3f(
            entity.Scale.x / 1f,
            entity.Scale.y / 1f,
            entity.Scale.z / 1f
        ));

        Transform startTransform = new Transform();
        startTransform.setIdentity();
        startTransform.origin.set(entity.Position.x,entity.Position.y, entity.Position.z );
        startTransform.setRotation(entity.Rotation);

        float mass = entity.isStatic ? 0f : 1f;
        Vector3f inertia = new Vector3f();
        if(mass != 0f) boxSpahShape.calculateLocalInertia(mass, inertia);

        DefaultMotionState motion =new DefaultMotionState(startTransform);
        RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, motion, boxSpahShape, inertia);
        RigidBody body = new RigidBody(rbInfo);
        entity.body = body;

        dynamicsWorld.addRigidBody(body);
    }
    public void UpdatePhysics(List<Entity> entities, Player player)
    {
        dynamicsWorld.stepSimulation(1/120f, 10);

        for(Entity e : entities)
        {
            Transform trans = new Transform();
            e.body.getMotionState().getWorldTransform(trans);
            Vector3f pos = trans.origin;
            Quat4f rot = new Quat4f();
            trans.getRotation(rot);

            e.Position.set(pos.x, pos.y, pos.z);
            e.Rotation.set(rot);
        }

        UpdatePlayerPhysics(player);
    }
    void UpdatePlayerPhysics(Player player)
    {
        // Get transform in phyiscs
        Transform transform = new Transform();
        player.body.getMotionState().getWorldTransform(transform);
        Vector3f pos = transform.origin;

        // Connect the player transform with the physics
        player.position.set(pos.x, pos.y, pos.z);
        player.camera.position.set(pos.x, pos.y, pos.z);

        player.isGrounded = isGrounded(player);

        // Set the velocity in the physics
        Vector3f velocity = player.body.getLinearVelocity(new Vector3f());
        velocity.x = player.inputX;
        velocity.z = player.inputZ;
        if(player.JumpStrenght > 0)
            velocity.y = player.JumpStrenght;
        player.body.setLinearVelocity(velocity);
    }

    public boolean isGrounded(Player player) {
        final float CONTACT_EPS = 0.01f;   // tolerance for "touching"
        final float MIN_UP_DOT = 0.7f;     // how much the contact normal must point upward
        
        int numManifolds = dynamicsWorld.getDispatcher().getNumManifolds();
        for (int i = 0; i < numManifolds; i++) {
            PersistentManifold manifold = dynamicsWorld.getDispatcher().getManifoldByIndexInternal(i);
            CollisionObject obA = (CollisionObject) manifold.getBody0();
            CollisionObject obB = (CollisionObject) manifold.getBody1();
        
            // is player involved in this manifold?
            if (obA == player.body || obB == player.body) {
                int numContacts = manifold.getNumContacts();
                for (int j = 0; j < numContacts; j++) {
                    ManifoldPoint pt = manifold.getContactPoint(j);
                
                    // contact distance (<= eps means contact or penetration)
                    if (pt.getDistance() <= CONTACT_EPS) {
                        // get normal pointing *towards* the player
                        javax.vecmath.Vector3f normalForPlayer = new javax.vecmath.Vector3f(pt.normalWorldOnB);
                        // normalWorldOnB is the normal on body B pointing from B -> A
                        // if player is body1 (B), we need to invert it so it points to player
                        if (obB == player.body) {
                            normalForPlayer.negate();
                        }
                    
                        // check if normal has an upward component (i.e. floor beneath player)
                        if (normalForPlayer.y > MIN_UP_DOT) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
