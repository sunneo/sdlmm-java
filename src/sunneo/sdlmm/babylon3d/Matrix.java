package sunneo.sdlmm.babylon3d;

/**
 * 4x4 Matrix class for Babylon3D engine transformations
 */
public class Matrix {
    public float[] m = new float[16];

    public Matrix() {
        // Initialize to identity
        m[0] = 1; m[1] = 0; m[2] = 0; m[3] = 0;
        m[4] = 0; m[5] = 1; m[6] = 0; m[7] = 0;
        m[8] = 0; m[9] = 0; m[10] = 1; m[11] = 0;
        m[12] = 0; m[13] = 0; m[14] = 0; m[15] = 1;
    }

    public static Matrix identity() {
        return new Matrix();
    }

    public static Matrix zero() {
        Matrix mat = new Matrix();
        for (int i = 0; i < 16; i++) {
            mat.m[i] = 0;
        }
        return mat;
    }

    public Matrix copy() {
        Matrix mat = new Matrix();
        System.arraycopy(m, 0, mat.m, 0, 16);
        return mat;
    }

    public boolean isIdentity() {
        if (m[0] != 1.0f || m[5] != 1.0f || m[10] != 1.0f || m[15] != 1.0f) return false;
        if (m[1] != 0.0f || m[2] != 0.0f || m[3] != 0.0f) return false;
        if (m[4] != 0.0f || m[6] != 0.0f || m[7] != 0.0f) return false;
        if (m[8] != 0.0f || m[9] != 0.0f || m[11] != 0.0f) return false;
        if (m[12] != 0.0f || m[13] != 0.0f || m[14] != 0.0f) return false;
        return true;
    }

    public float determinant() {
        float temp1 = (m[10] * m[15]) - (m[11] * m[14]);
        float temp2 = (m[9] * m[15]) - (m[11] * m[13]);
        float temp3 = (m[9] * m[14]) - (m[10] * m[13]);
        float temp4 = (m[8] * m[15]) - (m[11] * m[12]);
        float temp5 = (m[8] * m[14]) - (m[10] * m[12]);
        float temp6 = (m[8] * m[13]) - (m[9] * m[12]);

        return ((((m[0] * (((m[5] * temp1) - (m[6] * temp2)) + (m[7] * temp3))) 
                - (m[1] * (((m[4] * temp1) - (m[6] * temp4)) + (m[7] * temp5)))) 
                + (m[2] * (((m[4] * temp2) - (m[5] * temp4)) + (m[7] * temp6)))) 
                - (m[3] * (((m[4] * temp3) - (m[5] * temp5)) + (m[6] * temp6))));
    }

    public Matrix multiply(Matrix other) {
        Matrix result = new Matrix();
        float[] r = result.m;
        float[] a = this.m;
        float[] b = other.m;

        r[0] = a[0] * b[0] + a[1] * b[4] + a[2] * b[8] + a[3] * b[12];
        r[1] = a[0] * b[1] + a[1] * b[5] + a[2] * b[9] + a[3] * b[13];
        r[2] = a[0] * b[2] + a[1] * b[6] + a[2] * b[10] + a[3] * b[14];
        r[3] = a[0] * b[3] + a[1] * b[7] + a[2] * b[11] + a[3] * b[15];

        r[4] = a[4] * b[0] + a[5] * b[4] + a[6] * b[8] + a[7] * b[12];
        r[5] = a[4] * b[1] + a[5] * b[5] + a[6] * b[9] + a[7] * b[13];
        r[6] = a[4] * b[2] + a[5] * b[6] + a[6] * b[10] + a[7] * b[14];
        r[7] = a[4] * b[3] + a[5] * b[7] + a[6] * b[11] + a[7] * b[15];

        r[8] = a[8] * b[0] + a[9] * b[4] + a[10] * b[8] + a[11] * b[12];
        r[9] = a[8] * b[1] + a[9] * b[5] + a[10] * b[9] + a[11] * b[13];
        r[10] = a[8] * b[2] + a[9] * b[6] + a[10] * b[10] + a[11] * b[14];
        r[11] = a[8] * b[3] + a[9] * b[7] + a[10] * b[11] + a[11] * b[15];

        r[12] = a[12] * b[0] + a[13] * b[4] + a[14] * b[8] + a[15] * b[12];
        r[13] = a[12] * b[1] + a[13] * b[5] + a[14] * b[9] + a[15] * b[13];
        r[14] = a[12] * b[2] + a[13] * b[6] + a[14] * b[10] + a[15] * b[14];
        r[15] = a[12] * b[3] + a[13] * b[7] + a[14] * b[11] + a[15] * b[15];

        return result;
    }

    public static Matrix fromValues(
        float m11, float m12, float m13, float m14,
        float m21, float m22, float m23, float m24,
        float m31, float m32, float m33, float m34,
        float m41, float m42, float m43, float m44
    ) {
        Matrix mat = new Matrix();
        mat.m[0] = m11; mat.m[1] = m12; mat.m[2] = m13; mat.m[3] = m14;
        mat.m[4] = m21; mat.m[5] = m22; mat.m[6] = m23; mat.m[7] = m24;
        mat.m[8] = m31; mat.m[9] = m32; mat.m[10] = m33; mat.m[11] = m34;
        mat.m[12] = m41; mat.m[13] = m42; mat.m[14] = m43; mat.m[15] = m44;
        return mat;
    }

    public static Matrix rotationX(float angle) {
        Matrix result = new Matrix();
        float s = (float) Math.sin(angle);
        float c = (float) Math.cos(angle);
        result.m[0] = 1; result.m[1] = 0; result.m[2] = 0; result.m[3] = 0;
        result.m[4] = 0; result.m[5] = c; result.m[6] = s; result.m[7] = 0;
        result.m[8] = 0; result.m[9] = -s; result.m[10] = c; result.m[11] = 0;
        result.m[12] = 0; result.m[13] = 0; result.m[14] = 0; result.m[15] = 1;
        return result;
    }

    public static Matrix rotationY(float angle) {
        Matrix result = new Matrix();
        float s = (float) Math.sin(angle);
        float c = (float) Math.cos(angle);
        result.m[0] = c; result.m[1] = 0; result.m[2] = -s; result.m[3] = 0;
        result.m[4] = 0; result.m[5] = 1; result.m[6] = 0; result.m[7] = 0;
        result.m[8] = s; result.m[9] = 0; result.m[10] = c; result.m[11] = 0;
        result.m[12] = 0; result.m[13] = 0; result.m[14] = 0; result.m[15] = 1;
        return result;
    }

    public static Matrix rotationZ(float angle) {
        Matrix result = new Matrix();
        float s = (float) Math.sin(angle);
        float c = (float) Math.cos(angle);
        result.m[0] = c; result.m[1] = s; result.m[2] = 0; result.m[3] = 0;
        result.m[4] = -s; result.m[5] = c; result.m[6] = 0; result.m[7] = 0;
        result.m[8] = 0; result.m[9] = 0; result.m[10] = 1; result.m[11] = 0;
        result.m[12] = 0; result.m[13] = 0; result.m[14] = 0; result.m[15] = 1;
        return result;
    }

    public static Matrix rotationYawPitchRoll(float yaw, float pitch, float roll) {
        return rotationZ(roll).multiply(rotationX(pitch)).multiply(rotationY(yaw));
    }

    public static Matrix scaling(float x, float y, float z) {
        Matrix result = Matrix.zero();
        result.m[0] = x;
        result.m[5] = y;
        result.m[10] = z;
        result.m[15] = 1;
        return result;
    }

    public static Matrix translation(float x, float y, float z) {
        Matrix result = Matrix.identity();
        result.m[12] = x;
        result.m[13] = y;
        result.m[14] = z;
        return result;
    }

    public static Matrix lookAtLH(Vector3 eye, Vector3 target, Vector3 up) {
        Vector3 zAxis = target.subtract(eye).normalizeCopy();
        Vector3 xAxis = Vector3.cross(up, zAxis).normalizeCopy();
        Vector3 yAxis = Vector3.cross(zAxis, xAxis);

        float ex = -Vector3.dot(xAxis, eye);
        float ey = -Vector3.dot(yAxis, eye);
        float ez = -Vector3.dot(zAxis, eye);

        return Matrix.fromValues(
            xAxis.x, yAxis.x, zAxis.x, 0,
            xAxis.y, yAxis.y, zAxis.y, 0,
            xAxis.z, yAxis.z, zAxis.z, 0,
            ex, ey, ez, 1
        );
    }

    public static Matrix perspectiveLH(float width, float height, float znear, float zfar) {
        Matrix result = Matrix.zero();
        result.m[0] = (2.0f * znear) / width;
        result.m[5] = (2.0f * znear) / height;
        result.m[10] = -zfar / (znear - zfar);
        result.m[11] = 1.0f;
        result.m[14] = (znear * zfar) / (znear - zfar);
        return result;
    }

    public static Matrix perspectiveFovLH(float fov, float aspect, float znear, float zfar) {
        Matrix result = Matrix.zero();
        float tan = 1.0f / (float) Math.tan(fov * 0.5f);
        result.m[0] = tan / aspect;
        result.m[5] = tan;
        result.m[10] = -zfar / (znear - zfar);
        result.m[11] = 1.0f;
        result.m[14] = (znear * zfar) / (znear - zfar);
        return result;
    }

    public Matrix transpose() {
        Matrix result = new Matrix();
        result.m[0] = m[0]; result.m[1] = m[4]; result.m[2] = m[8]; result.m[3] = m[12];
        result.m[4] = m[1]; result.m[5] = m[5]; result.m[6] = m[9]; result.m[7] = m[13];
        result.m[8] = m[2]; result.m[9] = m[6]; result.m[10] = m[10]; result.m[11] = m[14];
        result.m[12] = m[3]; result.m[13] = m[7]; result.m[14] = m[11]; result.m[15] = m[15];
        return result;
    }

    /**
     * Invert the matrix in place
     * Based on standard 4x4 matrix inversion algorithm
     */
    public void invert() {
        float[] inv = new float[16];
        float det;
        int i;

        inv[0] = m[5]  * m[10] * m[15] - 
                 m[5]  * m[11] * m[14] - 
                 m[9]  * m[6]  * m[15] + 
                 m[9]  * m[7]  * m[14] +
                 m[13] * m[6]  * m[11] - 
                 m[13] * m[7]  * m[10];

        inv[4] = -m[4]  * m[10] * m[15] + 
                  m[4]  * m[11] * m[14] + 
                  m[8]  * m[6]  * m[15] - 
                  m[8]  * m[7]  * m[14] - 
                  m[12] * m[6]  * m[11] + 
                  m[12] * m[7]  * m[10];

        inv[8] = m[4]  * m[9] * m[15] - 
                 m[4]  * m[11] * m[13] - 
                 m[8]  * m[5] * m[15] + 
                 m[8]  * m[7] * m[13] + 
                 m[12] * m[5] * m[11] - 
                 m[12] * m[7] * m[9];

        inv[12] = -m[4]  * m[9] * m[14] + 
                   m[4]  * m[10] * m[13] +
                   m[8]  * m[5] * m[14] - 
                   m[8]  * m[6] * m[13] - 
                   m[12] * m[5] * m[10] + 
                   m[12] * m[6] * m[9];

        inv[1] = -m[1]  * m[10] * m[15] + 
                  m[1]  * m[11] * m[14] + 
                  m[9]  * m[2] * m[15] - 
                  m[9]  * m[3] * m[14] - 
                  m[13] * m[2] * m[11] + 
                  m[13] * m[3] * m[10];

        inv[5] = m[0]  * m[10] * m[15] - 
                 m[0]  * m[11] * m[14] - 
                 m[8]  * m[2] * m[15] + 
                 m[8]  * m[3] * m[14] + 
                 m[12] * m[2] * m[11] - 
                 m[12] * m[3] * m[10];

        inv[9] = -m[0]  * m[9] * m[15] + 
                  m[0]  * m[11] * m[13] + 
                  m[8]  * m[1] * m[15] - 
                  m[8]  * m[3] * m[13] - 
                  m[12] * m[1] * m[11] + 
                  m[12] * m[3] * m[9];

        inv[13] = m[0]  * m[9] * m[14] - 
                  m[0]  * m[10] * m[13] - 
                  m[8]  * m[1] * m[14] + 
                  m[8]  * m[2] * m[13] + 
                  m[12] * m[1] * m[10] - 
                  m[12] * m[2] * m[9];

        inv[2] = m[1]  * m[6] * m[15] - 
                 m[1]  * m[7] * m[14] - 
                 m[5]  * m[2] * m[15] + 
                 m[5]  * m[3] * m[14] + 
                 m[13] * m[2] * m[7] - 
                 m[13] * m[3] * m[6];

        inv[6] = -m[0]  * m[6] * m[15] + 
                  m[0]  * m[7] * m[14] + 
                  m[4]  * m[2] * m[15] - 
                  m[4]  * m[3] * m[14] - 
                  m[12] * m[2] * m[7] + 
                  m[12] * m[3] * m[6];

        inv[10] = m[0]  * m[5] * m[15] - 
                  m[0]  * m[7] * m[13] - 
                  m[4]  * m[1] * m[15] + 
                  m[4]  * m[3] * m[13] + 
                  m[12] * m[1] * m[7] - 
                  m[12] * m[3] * m[5];

        inv[14] = -m[0]  * m[5] * m[14] + 
                   m[0]  * m[6] * m[13] + 
                   m[4]  * m[1] * m[14] - 
                   m[4]  * m[2] * m[13] - 
                   m[12] * m[1] * m[6] + 
                   m[12] * m[2] * m[5];

        inv[3] = -m[1] * m[6] * m[11] + 
                  m[1] * m[7] * m[10] + 
                  m[5] * m[2] * m[11] - 
                  m[5] * m[3] * m[10] - 
                  m[9] * m[2] * m[7] + 
                  m[9] * m[3] * m[6];

        inv[7] = m[0] * m[6] * m[11] - 
                 m[0] * m[7] * m[10] - 
                 m[4] * m[2] * m[11] + 
                 m[4] * m[3] * m[10] + 
                 m[8] * m[2] * m[7] - 
                 m[8] * m[3] * m[6];

        inv[11] = -m[0] * m[5] * m[11] + 
                   m[0] * m[7] * m[9] + 
                   m[4] * m[1] * m[11] - 
                   m[4] * m[3] * m[9] - 
                   m[8] * m[1] * m[7] + 
                   m[8] * m[3] * m[5];

        inv[15] = m[0] * m[5] * m[10] - 
                  m[0] * m[6] * m[9] - 
                  m[4] * m[1] * m[10] + 
                  m[4] * m[2] * m[9] + 
                  m[8] * m[1] * m[6] - 
                  m[8] * m[2] * m[5];

        det = m[0] * inv[0] + m[1] * inv[4] + m[2] * inv[8] + m[3] * inv[12];

        if (Math.abs(det) < 1e-10f) {
            // Can't invert - return identity
            for (i = 0; i < 16; i++) {
                m[i] = (i % 5 == 0) ? 1.0f : 0.0f;
            }
            return;
        }

        det = 1.0f / det;

        for (i = 0; i < 16; i++) {
            m[i] = inv[i] * det;
        }
    }
}
