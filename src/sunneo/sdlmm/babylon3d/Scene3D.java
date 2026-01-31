package sunneo.sdlmm.babylon3d;

import java.util.ArrayList;
import java.util.List;

/**
 * 3D Scene structure compatible with ref-sdlmm scene_json.h
 */
public class Scene3D {
    public static final int SCENE_FORMAT_3D = 1;
    
    public int format;
    public Camera camera;
    public List<Light3D> lights;
    public List<Model3D> models;
    public int width;
    public int height;
    public int backgroundColor;

    public Scene3D() {
        format = SCENE_FORMAT_3D;
        camera = new Camera();
        camera.Position = new Vector3(0, 0, 10);
        camera.Target = Vector3.zero();
        lights = new ArrayList<>();
        models = new ArrayList<>();
        width = 800;
        height = 600;
        backgroundColor = 0;
    }

    public Scene3D(int width, int height) {
        this();
        this.width = width;
        this.height = height;
    }

    public void addLight(Light3D light) {
        lights.add(light);
    }

    public void addModel(Model3D model) {
        models.add(model);
    }

    /**
     * Get all meshes from models for rendering
     */
    public Mesh[] getMeshes() {
        List<Mesh> meshList = new ArrayList<>();
        for (Model3D model : models) {
            if (model.mesh != null) {
                meshList.add(model.mesh);
            }
        }
        return meshList.toArray(new Mesh[0]);
    }

    /**
     * Get the first light position, or default if no lights
     */
    public Vector3 getLightPosition() {
        if (!lights.isEmpty()) {
            return lights.get(0).position;
        }
        return new Vector3(0, 10, 10);
    }

    /**
     * Render the scene using the given device
     */
    public void render(Device device) {
        device.render(camera, getMeshes(), getLightPosition());
    }
}
