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
    
    // 3D components
    private boolean use3DRender = true;
    private Device device;
    private Camera camera;
    private Mesh[] enemyMissileMeshes;
    private Mesh[] launchedMissileMeshes;
    private Mesh[] buildingMeshes;
    private Vector3 lightPosition;
    private float cameraAngle = 0;
    
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
        camera.Position = new Vector3(400, 300, -800);
        camera.Target = new Vector3(400, 300, 0);
        lightPosition = new Vector3(400, 1000, -500);
        
        // Create meshes for enemy missiles
        enemyMissileMeshes = new Mesh[20];
        for (int i = 0; i < 20; i++) {
            enemyMissileMeshes[i] = Mesh.createSphere(5.0f, 8, 8);
            enemyMissileMeshes[i].name = "EnemyMissile_" + i;
        }
        
        // Create meshes for launched missiles
        launchedMissileMeshes = new Mesh[maxMissile];
        for (int i = 0; i < maxMissile; i++) {
            launchedMissileMeshes[i] = Mesh.createSphere(3.0f, 8, 8);
            launchedMissileMeshes[i].name = "LaunchedMissile_" + i;
        }
        
        // Create meshes for buildings
        buildingMeshes = new Mesh[MAX_BUILD];
        for (int i = 0; i < MAX_BUILD; i++) {
            buildingMeshes[i] = Mesh.createCube();
            buildingMeshes[i].name = "Building_" + i;
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
                        if (distX * distX + distY * distY + distZ * distZ < 
                            launchedMissile[j].r * launchedMissile[j].r) {
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
                    if (distX * distX + distY * distY + distZ * distZ < 
                        enermy[j].r * enermy[j].r) {
                        enermy[i].expl = true;
                        enermy[i].ishit = true;
                        score += 100;
                        return;
                    }
                }
                
                enermy[i].x += enermy[i].dx;
                enermy[i].y += enermy[i].dy;
                enermy[i].z += enermy[i].dz;
                
                if ((int)(enermy[i].tx - enermy[i].x) <= 0 && 
                    (int)(enermy[i].ty - enermy[i].y) >= 0) {
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
                
                int targetIdx = (int)(Math.random() * MAX_BUILD);
                int sx = (int)(Math.random() * width);
                int sy = 0;
                int tx = (build[targetIdx].left + build[targetIdx].right) / 2;
                int ty = build[targetIdx].top;
                int enermySpeed = (int)(Math.random() * MAX_ENERMY_SPEED);
                if (enermySpeed == 0) enermySpeed = 1;
                
                float dx = ((float)(tx - sx)) / (1024 / enermySpeed);
                float dy = ((float)(ty - sy)) / (1024 / enermySpeed);
                
                enermy[i].x = enermy[i].fx = sx;
                enermy[i].y = enermy[i].fy = sy;
                enermy[i].z = (float)(Math.random() * 200 - 100);
                enermy[i].dx = dx;
                enermy[i].dy = dy;
                enermy[i].dz = 0;
                enermy[i].tx = tx;
                enermy[i].ty = ty;
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
        int padding = 10;
        int buildtop = height - buildHeight;
        for (int i = 0; i < cnt; i++) {
            build[i].left = (padding + buildWidth) * i;
            build[i].top = buildtop;
            build[i].right = build[i].left + buildWidth;
            build[i].bottom = height;
            build[i].alive = true;
            build[i].isbuild = true;
        }
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
                if ((int)(launchedMissile[i].tx - launchedMissile[i].x) == 0 && 
                    (int)(launchedMissile[i].ty - launchedMissile[i].y) == 0) {
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
        String cscore = String.format("Score:%-04d", score);
        String cmissile = String.format(":%-04d", remainMissile);
        String cenermy = String.format("Enemy:%03d/%03d", remainEnermy, remainGenEnermy);
        drawString(cscore, 0, 0, 0xffffffff);
        drawString(cmissile, width - 80, 24, 0xffffffff);
        drawString(cenermy, width - 200, 0, 0xffffffff);
        drawString(use3DRender ? "3D [D]" : "2D [D]", 0, 20, 0xffffffff);
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
        // Update camera
        cameraAngle += 0.01f;
        camera.Position = new Vector3(
            (float)(400 + 400 * Math.sin(cameraAngle)),
            (float)(300 + 200 * Math.cos(cameraAngle * 2)),
            -800
        );
        camera.Target = new Vector3(400, 300, 0);
        
        // Update mesh positions
        for (int i = 0; i < 20; i++) {
            if (enermy[i].alive) {
                enemyMissileMeshes[i].Position = new Vector3(enermy[i].x, enermy[i].y, enermy[i].z);
            } else {
                enemyMissileMeshes[i].Position = new Vector3(-10000, -10000, -10000);
            }
        }
        
        for (int i = 0; i < maxMissile; i++) {
            if (launchedMissile[i].active) {
                launchedMissileMeshes[i].Position = new Vector3(
                    launchedMissile[i].x, launchedMissile[i].y, launchedMissile[i].z);
            } else {
                launchedMissileMeshes[i].Position = new Vector3(-10000, -10000, -10000);
            }
        }
        
        for (int i = 0; i < MAX_BUILD; i++) {
            buildingMeshes[i].Position = new Vector3(
                (build[i].left + build[i].right) / 2,
                (build[i].top + build[i].bottom) / 2,
                0
            );
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
                int sx = build[2].left + 32;
                int sy = build[2].top;
                float dx = ((float)(mx - sx)) / 50;
                float dy = ((float)(my - sy)) / 50;
                launchedMissile[i].active = true;
                launchedMissile[i].x = sx;
                launchedMissile[i].y = sy;
                launchedMissile[i].z = 0;
                launchedMissile[i].r = 3;
                launchedMissile[i].tx = mx;
                launchedMissile[i].ty = my;
                launchedMissile[i].tz = 0;
                launchedMissile[i].dx = dx;
                launchedMissile[i].dy = dy;
                launchedMissile[i].dz = 0;
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
