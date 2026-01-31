package sunneo.sdlmm.exams;

import sunneo.sdlmm.babylon3d.*;
import sunneo.sdlmm.implement.SDLMMFrame;

/**
 * Babylon3D Scene Viewer Demo
 * Port of ref-sdlmm/scene_viewer.c to Java
 * Supports loading scenes from JSON files (compatible with ref-sdlmm format)
 */
public class Babylon3DSceneViewer extends SDLMMFrame {
    private static final long serialVersionUID = 1L;

    private Device device;
    private Scene3D scene;
    private Mesh[] meshes;
    private Camera camera;
    private Vector3 lightPosition;
    private int frameNumber;
    private long lastTime;
    private int fpsCount;
    private boolean rotateCamera;
    private boolean rotateMeshes;
    private int width;
    private int height;

    public Babylon3DSceneViewer(String sceneFile) {
        super("SDLMM+Babylon3D Scene Viewer", 800, 600);
        
        if (sceneFile != null) {
            loadFromJson(sceneFile);
        } else {
            createDefaultScene();
        }
        
        frameNumber = 0;
        lastTime = System.currentTimeMillis();
        fpsCount = 0;
        rotateCamera = true;
        rotateMeshes = true;
    }

    public Babylon3DSceneViewer() {
        this(null);
    }

    /**
     * Load scene from JSON file (compatible with ref-sdlmm format)
     */
    private void loadFromJson(String filename) {
        System.out.println("Loading scene from: " + filename);
        
        scene = SceneLoader.loadScene3D(filename);
        if (scene == null) {
            System.err.println("Failed to load scene, using default");
            createDefaultScene();
            return;
        }

        width = scene.width;
        height = scene.height;
        
        // Create rendering device
        device = new Device(width, height);
        
        // Get scene data
        camera = scene.camera;
        meshes = scene.getMeshes();
        lightPosition = scene.getLightPosition();
        
        // Resize window to match scene dimensions
        setSize(width, height);
        
        System.out.println("Scene loaded successfully: " + meshes.length + " meshes");
    }

    private void createDefaultScene() {
        width = 800;
        height = 600;
        
        // Create rendering device
        device = new Device(width, height);

        // Setup camera
        camera = new Camera();
        camera.Position = new Vector3(0, 5, -15);
        camera.Target = Vector3.zero();
        
        // Setup light
        lightPosition = new Vector3(0, 10, 10);

        // Create multiple cubes in a scene
        meshes = new Mesh[3];
        
        // Central cube
        meshes[0] = Mesh.createCube();
        meshes[0].Position = new Vector3(0, 0, 10);
        meshes[0].Rotation = Vector3.zero();
        
        // Left cube
        meshes[1] = Mesh.createCube();
        meshes[1].Position = new Vector3(-5, 0, 10);
        meshes[1].Rotation = new Vector3(0.3f, 0.3f, 0);
        
        // Right cube
        meshes[2] = Mesh.createCube();
        meshes[2].Position = new Vector3(5, 0, 10);
        meshes[2].Rotation = new Vector3(-0.3f, 0.3f, 0);
        
        // Try to load texture for all cubes
        Texture tex = Texture.load("texture.png");
        if (tex != null) {
            meshes[0].texture = tex;
            meshes[1].texture = tex;
            meshes[2].texture = tex;
            System.out.println("Texture loaded successfully");
        } else {
            System.out.println("Warning: texture.png not found, using default colors");
        }
    }

    @Override
    public void run() {
        System.out.println("Starting Babylon3D Scene Viewer...");
        System.out.println("Rendering scene with rotating camera...");
        System.out.println("Close window to exit");

        while (true) {
            renderFrame();
            
            // FPS counter
            fpsCount++;
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTime >= 1000) {
                lastTime = currentTime;
                fpsCount = 0;
            }
            
            // ~60 FPS
            sleep(16);
        }
    }

    private void renderFrame() {
        // Clear device buffer
        device.clear();
        
        // Rotate all cubes
        if (rotateMeshes && meshes != null) {
            for (Mesh mesh : meshes) {
                mesh.Rotation.x += 0.01f;
                mesh.Rotation.y += 0.01f;
            }
        }
        
        // Rotate camera around the scene
        if (rotateCamera) {
            float angle = frameNumber * 0.01f;
            camera.Position.x = 10.0f * (float) Math.cos(angle);
            camera.Position.z = 10.0f * (float) Math.sin(angle);
        }
        
        frameNumber++;
        
        // Render all meshes
        if (meshes != null && meshes.length > 0) {
            device.render(camera, meshes, lightPosition);
        }
        
        // Present to screen
        drawPixels(device.backbuffer, 0, 0, width, height);
        
        // Draw info
        fillRect(0, 0, 250, 40, 0xFF000000);
        drawString("Scene Viewer Demo", 5, 2, 0xFFFFFF);
        drawString("FPS: " + fpsCount + " | Frame: " + frameNumber + " | Meshes: " + 
                   (meshes != null ? meshes.length : 0), 5, 18, 0xFFFFFF);
        
        flush();
    }

    public static void main(String[] args) {
        System.out.println("Babylon3D Scene Viewer Demo");
        System.out.println("===========================");
        System.out.println("Usage: java Babylon3DSceneViewer [scene.json]");
        System.out.println();
        
        String sceneFile = null;
        if (args.length > 0) {
            sceneFile = args[0];
        }
        
        Babylon3DSceneViewer viewer = new Babylon3DSceneViewer(sceneFile);
        viewer.setVisible(true);
    }
}
