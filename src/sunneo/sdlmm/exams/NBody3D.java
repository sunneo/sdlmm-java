package sunneo.sdlmm.exams;

import sunneo.sdlmm.babylon3d.*;
import sunneo.sdlmm.implement.SDLMMFrame;
import sunneo.sdlmm.interfaces.SDLMMInterface;

/**
 * nbody3d.java - 3D N-Body Gravitational Simulation using Babylon3D
 *
 * Renders gravitational bodies as 3D particles with additive blending
 * Based on NVIDIA CUDA nbody sample with softening parameter
 * Translated from ref-sdlmm/exams/nbody3d.c
 *
 * Physics Algorithm:
 *   Uses NVIDIA nbody sample algorithm with softening parameter to prevent
 *   numerical instabilities when particles get very close:
 *   F = G * m_i * m_j * r / (r^2 + epsilon^2)^(3/2)
 *
 * Controls:
 *   C   : Toggle camera tracking (smooth transition, no shake)
 *   r/R : Toggle random simulation factor
 *   h/H : Toggle help overlay
 *   +/- : Zoom camera in/out
 *   Arrow keys: Rotate camera
 *   Mouse drag: Rotate camera (like NVIDIA sample)
 *   Mouse sliders: Adjust simulation speed and particle size
 */
public class NBody3D extends SDLMMFrame {
    private static final long serialVersionUID = 1L;
    
    // Screen dimensions
    static final int SCREENX = 1400;
    static final int SCREENY = 800;
    static final int NUM_BODY = 1024;  // Reduced for performance
    static final int LOOP = 2000;
    static final int MIN_X_axis = 0;
    static final int MIN_Y_axis = 0;
    static final int MIN_Z_axis = 0;
    static final int MIN_velocity = 1;
    static final int MAX_Mass = 300;
    static final int MIN_Mass = 200;
    
    // Parameters that are randomized each loop cycle
    private float MAX_X_axis = 300.0f;
    private float MAX_Y_axis = 300.0f;
    private float MAX_Z_axis = 100.0f;
    private float MAX_Velocity = 10.0f;
    private float Gravity_Coef = 30.3f;
    private float SOFTENING = 100.001f;  // Softening parameter (epsilon) - NVIDIA nbody sample
    private float SOFTENING_SQUARED;
    
    // UI slider constants
    private static final int SLIDER_X_START = 320;
    private static final int SLIDER_WIDTH = 400;
    private static final int SLIDER_SIM_Y = 25;
    private static final int SLIDER_SIM_HEIGHT = 15;
    private static final int SLIDER_PARTICLE_Y = 65;
    private static final int SLIDER_PARTICLE_HEIGHT = 15;
    
    // Particle size range
    private static final float PARTICLE_SIZE_MIN = 5.0f;
    private static final float PARTICLE_SIZE_MAX = 200.0f;
    private static final float PARTICLE_SIZE_DEFAULT = 15.0f;
    
    // Mouse control constants
    private static final float MOUSE_ROTATION_SENSITIVITY = 0.005f;
    private static final float CAM_ANGLE_X_MAX = 1.5f;
    private static final float CAM_ANGLE_X_MIN = -1.5f;
    
    // Global state
    private boolean showhelp = true;
    private float simulatetime_factor = 0.02f;
    private boolean random_simulatefactor = false;
    private int SZ = NUM_BODY;
    
    // Camera
    private float camDist = 50.0f;
    private float camAngleX = 0.3f;
    private float camAngleY = 0.0f;
    
    // Camera tracking
    private boolean camera_tracking = true;
    private float target_cam_angle_x = 0.3f;
    private float target_cam_angle_y = 0.0f;
    private float target_cam_dist = 50.0f;
    private static final float cam_transition_speed = 0.1f;
    
    // Snapshot of cluster center when 'C' is pressed
    private float snapshot_center_x = 0.0f;
    private float snapshot_center_y = 0.0f;
    private float snapshot_center_z = 0.0f;
    
    // Current cluster center (updated each frame)
    private float current_center_x = 0.0f;
    private float current_center_y = 0.0f;
    private float current_center_z = 0.0f;
    
    // Mouse control
    private boolean mouse_down = false;
    private int last_mouse_x = 0;
    private int last_mouse_y = 0;
    
    // Particle size control
    private float particle_size = PARTICLE_SIZE_DEFAULT;
    
    // Physics arrays
    private float[] X_axis, Y_axis, Z_axis;
    private float[] X_Velocity, Y_Velocity, Z_Velocity;
    private float[] newX_velocity, newY_velocity, newZ_velocity;
    private float[] Mass;
    private float[] gravitationalPotential;  // For cluster tracking
    
    // 3D rendering
    private Device m_device;
    private Camera camera;
    private Texture particleTexture;
    private Vector3[] particlePositions;
    private int[] particleColors;
    
    // Glow colors for particles - cyan/turquoise/white like NVIDIA nbody demo
    private static final int[] glowColors = {
        0xFFFF00,  // cyan
        0xFFFF40,  // light cyan
        0xFFFF80,  // lighter cyan
        0xFFFFC0,  // very light cyan
        0xFFFFFF,  // white
        0xE0E000,  // darker cyan
        0xC0C000,  // dark cyan
        0xFFFF60,  // cyan variant
        0xFFFFA0,  // cyan variant 2
        0xFFFFE0   // almost white
    };
    
    public NBody3D(String title, int width, int height, int sz) {
        super(title, width, height);
        this.SZ = sz;
    }
    
    private double getDoubleTime() {
        return (double) System.currentTimeMillis();
    }
    
    private float clampf(float v, float minv, float maxv) {
        if (v > maxv) v = (v + maxv) / 2;
        if (v < minv) v = (v + minv) / 2;
        return v;
    }
    
    /**
     * Randomize simulation parameters for each loop cycle
     */
    private void Randomize_Parameters() {
        float max_screen = Math.max(SCREENX, SCREENY);
        
        MAX_X_axis = 10.0f + (float)Math.random() * (SCREENX - 10.0f);
        MAX_Y_axis = 10.0f + (float)Math.random() * (SCREENY - 10.0f);
        MAX_Z_axis = 10.0f + (float)Math.random() * (max_screen - 10.0f);
        MAX_Velocity = 10.0f + (float)Math.random() * (SCREENY/2.0f - 10.0f);
        Gravity_Coef = 10.0f + (float)Math.random() * 50.0f;  // Range: 10-60
        SOFTENING = 50.0f + (float)Math.random() * 150.0f;    // Range: 50-200
        SOFTENING_SQUARED = SOFTENING * SOFTENING;
    }
    
    private float[] allocateBody() {
        return new float[SZ];
    }
    
    /**
     * Initialize body positions and velocities
     */
    private void Init_AllBody() {
        for (int i = 0; i < SZ; i++) {
            X_axis[i] = MIN_X_axis + (float)Math.random() * (MAX_X_axis - MIN_X_axis);
            Y_axis[i] = MIN_Y_axis + (float)Math.random() * (MAX_Y_axis - MIN_Y_axis);
            Z_axis[i] = MIN_Z_axis + (float)Math.random() * (MAX_Z_axis - MIN_Z_axis);
            X_Velocity[i] = newX_velocity[i] = 0;
            Y_Velocity[i] = newY_velocity[i] = 0;
            Z_Velocity[i] = newZ_velocity[i] = 0;
            Mass[i] = (int)(Math.random() * (MAX_Mass - MIN_Mass)) + MIN_Mass;
        }
    }
    
    /**
     * N-body gravitational calculation for body i
     * Uses softening parameter to match NVIDIA CUDA nbody sample algorithm
     * Force = G * m_i * m_j * r / (r^2 + epsilon^2)^(3/2)
     * 
     * Also accumulates gravitational potential for cluster tracking optimization.
     */
    private void Nbody(int i, int sz) {
        float sumX = 0, sumY = 0, sumZ = 0;
        float potential = 0.0f;  // Accumulate gravitational potential for this particle
        
        for (int j = 0; j < sz; j++) {
            if (j == i) continue;
            
            // Calculate position difference vector
            float X_position = X_axis[j] - X_axis[i];
            float Y_position = Y_axis[j] - Y_axis[i];
            float Z_position = Z_axis[j] - Z_axis[i];
            
            // Distance squared with softening (prevents singularities)
            float distSqr = X_position * X_position + Y_position * Y_position + Z_position * Z_position + SOFTENING_SQUARED;
            
            // Inverse distance and inverse distance cubed
            float invDist = 1.0f / (float)Math.sqrt(distSqr);
            float invDistCube = invDist * invDist * invDist;
            
            // Force factor: G * m_j * invDistCube
            float s = Gravity_Coef * Mass[j] * invDistCube;
            
            // Accumulate force components
            sumX += s * X_position;
            sumY += s * Y_position;
            sumZ += s * Z_position;
            
            // Accumulate gravitational potential: U = sum(m_j / r)
            potential += Mass[j] * invDist;
        }
        
        // Store gravitational potential for cluster tracking
        gravitationalPotential[i] = potential;
        
        // Update velocities
        newX_velocity[i] += sumX * simulatetime_factor;
        newY_velocity[i] += sumY * simulatetime_factor;
        newZ_velocity[i] += sumZ * simulatetime_factor;
        
        // Update positions with clamped velocities
        X_axis[i] += clampf(newX_velocity[i], MIN_velocity, MAX_Velocity) * simulatetime_factor;
        Y_axis[i] += clampf(newY_velocity[i], MIN_velocity, MAX_Velocity) * simulatetime_factor;
        Z_axis[i] += clampf(newZ_velocity[i], MIN_velocity, MAX_Velocity) * simulatetime_factor;
        
        // Store final velocities
        X_Velocity[i] = newX_velocity[i];
        Y_Velocity[i] = newY_velocity[i];
        Z_Velocity[i] = newZ_velocity[i];
    }
    
    /**
     * Initialize particle rendering structures
     */
    private void initParticles() {
        particlePositions = new Vector3[SZ];
        for (int i = 0; i < SZ; i++) {
            particlePositions[i] = new Vector3();
        }
        particleColors = new int[SZ];
        
        // Create Gaussian texture for particle sprites
        particleTexture = Texture.createGaussian(64);
        
        // Assign colors to particles based on their index
        for (int i = 0; i < SZ; i++) {
            particleColors[i] = glowColors[i % glowColors.length];
        }
    }
    
    /**
     * Update particle positions from physics simulation.
     * Maps simulation coordinates to 3D world space.
     * When camera tracking is enabled, centers particles around the snapshot center.
     */
    private void updateParticlePositions(float avgX, float avgY, float avgZ) {
        float scale = 0.1f;  // Scale factor for world coordinates
        
        // When camera tracking is enabled, centralize particles around snapshot center
        boolean should_centralize = camera_tracking;
        
        for (int i = 0; i < SZ; i++) {
            if (should_centralize) {
                particlePositions[i].x = (X_axis[i] - snapshot_center_x) * scale;
                particlePositions[i].y = (Y_axis[i] - snapshot_center_y) * scale;
                particlePositions[i].z = (Z_axis[i] - snapshot_center_z) * scale;
            } else {
                particlePositions[i].x = (X_axis[i] - MAX_X_axis/2) * scale;
                particlePositions[i].y = (Y_axis[i] - MAX_Y_axis/2) * scale;
                particlePositions[i].z = (Z_axis[i] - MAX_Z_axis/2) * scale;
            }
        }
    }
    
    /**
     * Update camera position based on angles and distance.
     * Smoothly transitions camera angles and distance when tracking is enabled.
     */
    private void updateCamera() {
        // Smooth camera transition
        if (camera_tracking) {
            // Gradually move towards target angles
            camAngleX += (target_cam_angle_x - camAngleX) * cam_transition_speed;
            camAngleY += (target_cam_angle_y - camAngleY) * cam_transition_speed;
            camDist += (target_cam_dist - camDist) * cam_transition_speed;
        } else {
            // Update targets to match current position when not tracking
            target_cam_angle_x = camAngleX;
            target_cam_angle_y = camAngleY;
            target_cam_dist = camDist;
        }
        
        camera.Position = new Vector3(
            (float)(camDist * Math.sin(camAngleY) * Math.cos(camAngleX)),
            (float)(camDist * Math.sin(camAngleX)),
            (float)(-camDist * Math.cos(camAngleY) * Math.cos(camAngleX))
        );
        camera.Target = new Vector3(0, 0, 0);  // Look at center
    }
    
    /**
     * Draw the 3D scene: clear, render particles, draw HUD text overlay
     */
    private void draw3D(int loop, int totalLoop, double tm, float avgX, float avgY, float avgZ) {
        String buf;
        double rendert1, rendert2;
        
        rendert1 = getDoubleTime();
        
        // Update particle positions from simulation
        updateParticlePositions(avgX, avgY, avgZ);
        
        // Update camera
        updateCamera();
        
        // Clear device to black
        m_device.clear();
        
        // Render all particles with additive blending
        m_device.renderParticles(camera, particlePositions, particleColors, 
                               SZ, particle_size, particleTexture, true);  // true = additive blending
        
        // Copy device backbuffer to screen
        int[] backbuffer = m_device.backbuffer;
        for (int y = 0; y < SCREENY; y++) {
            for (int x = 0; x < SCREENX; x++) {
                int idx = y * SCREENX + x;
                if (idx < backbuffer.length) {
                    drawPixel(x, y, backbuffer[idx]);
                }
            }
        }
        
        // Draw HUD overlay
        if (showhelp) {
            buf = String.format("[%-3d/%-3d] tm:%-3.3f bodies:%d", loop, totalLoop, tm, SZ);
            drawString(buf, 5, 5, 0xffffffff);
            buf = String.format("simulate factor: %-3.5f", simulatetime_factor);
            drawString(buf, 5, 25, 0xffffffff);
            buf = String.format("random factor: %s[r]", random_simulatefactor ? "on" : "off");
            drawString(buf, 5, 45, 0xffffffff);
            // Draw simulation factor slider
            fillRect(SLIDER_X_START, SLIDER_SIM_Y, (int)(SLIDER_WIDTH * (simulatetime_factor / 2.0f)), SLIDER_SIM_HEIGHT, 0xfffdfd00);
            drawRect(SLIDER_X_START, SLIDER_SIM_Y, SLIDER_WIDTH, SLIDER_SIM_HEIGHT, 0xffffffff);
            
            buf = String.format("particle size: %-3.1f", particle_size);
            drawString(buf, 5, 65, 0xffffffff);
            // Draw particle size slider
            fillRect(SLIDER_X_START, SLIDER_PARTICLE_Y, (int)(SLIDER_WIDTH * ((particle_size - PARTICLE_SIZE_MIN) / (PARTICLE_SIZE_MAX - PARTICLE_SIZE_MIN))), SLIDER_PARTICLE_HEIGHT, 0xff00ff00);
            drawRect(SLIDER_X_START, SLIDER_PARTICLE_Y, SLIDER_WIDTH, SLIDER_PARTICLE_HEIGHT, 0xffffffff);
            
            buf = String.format("cam dist:%.1f angle:(%.2f,%.2f)", camDist, camAngleX, camAngleY);
            drawString(buf, 5, 85, 0xffffffff);
            buf = String.format("camera tracking: %s[C]", camera_tracking ? "on" : "off");
            drawString(buf, 5, 105, 0xffffffff);
            
            // Display randomized parameters
            buf = String.format("MAX_X:%.1f MAX_Y:%.1f MAX_Z:%.1f", MAX_X_axis, MAX_Y_axis, MAX_Z_axis);
            drawString(buf, 5, 165, 0xffaaaaaa);
            buf = String.format("MAX_Vel:%.1f Grav:%.1f Soft:%.1f", MAX_Velocity, Gravity_Coef, SOFTENING);
            drawString(buf, 5, 185, 0xffaaaaaa);
            drawString("[h]help [+/-]zoom [arrows/mouse]rotate [C]track", 5, 205, 0xffaaaaaa);
        }
        
        flush();
        
        rendert2 = getDoubleTime();
        tm += (rendert2 - rendert1);
        if (tm < 1.0 / 60) {
            sleep((int)((1.0 / 60) * 1000 - tm * 1000));
        }
    }
    
    /**
     * Find the center of the cluster with the greatest gravitational pull.
     * Uses pre-calculated gravitational potentials from Nbody calculations.
     */
    private void findClusterCenter(float[] centerX, float[] centerY, float[] centerZ) {
        float maxPotential = -1e30f;
        int maxPotentialIdx = 0;
        
        // Find particle with highest gravitational potential
        for (int i = 0; i < SZ; i++) {
            if (gravitationalPotential[i] > maxPotential) {
                maxPotential = gravitationalPotential[i];
                maxPotentialIdx = i;
            }
        }
        
        // Calculate weighted center around the densest region
        float sumX = 0, sumY = 0, sumZ = 0;
        float totalWeight = 0;
        float clusterRadius = 100.0f;  // Radius to consider particles as part of cluster
        
        for (int i = 0; i < SZ; i++) {
            float dx = X_axis[i] - X_axis[maxPotentialIdx];
            float dy = Y_axis[i] - Y_axis[maxPotentialIdx];
            float dz = Z_axis[i] - Z_axis[maxPotentialIdx];
            float dist = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
            
            if (dist < clusterRadius) {
                // Weight by mass and inverse distance to favor closer, heavier particles
                float weight = Mass[i] / (dist + 1.0f);
                sumX += X_axis[i] * weight;
                sumY += Y_axis[i] * weight;
                sumZ += Z_axis[i] * weight;
                totalWeight += weight;
            }
        }
        
        if (totalWeight > 0) {
            centerX[0] = sumX / totalWeight;
            centerY[0] = sumY / totalWeight;
            centerZ[0] = sumZ / totalWeight;
        } else {
            // Fallback to the max potential particle position
            centerX[0] = X_axis[maxPotentialIdx];
            centerY[0] = Y_axis[maxPotentialIdx];
            centerZ[0] = Z_axis[maxPotentialIdx];
        }
    }
    
    /**
     * Main simulation loop
     */
    private void main_run() {
        double tmstart, tmend;
        double fps_time_1, fps_time_2;
        float[] avgX = new float[1];
        float[] avgY = new float[1];
        float[] avgZ = new float[1];
        
        // Randomize simulation parameters for this loop cycle
        Randomize_Parameters();
        
        tmstart = getDoubleTime();
        Init_AllBody();
        
        for (int loop = 0; loop < LOOP; loop++) {
            avgX[0] = 0; avgY[0] = 0; avgZ[0] = 0;
            fps_time_1 = getDoubleTime();
            
            for (int i = 0; i < SZ; i++) {
                Nbody(i, SZ);
            }
            
            // Find the cluster center with greatest gravitational pull
            findClusterCenter(avgX, avgY, avgZ);
            
            // Store current cluster center globally for camera tracking snapshot
            current_center_x = avgX[0];
            current_center_y = avgY[0];
            current_center_z = avgZ[0];
            
            fps_time_2 = getDoubleTime();
            draw3D(loop, LOOP, fps_time_2 - fps_time_1, avgX[0], avgY[0], avgZ[0]);
        }
        
        tmend = getDoubleTime();
        System.out.printf("%d %f\n", SZ, tmend - tmstart);
        if (random_simulatefactor) {
            simulatetime_factor = (float)Math.random();
        }
    }
    
    SDLMMInterface.OnKeyboardListener kbfnc = new SDLMMInterface.OnKeyboardListener() {
        @Override
        public void onkey(int key, boolean shift, boolean ctrl, boolean alt, boolean ison) {
            if (!ison) return;
            
            switch (key) {
                case 'c': case 'C':
                    // Toggle camera tracking mode
                    camera_tracking = !camera_tracking;
                    if (camera_tracking) {
                        // Snapshot current cluster center to prevent camera shake
                        snapshot_center_x = current_center_x;
                        snapshot_center_y = current_center_y;
                        snapshot_center_z = current_center_z;
                        // Set target to center view (looking down slightly)
                        target_cam_angle_x = 0.3f;
                        target_cam_angle_y = 0.0f;
                        target_cam_dist = 50.0f;
                    }
                    break;
                case 'r': case 'R':
                    random_simulatefactor = !random_simulatefactor;
                    break;
                case 'h': case 'H':
                    showhelp = !showhelp;
                    break;
                case '+': case '=':
                    camDist -= 3.0f;
                    camera_tracking = false;
                    break;
                case '-': case '_':
                    camDist += 3.0f;
                    camera_tracking = false;
                    break;
                // Arrow keys - using key codes
                case 38:  // Up
                    camAngleX += 0.1f;
                    camera_tracking = false;
                    break;
                case 40:  // Down
                    camAngleX -= 0.1f;
                    camera_tracking = false;
                    break;
                case 37:  // Left
                    camAngleY -= 0.1f;
                    camera_tracking = false;
                    break;
                case 39:  // Right
                    camAngleY += 0.1f;
                    camera_tracking = false;
                    break;
            }
        }
    };
    
    SDLMMInterface.OnMousePressListener mousefnc = new SDLMMInterface.OnMousePressListener() {
        @Override
        public void onClick(int x, int y, int btn, boolean ison) {
            if (ison) {
                // Check for simulation factor slider
                if (y > SLIDER_SIM_Y && y < SLIDER_SIM_Y + SLIDER_SIM_HEIGHT && 
                    x >= SLIDER_X_START && x <= SLIDER_X_START + SLIDER_WIDTH) {
                    float value = 2.0f * ((float)(x - SLIDER_X_START)) / SLIDER_WIDTH;
                    if (value >= 0.0f && value <= 2.0f) {
                        simulatetime_factor = value;
                    }
                }
                // Check for particle size slider
                else if (y > SLIDER_PARTICLE_Y && y < SLIDER_PARTICLE_Y + SLIDER_PARTICLE_HEIGHT && 
                         x >= SLIDER_X_START && x <= SLIDER_X_START + SLIDER_WIDTH) {
                    float value = PARTICLE_SIZE_MIN + (PARTICLE_SIZE_MAX - PARTICLE_SIZE_MIN) * 
                                 ((float)(x - SLIDER_X_START)) / SLIDER_WIDTH;
                    if (value >= PARTICLE_SIZE_MIN && value <= PARTICLE_SIZE_MAX) {
                        particle_size = value;
                    }
                }
                // Camera rotation - mouse drag outside slider areas
                else {
                    if (!mouse_down) {
                        mouse_down = true;
                        last_mouse_x = x;
                        last_mouse_y = y;
                    }
                }
            } else {
                mouse_down = false;
            }
        }
    };
    
    SDLMMInterface.OnMouseMotionListener mousemotion = new SDLMMInterface.OnMouseMotionListener() {
        @Override
        public void onMove(int x, int y) {
            // Handle mouse dragging for camera rotation
            if (mouse_down) {
                int dx = x - last_mouse_x;
                int dy = y - last_mouse_y;
                
                // Only rotate if not clicking on sliders
                boolean in_slider_area = false;
                // Check simulation slider area
                if (y >= SLIDER_SIM_Y && y < SLIDER_SIM_Y + SLIDER_SIM_HEIGHT && 
                    x >= SLIDER_X_START && x <= SLIDER_X_START + SLIDER_WIDTH) {
                    in_slider_area = true;
                }
                // Check particle size slider area
                if (y >= SLIDER_PARTICLE_Y && y < SLIDER_PARTICLE_Y + SLIDER_PARTICLE_HEIGHT && 
                    x >= SLIDER_X_START && x <= SLIDER_X_START + SLIDER_WIDTH) {
                    in_slider_area = true;
                }
                
                if (!in_slider_area) {
                    // Rotate camera based on mouse movement
                    camAngleY += dx * MOUSE_ROTATION_SENSITIVITY;  // Horizontal rotation
                    camAngleX -= dy * MOUSE_ROTATION_SENSITIVITY;  // Vertical rotation (inverted)
                    
                    // Clamp vertical angle to prevent flipping
                    if (camAngleX > CAM_ANGLE_X_MAX) camAngleX = CAM_ANGLE_X_MAX;
                    if (camAngleX < CAM_ANGLE_X_MIN) camAngleX = CAM_ANGLE_X_MIN;
                    
                    camera_tracking = false;  // Disable tracking on manual control
                }
                
                last_mouse_x = x;
                last_mouse_y = y;
            }
            // Also handle slider dragging by calling mousefnc directly
            if (mouse_down) {
                mousefnc.onClick(x, y, 0, true);
            }
        }
    };
    
    @Override
    public void run() {
        // Initialize SOFTENING_SQUARED from initial SOFTENING value
        SOFTENING_SQUARED = SOFTENING * SOFTENING;
        
        X_axis = allocateBody();
        Y_axis = allocateBody();
        Z_axis = allocateBody();
        X_Velocity = allocateBody();
        Y_Velocity = allocateBody();
        Z_Velocity = allocateBody();
        Mass = allocateBody();
        newX_velocity = allocateBody();
        newY_velocity = allocateBody();
        newZ_velocity = allocateBody();
        gravitationalPotential = allocateBody();  // For optimized cluster tracking
        
        setTextFont("Consolas-16");
        setOnKeyboard(kbfnc);
        setOnMousePress(mousefnc);
        setOnMouseMotion(mousemotion);
        
        // Initialize 3D device
        m_device = new Device(SCREENX, SCREENY);
        
        // Initialize camera
        camera = new Camera();
        camera.Position = new Vector3(0, 0, -camDist);
        camera.Target = new Vector3(0, 0, 0);
        
        // Initialize bodies first so we know masses
        Init_AllBody();
        
        // Create particle rendering structures
        initParticles();
        
        for (int i = 0; i < 999999; ++i) {
            main_run();
        }
    }
    
    public static void main(String[] argv) {
        int sz = NUM_BODY;
        if (argv.length > 0) {
            sz = Integer.parseInt(argv[0]);
        }
        NBody3D demo = new NBody3D("[3D] NBody-Simulation (Babylon3D)", SCREENX, SCREENY, sz);
        demo.setVisible(true);
    }
}
