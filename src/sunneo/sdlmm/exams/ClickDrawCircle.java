package sunneo.sdlmm.exams;

import sunneo.sdlmm.implement.SDLMMFrame;
import sunneo.sdlmm.interfaces.SDLMMInterface;

public class ClickDrawCircle extends SDLMMFrame {
    private static final long serialVersionUID = 2461861271650411948L;
    final static int WIDTH = 800;
    final static int HEIGHT = 600;
    final static int MAXCNT = 500;
    final static int MAXRAD = 300;
    int[] r = new int[MAXCNT];
    int[] xs = new int[MAXCNT];
    int[] ys = new int[MAXCNT];
    int[] c = new int[MAXCNT];
    boolean[] active = new boolean[MAXCNT];
    volatile int cnt = 0;
    volatile boolean clicking = false;

    void mouseFnc(int x, int y, boolean clicking) {
        if (clicking) {
            int select = cnt;
            r[select] = 1;
            xs[select] = x;
            ys[select] = y;
            c[select] = (int) (Math.random() * 0xffffff) | 0xff000000;
            active[select] = true;
            cnt = cnt + 1;
            if (cnt >= MAXCNT)
                cnt = 0;
        }
    }

    public ClickDrawCircle(String title) {
        super(title, WIDTH, HEIGHT);
        setOnMouseMotion(new SDLMMInterface.OnMouseMotionListener() {

            @Override
            public void onMove(int x, int y) {
                mouseFnc(x, y, clicking);
            }
        });
        setOnMousePress(new SDLMMInterface.OnMousePressListener() {

            @Override
            public void onClick(int x, int y, int btn, boolean ison) {
                clicking = ison;
                if (isHit(x, y, 0, 0, 120, 20)) {
                    messageBox.okCancel("ouch", "YOU Clicked ME");
                }
                mouseFnc(x, y, clicking);
            }
        });

    }

    @Override
    public void run() {
        while (true) {
            fillRect(0, 0, WIDTH, HEIGHT, 0xffffffff);

            for (int i = 0; i < cnt; ++i) {
                if (active[i]) {
                    if (r[i] + 1 < MAXRAD) {
                        drawCircle(xs[i], ys[i], r[i], c[i]);
                        ++r[i];
                    }
                    else {
                        active[i] = false;
                    }
                }
            }
            fillRect(0, 0, 120, 20, 0xff000000);
            drawString("Click&Drag Mouse", 0, 0, 0xffffffff);
            sleep(1);
            flush();
        }

    }

    public static void main(String[] argv) {
        ClickDrawCircle sdlmmtest = new ClickDrawCircle("Click & Drag Mouse");
        sdlmmtest.setVisible(true);
    }

}
