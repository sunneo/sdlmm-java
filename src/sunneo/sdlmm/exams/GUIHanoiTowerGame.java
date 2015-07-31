package sunneo.sdlmm.exams;

import sunneo.sdlmm.implement.SDLMMFrame;

public class GUIHanoiTowerGame extends SDLMMFrame {

	static final long serialVersionUID = 1L;
	final static int SCREEN_X = 500;
	final static int SCREEN_Y = 400;

	public static boolean checkHanoiEnd(int[] c) {
		int len = c.length;
		int maxValue = len + 1;
		int[] cloneC = new int[len];
		for (int i = 0; i < c.length; ++i) {
			cloneC[i] = maxValue;
			--maxValue;
		}
		for (int i = 0; i < c.length; ++i) {
			if (c[i] != cloneC[i]) {
				return false;
			}
		}
		return true;
	}

	public void drawOneHanoi(int[] arr, int x, int y, int widthPerStack,
			int heightPerStack, int stickWidth) {
		fillRect(x, y, stickWidth, arr.length * heightPerStack, 0xff00ffff);
		int topIdx = getTopIndex(arr);
		int bottom = y + arr.length * heightPerStack;
		if (topIdx == -1)
			return;
		for (int i = 0; i <= topIdx; ++i) {
			int width = widthPerStack * arr[i];
			fillRect(x - width / 2, bottom - heightPerStack * (i + 1), width,
					heightPerStack, 0xff000000 + 0xff123 * arr[i]);
		}
	}

	public static int[] selectTower(int selection, int[] a, int[] b, int[] c) {
		switch (selection) {
		case 0:
			return a;
		case 1:
			return b;
		case 2:
			return c;
		}
		return null;
	}

	public static int getTopIndex(int[] tower) {
		for (int i = tower.length - 1; i >= 0; --i) {
			if (tower[i] != 0) {
				return i;
			}
		}
		return -1;
	}

	public static void moveHanoi(int from, int to, int[] a, int[] b, int[] c) {
		int[] fromArray = selectTower(from, a, b, c);

		// ��from��ܨӷ�fromArray
		// ��to��ܥؼ�toArray
		int[] toArray = selectTower(to, a, b, c);
		// �ˬd�ӷ��O�_�Ū�,�Ū��N���h��
		// ���Xfrom�����ݦ�m
		int fromTopIdx = getTopIndex(fromArray);
		if (fromTopIdx == -1) {
			return;
		}
		int topValue = fromArray[fromTopIdx];
		// ���Xto�����ݦ�m
		int toTopIdx = getTopIndex(toArray);
		// �ˬdfromArray���ݤ��e�O�_�W�LtoArray������,�W�L�N���X�W�h
		if (toTopIdx != -1) {
			if (topValue > toArray[toTopIdx]) {
				return;
			}
			fromArray[fromTopIdx] = 0;
			toArray[toTopIdx + 1] = topValue;
		} else {
			fromArray[fromTopIdx] = 0;
			toArray[toTopIdx + 1] = topValue;
		}
		// �NfromArray���ݳ]��0 ��Ȳ��ʨ�toArray����+1��m

	}

	public void showMessage(String str) {
		fillRect(0, 0, 200, 30, 0xffffffff);
		drawString(str, 0, 0, 0xff000000);
	}

	int from = -1;
	int to = -1;
	volatile int currentIdx = 0;
	boolean waitSelection = false;
	volatile int pressKey = 0;

	private int showSelection(int[] xCenters, int y) {
		if (!waitSelection)
			return -1;
		int centerX = xCenters[currentIdx];
		int leftX = xCenters[currentIdx] - 10;
		int rightX = xCenters[currentIdx] + 10;
		drawLine(centerX, y, leftX, y + 20, 0xff0000ff);
		drawLine(leftX, y + 20, rightX, y + 20, 0xff0000ff);
		drawLine(centerX, y, rightX, y + 20, 0xff0000ff);
		switch (pressKey) {
		case 37: // left
			currentIdx = currentIdx - 1;
			if (currentIdx < 0)
				currentIdx = 2;
			break;
		case 39: // right
			currentIdx = currentIdx + 1;
			if (currentIdx > 2)
				currentIdx = 0;
			break;
		case 10:// enter
		case 32:// space
			waitSelection = false;
			pressKey = 0;
			return currentIdx;
		case '1':
		case '2':
		case '3':
			currentIdx = pressKey - '0';
			break;
		}
		pressKey = 0;
		return -1;

	}

	private int showSelection() {
		int[] xCenters = { 100, 250, 400 };
		return showSelection(xCenters, 250);
	}

	public void drawHanois(int[] a, int[] b, int[] c) {
		fillRect(0, 0, SCREEN_X, SCREEN_Y, 0xffffffff);
		drawOneHanoi(a, 100, 100, 25, 20, 5);
		drawOneHanoi(b, 250, 100, 25, 20, 5);
		drawOneHanoi(c, 400, 100, 25, 20, 5);
	}

	public void run() {
		int[] a = new int[] { 5, 4, 3, 2, 1 };
		int[] b = new int[] { 0, 0, 0, 0, 0 };
		int[] c = new int[] { 0, 0, 0, 0, 0 };
		while (!checkHanoiEnd(c)) {
			drawHanois(a, b, c);
			showMessage("�п�ܷh�ʪ��ӷ�");
			if (from == -1) {
				waitSelection = true;
				from = showSelection();
				sleep(1);
				flush();
				continue;
			}
			int n1 = from;
			showMessage("�n�h����h ");
			if (to == -1) {
				waitSelection = true;
				to = showSelection();
				sleep(1);
				flush();
				continue;
			}
			int n2 = to;
			moveHanoi(n1, n2, a, b, c);
			from = -1;
			to = -1;
		}
	}

	public GUIHanoiTowerGame() {
		super("Hanoi", SCREEN_X, SCREEN_Y);
		setTextFont("�s�ө���-bold-20");
		this.setOnKeyboard(new OnKeyboardListener() {
			@Override
			public void onkey(int key, boolean shift, boolean ctrl,
					boolean alt, boolean ison) {
				if (ison)
					pressKey = key;
			}
		});
	}

	public static void main(String[] argv) {
		GUIHanoiTowerGame game = new GUIHanoiTowerGame();
		game.setVisible(true);
	}
}
