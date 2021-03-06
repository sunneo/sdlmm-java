package sunneo.sdlmm.exams;

import java.util.Random;

import sunneo.sdlmm.implement.SDLMMFrame;

public class BubbleSort extends SDLMMFrame {
    static final long serialVersionUID = 1L;
    final static int SCREEN_X = 800;
    final static int SCREEN_Y = 600;
    int[] arr = new int[128];
    int MAXDATA = 16384;

    void swap(int[] arr, int i, int j) {
        int t = arr[i];
        arr[i] = arr[j];
        arr[j] = t;
    }

    void showData1D(int[] data, int p1, int p2) {
        int size = data.length;
        int widthOfX = SCREEN_X / data.length;
        int i;
        fillRect(0, 0, SCREEN_X, SCREEN_Y, 0xff000000);
        for (i = 0; i < size; ++i) {
            int x = widthOfX * i;
            float dataHeightRatio = ((float) data[i]) / MAXDATA;
            int dataHeight = (int) (SCREEN_Y * dataHeightRatio);
            int y = SCREEN_Y - dataHeight;
            if (y < 0)
                y = 0;
            int color = 0xff00ffff;
            if (i == p1) {
                color = 0xffff0000;
            }
            else if (i == p2) {
                color = 0xffffff00;
            }
            fillRect(x, y, widthOfX, dataHeight, color);
        }
        flush();
        sleep(1);
    }

    void init() {
        Random r = new Random();
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = r.nextInt(16384);
        }
    }

    public BubbleSort() {
        super("Bubble Sort", SCREEN_X, SCREEN_Y);
        init();
    }

    public void run() {
        for (int i = 0; i < arr.length; ++i) {
            for (int j = 0; j < arr.length - i - 1; ++j) {
                if (arr[j] > arr[j + 1]) {
                    showData1D(arr, j, j + 1);
                    swap(arr, j, j + 1);
                }
            }
        }

    }

    public static void main(String[] argv) {
        BubbleSort bbsort = new BubbleSort();
        bbsort.setVisible(true);
    }

}
