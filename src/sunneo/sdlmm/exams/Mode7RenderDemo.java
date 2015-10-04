package sunneo.sdlmm.exams;

import java.io.IOException;

import sunneo.sdlmm.implement.SDLMMFrame;
import sunneo.sdlmm.interfaces.SDLMMInterface;

public class Mode7RenderDemo extends SDLMMFrame {
	private static final long serialVersionUID = 1L;
	static final int width = 1024;
	static final int height = 768;
	static int[] bg;
	static int[] bg_small;
	private int mx, my, vx = 1440, vy = 1440, bh, bw, ox, oy;
	private boolean UP, LEFT, DOWN, RIGHT, TLEFT, TRIGHT;
	static final int VK_UP = 38;
	static final int VK_LEFT = 37;
	static final int VK_DOWN = 40;
	static final int VK_RIGHT = 39;
	double angle = 0;

	public Mode7RenderDemo(String title, int width, int height) {
		super(title, width, height);
		init();
	}

	SDLMMInterface.OnMouseMotionListener mouse = new SDLMMInterface.OnMouseMotionListener() {

		@Override
		public void onMove(int x, int y) {
			ox = mx;
			oy = my;
			mx = x;
			my = y;
		}
	};
	SDLMMInterface.OnKeyboardListener kb = new SDLMMInterface.OnKeyboardListener() {

		@Override
		public void onkey(int key, boolean shift, boolean ctrl, boolean alt, boolean o) {
			switch (key) {
			case VK_UP:	  case 'W': case 'w': UP = o;	break;
			case VK_LEFT: case 'A':	case 'a': LEFT = o; break;
			case VK_DOWN: case 'S': case 's': DOWN = o; break;
			case VK_RIGHT: case 'D':case 'd': RIGHT = o;break;
			case 'O': case 'o': TLEFT = o;break;
			case 'P': case 'p':	TRIGHT = o; break;
			}
		}
	};

	private void handlekb() {
		final double delta = 5;
		int deltaX = Math.abs(ox - mx);
		if (ox < mx)TLEFT = true;
		else if (ox > mx)TRIGHT = true;
		// First Shooting View
		if (UP || DOWN) {
			double sY = Math.cos(Math.abs(angle)) * delta;
			double sX = Math.sin(Math.abs(angle)) * delta;
			vx += sX * (DOWN ? 1 : -1);
			vy += sY * (DOWN ? -1 : 1);
		}
		if (LEFT || RIGHT) {
			double sY = Math.cos(Math.abs(angle)) * delta;
			double sX = Math.sin(Math.abs(angle)) * delta;
			vx -= sY * (LEFT ? -1 : 1);
			vy -= sX * (LEFT ? -1 : 1);
		}

		if (vy < 100)vy = 100;
		if (vy > bh - 100) vy = bh - 100;
		if (vx > bw - 100) vx = bw - 100;
		if (vx < 100) vx = 100;

		if (TLEFT)	angle +=deltaX * 0.01 * delta;
		if (TRIGHT)	angle -=deltaX * 0.01 * delta;
		if (ox == mx)TLEFT = TRIGHT = false;
		if (angle > 2 * Math.PI)angle -= Math.PI * 2;
		if (angle < 0)angle = Math.PI * 2 - angle;
		ox = mx;
	}

	public void drawfnc() {
		double viewY = Math.cos(-angle);
		double viewX = Math.sin(-angle);
		int mapCenterX = (int) (width - 400 + 400 * (((double) vx) / bw));
		int mapCenterY = (int) (400 * (((double) vy) / bh));
		fillRect(0, 0, width, height, 0xffcccccc);
		mode7Render(angle, vx, vy, bg, bw, bh, 0, height / 2, width, 500);

		drawPixels(bg_small, width - 400, 0, 400, 400);
		fillCircle(mapCenterX, mapCenterY, 5, 0xffffffff);
		fillCircle(mapCenterX, mapCenterY, 3, 0xff0000ff);
		drawLine(mapCenterX, mapCenterY, (int) (mapCenterX + viewX * 15), (int) (mapCenterY + viewY * 15), 0xffffff00);
		drawLine(mapCenterX + 2, mapCenterY, (int) (mapCenterX + viewX * 15), (int) (mapCenterY + viewY * 15),0xffff0000);
		drawLine(mapCenterX - 2, mapCenterY, (int) (mapCenterX + viewX * 15), (int) (mapCenterY + viewY * 15),0xffffff00);

		fillRect(0, 0, 300, 22, 0xffffffff);
		drawString(String.format("%-4d,%-4d,vx=%-3d,vy=%-3d,angle=%-5.2f", mx, my, vx, vy, angle), 0, 0, 0x0);
		flush();
		sleep(5);
	}

	private void init() {
		try {
			int[] sizes = this.readImageSize("g.bmp");
			this.bg = this.readImage("g.bmp");
			this.bg = this.stretchImage(this.bg, sizes[0], sizes[1], 2880, 2880);
			this.bw = 2880;
			this.bh = 2880;
			this.bg_small = this.readImage("g.bmp");
			this.bg_small = this.stretchImage(this.bg_small, this.bw, this.bh, 400, 400);
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.setOnKeyboard(kb);
		this.setOnMouseMotion(mouse);
	}


	@Override
	public void run() {
		while (true) {
			handlekb();
			drawfnc();
		}
	}
	public static void main(String[] argv) {
		Mode7RenderDemo demo = new Mode7RenderDemo("Mode7 Render Demo", 1024, 768);
		demo.setVisible(true);
	}
}
