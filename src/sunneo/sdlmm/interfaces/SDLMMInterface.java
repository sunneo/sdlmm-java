package sunneo.sdlmm.interfaces;

import java.awt.Font;
import java.io.IOException;

public interface SDLMMInterface {

    public static interface OnMouseMotionListener {
        public void onMove(int x, int y);
    }

    public static interface OnMousePressListener {
        public void onClick(int x, int y, int btn, boolean ison);
    }

    public static interface OnMouseClickListener {
        public void onBtn(int x, int y, int btn);
    }

    public static interface OnKeyboardListener {
        public void onkey(int key, boolean shift, boolean ctrl, boolean alt,
                boolean ison);
    }

    public SDLMMInterface getInterface();

    /**
     * read BMP image from file
     * 
     * @param filename
     *            full path of file
     * @return an array that is consisted with pixels
     * @throws IOException
     */
    public abstract int[] readImage(String filename) throws IOException;

    /**
     * set the current font for drawing string
     * 
     * @param f
     *            font object
     */
    public abstract void setFont(Font f);

    /**
     * set the curretn front by name for drawing string
     * 
     * @param f
     *            name of font
     * 
     */
    public void setFont(String f);

    /**
     * set mouse-motion event handler
     * 
     * @param l
     *            listener
     */
    public void setOnMouseMotion(OnMouseMotionListener l);

    /**
     * set mouse-click event handler
     * 
     * @param l
     *            listener
     */
    public void setOnMouseClick(OnMouseClickListener l);

    /**
     * set mouse-press event handler
     * 
     * @param l
     *            listener
     */
    public void setOnMousePress(OnMousePressListener l);

    /**
     * set keyboard event handler
     * 
     * @param l
     *            listener
     */
    public void setOnKeyboard(OnKeyboardListener l);

    /**
     * drawing string at specified position
     * 
     * @param str
     *            string content
     * @param x
     *            x position
     * @param y
     *            y position
     * @param color
     *            a int value which represent RGBA color code
     */
    void drawString(String str, int x, int y, int color);

    /**
     * drawing a pixel at specified position
     * 
     * @param str
     * @param x
     *            x position
     * @param y
     *            y position
     * @param color
     *            a int value which represent RGBA color code
     */
    void drawPixel(int x, int y, int color);

    /**
     * drawing a line (x1,y1) ~ (x2,y2)
     * 
     * @param x1
     *            start x
     * @param y1
     *            start y
     * @param x2
     *            destination x
     * @param y2
     *            destination y
     * @param color
     *            a int value which represent RGBA color code
     */
    void drawLine(int x1, int y1, int x2, int y2, int color);

    /**
     * drawing a circle with radius at specified axis
     * 
     * @param x
     *            x-center
     * @param y
     *            y-center
     * @param r
     *            radius
     * @param color
     *            a int value which represent RGBA color code
     */
    void drawCircle(int x, int y, int r, int color);

    /**
     * drawing a width x height rectangle at specified left-top position
     * 
     * @param left
     *            the most-left position of rectangle
     * @param top
     *            the most-top position of rectange
     * @param width
     *            width of rectangle
     * @param height
     *            height of rectangle
     * @param color
     *            a int value which represent RGBA color code
     */
    void drawRect(int left, int top, int width, int height, int color);

    /**
     * fill a region of rectangle at specified left-top position with color
     * 
     * @param left
     *            the most-left position of rectangle
     * @param top
     *            the most-top position of rectange
     * @param width
     *            width of rectangle
     * @param height
     *            height of rectangle
     * @param color
     *            a int value which represent RGBA color code
     */
    void fillRect(int left, int top, int width, int height, int color);

    /**
     * drawing a filled-circle with radius at specified axis
     * 
     * @param x
     *            x-center
     * @param y
     *            y-center
     * @param r
     *            radius
     * @param color
     *            a int value which represent RGBA color code
     */
    void fillCircle(int x, int y, int r, int color);

    /**
     * paste image onto screen at specified position
     * 
     * @param pixels
     * @param x
     *            most-left position
     * @param y
     *            most-top position
     * @param width
     *            width of image
     * @param height
     *            height of image
     */
    void drawPixels(int[] pixels, int x, int y, int width, int height);

    /**
     * update the panel (it is used for double-buffered panel_
     */
    void flush();

    /**
     * wait for milli milliseconds
     * 
     * @param millis
     *            count of milliseconds
     */
    void sleep(int millis);
}
