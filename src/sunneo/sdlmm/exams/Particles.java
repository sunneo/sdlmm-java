package sunneo.sdlmm.exams;

import sunneo.sdlmm.implement.SDLMMFrame;
import sunneo.sdlmm.interfaces.SDLMMInterface;

/**
 * Particle simulation with collision detection
 * Based on ref-sdlmm particles.c
 */
public class Particles extends SDLMMFrame {
    private static final long serialVersionUID = 1L;
    
    private int N = 300;
    private float[] X;
    private float[] Y;
    private float[] vX;
    private float[] vY;
    private int[] collision;
    private int[] Color;
    
    private float secPerSimulate = 0.1f;
    private float decreasePerFrame = 0.001f;
    private int radius = 5;
    private int SCREEN_X = 800;
    private int SCREEN_Y = 600;
    private int minV = -10;
    private int maxV = 10;
    private boolean runFlag = true;
    
    public Particles(String title, int width, int height, int particleCount) {
        super(title, width, height);
        this.SCREEN_X = width;
        this.SCREEN_Y = height;
        this.N = particleCount;
    }
    
    private float randomf(float l, float u) {
        if (l > u) {
            float t = l;
            l = u;
            u = t;
        }
        float r = u - l;
        return l + (r * (float)Math.random());
    }
    
    private void init(int size) {
        N = size;
        X = new float[size];
        Y = new float[size];
        vX = new float[size];
        vY = new float[size];
        Color = new int[size];
        collision = new int[size];
        
        for (int i = 0; i < N; i++) {
            X[i] = randomf(0, SCREEN_X - 1);
            Y[i] = randomf(0, SCREEN_Y - 1);
            vX[i] = randomf(minV, maxV);
            vY[i] = randomf(minV, maxV);
            Color[i] = (int)(Math.random() * 0xFFFFFF) | 0xFF000000;
        }
    }
    
    private int getSign(float f) {
        if (f < 0) return -1;
        return 1;
    }
    
    private void simulate() {
        float collisionDistanceSquare = (radius + radius);
        collisionDistanceSquare *= collisionDistanceSquare;
        
        for (int i = 0; i < N; i++) {
            collision[i] = 0;
            X[i] += vX[i] * secPerSimulate;
            Y[i] += vY[i] * secPerSimulate;
            
            // Check collision with other particles
            for (int j = 0; j < N; j++) {
                if (j == i) continue;
                float vXValue = X[j] - X[i];
                float vYValue = Y[j] - Y[i];
                vXValue *= vXValue;
                vYValue *= vYValue;
                
                if (vXValue + vYValue <= collisionDistanceSquare) {
                    // Hit
                    collision[i] = 1;
                    collision[j] = 1;
                    
                    if (getSign(vX[j]) != getSign(vX[i])) {
                        vXValue = (vX[i] + vX[j]) / 2;
                        vX[j] -= getSign(vX[j]) * vXValue;
                        vX[i] -= getSign(vX[i]) * vXValue;
                    }
                    if (getSign(vY[j]) != getSign(vY[i])) {
                        vYValue = (vY[i] + vY[j]) / 2;
                        vY[j] -= getSign(vY[j]) * vYValue;
                        vY[i] -= getSign(vY[i]) * vYValue;
                    }
                }
            }
            
            // Bounce off walls
            if ((X[i] <= radius && vX[i] < 0) || (X[i] >= SCREEN_X - radius && vX[i] > 0)) {
                vX[i] = -vX[i];
            }
            if ((Y[i] <= radius && vY[i] < 0) || (Y[i] >= SCREEN_Y - radius && vY[i] > 0)) {
                vY[i] = -vY[i];
            }
            
            // Clamp velocity
            if (getSign(vX[i]) * vX[i] > maxV) vX[i] = maxV * getSign(vX[i]);
            if (getSign(vY[i]) * vY[i] > maxV) vY[i] = maxV * getSign(vY[i]);
            
            // Apply friction
            vX[i] -= getSign(vX[i]) * decreasePerFrame;
            vY[i] -= getSign(vY[i]) * decreasePerFrame;
        }
    }
    
    private void draw() {
        fillRect(0, 0, SCREEN_X, SCREEN_Y, 0xFFFFFFFF);
        
        for (int i = 0; i < N; i++) {
            fillCircle((int)X[i], (int)Y[i], radius, Color[i]);
            if (collision[i] != 0) {
                drawCircle((int)X[i], (int)Y[i], radius + 1, ~Color[i]);
            }
            collision[i] = 0;
        }
        
        flush();
    }
    
    @Override
    public void run() {
        init(N);
        
        while (runFlag) {
            simulate();
            draw();
            sleep(1);
        }
    }
    
    SDLMMInterface.OnKeyboardListener kbfnc = new SDLMMInterface.OnKeyboardListener() {
        @Override
        public void onkey(int key, boolean shift, boolean ctrl, boolean alt, boolean ison) {
            if (!ison) return;
            
            switch (key) {
                case 'q':
                case 'Q':
                case 27: // ESC
                    runFlag = false;
                    break;
            }
        }
    };
    
    public static void main(String[] args) {
        int size = 300;
        int radius = 5;
        int width = 800;
        int height = 600;
        
        if (args.length > 0) size = Integer.parseInt(args[0]);
        if (args.length > 1) radius = Integer.parseInt(args[1]);
        
        System.out.println("Simulate particles: " + size);
        
        Particles demo = new Particles("Particles Simulation", width, height, size);
        demo.radius = radius;
        demo.setOnKeyboard(demo.kbfnc);
        demo.setVisible(true);
    }
}
