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
		return GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getAvailableFontFamilyNames();
	}

	private static class SDLMMPanelImpl extends SDLMMPanel {
		BufferedImage[] image = new BufferedImage[2];
		Graphics[] graphics = new Graphics[2];
		int toDraw = 0;

		public SDLMMPanelImpl(int width, int height, boolean doubleBuffered) {
			setSize(width, height);
			this.doubleBuffered = doubleBuffered;
			image[0] = new BufferedImage(width, height,
					BufferedImage.TYPE_4BYTE_ABGR);
			image[1] = new BufferedImage(width, height,
					BufferedImage.TYPE_4BYTE_ABGR);
			graphics[0] = image[0].createGraphics();
			graphics[1] = image[1].createGraphics();
			this.addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent arg0) {
					if (onKeyboard != null) {
						onKeyboard.onkey(arg0.getKeyCode(), arg0.isShiftDown(),
								arg0.isControlDown(), arg0.isAltDown(), true);
					}

				}

				@Override
				public void keyReleased(KeyEvent arg0) {
					if (onKeyboard != null) {
						onKeyboard.onkey(arg0.getKeyCode(), arg0.isShiftDown(),
								arg0.isControlDown(), arg0.isAltDown(), false);
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
						onMouseClick.onBtn(arg0.getX(), arg0.getY(),
								arg0.getButton());
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
						onMousePress.onClick(arg0.getX(), arg0.getY(),
								arg0.getButton(), true);
					}

				}

				@Override
				public void mouseReleased(MouseEvent arg0) {
					if (onMousePress != null) {
						onMousePress.onClick(arg0.getX(), arg0.getY(),
								arg0.getButton(), false);
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
			image[toDraw].getRaster().setPixels(x, y, width, height, pixels);
			if (!this.isDoubleBuffered()) {
				this.repaint();
			}
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
				g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), 0, 0,
						this.getWidth(), this.getHeight(), null);
			}
		}

		@Override
		public int[] readImage(String filename) throws IOException {
			BufferedImage image = javax.imageio.ImageIO
					.read(new File(filename));
			int[] ret = new int[image.getWidth() * image.getHeight()];
			return image.getRaster().getPixels(0, 0, image.getWidth(),
					image.getHeight(), ret);
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

	}

	public static SDLMMPanel newInstance(int width, int height,
			boolean isdoubleBuffered) {
		return new SDLMMPanelImpl(width, height, isdoubleBuffered);
	}
}
