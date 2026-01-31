package sunneo.sdlmm.babylon3d;

/**
 * 3D Model structure compatible with ref-sdlmm scene_json.h
 */
public class Model3D {
    public Mesh mesh;
    public String modelFile;
    public String textureFile;
    public Vector3 position;
    public Vector3 rotation;
    public Vector3 scale;

    public Model3D() {
        mesh = null;
        modelFile = null;
        textureFile = null;
        position = Vector3.zero();
        rotation = Vector3.zero();
        scale = new Vector3(1, 1, 1);
    }
}
