package sunneo.sdlmm.babylon3d;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import sunneo.sdlmm.interfaces.SDLMMInterface;

/**
 * 3D Rendering Device for Babylon3D engine
 * Handles software rasterization with depth buffer and texture mapping
 * Optimized with parallel processing for improved performance
 */
public class Device {
    public int workingWidth, workingHeight;
    public int[] backbuffer;
    public int[] depthbuffer;

    private static final float PERSPECTIVE_EPSILON = 0.0001f;
    
    // Parallel processing support
    private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService executor = Executors.newFixedThreadPool(NUM_CORES);
    private static final int PARALLEL_THRESHOLD = 512; // Minimum scanline width to use parallelization

    public Device(int width, int height) {
        this.workingWidth = width;
        this.workingHeight = height;
        this.backbuffer = new int[width * height];
        this.depthbuffer = new int[width * height];
        clear();
    }

    /**
     * Clear the backbuffer and depth buffer using parallel processing
     */
    public void clear() {
        final int size = backbuffer.length;
        final int chunkSize = size / NUM_CORES;
        
        if (size > 10000) {
            // Use parallel clearing for large buffers
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int i = 0; i < NUM_CORES; i++) {
                final int start = i * chunkSize;
                final int end = (i == NUM_CORES - 1) ? size : (i + 1) * chunkSize;
                tasks.add(() -> {
                    Arrays.fill(backbuffer, start, end, 0xFF000000);
                    Arrays.fill(depthbuffer, start, end, Integer.MAX_VALUE);
                    return null;
                });
            }
            try {
                executor.invokeAll(tasks);
            } catch (InterruptedException e) {
                // Fall back to sequential
                Arrays.fill(backbuffer, 0xFF000000);
                Arrays.fill(depthbuffer, Integer.MAX_VALUE);
            }
        } else {
            Arrays.fill(backbuffer, 0xFF000000);
            Arrays.fill(depthbuffer, Integer.MAX_VALUE);
        }
    }

    /**
     * Put a pixel with depth testing
     */
    public void putPixel(int x, int y, int z, int color) {
        if (x < 0 || y < 0 || x >= workingWidth || y >= workingHeight) {
            return;
        }
        int idx = y * workingWidth + x;
        if (depthbuffer[idx] < z) {
            return;
        }
        depthbuffer[idx] = z;
        backbuffer[idx] = color;
    }

    /**
     * Draw a point with depth
     */
    public void drawPoint(Vector3 point, int color) {
        putPixel((int) point.x, (int) point.y, (int) point.z, color);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }

    private static float clamp0(float value) {
        return clamp(value, 0, 1);
    }

    private static float interpolate(float min, float max, float gradient) {
        return min + (max - min) * clamp0(gradient);
    }

    /**
     * Project 3D vertex to 2D screen coordinates
     */
    public Vertex project(Vertex vertex, Matrix transMat, Matrix world) {
        Vertex ret = new Vertex();
        Vector3 point2d = vertex.Coordinates.transformCoordinates(transMat);
        float x = point2d.x * workingWidth + workingWidth / 2.0f;
        float y = -point2d.y * workingHeight + workingHeight / 2.0f;
        ret.Coordinates = new Vector3(x, y, point2d.z);
        ret.Normal = vertex.Normal.transformCoordinates(world);
        ret.WorldCoordinates = vertex.Coordinates.transformCoordinates(world);
        ret.TextureCoordinates = vertex.TextureCoordinates.copy();
        return ret;
    }

    /**
     * Compute N dot L for lighting
     */
    public static float computeNDotL(Vector3 vertex, Vector3 normal, Vector3 lightPosition) {
        Vector3 lightDirection = lightPosition.subtract(vertex);
        normal.normalize();
        lightDirection.normalize();
        return Math.max(0, Vector3.dot(normal, lightDirection));
    }

    /**
     * Create ARGB color from components
     */
    public static int color4(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Apply lighting to reference color
     */
    public static int color4ref(int refColor, float r, float g, float b, float a) {
        int origR = (refColor >> 16) & 0xFF;
        int origG = (refColor >> 8) & 0xFF;
        int origB = refColor & 0xFF;
        int origA = (refColor >> 24) & 0xFF;
        return color4(
            (int) (origR * r),
            (int) (origG * g),
            (int) (origB * b),
            (int) (origA * a)
        );
    }

    private static float calculateInverseZ(float z) {
        return (z != 0.0f) ? 1.0f / z : 0.0f;
    }

    /**
     * DrawData structure for scanline processing
     */
    private static class DrawData {
        int currentY;
        float ndotla, ndotlb, ndotlc, ndotld;
        float ua, ub, uc, ud;
        float va, vb, vc, vd;
        float za, zb, zc, zd;
    }

    /**
     * Process a scanline for triangle rasterization
     */
    private void processScanLine(DrawData data, Vertex va, Vertex vb, Vertex vc, Vertex vd, 
                                  float color, Texture texture) {
        float gradient1 = va.Coordinates.y != vb.Coordinates.y 
            ? (data.currentY - va.Coordinates.y) / (vb.Coordinates.y - va.Coordinates.y) : 1;
        float gradient2 = vc.Coordinates.y != vd.Coordinates.y 
            ? (data.currentY - vc.Coordinates.y) / (vd.Coordinates.y - vc.Coordinates.y) : 1;

        int sx = (int) interpolate(va.Coordinates.x, vb.Coordinates.x, gradient1);
        int ex = (int) interpolate(vc.Coordinates.x, vd.Coordinates.x, gradient2);

        float z1 = interpolate(va.Coordinates.z, vb.Coordinates.z, gradient1);
        float z2 = interpolate(vc.Coordinates.z, vd.Coordinates.z, gradient2);

        float snl = interpolate(data.ndotla, data.ndotlb, gradient1);
        float enl = interpolate(data.ndotlc, data.ndotld, gradient2);

        // Perspective-correct texture mapping
        float sz1 = interpolate(data.za, data.zb, gradient1);
        float sz2 = interpolate(data.zc, data.zd, gradient2);
        float su = interpolate(data.ua, data.ub, gradient1);
        float eu = interpolate(data.uc, data.ud, gradient2);
        float sv = interpolate(data.va, data.vb, gradient1);
        float ev = interpolate(data.vc, data.vd, gradient2);
        int currentY = data.currentY;

        for (int x = sx; x < ex; x++) {
            float gradient = (ex > sx) ? ((float) (x - sx) / (float) (ex - sx)) : 0.0f;

            float z = interpolate(z1, z2, gradient);
            float ndotl = interpolate(snl, enl, gradient) * color;

            // Perspective-correct texture coordinate interpolation
            float sz = interpolate(sz1, sz2, gradient);
            float u = interpolate(su, eu, gradient);
            float v = interpolate(sv, ev, gradient);

            if (sz > PERSPECTIVE_EPSILON) {
                u /= sz;
                v /= sz;
            }

            int textureColor;
            if (texture != null && texture.internalBuffer != null) {
                textureColor = texture.map(u, v);
            } else {
                textureColor = 0xFFFFFFFF;
            }

            // Apply lighting
            float lightingFactor = 0.2f + 0.8f * ndotl;
            int finalColor = color4ref(textureColor, lightingFactor, lightingFactor, lightingFactor, 1);
            putPixel(x, currentY, (int) (z * 10000000), finalColor);
        }
    }

    /**
     * Draw a filled triangle with texture and lighting
     */
    public void drawTriangle(Vertex v1, Vertex v2, Vertex v3, float color, Texture texture, Vector3 lightPos) {
        // Sort vertices by Y coordinate
        if (v1.Coordinates.y > v2.Coordinates.y) {
            Vertex temp = v2; v2 = v1; v1 = temp;
        }
        if (v2.Coordinates.y > v3.Coordinates.y) {
            Vertex temp = v2; v2 = v3; v3 = temp;
        }
        if (v1.Coordinates.y > v2.Coordinates.y) {
            Vertex temp = v2; v2 = v1; v1 = temp;
        }

        float nl1 = computeNDotL(v1.WorldCoordinates, v1.Normal.copy(), lightPos);
        float nl2 = computeNDotL(v2.WorldCoordinates, v2.Normal.copy(), lightPos);
        float nl3 = computeNDotL(v3.WorldCoordinates, v3.Normal.copy(), lightPos);

        DrawData data = new DrawData();

        float dP1P2 = 0;
        float dP1P3 = 0;

        if (v2.Coordinates.y - v1.Coordinates.y > 0) {
            dP1P2 = (v2.Coordinates.x - v1.Coordinates.x) / (v2.Coordinates.y - v1.Coordinates.y);
        }
        if (v3.Coordinates.y - v1.Coordinates.y > 0) {
            dP1P3 = (v3.Coordinates.x - v1.Coordinates.x) / (v3.Coordinates.y - v1.Coordinates.y);
        }

        if (dP1P2 > dP1P3) {
            for (int y = (int) v1.Coordinates.y; y <= (int) v3.Coordinates.y; y++) {
                data.currentY = y;

                float z1_inv = calculateInverseZ(v1.Coordinates.z);
                float z2_inv = calculateInverseZ(v2.Coordinates.z);
                float z3_inv = calculateInverseZ(v3.Coordinates.z);

                if (y < v2.Coordinates.y) {
                    data.ndotla = nl1;
                    data.ndotlb = nl3;
                    data.ndotlc = nl1;
                    data.ndotld = nl2;

                    data.ua = v1.TextureCoordinates.x * z1_inv;
                    data.ub = v3.TextureCoordinates.x * z3_inv;
                    data.uc = v1.TextureCoordinates.x * z1_inv;
                    data.ud = v2.TextureCoordinates.x * z2_inv;

                    data.va = v1.TextureCoordinates.y * z1_inv;
                    data.vb = v3.TextureCoordinates.y * z3_inv;
                    data.vc = v1.TextureCoordinates.y * z1_inv;
                    data.vd = v2.TextureCoordinates.y * z2_inv;

                    data.za = z1_inv;
                    data.zb = z3_inv;
                    data.zc = z1_inv;
                    data.zd = z2_inv;

                    processScanLine(data, v1, v3, v1, v2, color, texture);
                } else {
                    data.ndotla = nl1;
                    data.ndotlb = nl3;
                    data.ndotlc = nl2;
                    data.ndotld = nl3;

                    data.ua = v1.TextureCoordinates.x * z1_inv;
                    data.ub = v3.TextureCoordinates.x * z3_inv;
                    data.uc = v2.TextureCoordinates.x * z2_inv;
                    data.ud = v3.TextureCoordinates.x * z3_inv;

                    data.va = v1.TextureCoordinates.y * z1_inv;
                    data.vb = v3.TextureCoordinates.y * z3_inv;
                    data.vc = v2.TextureCoordinates.y * z2_inv;
                    data.vd = v3.TextureCoordinates.y * z3_inv;

                    data.za = z1_inv;
                    data.zb = z3_inv;
                    data.zc = z2_inv;
                    data.zd = z3_inv;

                    processScanLine(data, v1, v3, v2, v3, color, texture);
                }
            }
        } else {
            for (int y = (int) v1.Coordinates.y; y <= (int) v3.Coordinates.y; y++) {
                data.currentY = y;

                float z1_inv = calculateInverseZ(v1.Coordinates.z);
                float z2_inv = calculateInverseZ(v2.Coordinates.z);
                float z3_inv = calculateInverseZ(v3.Coordinates.z);

                if (y < v2.Coordinates.y) {
                    data.ndotla = nl1;
                    data.ndotlb = nl2;
                    data.ndotlc = nl1;
                    data.ndotld = nl3;

                    data.ua = v1.TextureCoordinates.x * z1_inv;
                    data.ub = v2.TextureCoordinates.x * z2_inv;
                    data.uc = v1.TextureCoordinates.x * z1_inv;
                    data.ud = v3.TextureCoordinates.x * z3_inv;

                    data.va = v1.TextureCoordinates.y * z1_inv;
                    data.vb = v2.TextureCoordinates.y * z2_inv;
                    data.vc = v1.TextureCoordinates.y * z1_inv;
                    data.vd = v3.TextureCoordinates.y * z3_inv;

                    data.za = z1_inv;
                    data.zb = z2_inv;
                    data.zc = z1_inv;
                    data.zd = z3_inv;

                    processScanLine(data, v1, v2, v1, v3, color, texture);
                } else {
                    data.ndotla = nl2;
                    data.ndotlb = nl3;
                    data.ndotlc = nl1;
                    data.ndotld = nl3;

                    data.ua = v2.TextureCoordinates.x * z2_inv;
                    data.ub = v3.TextureCoordinates.x * z3_inv;
                    data.uc = v1.TextureCoordinates.x * z1_inv;
                    data.ud = v3.TextureCoordinates.x * z3_inv;

                    data.va = v2.TextureCoordinates.y * z2_inv;
                    data.vb = v3.TextureCoordinates.y * z3_inv;
                    data.vc = v1.TextureCoordinates.y * z1_inv;
                    data.vd = v3.TextureCoordinates.y * z3_inv;

                    data.za = z2_inv;
                    data.zb = z3_inv;
                    data.zc = z1_inv;
                    data.zd = z3_inv;

                    processScanLine(data, v2, v3, v1, v3, color, texture);
                }
            }
        }
    }

    /**
     * Main render function - renders all meshes
     */
    public void render(Camera camera, Mesh[] meshes, Vector3 lightPosition) {
        Vector3 up = Vector3.up();
        Matrix viewMatrix = Matrix.lookAtLH(camera.Position, camera.Target, up);
        Matrix projectionMatrix = Matrix.perspectiveFovLH(0.78f, 
            (float) workingWidth / workingHeight, 0.01f, 1.0f);

        Vector3 lightPos = lightPosition != null ? lightPosition : new Vector3(0, 10, 10);

        for (Mesh cMesh : meshes) {
            Matrix rotationYPR = Matrix.rotationYawPitchRoll(
                cMesh.Rotation.y, cMesh.Rotation.x, cMesh.Rotation.z);
            Matrix translation = Matrix.translation(
                cMesh.Position.x, cMesh.Position.y, cMesh.Position.z);
            Matrix worldMatrix = rotationYPR.multiply(translation);
            Matrix res1 = worldMatrix.multiply(viewMatrix);
            Matrix transformMatrix = res1.multiply(projectionMatrix);

            for (int i = 0; i < cMesh.faceCount; i++) {
                Face currentFace = cMesh.faces[i];
                Vertex vertexA = cMesh.Vertices[currentFace.A];
                Vertex vertexB = cMesh.Vertices[currentFace.B];
                Vertex vertexC = cMesh.Vertices[currentFace.C];
                
                Vertex pixelA = project(vertexA, transformMatrix, worldMatrix);
                Vertex pixelB = project(vertexB, transformMatrix, worldMatrix);
                Vertex pixelC = project(vertexC, transformMatrix, worldMatrix);

                // Backface culling
                float edge1_x = pixelB.Coordinates.x - pixelA.Coordinates.x;
                float edge1_y = pixelB.Coordinates.y - pixelA.Coordinates.y;
                float edge2_x = pixelC.Coordinates.x - pixelA.Coordinates.x;
                float edge2_y = pixelC.Coordinates.y - pixelA.Coordinates.y;
                float cross_z = edge1_x * edge2_y - edge1_y * edge2_x;

                if (cross_z < 0) {
                    continue;
                }

                float color = 1.0f;
                drawTriangle(pixelA, pixelB, pixelC, color, cMesh.texture, lightPos);
            }
        }
    }

    /**
     * Render a single mesh
     */
    public void render(Camera camera, Mesh mesh, Vector3 lightPosition) {
        render(camera, new Mesh[] { mesh }, lightPosition);
    }

    /**
     * Draw a single point sprite with texture and optional additive blending
     */
    public void drawPointSprite(Vector3 position, float size, Texture texture, int color, boolean additive) {
        if (position == null || texture == null) return;
        
        int cx = (int)position.x;
        int cy = (int)position.y;
        int halfSize = (int)(size / 2.0f);
        
        // Extract color components
        int baseR = (color >> 16) & 0xFF;
        int baseG = (color >> 8) & 0xFF;
        int baseB = color & 0xFF;
        
        // Draw textured quad centered on particle position
        for (int dy = -halfSize; dy <= halfSize; dy++) {
            for (int dx = -halfSize; dx <= halfSize; dx++) {
                int px = cx + dx;
                int py = cy + dy;
                
                // Bounds check
                if (px < 0 || py < 0 || px >= workingWidth || py >= workingHeight) {
                    continue;
                }
                
                // Sample texture
                int tx = (int)(((float)(dx + halfSize) / size) * texture.width);
                int ty = (int)(((float)(dy + halfSize) / size) * texture.height);
                
                if (tx < 0) tx = 0;
                if (ty < 0) ty = 0;
                if (tx >= texture.width) tx = texture.width - 1;
                if (ty >= texture.height) ty = texture.height - 1;
                
                int texColor = texture.internalBuffer[ty * texture.width + tx];
                int texAlpha = (texColor >> 24) & 0xFF;
                
                if (texAlpha == 0) continue;
                
                // Apply texture intensity to base color
                float intensity = texAlpha / 255.0f;
                int r = (int)(baseR * intensity);
                int g = (int)(baseG * intensity);
                int b = (int)(baseB * intensity);
                
                int idx = py * workingWidth + px;
                
                if (additive) {
                    // Additive blending
                    int bgColor = backbuffer[idx];
                    int bgR = (bgColor >> 16) & 0xFF;
                    int bgG = (bgColor >> 8) & 0xFF;
                    int bgB = bgColor & 0xFF;
                    
                    r = bgR + r; if (r > 255) r = 255;
                    g = bgG + g; if (g > 255) g = 255;
                    b = bgB + b; if (b > 255) b = 255;
                }
                
                backbuffer[idx] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
        }
    }

    /**
     * Render multiple particles with camera projection
     */
    public void renderParticles(Camera camera, Vector3[] positions, int[] colors, 
                                int particleCount, float spriteSize, Texture spriteTexture, boolean additive) {
        if (camera == null || positions == null || spriteTexture == null) return;
        
        // Build view and projection matrices
        Vector3 up = Vector3.up();
        Matrix viewMatrix = Matrix.lookAtLH(camera.Position, camera.Target, up);
        Matrix projectionMatrix = Matrix.perspectiveFovLH(0.78f, 
            (float)workingWidth / (float)workingHeight, 0.01f, 1000.0f);
        Matrix viewProj = viewMatrix.multiply(projectionMatrix);
        
        // Render each particle
        for (int i = 0; i < particleCount && i < positions.length; i++) {
            Vector3 worldPos = positions[i];
            
            // Transform to view space first to get actual distance
            Vector3 viewPos = worldPos.transformCoordinates(viewMatrix);
            
            // In LH system, positive Z is forward from camera
            // Skip particles behind camera or too close
            if (viewPos.z <= 0.1f) continue;
            
            // Transform to clip space (already includes perspective divide)
            Vector3 clipPos = worldPos.transformCoordinates(viewProj);
            
            // clipPos is now in NDC space (-1 to 1)
            // Check if roughly on screen (with some margin for large sprites)
            if (clipPos.x < -2.0f || clipPos.x > 2.0f || clipPos.y < -2.0f || clipPos.y > 2.0f) {
                continue;
            }
            
            // Project to screen space
            float screenX = (clipPos.x + 1.0f) * 0.5f * workingWidth;
            float screenY = (1.0f - clipPos.y) * 0.5f * workingHeight;
            
            // Scale sprite size based on distance (perspective)
            // Larger distance = smaller sprite
            float distScale = 50.0f / viewPos.z;  // Adjusted: base size at distance 50
            float finalSize = spriteSize * distScale;
            
            // Clamp size for visibility and performance
            if (finalSize < 3.0f) finalSize = 3.0f;
            if (finalSize > 80.0f) finalSize = 80.0f;
            
            Vector3 screenPos = new Vector3(screenX, screenY, viewPos.z);
            int particleColor = (colors != null && i < colors.length) ? colors[i] : 0xFFFFFF;
            
            drawPointSprite(screenPos, finalSize, spriteTexture, particleColor, additive);
        }
    }

    /**
     * Present the backbuffer to screen using the provided rendering interface
     * This is the primary method for displaying the rendered content
     * 
     * @param screen The SDLMMInterface to draw to (typically the application window)
     */
    public void presentToScreen(SDLMMInterface screen) {
        if (screen == null) {
            return;
        }
        // Use bulk copy operation for optimal performance
        screen.drawPixels(backbuffer, 0, 0, workingWidth, workingHeight);
    }

    /**
     * Present the backbuffer to screen (legacy compatibility method)
     * Note: This method requires a screen interface to be set separately.
     * Consider using presentToScreen(SDLMMInterface) instead.
     */
    public void present() {
        // Legacy no-op method for C compatibility
        // Users should call presentToScreen(screen) to actually display content
    }
}
