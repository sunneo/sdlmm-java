package sunneo.sdlmm.babylon3d;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Texture class for Babylon3D engine
 */
public class Texture {
    public int width, height;
    public int[] internalBuffer;

    public Texture() {
        width = 0;
        height = 0;
        internalBuffer = null;
    }

    public Texture(int width, int height) {
        this.width = width;
        this.height = height;
        this.internalBuffer = new int[width * height];
    }

    /**
     * Load texture from file
     */
    public static Texture load(String filename) {
        try {
            BufferedImage image = ImageIO.read(new File(filename));
            if (image == null) {
                System.err.println("Failed to load texture: " + filename);
                return null;
            }
            Texture tex = new Texture();
            tex.width = image.getWidth();
            tex.height = image.getHeight();
            tex.internalBuffer = new int[tex.width * tex.height];
            image.getRGB(0, 0, tex.width, tex.height, tex.internalBuffer, 0, tex.width);
            return tex;
        } catch (IOException e) {
            System.err.println("Error loading texture: " + filename + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Get pixel color at UV coordinates
     */
    public int map(float tu, float tv) {
        if (internalBuffer == null || width == 0 || height == 0) {
            return 0xFFFFFFFF; // White if no texture
        }

        // Clamp UV to [0, 1]
        tu = Math.max(0, Math.min(1, tu));
        tv = Math.max(0, Math.min(1, tv));

        int u = (int) (tu * (width - 1));
        int v = (int) (tv * (height - 1));

        int idx = v * width + u;
        if (idx >= 0 && idx < internalBuffer.length) {
            return internalBuffer[idx];
        }
        return 0xFFFFFFFF;
    }
}
