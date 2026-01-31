package sunneo.sdlmm.babylon3d;

/**
 * Face class representing a triangle by vertex indices for Babylon3D engine
 */
public class Face {
    public int A, B, C;

    public Face() {
        A = 0;
        B = 0;
        C = 0;
    }

    public Face(int a, int b, int c) {
        this.A = a;
        this.B = b;
        this.C = c;
    }
}
