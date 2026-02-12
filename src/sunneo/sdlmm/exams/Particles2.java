package sunneo.sdlmm.exams;

import sunneo.sdlmm.implement.SDLMMFrame;
import sunneo.sdlmm.interfaces.SDLMMInterface;

/**
 * Particle simulation with gravity and dynamic particle generation
 * Based on ref-sdlmm particles2.c
 */
public class Particles2 extends SDLMMFrame {
    private static final long serialVersionUID = 1L;
    
    private int N = 300;
    private int maxN = 1000;
    private float[] X;
    private float[] Y;
    private float[] vX;
    private float[] vY;
    private int[] collision;
    private int[] Color;
    private int currentN = 0;
    
    private float secPerSimulate = 0.1f;
    private float decreasePerFrame = 0.001f;
    private int radius = 5;
    private int SCREEN_X = 1024;
    private int SCREEN_Y = 768;
    private int minV = -10;
    private int maxV = 10;
    private boolean runFlag = true;
    
    private int retryCnt = 0;
    private int waitForEmpty = 0;
    private int waitCycle = 0;
    private int runCycle = 0;
    
    public Particles2(String title, int width, int height, int particleCount, int maxParticles) {
        super(title, width, height);
        this.SCREEN_X = width;
        this.SCREEN_Y = height;
        this.N = particleCount;
        this.maxN = maxParticles;
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
    
    private void init(int size, int allocSize) {
        N = size;
        X = new float[allocSize];
        Y = new float[allocSize];
        vX = new float[allocSize];
        vY = new float[allocSize];
        Color = new int[allocSize];
        collision = new int[allocSize];
        
        generate(size);
    }
    
    private int getSign(float f) {
        if (f < 0) return -1;
        return 1;
    }
    
    private void swapf(int i, int j) {
        float t = X[i]; X[i] = X[j]; X[j] = t;
        t = vX[i]; vX[i] = vX[j]; vX[j] = t;
        t = Y[i]; Y[i] = Y[j]; Y[j] = t;
        t = vY[i]; vY[i] = vY[j]; vY[j] = t;
    }
    
    private void swapi(int i, int j) {
        int t = Color[i]; Color[i] = Color[j]; Color[j] = t;
    }
    
    private void swap(int i, int j) {
        swapf(i, j);
        swapi(i, j);
    }
    
    private void simulate() {
        float collisionDistanceSquare = (radius + radius);
        collisionDistanceSquare *= collisionDistanceSquare;
        
        for (int i = currentN - 1; i >= 0; i--) {
            float distanceToY;
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
            
            // Bounce off left wall
            if (X[i] <= radius && vX[i] < 0) {
                vX[i] = -vX[i];
            }
            
            // Remove particles that go off right edge
            if (X[i] > SCREEN_X) {
                swap(i, currentN - 1);
                if (currentN - 1 > 0) {
                    currentN--;
                }
                continue;
            }
            
            // Bounce off top/bottom walls
            if ((Y[i] <= radius && vY[i] < 0) || (Y[i] >= SCREEN_Y - radius && vY[i] > 0)) {
                vY[i] = -vY[i];
                
                // Remove particles that settle at the bottom
                if (Y[i] >= SCREEN_Y - radius && Math.abs(vY[i]) < 0.001) {
                    swap(i, currentN - 1);
                    if (currentN - 1 > 0) {
                        currentN--;
                    }
                }
            }
            
            // Clamp velocity
            if (getSign(vX[i]) * vX[i] > maxV) vX[i] = maxV * getSign(vX[i]);
            if (getSign(vY[i]) * vY[i] > maxV) vY[i] = maxV * getSign(vY[i]);
            
            // Apply gravity (inverse square law)
            distanceToY = (SCREEN_Y - Y[i]) / 2;
            vY[i] += (9.8f * secPerSimulate) / (distanceToY * distanceToY);
        }
    }
    
    private void draw() {
        fillRect(0, 0, SCREEN_X, SCREEN_Y, 0xFFFFFFFF);
        
        for (int i = 0; i < currentN; i++) {
            fillCircle((int)X[i], (int)Y[i], radius, Color[i]);
            if (collision[i] != 0) {
                drawCircle((int)X[i], (int)Y[i], radius + 1, ~Color[i]);
            }
            collision[i] = 0;
        }
        
        flush();
    }
    
    private void generate(int cnt) {
        if (runCycle > 200) {
            waitCycle = 1;
            runCycle = 0;
            return;
        }
        
        if (waitCycle != 0) {
            if (retryCnt < 1000) {
                retryCnt++;
                return;
            } else {
                retryCnt = 0;
                waitCycle = 0;
            }
        }
        
        if (waitForEmpty == 0) {
            if (currentN >= maxN) {
                waitForEmpty = 1;
                return;
            }
        }
        
        if (waitForEmpty != 0) {
            if (retryCnt < 1000) {
                retryCnt++;
                return;
            } else {
                waitForEmpty = 0;
                retryCnt = 0;
            }
        }
        
        int end = currentN + cnt;
        if (end >= maxN) end = N;
        int start = currentN - 1;
        if (start < 0) start = 0;
        
        for (int i = start; i < end; i++) {
            X[i] = randomf(0, 10);
            Y[i] = randomf(0, 10);
            vX[i] = randomf(minV, maxV);
            vY[i] = randomf(minV, maxV);
            Color[i] = (int)(Math.random() * 0xFFFFFF);
            if (Color[i] < 65536) {
                Color[i] |= (Color[i] << 15);
            }
        }
        
        if (currentN + cnt < maxN) {
            currentN += cnt;
        } else {
            currentN = maxN;
        }
        runCycle++;
    }
    
    @Override
    public void run() {
        init(N, maxN);
        
        while (runFlag) {
            generate(1);
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
        int maxSize = 1000;
        int radius = 5;
        int maxV = 10;
        int width = 1024;
        int height = 768;
        
        if (args.length > 0) size = Integer.parseInt(args[0]);
        if (args.length > 1) maxSize = Integer.parseInt(args[1]);
        if (args.length > 2) radius = Integer.parseInt(args[2]);
        if (args.length > 3) maxV = Integer.parseInt(args[3]);
        
        System.out.println("Simulate particles: " + size + " (max: " + maxSize + ")");
        
        Particles2 demo = new Particles2("Particles2 Simulation with Gravity", width, height, size, maxSize);
        demo.radius = radius;
        demo.maxV = maxV;
        demo.setOnKeyboard(demo.kbfnc);
        demo.setVisible(true);
    }
}
