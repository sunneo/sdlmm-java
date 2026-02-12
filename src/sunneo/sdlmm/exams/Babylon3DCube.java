package sunneo.sdlmm.exams;

import sunneo.sdlmm.babylon3d.*;
import sunneo.sdlmm.implement.SDLMMFrame;

/**
 * Babylon3D Rotating Cube Demo
 * Port of ref-sdlmm/exams/babylon3D_cube.c to Java
 */
public class Babylon3DCube extends SDLMMFrame {
    private static final long serialVersionUID = 1L;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private Device device;
    private Mesh cubeMesh;
    private Camera camera;
    private long lastTime;
    private int frameCount;
    private int displayFps;

    public Babylon3DCube() {
        super("Babylon 3D - Rotating Cube Demo", WIDTH, HEIGHT);
        
        // Create rendering device
        device = new Device(WIDTH, HEIGHT);

        // Setup camera
        camera = new Camera();
        camera.Position = new Vector3(0, 0, -10);
        camera.Target = Vector3.zero();

        // Create cube mesh
        cubeMesh = Mesh.createCube();
        cubeMesh.Position = new Vector3(0, 0, 10);

        // Try to load texture
        Texture tex = Texture.load("texture.png");
        if (tex != null) {
            cubeMesh.texture = tex;
            System.out.println("Texture loaded successfully");
        } else {
            System.out.println("Warning: Failed to load texture.png, using default colors");
        }
        
        lastTime = System.currentTimeMillis();
        frameCount = 0;
    }

    @Override
    public void run() {
        System.out.println("Starting Babylon3D cube demo...");
        System.out.println("Close window to exit");

        while (true) {
            // Clear device buffer
            device.clear();

            // Rotate the cube
            cubeMesh.Rotation.x += 0.01f;
            cubeMesh.Rotation.y += 0.01f;

            // Render the mesh
            device.render(camera, cubeMesh, null);

            // Present to screen using Babylon3D API
            device.presentToScreen(this);
            
            // Draw FPS
            fillRect(0, 0, 100, 20, 0xFF000000);
            drawString("FPS: " + displayFps, 5, 2, 0xFFFFFF);
            
            flush();

            // FPS counter
            frameCount++;
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTime >= 1000) {
                displayFps = frameCount;
                lastTime = currentTime;
                frameCount = 0;
            }

            // ~60 FPS
            sleep(16);
        }
    }

    public static void main(String[] args) {
        System.out.println("Babylon3D Cube Demo");
        System.out.println("==================");
        
        Babylon3DCube demo = new Babylon3DCube();
        demo.setVisible(true);
    }
}
