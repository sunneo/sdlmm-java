package sunneo.sdlmm.babylon3d;

/**
 * Camera class for Babylon3D engine
 */
public class Camera {
    public Vector3 Position;
    public Vector3 Target;

    public Camera() {
        Position = Vector3.zero();
        Target = Vector3.zero();
    }

    public Camera(Vector3 position, Vector3 target) {
        this.Position = position;
        this.Target = target;
    }
}
