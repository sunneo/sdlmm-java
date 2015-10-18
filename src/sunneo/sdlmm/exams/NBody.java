package sunneo.sdlmm.exams;

import sunneo.sdlmm.implement.SDLMMFrame;
import sunneo.sdlmm.interfaces.SDLMMInterface;

public class NBody extends SDLMMFrame {
	private static final long serialVersionUID = 1L;
	static final int SCREENX = 1024;
	static final int SCREENY = 768;
	final static int LOOP = 500;
	final static int NUM_BODY = 2000;
	final static int MAX_X_axis = 1000;
	final static int MIN_X_axis = 0;
	final static int MAX_Y_axis = 1000;
	final static int MIN_Y_axis = 0;
	final static int MAX_Velocity = 200;
	final static int MIN_velocity = -200;
	final static int MAX_Mass = 150;
	final static int MIN_Mass = 3;
	boolean mouseIsOn = false;
	int SZ = NUM_BODY;
	boolean showhelp = true;
	int showmode = 3;
	float simulatetime_factor = 0.01f;
	boolean random_simulatefactor = true;
	boolean centralize = false;

	float[] X_axis, Y_axis, Z_axis, X_Velocity, Y_Velocity, Z_Velocity, Mass;
	float[] newX_velocity, newY_velocity, newZ_velocity;

	public NBody(String title, int width, int height, int sz) {
		super(title, width, height);
		this.SZ = sz;
	}

	double getDoubleTime() {
		return (double) System.currentTimeMillis();
	}

	int rand() {
		return (int) (Math.random() * 65536);
	}

	float clamp(float v, float minv, float maxv) {
		if (v > maxv)
			v = (v + maxv) / 2;
		if (v < minv)
			v = (v + minv) / 2;
		return v;
	}

	float[] allocateBody() {
		return new float[SZ];
	}

	void Init_AllBody() {
		int i = 0;
		for (i = 0; i < SZ; i++) {
			X_axis[i] = rand() % (MAX_X_axis - MIN_X_axis) + MIN_X_axis;
			Y_axis[i] = rand() % (MAX_Y_axis - MIN_Y_axis) + MIN_Y_axis;
			Z_axis[i] = rand() % (MAX_Y_axis - MIN_Y_axis) + MIN_Y_axis;
			X_Velocity[i] = newX_velocity[i] = 0;
			Y_Velocity[i] = newY_velocity[i] = 0;
			Z_Velocity[i] = newZ_velocity[i] = 0;
			Mass[i] = rand() % (MAX_Mass - MIN_Mass) + MIN_Mass;
		}
	}

	void Nbody(int i, int sz, float[] X_axis, float[] Y_axis, float[] Z_axis, float[] newX_velocity,
			float[] newY_velocity, float[] newZ_velocity, final float[] Mass, float simulatetime) {
		int j;
		float sumX = 0, sumY = 0, sumZ = 0;
		final float Gravity_Coef = 3.3f;
		for (j = 0; j < sz; j++) {
			float X_position, Y_position, Z_position;
			float Distance;
			float Force = 0;
			if (j == i)
				continue;
			X_position = X_axis[j] - X_axis[i];
			Y_position = Y_axis[j] - Y_axis[i];
			Z_position = Z_axis[j] - Z_axis[i];
			Distance = (float) Math.sqrt(X_position * X_position + Y_position * Y_position + Z_position * Z_position);
			if (Distance == 0)
				continue;
			Force = Gravity_Coef * Mass[i] / (Distance * Distance);
			sumX += Force * X_position;
			sumY += Force * Y_position;
			sumZ += Force * Z_position;
		}
		newX_velocity[i] += sumX * simulatetime;
		newY_velocity[i] += sumY * simulatetime;
		newZ_velocity[i] += sumZ * simulatetime;
		X_axis[i] += clamp(newX_velocity[i], MIN_velocity, MAX_Velocity) * simulatetime;
		Y_axis[i] += clamp(newY_velocity[i], MIN_velocity, MAX_Velocity) * simulatetime;
		Z_axis[i] += clamp(newZ_velocity[i], MIN_velocity, MAX_Velocity) * simulatetime;
		X_Velocity[i] = newX_velocity[i];
		Y_Velocity[i] = newY_velocity[i];
		Z_Velocity[i] = newZ_velocity[i];
	}

	void drawBodys(int loop, int totalLoop, double tm, float avgX, float avgY, float avgZ) {

		int i;
		double rendert1, rendert2;
		String buf;
		rendert1 = getDoubleTime();
		fillRect(0, 0, SCREENX, SCREENY, 0xff2f2f2f);
		for (i = 0; i < SZ; ++i) {
			int x, y, r, c;
			if (centralize) {
				x = (int) (SCREENX / 2 * (X_axis[i] / avgX));
				y = (int) (SCREENY / 2 * (Y_axis[i] / avgY));
			} else {
				x = (int) (avgX - X_axis[i] + SCREENX / 2);
				y = (int) (avgY - Y_axis[i] + SCREENY / 2);
			}
			r = (int) (5 * (Z_axis[i] / avgZ));
			if (r < 0 || r > 255)
				continue;
			if (showmode == 0) {
				drawCircle(x, y, r, 0xff00D0D0 | (r * 0xa0a000));
			} else if (showmode == 1) {
				drawPixel(x, y, 0xff00D0D0 | (r * 0xa0a000));
			} else if (showmode == 2) {
				drawPixel(x, y, 0xffffffff);
				drawCircle(x, y, r, 0xff00D0D0 | (r * 0xa0a000));
			} else if (showmode == 3) {
				fillCircle(x, y, r, 0xff00D0D0 | (r * 0xa0a000));
			}
		}
		if (showhelp) {
			buf = String.format("[%-3d/%-3d] tm:%-3.3f", loop, totalLoop, tm);
			drawString(buf, 0, 0, 0xffffffff);
			buf = String.format("show mode: %-2d[0-3]", showmode);
			drawString(buf, 0, 20, 0xffffffff);
			buf = String.format("simulate factor: %-3.5f", simulatetime_factor);
			drawString(buf, 0, 40, 0xffffffff);
			buf = String.format("randomize simulate factor: %s[r]", random_simulatefactor ? "on" : "off");
			drawString(buf, 0, 60, 0xffffffff);
			fillRect(320, 60, (int) (400 * (simulatetime_factor / 2)), 20, 0xfffdfd00);
			drawRect(320, 60, 400, 20, 0xffffffff);
			buf = String.format("centralize: %s[c]", centralize ? "on" : "off");
			drawString(buf, 0, 80, 0xffffffff);
		}
		rendert2 = getDoubleTime();
		flush();
		tm += (rendert2 - rendert1);

		if (tm < 1.0 / 60) {
			sleep((int) ((1.0 / 60) * 1000 - tm));
		}

	}

	private void main_run() {
		int loop;
		double tmstart, tmend;
		double totalTime = 0.0;
		double fps_time_1, fps_time_2;
		float avgX = 0, avgY = 0, avgZ = 0;
		tmstart = getDoubleTime();
		Init_AllBody();
		for (loop = 0; loop < LOOP; loop++) {
			int i;
			// printf("loop %d (%f,%f)\n",loop,avgX,avgY);
			avgX = 0;
			avgY = 0;
			avgZ = 0;
			fps_time_1 = getDoubleTime();
			for (i = 0; i < SZ; i++) {
				Nbody(i, SZ, X_axis, Y_axis, Z_axis, newX_velocity, newY_velocity, newZ_velocity, Mass,
						simulatetime_factor);
				avgX += X_axis[i];
				avgY += Y_axis[i];
				avgZ += Z_axis[i];
			}
			avgX /= SZ;
			avgY /= SZ;
			avgZ /= SZ;
			fps_time_2 = getDoubleTime();
			drawBodys(loop, LOOP, fps_time_2 - fps_time_1, avgX, avgY, avgZ);
		}
		tmend = getDoubleTime();
		System.out.printf("%d %f\n", NUM_BODY, tmend - tmstart);
		if (random_simulatefactor) {
			simulatetime_factor = (float) Math.random();
		}

	}

	SDLMMInterface.OnKeyboardListener kbfnc = new SDLMMInterface.OnKeyboardListener() {

		@Override
		public void onkey(int key, boolean shift, boolean ctrl, boolean alt, boolean ison) {
			if (!ison)
				return;
			switch (key) {
			case '0':
			case '1':
			case '2':
			case '3':
				showmode = key - '0';
				break;
			case 'c':
			case 'C':
				centralize = !centralize;
				break;
			case 'r':
			case 'R':
				random_simulatefactor = !random_simulatefactor;
				break;
			case 'h':
			case 'H':
				showhelp = !showhelp;
				break;
			}
		}
	};
	SDLMMInterface.OnMousePressListener mousefnc = new SDLMMInterface.OnMousePressListener() {

		@Override
		public void onClick(int x, int y, int btn, boolean ison) {
			mouseIsOn = ison;
			if (!ison)
				return;
			if (y > 60 && y < 80 && x >= 320 && x <= 320 + 400) {
				float value = (float) (2.0 * ((float) (x - 320)) / 400);
				simulatetime_factor = value;
			}
		}
	};

	SDLMMInterface.OnMouseMotionListener mousemotion = new SDLMMInterface.OnMouseMotionListener() {

		@Override
		public void onMove(int x, int y) {
			if (!mouseIsOn)
				return;
			if (y > 60 && y < 80 && x >= 320 && x <= 320 + 400) {
				float value = (float) (2.0 * ((float) (x - 320)) / 400);
				simulatetime_factor = value;
			}
		}
	};

	@Override
	public void run() {
		int i;
		X_axis = allocateBody();
		Y_axis = allocateBody();
		Z_axis = allocateBody();
		X_Velocity = allocateBody();
		Y_Velocity = allocateBody();
		Z_Velocity = allocateBody();
		Mass = allocateBody();
		newX_velocity = allocateBody();
		newY_velocity = allocateBody();
		newZ_velocity = allocateBody();
		this.setTextFont("Consolas-18");
		setOnKeyboard(kbfnc);
		this.setOnMousePress(mousefnc);
		setOnMouseMotion(mousemotion);

		for (i = 0; i < 20; ++i) {
			main_run();
		}

	}

	public static void main(String[] argv) {
		int sz = NUM_BODY;
		if (argv.length > 0) {
			sz = Integer.parseInt(argv[1]);
		}
		NBody demo = new NBody("NBody-Simulation", SCREENX, SCREENY, sz);
		demo.setVisible(true);
	}
}
