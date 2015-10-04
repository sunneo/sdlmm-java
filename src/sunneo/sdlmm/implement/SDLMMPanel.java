package sunneo.sdlmm.implement;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;

import sunneo.sdlmm.interfaces.SDLMMInterface;

public abstract class SDLMMPanel extends JPanel implements SDLMMInterface {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	boolean doubleBuffered;
	OnMouseMotionListener onMouseMotion;
	OnMousePressListener onMousePress;
	OnMouseClickListener onMouseClick;
	OnKeyboardListener onKeyboard;

	public void setOnMouseMotion(OnMouseMotionListener l) {
		this.onMouseMotion = l;

	}

	public void setOnMouseClick(OnMouseClickListener l) {
		this.onMouseClick = l;
	}

	public void setOnMousePress(OnMousePressListener l) {
		this.onMousePress = l;
	}

	public void setOnKeyboard(OnKeyboardListener l) {
		this.onKeyboard = l;
	}

	public boolean isDoubleBuffered() {
		return doubleBuffered;
	}

	public void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void setTextFont(String f) {
		this.setTextFont(Font.decode(f));
	}

	public String[] listFonts() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	}

	private static class SDLMMPanelImpl extends SDLMMPanel {
		BufferedImage[] image = new BufferedImage[2];
		Graphics[] graphics = new Graphics[2];
		int toDraw = 0;

		public SDLMMPanelImpl(int width, int height, boolean doubleBuffered) {
			setSize(width, height);
			this.doubleBuffered = doubleBuffered;
			image[0] = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			image[1] = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			graphics[0] = image[0].createGraphics();
			graphics[1] = image[1].createGraphics();
			this.addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent arg0) {
					if (onKeyboard != null) {
						onKeyboard.onkey(arg0.getKeyCode(), arg0.isShiftDown(), arg0.isControlDown(), arg0.isAltDown(),
								true);
					}

				}

				@Override
				public void keyReleased(KeyEvent arg0) {
					if (onKeyboard != null) {
						onKeyboard.onkey(arg0.getKeyCode(), arg0.isShiftDown(), arg0.isControlDown(), arg0.isAltDown(),
								false);
					}

				}

				@Override
				public void keyTyped(KeyEvent arg0) {

				}

			});
			this.setFocusable(true);
			this.addMouseMotionListener(new MouseMotionListener() {

				@Override
				public void mouseDragged(MouseEvent arg0) {
					if (onMouseMotion != null) {
						onMouseMotion.onMove(arg0.getX(), arg0.getY());
					}
				}

				@Override
				public void mouseMoved(MouseEvent arg0) {
					if (onMouseMotion != null) {
						onMouseMotion.onMove(arg0.getX(), arg0.getY());
					}

				}

			});
			this.addMouseListener(new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent arg0) {
					if (onMouseClick != null) {
						onMouseClick.onBtn(arg0.getX(), arg0.getY(), arg0.getButton());
					}
				}

				@Override
				public void mouseEntered(MouseEvent arg0) {

				}

				@Override
				public void mouseExited(MouseEvent arg0) {

				}

				@Override
				public void mousePressed(MouseEvent arg0) {
					if (onMousePress != null) {
						onMousePress.onClick(arg0.getX(), arg0.getY(), arg0.getButton(), true);
					}

				}

				@Override
				public void mouseReleased(MouseEvent arg0) {
					if (onMousePress != null) {
						onMousePress.onClick(arg0.getX(), arg0.getY(), arg0.getButton(), false);
					}

				}

			});
		}

		@Override
		public void drawPixel(int x, int y, int color) {
			graphics[toDraw].setColor(new Color(color, true));
			graphics[toDraw].drawLine(x, y, x, y);
			if (!this.isDoubleBuffered()) {
				this.repaint();
			}
		}

		@Override
		public void drawLine(int x1, int y1, int x2, int y2, int color) {
			graphics[toDraw].setColor(new Color(color, true));
			graphics[toDraw].drawLine(x1, y1, x2, y2);
			if (!this.isDoubleBuffered()) {
				this.repaint();
			}
		}

		@Override
		public void drawCircle(int x, int y, int r, int color) {
			graphics[toDraw].setColor(new Color(color, true));
			graphics[toDraw].drawOval(x - r, y - r, 2 * r, 2 * r);
			if (!this.isDoubleBuffered()) {
				this.repaint();
			}
		}

		@Override
		public void drawRect(int left, int top, int width, int height, int color) {
			graphics[toDraw].setColor(new Color(color, true));
			graphics[toDraw].drawRect(left, top, width, height);
			if (!this.isDoubleBuffered()) {
				this.repaint();
			}
		}

		@Override
		public void fillRect(int left, int top, int width, int height, int color) {
			graphics[toDraw].setColor(new Color(color, true));
			graphics[toDraw].fillRect(left, top, width, height);
			if (!this.isDoubleBuffered()) {
				this.repaint();
			}
		}

		@Override
		public void fillCircle(int x, int y, int r, int color) {
			graphics[toDraw].setColor(new Color(color, true));
			graphics[toDraw].fillOval(x - r, y - r, 2 * r, 2 * r);
			if (!this.isDoubleBuffered()) {
				this.repaint();
			}

		}

		@Override
		public void drawPixels(int[] pixels, int x, int y, int width, int height) {
			image[toDraw].setRGB(x, y, width, height, pixels, 0, width);

			if (!this.isDoubleBuffered()) {
				this.repaint();
			}
		}

		public void mode7Render(double angle, int vx, int vy, int[] pixels, int bw, int bh, int x, int y, int w,
				int h) {
			mode7render_internal(0.5, 1.5, 2, 1, angle, vx, vy, pixels, bw, bh, x, y, w, h);
		}

		public void mode7render_internal(double groundFactor, double xFac, double yFac, int scanlineJump, double angle,
				int vx, int vy, int[] bg, int bw, int bh, int tx, int ty, int w, int h) {
			if (tx + w >= this.getWidth()) {
				w = this.getWidth() - tx;
			}
			if (ty + h > this.getHeight()) {
				h = this.getHeight() - ty;
			}
			int[] toDraw = new int[w * h];
			double ca, sa, can, san;
			int lev = w / scanlineJump;
			int x;
			ca = Math.cos(angle) * 48 * groundFactor * xFac;
			sa = Math.sin(angle) * 48 * groundFactor * xFac;
			can = Math.cos(angle + 3.1415926 / 2) * 16 * groundFactor * yFac;
			san = Math.sin(angle + 3.1415926 / 2) * 16 * groundFactor * yFac;
			for (x = 0; x < lev; ++x) {
				int y;
				double xr = -(((float) x / lev) - 0.5);
				double cax = (ca * xr) + can;
				double sax = (sa * xr) + san;
				for (y = 0; y < h; ++y) {
					double zf = ((float) h) / y;
					int xd = (int) (vx + zf * cax);
					int yd = (int) (vy + zf * sax);
					if (yd < bh && xd < bw && yd > 0 && xd > 0) {

						toDraw[y * w + x] = bg[yd * bw + xd];
					}
				}
			}

			drawPixels(toDraw, tx, ty, w, h);
		}

		@Override
		public void flush() {
			if (!this.isDoubleBuffered()) {
				return;
			}
			toDraw = (toDraw + 1) % 2;
			this.repaint();

		}

		public void paint(Graphics g) {
			// super.paint(g);
			synchronized (g) {
				Image img = null;
				if (this.isDoubleBuffered()) {
					img = image[(toDraw + 1) % 2];
				} else {
					img = image[toDraw];
				}
				g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), 0, 0, this.getWidth(), this.getHeight(),
						null);
			}
		}

		public int[] readImageSize(String filename) throws IOException {
			int[] ret = new int[2];
			BufferedImage image = javax.imageio.ImageIO.read(new File(filename));
			ret[0] = image.getWidth();
			ret[1] = image.getHeight();
			return ret;
		}

		@Override
		public int[] readImage(String filename) throws IOException {
			BufferedImage image = javax.imageio.ImageIO.read(new File(filename));
			int[] ret = new int[image.getWidth() * image.getHeight()];
			image.getRGB(0, 0, image.getWidth(), image.getHeight(), ret, 0, image.getWidth());
			return ret;
		}

		public int[] stretchImage(int[] pixels, int w, int h, int w2, int h2) {
			int[] output = new int[w2 * h2];
			float dw = ((float) w2) / w;
			float dh = ((float) h2) / h;
			int ii, e;
			e = h2 * w2;
			for (ii = 0; ii < e; ii += 1) {
				int origi, origj;
				float i = ii % w2;
				float j = ii / w2;
				origi = (int) (i / dw);
				origj = (int) (j / dh);
				output[(int) (j * w2 + i)] = pixels[(int) (origj * w + origi)];

			}
			return output;
		}

		@Override
		public void drawString(String str, int x, int y, int color) {
			graphics[toDraw].setColor(new Color(color));
			int size = graphics[toDraw].getFont().getSize();
			graphics[toDraw].drawString(str, x, y + size);
			if (!this.isDoubleBuffered()) {
				this.repaint();
			}
		}

		@Override
		public void setTextFont(Font f) {

			if (graphics != null) {
				if (graphics[toDraw] != null) {
					graphics[toDraw].setFont(f);
				}
				if (graphics[(toDraw + 1) % 2] != null) {
					graphics[(toDraw + 1) % 2].setFont(f);
				}
			}

		}

		@Override
		public SDLMMInterface getInterface() {
			return this;
		}

		@Override
		public void drawPolygon(int[] x, int[] y, int color) {
			graphics[toDraw].setColor(new Color(color, true));
			graphics[toDraw].drawPolygon(x, y, Math.min(x.length, y.length));
			if (!this.isDoubleBuffered()) {
				this.repaint();
			}
		}

		@Override
		public void fillPolygon(int[] x, int[] y, int color) {
			graphics[toDraw].setColor(new Color(color, true));
			graphics[toDraw].fillPolygon(x, y, Math.min(x.length, y.length));
			if (!this.isDoubleBuffered()) {
				this.repaint();
			}
		}

	}

	public static SDLMMPanel newInstance(int width, int height, boolean isdoubleBuffered) {
		return new SDLMMPanelImpl(width, height, isdoubleBuffered);
	}
}
