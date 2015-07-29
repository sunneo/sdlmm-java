package sunneo.sdlmm.implement;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import sunneo.sdlmm.interfaces.SDLMMInterface;


public abstract class SDLMMFrame extends JFrame implements Runnable,
        SDLMMInterface {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    SDLMMPanel panel;

    public SDLMMFrame(String title, int width, int height) {

        super(title);

        messageBox = new MessageBoxInterface(this);
        panel = SDLMMPanel.newInstance(width, height, true);
        this.setSize(width, height);
        this.setContentPane(panel);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                System.exit(0);
            }
        });
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        Thread th = new Thread(this);
        th.start();
    }

    @Override
    public int[] readImage(String filename) throws IOException {
        return panel.readImage(filename);
    }

    @Override
    public void setFont(String f) {
        panel.setFont(f);
    }

    @Override
    public void setOnMouseMotion(OnMouseMotionListener l) {
        panel.setOnMouseMotion(l);
    }

    @Override
    public void setOnMouseClick(OnMouseClickListener l) {
        panel.setOnMouseClick(l);
    }

    @Override
    public void setOnMousePress(OnMousePressListener l) {
        panel.setOnMousePress(l);
    }

    @Override
    public void setOnKeyboard(OnKeyboardListener l) {
        panel.setOnKeyboard(l);
    }

    @Override
    public void drawString(String str, int x, int y, int color) {
        panel.drawString(str, x, y, color);
    }

    @Override
    public void drawPixel(int x, int y, int color) {
        panel.drawPixel(x, y, color);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, int color) {
        panel.drawLine(x1, y1, x2, y2, color);
    }

    @Override
    public void drawCircle(int x, int y, int r, int color) {
        panel.drawCircle(x, y, r, color);
    }

    @Override
    public void drawRect(int left, int top, int width, int height, int color) {
        panel.drawRect(left, top, width, height, color);
    }

    @Override
    public void fillRect(int left, int top, int width, int height, int color) {
        panel.fillRect(left, top, width, height, color);
    }

    @Override
    public void fillCircle(int x, int y, int r, int color) {
        panel.fillCircle(x, y, r, color);
    }

    @Override
    public void drawPixels(int[] pixels, int x, int y, int width, int height) {
        panel.drawPixels(pixels, x, y, width, height);
    }

    @Override
    public void flush() {
        panel.flush();
    }

    @Override
    public void sleep(int millis) {
        panel.sleep(millis);
    }

    public class MessageBoxInterface {
        SDLMMFrame parent;

        public void ok(String title, String msg) {
            JOptionPane.showMessageDialog(parent, msg, title, JOptionPane.INFORMATION_MESSAGE);
        }

        public boolean okCancel(String title, String msg) {
            return JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(parent, msg, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.CANCEL_OPTION);
        }

        public MessageBoxInterface(SDLMMFrame parent) {
            this.parent = parent;
        }

    }

    public MessageBoxInterface messageBox;

    public boolean isHit(int x, int y, int left, int top, int width, int height) {
        return (x >= left && x <= left + width)
                && (y >= top && y <= top + height);
    }

    public SDLMMInterface getInterface() {
        return this;
    }
}
