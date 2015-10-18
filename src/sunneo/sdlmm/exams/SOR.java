package sunneo.sdlmm.exams;

import sunneo.sdlmm.implement.SDLMMFrame;
import sunneo.sdlmm.interfaces.SDLMMInterface;

public class SOR extends SDLMMFrame {
	final static int DEFSIZE = 512;
	final static int SCREENX = 800;
	final static int SCREENY = 600;
	final static int LOOP = 5000;
	float[] A, B;
	int SZ = DEFSIZE;
	boolean invert = false;
	boolean pause = false;

	double getDoubleTime() {
		return (double) System.currentTimeMillis();
	}

	boolean isInBound(int y, int x, int sz) {
		return y >= 0 && y < sz && x >= 0 && x < sz;
	}

	float fetch(float[] a, int y, int x, int sz) {
		int inbound = isInBound(y, x, sz) ? 1 : 0;
		return a[(y * sz + x) * inbound] * inbound;
	}

	void sor(float[] a, final float[] B, int sz) {
		int i, e = sz * sz;
		for (i = 0; i < e; i++) {
			int x = i % sz;
			int y = i / sz;
			float s = 0;
			int cnt = 0;
			if (x - 1 >= 0) {
				s += B[y * sz + (x - 1)];
				++cnt;
			} // left
			if (y - 1 >= 0) {
				s += B[(y - 1) * sz + x];
				++cnt;
			} // up
			if (x + 1 < sz) {
				s += B[y * sz + (x + 1)];
				++cnt;
			} // right
			if (y + 1 < sz) {
				s += B[(y + 1) * sz + x];
				++cnt;
			} // down
			if (false) {
				if (isInBound(y, x - 1, sz))
					++cnt;
				s += fetch(B, y, x - 1, sz);
				if (isInBound(y - 1, x, sz))
					++cnt;
				s += fetch(B, y - 1, x, sz);
				if (isInBound(y, x + 1, sz))
					++cnt;
				s += fetch(B, y, x + 1, sz);
				if (isInBound(y + 1, x, sz))
					++cnt;
				s += fetch(B, y + 1, x, sz);
			}
			a[i] = s / cnt;
		}
	}

	void stretchFloatPixels(final float[] pixels, int w, int h, int[] output, int w2, int h2) {
		float dw = ((float) w2) / w;
		float dh = ((float) h2) / h;
		int ii, e;
		e = h2 * w2;
		boolean currentInvert = invert;
		for (ii = 0; ii < e; ii += 1) {
			int origi, origj;
			float i = ii % w2;
			float j = ii / w2;
			origi = (int) (i / dw);
			origj = (int) (j / dh);
			if (currentInvert) {
				output[(int) (j * w2 + i)] = 0xff000000 | ~(int) (pixels[(int) (origj * w + origi)]);
			} else {
				output[(int) (j * w2 + i)] = 0xff000000 | (int) (pixels[(int) (origj * w + origi)]);
			}
		}
	}

	int[] canvas;

	void drawMatrix(float[] a, int loop, double tm) {

		double t1, t2;
		String buf;
		if (canvas == null) {
			canvas = new int[SCREENX * SCREENY];
		}
		t1 = getDoubleTime();
		stretchFloatPixels(a, SZ, SZ, canvas, SCREENX, SCREENY);
		drawPixels(canvas, 0, 0, SCREENX, SCREENY);
		buf = String.format("loop: %-3d/%-3d, time:%-3.4f", loop, LOOP, tm);
		if (invert) {
			drawString(buf, 0, 0, 0xff000000);
		} else {
			drawString(buf, 0, 0, 0xffffffff);
		}

		flush();
		t2 = getDoubleTime();
		if (tm + t2 - t1 < (1.0 / 60)) {
			sleep((int) ((1.0 / 60) - tm - (t2 - t1)));
		}

	}

	void main_run() {
		int i;
		double t1, t2;
		initMatrix();
		for (i = 0; i < LOOP; ++i) {
			t1 = getDoubleTime();
			if (pause) {
				--i;
				drawMatrix(A, i, 0);
				t2 = getDoubleTime();
				System.arraycopy(A, 0, B, 0, SZ * SZ);
				continue;
			}
			sor(A, B, SZ);
			t2 = getDoubleTime();
			drawMatrix(A, i, t2 - t1);
			System.arraycopy(A, 0, B, 0, SZ * SZ);
		}
	}

	public SOR(String title, int width, int height, int sz) {
		super(title, width, height);
		SZ = sz;
	}

	void initMatrix() {
		for (int i = 0; i < SZ * SZ; i++) {
			A[i] = B[i] = 0;
		}
	}

	SDLMMInterface.OnKeyboardListener kbfnc = new SDLMMInterface.OnKeyboardListener() {

		@Override
		public void onkey(int key, boolean shift, boolean ctrl, boolean alt, boolean ison) {
			if (!ison)
				return;
			switch (key) {
			case ' ':
			case 'i':
			case 'I':
				invert = !invert;
				break;
			case 'r':
			case 'R':
				initMatrix();
				break;
			case 'p':
			case 'P':
				pause = !pause;
				break;
			}
		}
	};
	boolean mouseIsOn = false;
	SDLMMInterface.OnMousePressListener mousefnc = new SDLMMInterface.OnMousePressListener() {

		@Override
		public void onClick(int x, int y, int btn, boolean ison) {
			mouseIsOn = ison;

		}
	};

	SDLMMInterface.OnMouseMotionListener mousemotion = new SDLMMInterface.OnMouseMotionListener() {

		@Override
		public void onMove(int x, int y) {
			if (!mouseIsOn)
				return;
			float xratio = ((float) SZ) / SCREENX;
			float yratio = ((float) SZ) / SCREENY;
			if (mouseIsOn) {
				x = (int) (x * xratio);
				y = (int) (y * yratio);
				A[y * SZ + x] = 0xffffff;
				if (y - 1 > 0)
					A[(y - 1) * SZ + x] = 0x0000ff;
				if (y + 1 < SZ)
					A[(y + 1) * SZ + x] = 0x00000f;
				if (x - 1 > 0)
					A[y * SZ + x - 1] = 0x00000f;
				if (x + 1 < SZ)
					A[(y + 1) * SZ + x + 1] = 0x00000f;
			}
		}
	};

	@Override
	public void run() {
		A = new float[SZ * SZ];
		B = new float[SZ * SZ];

		// sdlmm = sdlmm_get_instance("libsdlmm.so");

		setOnMousePress(mousefnc);
		setOnMouseMotion(mousemotion);
		setOnKeyboard(kbfnc);

		for (int i = 0; i < 20; ++i) {
			main_run();
		}
	}

	public static void main(String[] argv) {
		int sz = DEFSIZE;
		if (argv.length > 0) {
			sz = Integer.parseInt(argv[1]);
		}
		SOR demo = new SOR("SOR", SCREENX, SCREENY, sz);
		demo.setVisible(true);
	}

}
