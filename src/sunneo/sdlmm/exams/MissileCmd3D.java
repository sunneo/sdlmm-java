package sunneo.sdlmm.exams;

import sunneo.sdlmm.babylon3d.*;
import sunneo.sdlmm.implement.SDLMMFrame;
import sunneo.sdlmm.interfaces.SDLMMInterface;
import java.util.ArrayList;
import java.util.List;

/**
 * 3D Missile Command game using Babylon3D rendering
 * Complete rewrite matching ref-sdlmm/exams/missilecmd3d.c EXACTLY
 * 
 * Features:
 * - Particle systems (smoke trails and explosions)
 * - Glow textures for all objects
 * - Trajectory line meshes
 * - Ground plane
 * - Purple gradient sky background
 * - Complete visual consistency with reference
 */
public class MissileCmd3D extends SDLMMFrame {
    private static final long serialVersionUID = 1L;
    
    // Constants matching reference
    private static final int width = 800;
    private static final int height = 600;
    private static final int MAX_BUILD = 5;
    private static final int MAX_ENEMY = 15;
    private static final int MAX_OUR_MISSILE = 12;
    private static final float MAX_EXPL_R = 2.5f;
    private static final float ENEMY_MAX_EXPL_R = 3.0f;
    private static final float GROUND_Y = -3.0f;
    private static final float WORLD_WIDTH = 20.0f;
    private static final float WORLD_DEPTH = 5.0f;
    
    // Particle system constants
    private static final int MAX_SMOKE_PARTICLES = 200;
    private static final int MAX_EXPLOSION_PARTICLES = 500;
    private static final int SMOKE_MAX_ALPHA = 64;
    
    // Game state
    private int score = 0;
    private int remainMissile = 45;
    private int remainGenEnemy = 40;
    private int remainEnemy = 40;
    private int showHelp = 1;
    private volatile int mx = width / 2;
    private volatile int my = height / 2;
    
    // Camera
    private float camDist = 25.0f;
    private float camAngleX = 0.4f;
    private float camAngleY = 0.0f;
    
    // 3D rendering
    private Device device;
    private Camera camera;
    
    // Particle class
    static class Particle {
        Vector3 pos, vel;
        int color;
        float life;      // 0.0 to 1.0
        float size;
        boolean active;
        
        Particle() {
            pos = Vector3.zero();
            vel = Vector3.zero();
            color = 0xFFFFFF;
            life = 1.0f;
            size = 0.3f;
            active = false;
        }
    }
    
    // Build structure
    static class Build3D {
        Vector3 pos;
        boolean alive;
        boolean isbuild;
        
        Build3D() {
            pos = Vector3.zero();
            alive = true;
            isbuild = true;
        }
    }
    
    // Enemy missile structure
    static class EnemyMissile3D {
        Vector3 from, to, pos, vel;
        boolean alive, expl, ishit;
        int targetBuild;
        float r;
        
        EnemyMissile3D() {
            from = Vector3.zero();
            to = Vector3.zero();
            pos = Vector3.zero();
            vel = Vector3.zero();
            alive = false;
            expl = false;
            ishit = false;
            targetBuild = 0;
            r = 0.2f;
        }
    }
    
    // Our missile structure
    static class OurMissile3D {
        Vector3 target, pos, vel;
        boolean active, expl;
        float r;
        Vector3 launchPos;
        int smokeTick;
        int[] smokeParticleIds;
        int smokeCount;
        int smokeHead;
        
        OurMissile3D() {
            target = Vector3.zero();
            pos = Vector3.zero();
            vel = Vector3.zero();
            active = false;
            expl = false;
            r = 0.15f;
            launchPos = Vector3.zero();
            smokeTick = 0;
            smokeParticleIds = new int[5];
            smokeCount = 0;
            smokeHead = 0;
        }
    }
    
    // Game objects
    private Build3D[] builds = new Build3D[MAX_BUILD];
    private EnemyMissile3D[] enemies = new EnemyMissile3D[MAX_ENEMY];
    private OurMissile3D[] ourMissiles = new OurMissile3D[MAX_OUR_MISSILE];
    
    // Particle systems
    private Particle[] smokeParticles = new Particle[MAX_SMOKE_PARTICLES];
    private Particle[] explosionParticles = new Particle[MAX_EXPLOSION_PARTICLES];
    private int nextSmokeIdx = 0;
    private int nextExplosionIdx = 0;
    
    // Template meshes (shared)
    private Mesh buildingMesh, destroyedMesh, launcherMesh;
    private Mesh groundMesh;
    private Mesh explSphere, missileSphere;
    private Mesh ourExplSphere, ourMissileSphere;
    
    // Particle textures
    private Texture smokeTexture, explosionTexture;
    
    // Dynamic mesh list for rendering
    private List<Mesh> renderMeshes = new ArrayList<>();
    private List<Mesh> trajectoryMeshes = new ArrayList<>();
    
    public MissileCmd3D(String title, int width, int height) {
        super(title, width, height);
        
        // Initialize arrays
        for (int i = 0; i < MAX_BUILD; i++) {
            builds[i] = new Build3D();
        }
        for (int i = 0; i < MAX_ENEMY; i++) {
            enemies[i] = new EnemyMissile3D();
        }
        for (int i = 0; i < MAX_OUR_MISSILE; i++) {
            ourMissiles[i] = new OurMissile3D();
        }
        for (int i = 0; i < MAX_SMOKE_PARTICLES; i++) {
            smokeParticles[i] = new Particle();
        }
        for (int i = 0; i < MAX_EXPLOSION_PARTICLES; i++) {
            explosionParticles[i] = new Particle();
        }
    }
    
    private float frand() {
        return (float)Math.random();
    }
    
    /**
     * Generate glow texture matching reference implementation
     */
    private Texture generateGlowTexture(int baseColor) {
        int size = 16;
        Texture tex = new Texture(size, size);
        
        int br = (baseColor >> 16) & 0xff;
        int bg = (baseColor >> 8) & 0xff;
        int bb = baseColor & 0xff;
        
        float cx = size / 2.0f;
        float cy = size / 2.0f;
        float maxR = size / 2.0f;
        
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - cx;
                float dy = y - cy;
                float dist = (float)Math.sqrt(dx * dx + dy * dy) / maxR;
                if (dist > 1.0f) dist = 1.0f;
                
                int r, g, b;
                if (dist < 0.3f) {
                    float t = dist / 0.3f;
                    r = 255 - (int)((255 - br) * t);
                    g = 255 - (int)((255 - bg) * t);
                    b = 255 - (int)((255 - bb) * t);
                } else {
                    float t = (dist - 0.3f) / 0.7f;
                    float intensity = 1.0f - t * t;
                    r = (int)(br * intensity);
                    g = (int)(bg * intensity);
                    b = (int)(bb * intensity);
                }
                
                if (r > 255) r = 255; if (r < 0) r = 0;
                if (g > 255) g = 255; if (g < 0) g = 0;
                if (b > 255) b = 255; if (b < 0) b = 0;
                
                tex.internalBuffer[y * size + x] = (r << 16) | (g << 8) | b;
            }
        }
        
        return tex;
    }
    
    /**
     * Create a scaled cube mesh
     */
    private Mesh createScaledCube(float sx, float sy, float sz) {
        Mesh mesh = Mesh.createCube();
        // Scale vertices
        for (int i = 0; i < mesh.verticesCount; i++) {
            mesh.Vertices[i].Coordinates.x *= sx / 2.0f;
            mesh.Vertices[i].Coordinates.y *= sy / 2.0f;
            mesh.Vertices[i].Coordinates.z *= sz / 2.0f;
        }
        return mesh;
    }
    
    /**
     * Create line mesh for trajectory visualization
     */
    private Mesh createLineMesh(Vector3 start, Vector3 end, float width, int color) {
        Mesh mesh = new Mesh("line", 4, 2);
        
        // Generate glow texture with specified color
        mesh.texture = generateGlowTexture(color);
        
        // Calculate direction vector
        Vector3 dir = end.subtract(start);
        float len = dir.length();
        if (len < 0.001f) len = 0.001f;
        dir = dir.normalizeCopy();
        
        // Calculate perpendicular vector for width
        Vector3 up = new Vector3(0, 1, 0);
        Vector3 perp = Vector3.cross(dir, up);
        float plen = perp.length();
        if (plen < 0.001f) {
            up = new Vector3(1, 0, 0);
            perp = Vector3.cross(dir, up);
            plen = perp.length();
        }
        perp = perp.scale(width * 0.5f / plen);
        
        // Create quad vertices
        mesh.Vertices[0].Coordinates = start.subtract(perp);
        mesh.Vertices[1].Coordinates = start.add(perp);
        mesh.Vertices[2].Coordinates = end.subtract(perp);
        mesh.Vertices[3].Coordinates = end.add(perp);
        
        // Normals face camera (billboard)
        Vector3 normal = perp.normalizeCopy();
        for (int i = 0; i < 4; i++) {
            mesh.Vertices[i].Normal = normal;
            mesh.Vertices[i].WorldCoordinates = Vector3.zero();
            mesh.Vertices[i].TextureCoordinates = new Vector3(
                (i % 2 == 1) ? 1.0f : 0.0f,
                (i >= 2) ? 1.0f : 0.0f,
                0
            );
        }
        
        // Create two triangles
        mesh.faces[0].A = 0; mesh.faces[0].B = 1; mesh.faces[0].C = 2;
        mesh.faces[1].A = 1; mesh.faces[1].B = 3; mesh.faces[1].C = 2;
        
        mesh.Position = Vector3.zero();
        mesh.Rotation = Vector3.zero();
        
        return mesh;
    }
    
    /**
     * Initialize scene meshes matching reference
     */
    private void initSceneMeshes() {
        // Ground plane
        groundMesh = createScaledCube(WORLD_WIDTH + 4, 0.3f, WORLD_DEPTH + 4);
        groundMesh.texture = generateGlowTexture(0x403020);
        
        // Enemy explosion sphere
        explSphere = Mesh.createSphere(1.0f, 5, 3);
        explSphere.texture = generateGlowTexture(0xff4010);
        
        // Enemy missile sphere
        missileSphere = Mesh.createSphere(0.2f, 4, 3);
        missileSphere.texture = generateGlowTexture(0x40ff40);
        
        // Our explosion sphere
        ourExplSphere = Mesh.createSphere(0.8f, 5, 3);
        ourExplSphere.texture = generateGlowTexture(0x40c0ff);
        
        // Our missile sphere
        ourMissileSphere = Mesh.createSphere(0.15f, 4, 3);
        ourMissileSphere.texture = generateGlowTexture(0xe0e0ff);
        
        // Building meshes
        buildingMesh = createScaledCube(1.5f, 2.0f, 1.5f);
        buildingMesh.texture = generateGlowTexture(0x6080a0);
        
        destroyedMesh = createScaledCube(1.5f, 0.5f, 1.5f);
        destroyedMesh.texture = generateGlowTexture(0x804020);
        
        launcherMesh = createScaledCube(1.0f, 1.2f, 1.0f);
        launcherMesh.texture = generateGlowTexture(0x60a060);
        
        // Create particle textures
        smokeTexture = Texture.createGaussian(32);
        explosionTexture = Texture.createGaussian(32);
    }
    
    /**
     * Initialize buildings
     */
    private void initBuilds() {
        float spacing = WORLD_WIDTH / MAX_BUILD;
        float startX = -WORLD_WIDTH / 2 + spacing / 2;
        
        for (int i = 0; i < MAX_BUILD; i++) {
            builds[i].pos = new Vector3(startX + i * spacing, GROUND_Y + 1.0f, 0);
            builds[i].alive = true;
            builds[i].isbuild = true;
        }
        
        // Middle one is the launcher
        builds[MAX_BUILD / 2].isbuild = false;
        builds[MAX_BUILD / 2].pos.y = GROUND_Y + 0.6f;
    }
    
    /**
     * Spawn smoke particle
     */
    private int spawnSmokeParticle(Vector3 pos) {
        Particle p = smokeParticles[nextSmokeIdx];
        int particleId = nextSmokeIdx;
        nextSmokeIdx = (nextSmokeIdx + 1) % MAX_SMOKE_PARTICLES;
        
        p.pos = pos.copy();
        // Small random velocity for spread
        p.vel = new Vector3(
            (frand() - 0.5f) * 0.02f,
            (frand() - 0.5f) * 0.02f,
            (frand() - 0.5f) * 0.02f
        );
        p.life = 1.0f;
        p.size = 0.3f + frand() * 0.2f;
        p.color = 0xc0c0c0;  // Light gray
        p.active = true;
        return particleId;
    }
    
    // Fire colors for explosion particles
    private static final int[] explosionGlowColors = {
        0xFFFF00, 0xFFDD00, 0xFF8800, 0xFF4400, 0xFF0000
    };
    
    // Lightning green colors for our missile explosions
    private static final int[] greenGlowColors = {
        0x40ff40, 0x50ff50, 0x60ff60, 0x30ff30, 0x70ff70
    };
    
    /**
     * Spawn explosion particles (enemy explosions - fire colors)
     */
    private void spawnExplosionParticles(Vector3 pos, int count) {
        for (int i = 0; i < count; i++) {
            Particle p = explosionParticles[nextExplosionIdx];
            nextExplosionIdx = (nextExplosionIdx + 1) % MAX_EXPLOSION_PARTICLES;
            
            // Random direction
            float theta = frand() * 2.0f * (float)Math.PI;
            float phi = frand() * (float)Math.PI;
            float speed = 0.05f + frand() * 0.15f;
            
            p.pos = pos.copy();
            p.vel = new Vector3(
                (float)(Math.sin(phi) * Math.cos(theta)) * speed,
                (float)(Math.sin(phi) * Math.sin(theta)) * speed,
                (float)(Math.cos(phi)) * speed
            );
            p.life = 1.0f;
            p.size = 0.2f + frand() * 0.3f;
            // Fire colors
            int colorIdx = (int)(Math.random() * explosionGlowColors.length);
            p.color = explosionGlowColors[colorIdx];
            p.active = true;
        }
    }
    
    /**
     * Spawn our explosion particles (lightning green colors)
     */
    private void spawnOurExplosionParticles(Vector3 pos, int count) {
        for (int i = 0; i < count; i++) {
            Particle p = explosionParticles[nextExplosionIdx];
            nextExplosionIdx = (nextExplosionIdx + 1) % MAX_EXPLOSION_PARTICLES;
            
            // Random direction
            float theta = frand() * 2.0f * (float)Math.PI;
            float phi = frand() * (float)Math.PI;
            float speed = 0.05f + frand() * 0.15f;
            
            p.pos = pos.copy();
            p.vel = new Vector3(
                (float)(Math.sin(phi) * Math.cos(theta)) * speed,
                (float)(Math.sin(phi) * Math.sin(theta)) * speed,
                (float)(Math.cos(phi)) * speed
            );
            p.life = 1.0f;
            p.size = 0.2f + frand() * 0.3f;
            // Lightning green colors
            int colorIdx = (int)(Math.random() * greenGlowColors.length);
            p.color = greenGlowColors[colorIdx];
            p.active = true;
        }
    }
    
    /**
     * Update particles
     */
    private void updateParticles() {
        // Update smoke particles
        for (int i = 0; i < MAX_SMOKE_PARTICLES; i++) {
            if (!smokeParticles[i].active) continue;
            
            smokeParticles[i].pos = smokeParticles[i].pos.add(smokeParticles[i].vel);
            
            // Fade out
            smokeParticles[i].life -= 0.02f;
            if (smokeParticles[i].life <= 0) {
                smokeParticles[i].active = false;
            }
        }
        
        // Update explosion particles
        for (int i = 0; i < MAX_EXPLOSION_PARTICLES; i++) {
            if (!explosionParticles[i].active) continue;
            
            explosionParticles[i].pos = explosionParticles[i].pos.add(explosionParticles[i].vel);
            
            // Expand and fade
            explosionParticles[i].size += 0.03f;
            explosionParticles[i].life -= 0.015f;
            
            if (explosionParticles[i].life <= 0) {
                explosionParticles[i].active = false;
            }
        }
    }
    
    /**
     * Generate enemy missiles
     */
    private void generateEnemy() {
        if (remainGenEnemy <= 0) return;
        
        for (int i = 0; i < MAX_ENEMY; i++) {
            if (enemies[i].alive) continue;
            if (remainGenEnemy <= 0) return;
            
            int targetIdx = (int)(Math.random() * MAX_BUILD);
            float sx = (frand() - 0.5f) * WORLD_WIDTH;
            float sy = 15.0f;  // from the sky
            float sz = (frand() - 0.5f) * WORLD_DEPTH;
            float tx = builds[targetIdx].pos.x;
            float ty = builds[targetIdx].pos.y;
            float tz = builds[targetIdx].pos.z;
            float speed = 0.03f + frand() * 0.04f;
            
            float dx = tx - sx, dy = ty - sy, dz = tz - sz;
            float len = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len < 0.001f) len = 1.0f;
            
            enemies[i].from = new Vector3(sx, sy, sz);
            enemies[i].to = new Vector3(tx, ty, tz);
            enemies[i].pos = new Vector3(sx, sy, sz);
            enemies[i].vel = new Vector3(dx / len * speed, dy / len * speed, dz / len * speed);
            enemies[i].alive = true;
            enemies[i].expl = false;
            enemies[i].ishit = false;
            enemies[i].r = 0.2f;
            enemies[i].targetBuild = targetIdx;
            remainGenEnemy--;
        }
    }
    
    /**
     * Update enemy missiles
     */
    private void updateEnemies() {
        for (int i = 0; i < MAX_ENEMY; i++) {
            if (!enemies[i].alive) continue;
            
            if (enemies[i].expl) {
                enemies[i].r += 0.15f;
                // Spawn explosion particles for enemy explosions
                if (enemies[i].r < ENEMY_MAX_EXPL_R && Math.random() < 0.33) {
                    spawnExplosionParticles(enemies[i].pos, 3);
                }
                if (enemies[i].r >= ENEMY_MAX_EXPL_R) {
                    enemies[i].alive = false;
                    enemies[i].expl = false;
                    enemies[i].ishit = false;
                    if (remainEnemy > 0) remainEnemy--;
                }
                continue;
            }
            
            // Check collision with our explosions
            for (int j = 0; j < MAX_OUR_MISSILE; j++) {
                if (!ourMissiles[j].active || !ourMissiles[j].expl) continue;
                float dx = enemies[i].pos.x - ourMissiles[j].pos.x;
                float dy = enemies[i].pos.y - ourMissiles[j].pos.y;
                float dz = enemies[i].pos.z - ourMissiles[j].pos.z;
                if (dx * dx + dy * dy + dz * dz < ourMissiles[j].r * ourMissiles[j].r) {
                    enemies[i].expl = true;
                    enemies[i].ishit = true;
                    score += 100;
                    // Spawn initial explosion particles
                    spawnExplosionParticles(enemies[i].pos, 50);
                    break;
                }
            }
            if (enemies[i].expl) continue;
            
            // Check chain-reaction with other exploding enemies
            for (int j = 0; j < MAX_ENEMY; j++) {
                if (j == i || !enemies[j].alive || !enemies[j].expl || !enemies[j].ishit) continue;
                float dx = enemies[i].pos.x - enemies[j].pos.x;
                float dy = enemies[i].pos.y - enemies[j].pos.y;
                float dz = enemies[i].pos.z - enemies[j].pos.z;
                if (dx * dx + dy * dy + dz * dz < enemies[j].r * enemies[j].r) {
                    enemies[i].expl = true;
                    enemies[i].ishit = true;
                    score += 100;
                    // Spawn initial explosion particles
                    spawnExplosionParticles(enemies[i].pos, 50);
                    break;
                }
            }
            if (enemies[i].expl) continue;
            
            // Move
            enemies[i].pos = enemies[i].pos.add(enemies[i].vel);
            
            // Check if reached target
            float dx = enemies[i].to.x - enemies[i].pos.x;
            float dy = enemies[i].to.y - enemies[i].pos.y;
            float dz = enemies[i].to.z - enemies[i].pos.z;
            if (dx * dx + dy * dy + dz * dz < 0.5f) {
                enemies[i].expl = true;
                builds[enemies[i].targetBuild].alive = false;
                // Spawn initial explosion particles
                spawnExplosionParticles(enemies[i].pos, 50);
            }
        }
    }
    
    /**
     * Update our missiles
     */
    private void updateOurMissiles() {
        for (int i = 0; i < MAX_OUR_MISSILE; i++) {
            if (!ourMissiles[i].active) continue;
            
            if (!ourMissiles[i].expl) {
                // Spawn smoke particles periodically - limit to 5 particles
                ourMissiles[i].smokeTick++;
                if (ourMissiles[i].smokeTick % 2 == 0) {
                    int particleId = spawnSmokeParticle(ourMissiles[i].pos);
                    // If we already have 5 smoke particles, replace the oldest one
                    if (ourMissiles[i].smokeCount >= 5) {
                        // Deactivate the oldest particle at the head position
                        smokeParticles[ourMissiles[i].smokeParticleIds[ourMissiles[i].smokeHead]].active = false;
                        // Replace with new particle
                        ourMissiles[i].smokeParticleIds[ourMissiles[i].smokeHead] = particleId;
                        // Move head to next position in circular buffer
                        ourMissiles[i].smokeHead = (ourMissiles[i].smokeHead + 1) % 5;
                    } else {
                        // Still filling up the initial 5 particles
                        ourMissiles[i].smokeParticleIds[ourMissiles[i].smokeCount] = particleId;
                        ourMissiles[i].smokeCount++;
                    }
                }
                
                float dx = ourMissiles[i].target.x - ourMissiles[i].pos.x;
                float dy = ourMissiles[i].target.y - ourMissiles[i].pos.y;
                float dz = ourMissiles[i].target.z - ourMissiles[i].pos.z;
                if (dx * dx + dy * dy + dz * dz < 0.5f) {
                    ourMissiles[i].expl = true;
                    ourMissiles[i].r = 0.3f;
                    // Spawn explosion particles with lightning green color
                    spawnOurExplosionParticles(ourMissiles[i].pos, 50);
                }
                ourMissiles[i].pos = ourMissiles[i].pos.add(ourMissiles[i].vel);
            } else {
                if (ourMissiles[i].r < MAX_EXPL_R) {
                    ourMissiles[i].r += 0.08f;
                    // Continue spawning explosion particles
                    if (Math.random() < 0.33) {
                        spawnOurExplosionParticles(ourMissiles[i].pos, 3);
                    }
                } else {
                    ourMissiles[i].active = false;
                    ourMissiles[i].expl = false;
                }
            }
        }
    }
    
    /**
     * Launch missile with proper screen-to-world unprojection
     */
    private void launchMissile(int screenX, int screenY) {
        if (remainMissile <= 0) return;
        
        for (int i = 0; i < MAX_OUR_MISSILE; i++) {
            if (!ourMissiles[i].active) {
                // Proper screen-to-world unprojection using camera matrices
                Vector3 up = Vector3.up();
                Matrix viewMatrix = Matrix.lookAtLH(camera.Position, camera.Target, up);
                Matrix projectionMatrix = Matrix.perspectiveFovLH(0.78f,
                    (float)width / (float)height, 0.01f, 1000.0f);
                Matrix viewProj = viewMatrix.multiply(projectionMatrix);
                Matrix invViewProj = viewProj.copy();
                invViewProj.invert();
                
                // Convert screen coordinates to normalized device coordinates (NDC)
                float ndcX = ((float)screenX / width) * 2.0f - 1.0f;
                float ndcY = 1.0f - ((float)screenY / height) * 2.0f;
                
                // Unproject to world space at near and far planes
                Vector3 nearPoint = new Vector3(ndcX, ndcY, 0.0f);
                Vector3 farPoint = new Vector3(ndcX, ndcY, 1.0f);
                
                Vector3 worldNear = nearPoint.transformCoordinates(invViewProj);
                Vector3 worldFar = farPoint.transformCoordinates(invViewProj);
                
                // Create ray from near to far
                Vector3 rayDir = worldFar.subtract(worldNear);
                rayDir = rayDir.normalizeCopy();
                
                // Intersect ray with a plane at z = 0
                Vector3 targetPos;
                if (Math.abs(rayDir.z) < 0.0001f) {
                    targetPos = new Vector3(0.0f, 2.0f, 0.0f);
                } else {
                    float t = -worldNear.z / rayDir.z;
                    if (t < 0.0f) {
                        targetPos = new Vector3(0.0f, 2.0f, 0.0f);
                    } else {
                        targetPos = new Vector3(
                            worldNear.x + rayDir.x * t,
                            worldNear.y + rayDir.y * t,
                            0.0f
                        );
                    }
                }
                
                // Clamp to reasonable bounds
                if (targetPos.x < -WORLD_WIDTH / 2) targetPos.x = -WORLD_WIDTH / 2;
                if (targetPos.x > WORLD_WIDTH / 2) targetPos.x = WORLD_WIDTH / 2;
                if (targetPos.y < GROUND_Y) targetPos.y = GROUND_Y;
                if (targetPos.y > 10.0f) targetPos.y = 10.0f;
                
                // Launch from the launcher building position
                Vector3 launcherPos = builds[MAX_BUILD / 2].pos.copy();
                float tx = targetPos.x, ty = targetPos.y, tz = targetPos.z;
                float dx = tx - launcherPos.x;
                float dy = ty - launcherPos.y;
                float dz = tz - launcherPos.z;
                float len = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (len < 0.001f) len = 1.0f;
                float speed = 0.5f;
                
                ourMissiles[i].active = true;
                ourMissiles[i].expl = false;
                ourMissiles[i].pos = launcherPos;
                ourMissiles[i].launchPos = launcherPos;
                ourMissiles[i].smokeTick = 0;
                ourMissiles[i].smokeCount = 0;
                ourMissiles[i].smokeHead = 0;
                ourMissiles[i].target = new Vector3(tx, ty, tz);
                ourMissiles[i].vel = new Vector3(dx / len * speed, dy / len * speed, dz / len * speed);
                ourMissiles[i].r = 0.15f;
                remainMissile--;
                break;
            }
        }
    }
    
    /**
     * Check for round reset
     */
    private void checkReinit() {
        if (remainEnemy <= 0 && remainGenEnemy <= 0) {
            initBuilds();
            remainEnemy = 40;
            remainGenEnemy = 40;
            remainMissile = 45;
            
            for (int i = 0; i < MAX_ENEMY; i++) {
                enemies[i].alive = false;
                enemies[i].expl = false;
                enemies[i].ishit = false;
            }
            for (int i = 0; i < MAX_OUR_MISSILE; i++) {
                ourMissiles[i].active = false;
                ourMissiles[i].expl = false;
            }
        }
    }
    
    /**
     * Create a copy of a mesh template at a specific position
     */
    private Mesh meshAt(Mesh template, Vector3 pos) {
        Mesh copy = new Mesh(template.name, template.verticesCount, template.faceCount);
        // Copy vertices
        for (int i = 0; i < template.verticesCount; i++) {
            copy.Vertices[i].Coordinates = template.Vertices[i].Coordinates.copy();
            copy.Vertices[i].Normal = template.Vertices[i].Normal.copy();
            copy.Vertices[i].TextureCoordinates = template.Vertices[i].TextureCoordinates.copy();
            copy.Vertices[i].WorldCoordinates = Vector3.zero();
        }
        // Copy faces
        for (int i = 0; i < template.faceCount; i++) {
            copy.faces[i].A = template.faces[i].A;
            copy.faces[i].B = template.faces[i].B;
            copy.faces[i].C = template.faces[i].C;
        }
        // Set position and rotation
        copy.Position = pos.copy();
        copy.Rotation = template.Rotation.copy();
        copy.texture = template.texture;
        return copy;
    }
    
    /**
     * Draw the complete 3D scene matching reference
     */
    private void drawScene() {
        Vector3 lightPos = new Vector3(5, 15, -10);
        
        checkReinit();
        updateEnemies();
        updateOurMissiles();
        updateParticles();
        if (Math.random() * 100 < 15) generateEnemy();
        
        // Update camera
        camera.Position = new Vector3(
            (float)(camDist * Math.sin(camAngleY) * Math.cos(camAngleX)),
            (float)(camDist * Math.sin(camAngleX)),
            (float)(-camDist * Math.cos(camAngleY) * Math.cos(camAngleX))
        );
        camera.Target = new Vector3(0, 2, 0);
        
        // Clear and draw purple gradient sky background
        device.clear();
        for (int by = 0; by < height; by++) {
            float t = (float)by / height;
            int r = (int)(20 + t * 40);
            int g = (int)(0 + t * 15);
            int b = (int)(60 + t * 50);
            int color = 0xFF000000 | (r << 16) | (g << 8) | b;
            for (int bx = 0; bx < width; bx++) {
                device.backbuffer[by * width + bx] = color;
            }
        }
        
        // Build render mesh list
        renderMeshes.clear();
        
        // Ground plane
        renderMeshes.add(meshAt(groundMesh, new Vector3(0, GROUND_Y - 0.15f, 0)));
        
        // Buildings and launcher
        for (int i = 0; i < MAX_BUILD; i++) {
            if (builds[i].isbuild) {
                if (builds[i].alive) {
                    renderMeshes.add(meshAt(buildingMesh, builds[i].pos));
                } else {
                    renderMeshes.add(meshAt(destroyedMesh, builds[i].pos));
                }
            } else {
                renderMeshes.add(meshAt(launcherMesh, builds[i].pos));
            }
        }
        
        // Enemy missiles (only render when not exploding - explosions use particles)
        for (int i = 0; i < MAX_ENEMY; i++) {
            if (enemies[i].alive && !enemies[i].expl) {
                renderMeshes.add(meshAt(missileSphere, enemies[i].pos));
            }
        }
        
        // Our missiles (only render when not exploding - explosions use particles)
        for (int i = 0; i < MAX_OUR_MISSILE; i++) {
            if (ourMissiles[i].active && !ourMissiles[i].expl) {
                renderMeshes.add(meshAt(ourMissileSphere, ourMissiles[i].pos));
            }
        }
        
        // Trajectory lines as meshes
        // Enemy missile trajectories
        for (int i = 0; i < MAX_ENEMY; i++) {
            if (enemies[i].alive) {
                renderMeshes.add(createLineMesh(enemies[i].from, enemies[i].pos, 0.05f, 0x00ffff));
            }
        }
        
        // Our missile trajectories
        for (int i = 0; i < MAX_OUR_MISSILE; i++) {
            if (ourMissiles[i].active) {
                renderMeshes.add(createLineMesh(ourMissiles[i].launchPos, ourMissiles[i].pos, 0.05f, 0xffff00));
            }
        }
        
        // Render all meshes
        Mesh[] meshArray = renderMeshes.toArray(new Mesh[0]);
        device.render(camera, meshArray, lightPos);
        
        // Render smoke particles
        {
            int particleCount = 0;
            for (int i = 0; i < MAX_SMOKE_PARTICLES; i++) {
                if (smokeParticles[i].active) particleCount++;
            }
            
            if (particleCount > 0) {
                Vector3[] particlePositions = new Vector3[particleCount];
                int[] particleColors = new int[particleCount];
                int idx = 0;
                
                for (int i = 0; i < MAX_SMOKE_PARTICLES; i++) {
                    if (smokeParticles[i].active) {
                        particlePositions[idx] = smokeParticles[i].pos;
                        // Apply alpha based on life
                        int alpha = (int)(smokeParticles[i].life * SMOKE_MAX_ALPHA);
                        particleColors[idx] = (alpha << 24) | smokeParticles[i].color;
                        idx++;
                    }
                }
                
                // Render smoke with transparency
                device.renderParticles(camera, particlePositions, particleColors,
                    particleCount, 8.0f, smokeTexture, false);
            }
        }
        
        // Render explosion particles
        {
            int particleCount = 0;
            for (int i = 0; i < MAX_EXPLOSION_PARTICLES; i++) {
                if (explosionParticles[i].active) particleCount++;
            }
            
            if (particleCount > 0) {
                Vector3[] particlePositions = new Vector3[particleCount];
                int[] particleColors = new int[particleCount];
                int idx = 0;
                
                for (int i = 0; i < MAX_EXPLOSION_PARTICLES; i++) {
                    if (explosionParticles[i].active) {
                        particlePositions[idx] = explosionParticles[i].pos;
                        // Apply alpha based on life
                        int alpha = (int)(explosionParticles[i].life * 255);
                        particleColors[idx] = (alpha << 24) | explosionParticles[i].color;
                        idx++;
                    }
                }
                
                // Render explosions with additive blending
                device.renderParticles(camera, particlePositions, particleColors,
                    particleCount, 10.0f, explosionTexture, true);
            }
        }
        
        // HUD overlay
        String buf = String.format("Score:%04d", score);
        drawString(buf, 5, 5, 0xffffff);
        buf = String.format("Missiles:%03d", remainMissile);
        drawString(buf, 5, 25, 0xffffff);
        buf = String.format("Enemy:%03d/%03d", remainEnemy, remainGenEnemy);
        drawString(buf, width - 200, 5, 0xffffff);
        if (showHelp == 1) {
            drawString("[click]fire [wheel]zoom [h]help", 5, height - 25, 0xaaaaaa);
        }
        
        // Draw crosshair at mouse position
        drawLine(mx - 8, my, mx + 8, my, 0x00ff00);
        drawLine(mx, my - 8, mx, my + 8, 0x00ff00);
        
        // Present to screen
        device.presentToScreen(this);
        flush();
        sleep(16);
    }
    
    // Event handlers
    private void onmotion(int x, int y, int on) {
        mx = x;
        my = y;
    }
    
    SDLMMInterface.OnMouseMotionListener mousemotion = new SDLMMInterface.OnMouseMotionListener() {
        @Override
        public void onMove(int x, int y) {
            onmotion(x, y, 1);
        }
    };
    
    SDLMMInterface.OnMousePressListener onmouse = new SDLMMInterface.OnMousePressListener() {
        @Override
        public void onClick(int x, int y, int btn, boolean ison) {
            mx = x;
            my = y;
            if (ison) {
                launchMissile(mx, my);
            }
        }
    };
    
    SDLMMInterface.OnKeyboardListener kbfnc = new SDLMMInterface.OnKeyboardListener() {
        @Override
        public void onkey(int key, boolean shift, boolean ctrl, boolean alt, boolean ison) {
            if (!ison) return;
            switch (key) {
                case 'h':
                case 'H':
                    showHelp = 1 - showHelp;
                    break;
                case '+':
                case '=':
                    // Zoom in (mouse wheel up equivalent)
                    if (camDist > 8.0f) camDist -= 2.0f;
                    break;
                case '-':
                case '_':
                    // Zoom out (mouse wheel down equivalent)
                    if (camDist < 50.0f) camDist += 2.0f;
                    break;
            }
        }
    };
    
    @Override
    public void run() {
        // Initialize 3D device
        device = new Device(width, height);
        camera = new Camera();
        camera.Position = new Vector3(0, 10, -25);
        camera.Target = new Vector3(0, 2, 0);
        
        // Initialize scene
        initSceneMeshes();
        initBuilds();
        
        // Set event handlers
        setOnMouseMotion(mousemotion);
        setOnMousePress(onmouse);
        setOnKeyboard(kbfnc);
        setTextFont("Consolas-20");
        
        // Main game loop
        while (true) {
            drawScene();
        }
    }
    
    public static void main(String[] args) {
        MissileCmd3D demo = new MissileCmd3D("Missile Command 3D (Babylon3D)", width, height);
        demo.setVisible(true);
    }
}
