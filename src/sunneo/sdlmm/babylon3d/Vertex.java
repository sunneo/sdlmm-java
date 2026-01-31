package sunneo.sdlmm.babylon3d;

/**
 * Vertex class for Babylon3D engine with position, normals, world coordinates and texture coordinates
 */
public class Vertex {
    public Vector3 Normal;
    public Vector3 Coordinates;
    public Vector3 WorldCoordinates;
    public Vector3 TextureCoordinates;

    public Vertex() {
        Normal = Vector3.zero();
        Coordinates = Vector3.zero();
        WorldCoordinates = Vector3.zero();
        TextureCoordinates = Vector3.zero();
    }

    public Vertex copy() {
        Vertex v = new Vertex();
        v.Normal = Normal.copy();
        v.Coordinates = Coordinates.copy();
        v.WorldCoordinates = WorldCoordinates.copy();
        v.TextureCoordinates = TextureCoordinates.copy();
        return v;
    }
}
