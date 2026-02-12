package sunneo.sdlmm.exams;

import sunneo.sdlmm.babylon3d.*;
import sunneo.sdlmm.implement.SDLMMFrame;
import sunneo.sdlmm.interfaces.SDLMMInterface;

/**
 * 3D Missile Command game using Babylon3D rendering
 * Based on ref-sdlmm missilecmd.c with 3D visualization
 */
public class MissileCmd3D extends SDLMMFrame {
    private static final long serialVersionUID = 1L;
    
    private static final int MAX_BUILD = 5;
    private static final int buildWidth = 128;
    private static final int buildHeight = 128;
    private static final int launcherWidth = 128;
    private static final int launcherHeight = 128;
    private static final int padding = 10;
    private static final int width = 800;
    private static final int height = 600;
    private static final int maxRadius = 32;
    private static final int maxMissile = 16;
    private static final int MAX_ENERMY_SPEED = 2;
    
    private int score = 0;
    private int remainMissile = 45;
    private int remainGenEnermy = 40;
    private int remainEnermy = 40;
    private int maxEnemyMissile = 15;
    private volatile int mx = 0;
    private volatile int my = 0;
    private boolean showHelp = true;  // Show help by default
    
    // 3D components
    private boolean use3DRender = true;
    private Device device;
    private Camera camera;
    private Mesh[] enemyMissileMeshes;
    private Mesh[] launchedMissileMeshes;
    private Mesh[] buildingMeshes;
    private Vector3 lightPosition;
    
    // Camera control - FIXED angles matching ref-sdlmm
    private float camDist = 25.0f;
    private float camAngleX = 0.4f;  // Fixed vertical angle
    private float camAngleY = 0.0f;  // Fixed horizontal angle (NO auto-rotation!)
    
    static class Missile {
        int fx, fy, tx, ty;
        float x, y, z, dx, dy, dz;
        boolean alive, expl, ishit;
        int r, targetBuild;
    }
    
    static class OurLaunchedMissile {
        int tx, ty, tz, r;
        boolean active, expl;
        float x, y, z, dx, dy, dz;
    }
    
    static class Build {
        int left, top, right, bottom;
        boolean alive, isbuild;
    }
    
    private Build[] build = new Build[MAX_BUILD];
    private Missile[] enermy = new Missile[20];
    private OurLaunchedMissile[] launchedMissile = new OurLaunchedMissile[maxMissile];
    
    public MissileCmd3D(String title, int width, int height) {
        super(title, width, height);
        
        for (int i = 0; i < MAX_BUILD; i++) {
            build[i] = new Build();
        }
        for (int i = 0; i < 20; i++) {
            enermy[i] = new Missile();
        }
        for (int i = 0; i < maxMissile; i++) {
            launchedMissile[i] = new OurLaunchedMissile();
        }
    }
    
    private void init3DRender() {
        device = new Device(width, height);
        camera = new Camera();
        
        // Camera setup matching ref-sdlmm
        // Initial camera position using spherical coordinates
        // camDist = 25.0f, camAngleX = 0.4f, camAngleY = 0.0f
        camera.Position = new Vector3(0, 10, -25);
        camera.Target = new Vector3(0, 2, 0);  // Look at world center, slightly above ground
        
        lightPosition = new Vector3(0, 20, -10);
        
        // Create meshes for enemy missiles
        enemyMissileMeshes = new Mesh[20];
        for (int i = 0; i < 20; i++) {
            enemyMissileMeshes[i] = Mesh.createSphere(0.2f, 8, 8);
            enemyMissileMeshes[i].name = "EnemyMissile_" + i;
        }
        
        // Create meshes for launched missiles
        launchedMissileMeshes = new Mesh[maxMissile];
        for (int i = 0; i < maxMissile; i++) {
            launchedMissileMeshes[i] = Mesh.createSphere(0.15f, 8, 8);
            launchedMissileMeshes[i].name = "LaunchedMissile_" + i;
        }
        
        // Create meshes for buildings (world scale, not screen scale)
        buildingMeshes = new Mesh[MAX_BUILD];
        for (int i = 0; i < MAX_BUILD; i++) {
            buildingMeshes[i] = Mesh.createCube();
            buildingMeshes[i].name = "Building_" + i;
            // Scale buildings to match world coordinates (1.5 x 2.0 x 1.5)
            buildingMeshes[i].Rotation = new Vector3(0, 0, 0);
        }
    }
    
    private float frand() {
        return (float)Math.random();
    }
    
    private void draw_enermy() {
        for (int i = 0; i < 20; i++) {
            if (!enermy[i].alive) continue;
            if (enermy[i].expl) {
                fillCircle((int)enermy[i].x, (int)enermy[i].y, enermy[i].r, 
                    ((int)(Math.random() * 0xffee00)) | 0xf0f000);
            } else {
                drawLine(enermy[i].fx, enermy[i].fy, (int)enermy[i].x, (int)enermy[i].y, 0xff0000ff);
                drawLine(enermy[i].fx - 1, enermy[i].fy, (int)enermy[i].x, (int)enermy[i].y, 0xff0000bb);
                drawLine(enermy[i].fx + 1, enermy[i].fy, (int)enermy[i].x, (int)enermy[i].y, 0xff0000aa);
                fillCircle((int)enermy[i].x, (int)enermy[i].y, enermy[i].r + 1, 0xffffff00);
                drawCircle((int)enermy[i].x, (int)enermy[i].y, enermy[i].r, 0xffff0000);
            }
        }
    }
    
    private void update_enermy() {
        float GROUND_Y = -3.0f;
        
        for (int i = 0; i < 20; i++) {
            if (!enermy[i].alive) continue;
            if (enermy[i].expl) {
                if (enermy[i].r >= maxRadius * 2) {
                    enermy[i].alive = false;
                    enermy[i].ishit = false;
                    enermy[i].expl = false;
                    if (remainEnermy - 1 >= 0)
                        remainEnermy--;
                }
                enermy[i].r += 2;
            } else {
                // Check collision with our missiles
                for (int j = 0; j < maxMissile; j++) {
                    if (!launchedMissile[j].active) continue;
                    if (launchedMissile[j].expl) {
                        float distX = enermy[i].x - launchedMissile[j].x;
                        float distY = enermy[i].y - launchedMissile[j].y;
                        float distZ = enermy[i].z - launchedMissile[j].z;
                        float dist = distX * distX + distY * distY + distZ * distZ;
                        float explRadius = launchedMissile[j].r * 0.3f;  // Scale to world coordinates
                        if (dist < explRadius * explRadius) {
                            enermy[i].expl = true;
                            enermy[i].ishit = true;
                            score += 100;
                            return;
                        }
                    }
                }
                
                // Check chain reactions
                for (int j = 0; j < 20; j++) {
                    if (j == i) continue;
                    if (!enermy[j].alive) continue;
                    if (!enermy[j].expl) continue;
                    if (!enermy[j].ishit) continue;
                    float distX = enermy[i].x - enermy[j].x;
                    float distY = enermy[i].y - enermy[j].y;
                    float distZ = enermy[i].z - enermy[j].z;
                    float dist = distX * distX + distY * distY + distZ * distZ;
                    float explRadius = enermy[j].r * 0.3f;  // Scale to world coordinates
                    if (dist < explRadius * explRadius) {
                        enermy[i].expl = true;
                        enermy[i].ishit = true;
                        score += 100;
                        return;
                    }
                }
                
                // Update position in world coordinates
                enermy[i].x += enermy[i].dx;
                enermy[i].y += enermy[i].dy;
                enermy[i].z += enermy[i].dz;
                
                // Check if reached target (ground level)
                if (enermy[i].y <= GROUND_Y + 1.0f) {
                    enermy[i].expl = true;
                    build[enermy[i].targetBuild].alive = false;
                }
            }
        }
    }
    
    private void generate_enermy() {
        if (remainGenEnermy > 0) {
            int currentAlive = 0;
            for (int i = 0; i < 20; i++) {
                if (currentAlive >= maxEnemyMissile) break;
                if (enermy[i].alive) {
                    currentAlive++;
                    continue;
                }
                
                // World coordinates matching ref-sdlmm
                float WORLD_WIDTH = 20.0f;
                float WORLD_DEPTH = 5.0f;
                float GROUND_Y = -3.0f;
                
                int targetIdx = (int)(Math.random() * MAX_BUILD);
                
                // Enemy starts from the sky (Y=15.0) in world coordinates
                float sx = (float)((Math.random() - 0.5) * WORLD_WIDTH);
                float sy = 15.0f;  // from the sky
                float sz = (float)((Math.random() - 0.5) * WORLD_DEPTH);
                
                // Target is the building position in world coordinates
                float spacing = WORLD_WIDTH / MAX_BUILD;
                float startX = -WORLD_WIDTH / 2 + spacing / 2;
                float tx = startX + targetIdx * spacing;
                float ty = GROUND_Y + 1.0f;  // building base at ground level
                float tz = 0;
                
                float speed = 0.03f + (float)Math.random() * 0.04f;
                float dx = tx - sx;
                float dy = ty - sy;
                float dz = tz - sz;
                float len = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (len < 0.001f) len = 1.0f;
                
                // Store both screen coordinates (for 2D) and world coordinates (for 3D)
                // For 2D rendering, convert world to screen
                enermy[i].fx = (int)((sx + WORLD_WIDTH/2) / WORLD_WIDTH * width);
                enermy[i].fy = 0;  // top of screen
                enermy[i].tx = (build[targetIdx].left + build[targetIdx].right) / 2;
                enermy[i].ty = build[targetIdx].top;
                
                // World coordinates for 3D
                enermy[i].x = sx;
                enermy[i].y = sy;
                enermy[i].z = sz;
                enermy[i].dx = dx / len * speed;
                enermy[i].dy = dy / len * speed;
                enermy[i].dz = dz / len * speed;
                
                enermy[i].expl = false;
                enermy[i].alive = true;
                enermy[i].r = 2;
                enermy[i].targetBuild = targetIdx;
                currentAlive++;
                remainGenEnermy--;
                if (remainGenEnermy == 0) return;
            }
        }
    }
    
    private void init_build(int cnt) {
        // World coordinates: X from -10 to 10, ground at Y = -3.0
        float WORLD_WIDTH = 20.0f;
        float GROUND_Y = -3.0f;
        
        float spacing = WORLD_WIDTH / cnt;
        float startX = -WORLD_WIDTH / 2 + spacing / 2;
        
        for (int i = 0; i < cnt; i++) {
            // Convert to screen coordinates for 2D rendering
            build[i].left = (int)((padding + buildWidth) * i);
            build[i].top = height - buildHeight;
            build[i].right = build[i].left + buildWidth;
            build[i].bottom = height;
            build[i].alive = true;
            build[i].isbuild = true;
        }
        
        // Middle one is the launcher
        build[cnt / 2].isbuild = false;
        build[cnt / 2].top = height - launcherHeight;
    }
    
    private void draw_build(int cnt) {
        for (int i = 0; i < cnt; i++) {
            int color = build[i].alive ? 0xff00ff00 : 0xffff0000;
            if (build[i].isbuild) {
                fillRect(build[i].left, build[i].top, buildWidth, buildHeight, color);
                drawRect(build[i].left, build[i].top, buildWidth, buildHeight, 0xffffffff);
            } else {
                fillRect(build[i].left, build[i].top, launcherWidth, launcherHeight, 0xff0000ff);
                drawRect(build[i].left, build[i].top, launcherWidth, launcherHeight, 0xffffffff);
            }
        }
    }
    
    private void update_missile() {
        for (int i = 0; i < maxMissile; i++) {
            if (!launchedMissile[i].active) continue;
            if (!launchedMissile[i].expl) {
                // Check if reached target in world coordinates
                float dx = launchedMissile[i].tx - launchedMissile[i].x;
                float dy = launchedMissile[i].ty - launchedMissile[i].y;
                float dz = launchedMissile[i].tz - launchedMissile[i].z;
                float dist = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
                
                if (dist < 0.5f) {  // Close enough to target in world units
                    launchedMissile[i].expl = true;
                }
                launchedMissile[i].x += launchedMissile[i].dx;
                launchedMissile[i].y += launchedMissile[i].dy;
                launchedMissile[i].z += launchedMissile[i].dz;
            } else {
                if (launchedMissile[i].r < maxRadius) {
                    launchedMissile[i].r++;
                } else {
                    launchedMissile[i].active = false;
                    launchedMissile[i].expl = false;
                }
            }
        }
    }
    
    private void drawMessage() {
        String cscore = String.format("Score:%04d", score);
        String cmissile = String.format(":%04d", remainMissile);
        String cenermy = String.format("Enemy:%03d/%03d", remainEnermy, remainGenEnermy);
        drawString(cscore, 0, 0, 0xffffffff);
        drawString(cmissile, width - 80, 24, 0xffffffff);
        drawString(cenermy, width - 200, 0, 0xffffffff);
        
        // Show help text matching ref-sdlmm
        if (showHelp) {
            drawString("[click]fire [+/-]zoom [h]help [d]2D/3D", 5, height - 25, 0xaaaaaa);
        }
        
        // Show crosshair at mouse position
        if (mx > 0 && my > 0) {
            int crosshairSize = 10;
            drawLine(mx - crosshairSize, my, mx + crosshairSize, my, 0xffffffff);
            drawLine(mx, my - crosshairSize, mx, my + crosshairSize, 0xffffffff);
        }
    }
    
    private void draw_missile() {
        for (int i = 0; i < maxMissile; i++) {
            if (!launchedMissile[i].active) continue;
            if (!launchedMissile[i].expl) {
                float dx = launchedMissile[i].dx;
                float dy = launchedMissile[i].dy;
                fillCircle((int)launchedMissile[i].x, (int)launchedMissile[i].y, 
                    launchedMissile[i].r, 0xffffff00);
                for (int j = 0; j < 8; j++) {
                    fillCircle((int)(launchedMissile[i].x - j * dx), 
                        (int)(launchedMissile[i].y - j * dy), 
                        launchedMissile[i].r + j, 
                        0xffffff - 0x101010 * (j + 1));
                }
            } else {
                fillCircle((int)launchedMissile[i].x, (int)launchedMissile[i].y, 
                    launchedMissile[i].r, (int)(Math.random() * 0xFFFFFF) << 9);
            }
        }
    }
    
    private void render3D() {
        // Camera stays FIXED at these angles (matching ref-sdlmm)
        // Only camDist changes with mouse wheel zoom
        camera.Position = new Vector3(
            (float)(camDist * Math.sin(camAngleY) * Math.cos(camAngleX)),
            (float)(camDist * Math.sin(camAngleX)),
            (float)(-camDist * Math.cos(camAngleY) * Math.cos(camAngleX))
        );
        camera.Target = new Vector3(0, 2, 0);  // Look at center, slightly above ground
        
        // World coordinates
        float WORLD_WIDTH = 20.0f;
        float GROUND_Y = -3.0f;
        
        // Update enemy missile positions (using world coordinates)
        for (int i = 0; i < 20; i++) {
            if (enermy[i].alive) {
                enemyMissileMeshes[i].Position = new Vector3(enermy[i].x, enermy[i].y, enermy[i].z);
            } else {
                enemyMissileMeshes[i].Position = new Vector3(-10000, -10000, -10000);
            }
        }
        
        // Update our missile positions (using world coordinates)
        for (int i = 0; i < maxMissile; i++) {
            if (launchedMissile[i].active) {
                launchedMissileMeshes[i].Position = new Vector3(
                    launchedMissile[i].x, launchedMissile[i].y, launchedMissile[i].z);
            } else {
                launchedMissileMeshes[i].Position = new Vector3(-10000, -10000, -10000);
            }
        }
        
        // Update building positions (world coordinates)
        float spacing = WORLD_WIDTH / MAX_BUILD;
        float startX = -WORLD_WIDTH / 2 + spacing / 2;
        for (int i = 0; i < MAX_BUILD; i++) {
            float buildX = startX + i * spacing;
            float buildY = GROUND_Y + 1.0f;  // buildings sit on ground
            if (!build[i].isbuild) {
                buildY = GROUND_Y + 0.6f;  // launcher is shorter
            }
            buildingMeshes[i].Position = new Vector3(buildX, buildY, 0);
            
            // Scale buildings appropriately
            if (build[i].isbuild) {
                // Regular building: 1.5 x 2.0 x 1.5
                buildingMeshes[i].Rotation = new Vector3(0, 0, 0);
            } else {
                // Launcher: 1.0 x 1.2 x 1.0 (smaller)
                buildingMeshes[i].Rotation = new Vector3(0, 0, 0);
            }
        }
        
        // Combine all meshes for rendering
        Mesh[] allMeshes = new Mesh[20 + maxMissile + MAX_BUILD];
        System.arraycopy(enemyMissileMeshes, 0, allMeshes, 0, 20);
        System.arraycopy(launchedMissileMeshes, 0, allMeshes, 20, maxMissile);
        System.arraycopy(buildingMeshes, 0, allMeshes, 20 + maxMissile, MAX_BUILD);
        
        device.clear();
        device.render(camera, allMeshes, lightPosition);
        
        // Present rendered backbuffer to screen using Babylon3D API
        device.presentToScreen(this);
    }
    
    private void reinit() {
        if (remainEnermy <= 0 && remainGenEnermy <= 0) {
            init_build(MAX_BUILD);
            remainEnermy = 40;
            remainGenEnermy = 40;
            remainMissile = 45;
            
            // Clear all enemy and missile arrays (matching ref-sdlmm memset)
            for (int i = 0; i < 20; i++) {
                enermy[i].alive = false;
                enermy[i].expl = false;
                enermy[i].ishit = false;
                enermy[i].x = 0;
                enermy[i].y = 0;
                enermy[i].z = 0;
            }
            for (int i = 0; i < maxMissile; i++) {
                launchedMissile[i].active = false;
                launchedMissile[i].expl = false;
                launchedMissile[i].x = 0;
                launchedMissile[i].y = 0;
                launchedMissile[i].z = 0;
            }
        }
    }
    
    private void drawfnc() {
        reinit();
        
        if (use3DRender) {
            render3D();
        } else {
            fillRect(0, 0, width, height, 0xff2200dd);
            draw_build(MAX_BUILD);
            draw_missile();
            draw_enermy();
        }
        
        update_missile();
        update_enermy();
        drawMessage();
        
        if (Math.random() * 100 < 20) {
            generate_enermy();
        }
        
        flush();
        sleep(16);
    }
    
    private void onmotion(int x, int y, int on) {
        mx = x;
        my = y;
    }
    
    private void generate_missile(int mx, int my) {
        if (remainMissile <= 0) return;
        for (int i = 0; i < maxMissile; i++) {
            if (!launchedMissile[i].active) {
                // World coordinates
                float WORLD_WIDTH = 20.0f;
                float GROUND_Y = -3.0f;
                
                // Launcher position (middle building in world coords)
                float spacing = WORLD_WIDTH / MAX_BUILD;
                float startX = -WORLD_WIDTH / 2 + spacing / 2;
                float launcherX = startX + (MAX_BUILD / 2) * spacing;
                float launcherY = GROUND_Y + 0.6f;
                float launcherZ = 0;
                
                // Convert mouse screen coordinates to world coordinates
                // Simple mapping: screen X [0, width] -> world X [-10, 10]
                //                 screen Y [0, height] -> world Y [10, -3]
                float tx = (mx / (float)width) * WORLD_WIDTH - WORLD_WIDTH / 2;
                float ty = 10.0f - (my / (float)height) * 13.0f;  // Map screen Y to world Y (10 to -3)
                float tz = 0;  // Target on z=0 plane
                
                // Calculate velocity
                float dx = tx - launcherX;
                float dy = ty - launcherY;
                float dz = tz - launcherZ;
                float len = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (len < 0.001f) len = 1.0f;
                float speed = 0.5f;
                
                launchedMissile[i].active = true;
                launchedMissile[i].x = launcherX;
                launchedMissile[i].y = launcherY;
                launchedMissile[i].z = launcherZ;
                launchedMissile[i].r = 3;
                launchedMissile[i].tx = (int)tx;  // Store for compatibility
                launchedMissile[i].ty = (int)ty;
                launchedMissile[i].tz = (int)tz;
                launchedMissile[i].dx = dx / len * speed;
                launchedMissile[i].dy = dy / len * speed;
                launchedMissile[i].dz = dz / len * speed;
                launchedMissile[i].expl = false;
                remainMissile--;
                break;
            }
        }
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
                generate_missile(mx, my);
            }
        }
    };
    
    SDLMMInterface.OnKeyboardListener kbfnc = new SDLMMInterface.OnKeyboardListener() {
        @Override
        public void onkey(int key, boolean shift, boolean ctrl, boolean alt, boolean ison) {
            if (!ison) return;
            switch (key) {
                case 'd':
                case 'D':
                    use3DRender = !use3DRender;
                    break;
                case 'h':
                case 'H':
                    showHelp = !showHelp;
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
        init_build(MAX_BUILD);
        init3DRender();
        setOnMouseMotion(mousemotion);
        setOnMousePress(onmouse);
        setOnKeyboard(kbfnc);
        setTextFont("Consolas-20");
        
        while (true) {
            drawfnc();
        }
    }
    
    public static void main(String[] args) {
        MissileCmd3D demo = new MissileCmd3D("Missile Command 3D [demo]", width, height);
        demo.setVisible(true);
    }
}
