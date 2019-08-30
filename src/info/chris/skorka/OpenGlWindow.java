package info.chris.skorka;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.lang.*;
import java.util.Stack;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class OpenGlWindow {

    // The window handle
    private long window;

    private String title;

    private int width, height, scale;
    private long time_start = System.currentTimeMillis();
    private long time_last = System.currentTimeMillis();

    private DrawEventListener drawEventListener;
    private KeyboardEventListener keyboardEventListener;
    private MouseEventListener mouseEventListener;

    public OpenGlWindow(int width, int height, int scale, String title, DrawEventListener drawEventListener, KeyboardEventListener keyboardEventListener, MouseEventListener mouseEventListener){

        this.width = width;
        this.height = height;
        this.scale = scale;
        this.title = title;
        this.drawEventListener = drawEventListener;
        this.keyboardEventListener = keyboardEventListener;
        this.mouseEventListener = mouseEventListener;
//        this.open();
    }

    /**
     * Sets up and opens a window and begins the draw loop
     */
    public void open() {

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    /**
     * Initializes OpenGL and creates a window
     */
    private void init() {

        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(this.width * this.scale, this.height * this.scale, this.title, NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");
        System.out.printf("Window with size %dx%d at %dx%d resolution\n", this.width * this.scale, this.height * this.scale, this.width, this.height);

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop

            if(action == GLFW_PRESS){
                keyboardEventListener.onKeyDown(key);
            }

            if(action == GLFW_RELEASE){
                keyboardEventListener.onKeyUp(key);
            }

        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    /**
     * Loop to process redraws and events. Called repeatedly.
     */
    private void loop() {

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        // glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {

            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0.0, width * this.scale, 0.0, height * this.scale, 1.0, -1.0);
            glMatrixMode(GL_MODELVIEW);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            glClearColor(0,0,0,0);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glBegin(GL_QUADS);
            if(this.drawEventListener != null)
                drawEventListener.onDraw(new Context(this), System.currentTimeMillis() - time_start, System.currentTimeMillis() - time_last);
            glEnd();

            time_last = System.currentTimeMillis();
            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events.
            // The key callback above will only be
            // invoked during this call.
            glfwPollEvents();


        }
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    /**
     * Clears teh screen, should be called at the start of each frame.
     * @param red 0-1 red value
     * @param green 0-1 green value
     * @param blue 0-1 blue value
     * @param alpha 0-1 alpha value
     */
    public void clear(float red, float green, float blue, float alpha){
        glClearColor(red, green, blue, alpha);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

    }

    /**
     * Sets the drawing color for the internal pixel() function
     * @param c color for pixel() to use
     * @return bool false if no color is set (if c==null) and true otherwise
     */
    public boolean color(Color c){
        if(c == null)
            glColor4f(0,0,0,0);
        else
            glColor4f(c.getR(), c.getG(), c.getB(), c.getA());

        return c != null;
    }

    /**
     * paints an individual pixel with the color set by color()
     * @param x x coordinate
     * @param y y coordinate
     */
    public void pixel(int x, int y){

        // draw single pixel
        // glVertex2i(x, y);

        // draw rectangle
        glVertex2i(x * this.scale, y * this.scale);
        glVertex2i(x * this.scale + this.scale, y * this.scale);
        glVertex2i(x * this.scale + this.scale, y * this.scale + this.scale);
        glVertex2i(x * this.scale, y * this.scale + this.scale);

    }

    /**
     * Holds code for redraw events. onDraw is called to draw each frame.
     */
    public static abstract class DrawEventListener{
        /**
         * Called on each redraw of the window.
         * @param context OpenGLWindow object to draw onto.
         * @param millis time in millis since the window opened.
         */
        public abstract void onDraw(Context context, long millis, long delta);
    }

    public static abstract class KeyboardEventListener{
        public abstract void onKeyDown(int key);
        public abstract void onKeyUp(int key);

    }

    public static abstract class MouseEventListener{
        public abstract void onMouseDown();
        public abstract void onMouseUp();

    }
}
