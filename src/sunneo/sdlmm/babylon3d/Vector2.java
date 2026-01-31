package sunneo.sdlmm.babylon3d;

/**
 * 2D Vector class for Babylon3D engine
 */
public class Vector2 {
    public float x, y;

    public Vector2() {
        this.x = 0;
        this.y = 0;
    }

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static Vector2 zero() {
        return new Vector2(0, 0);
    }

    public Vector2 copy() {
        return new Vector2(x, y);
    }

    public Vector2 add(Vector2 b) {
        return new Vector2(x + b.x, y + b.y);
    }

    public Vector2 subtract(Vector2 b) {
        return new Vector2(x - b.x, y - b.y);
    }

    public Vector2 negate() {
        return new Vector2(-x, -y);
    }

    public Vector2 scale(float s) {
        return new Vector2(x * s, y * s);
    }

    public boolean equals(Vector2 b) {
        return x == b.x && y == b.y;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public float lengthSquared() {
        return x * x + y * y;
    }

    public void normalize() {
        float len = length();
        if (len == 0) return;
        float num = 1.0f / len;
        x *= num;
        y *= num;
    }

    public Vector2 normalizeCopy() {
        Vector2 ret = copy();
        ret.normalize();
        return ret;
    }

    public static Vector2 min(Vector2 a, Vector2 b) {
        return new Vector2(
            Math.min(a.x, b.x),
            Math.min(a.y, b.y)
        );
    }

    public static Vector2 max(Vector2 a, Vector2 b) {
        return new Vector2(
            Math.max(a.x, b.x),
            Math.max(a.y, b.y)
        );
    }

    public Vector2 transform(Matrix transformation) {
        return new Vector2(
            (x * transformation.m[0]) + (y * transformation.m[4]),
            (x * transformation.m[1]) + (y * transformation.m[5])
        );
    }

    public float distanceSquared(Vector2 b) {
        float dx = x - b.x;
        float dy = y - b.y;
        return dx * dx + dy * dy;
    }

    public float distance(Vector2 b) {
        return (float) Math.sqrt(distanceSquared(b));
    }

    @Override
    public String toString() {
        return "{X: " + x + ", Y: " + y + "}";
    }
}
