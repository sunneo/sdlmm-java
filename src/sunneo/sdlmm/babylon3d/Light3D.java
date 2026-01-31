package sunneo.sdlmm.babylon3d;

/**
 * 3D Light structure compatible with ref-sdlmm scene_json.h
 */
public class Light3D {
    public Vector3 position;
    public float intensity;
    public int color;

    public Light3D() {
        position = Vector3.zero();
        intensity = 1.0f;
        color = 0xFFFFFF;
    }

    public Light3D(Vector3 position, float intensity, int color) {
        this.position = position;
        this.intensity = intensity;
        this.color = color;
    }
}
