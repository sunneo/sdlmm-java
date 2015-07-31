package sunneo.sdlmm.exams;

import java.util.Random;

import sunneo.sdlmm.implement.SDLMMFrame;

public class Qsort extends SDLMMFrame {
	static final long serialVersionUID = 1L;
	final static int SCREEN_X = 800;
	final static int SCREEN_Y = 600;
	int[] arr = new int[400];
	int MAXDATA = 16384;
	static Random random = new Random();

	void showData1D(int[] data, int p1, int p2) {
		int size = data.length;
		int widthOfX = SCREEN_X / data.length;
		int i;
		fillRect(0, 0, SCREEN_X, SCREEN_Y, 0xffffffff);
		for (i = 0; i < size; ++i) {
			int x = widthOfX * i;
			float dataHeightRatio = ((float) data[i]) / MAXDATA;
			int dataHeight = (int) (SCREEN_Y * dataHeightRatio);
			int y = SCREEN_Y - dataHeight;
			if (y < 0)
				y = 0;
			int color = 0xff000000 | ((data[i] * 0x0c0c0c) & 0x00ffffff);
			if (i == p1) {
				color = 0xffff0000;
			} else if (i == p2) {
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

	void swap(int[] arr, int i, int j) {
		int t = arr[i];
		arr[i] = arr[j];
		arr[j] = t;
	}

	int partition(int arr[], int left, int right) {
		int i = left, j = right;
		int tmp;
		int pivot = arr[(left + right) / 2];
		while (i <= j) {
			while (arr[i] < pivot)
				i++;
			while (arr[j] > pivot)
				j--;
			if (i <= j) {
				swap(arr, i, j);
				i++;
				j--;
			}
			showData1D(arr, i, j);
		}
		;
		return i;
	}

	void quickSort(int arr[], int left, int right) {
		int index = partition(arr, left, right - 1);
		if (left < index - 1)
			quickSort(arr, left, index - 1);
		if (index < right)
			quickSort(arr, index, right);
	}

	public Qsort() {
		super("Quick Sort", SCREEN_X, SCREEN_Y);
		init();
	}

	public void run() {
		quickSort(arr, 0, arr.length);
	}

	public static void main(String[] argv) {
		Qsort sort = new Qsort();
		sort.setVisible(true);
	}
}
