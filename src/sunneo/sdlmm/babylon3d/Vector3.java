package sunneo.sdlmm.babylon3d;

/**
 * 3D Vector class for Babylon3D engine
 */
public class Vector3 {
    public float x, y, z;

    public Vector3() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vector3 zero() {
        return new Vector3(0, 0, 0);
    }

    public static Vector3 up() {
        return new Vector3(0, 1, 0);
    }

    public Vector3 copy() {
        return new Vector3(x, y, z);
    }

    public Vector3 add(Vector3 b) {
        return new Vector3(x + b.x, y + b.y, z + b.z);
    }

    public Vector3 subtract(Vector3 b) {
        return new Vector3(x - b.x, y - b.y, z - b.z);
    }

    public Vector3 negate() {
        return new Vector3(-x, -y, -z);
    }

    public Vector3 scale(float s) {
        return new Vector3(x * s, y * s, z * s);
    }

    public Vector3 multiply(Vector3 b) {
        return new Vector3(x * b.x, y * b.y, z * b.z);
    }

    public Vector3 divide(Vector3 b) {
        return new Vector3(x / b.x, y / b.y, z / b.z);
    }

    public boolean equals(Vector3 b) {
        return x == b.x && y == b.y && z == b.z;
    }

    public float lengthSquared() {
        return x * x + y * y + z * z;
    }

    public float length() {
        return (float) Math.sqrt(lengthSquared());
    }

    public void normalize() {
        float len = length();
        if (len == 0) return;
        float num = 1.0f / len;
        x *= num;
        y *= num;
        z *= num;
    }

    public Vector3 normalizeCopy() {
        Vector3 ret = copy();
        ret.normalize();
        return ret;
    }

    public static Vector3 fromArray(float[] f, int offset) {
        return new Vector3(f[offset], f[offset + 1], f[offset + 2]);
    }

    public Vector3 transformCoordinates(Matrix transformation) {
        float[] m = transformation.m;
        float x = (this.x * m[0]) + (this.y * m[4]) + (this.z * m[8]) + m[12];
        float y = (this.x * m[1]) + (this.y * m[5]) + (this.z * m[9]) + m[13];
        float z = (this.x * m[2]) + (this.y * m[6]) + (this.z * m[10]) + m[14];
        float w = (this.x * m[3]) + (this.y * m[7]) + (this.z * m[11]) + m[15];
        return new Vector3(x / w, y / w, z / w);
    }

    public Vector3 transformNormal(Matrix transformation) {
        float[] m = transformation.m;
        float x = (this.x * m[0]) + (this.y * m[4]) + (this.z * m[8]);
        float y = (this.x * m[1]) + (this.y * m[5]) + (this.z * m[9]);
        float z = (this.x * m[2]) + (this.y * m[6]) + (this.z * m[10]);
        return new Vector3(x, y, z);
    }

    public static float dot(Vector3 left, Vector3 right) {
        return left.x * right.x + left.y * right.y + left.z * right.z;
    }

    public static Vector3 cross(Vector3 left, Vector3 right) {
        return new Vector3(
            left.y * right.z - left.z * right.y,
            left.z * right.x - left.x * right.z,
            left.x * right.y - left.y * right.x
        );
    }

    public float distance(Vector3 b) {
        return (float) Math.sqrt(distanceSquared(b));
    }

    public float distanceSquared(Vector3 b) {
        float dx = x - b.x;
        float dy = y - b.y;
        float dz = z - b.z;
        return dx * dx + dy * dy + dz * dz;
    }

    @Override
    public String toString() {
        return "{X: " + x + ", Y: " + y + ", Z: " + z + "}";
    }
}
