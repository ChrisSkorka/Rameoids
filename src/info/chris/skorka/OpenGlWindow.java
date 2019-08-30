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
    private Color fill, stroke;
    private long time_start = System.currentTimeMillis();
    private long time_last = System.currentTimeMillis();

    private DrawEventListener drawEventListener;
    private KeyboardEventListener keyboardEventListener;
    private MouseEventListener mouseEventListener;

    private Stack<double[][]> transformations = new Stack<>();

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
        System.out.printf("Windows with %dx%d pixels at %dx%d resolution\n", this.width, this.height, this.width * this.scale, this.height * this.scale);

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

            transformations.clear();
            transformations.push(new double[][]{{1,0,0,0},{0,1,0,0},{0,0,1,0},{0,0,0,1}});

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
                drawEventListener.onDraw(this, System.currentTimeMillis() - time_start, System.currentTimeMillis() - time_last);
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
     * Sets the fill color
     * @param color color to use for filling operations
     */
    public void fill(Color color){
        this.fill = color;
    }

    /**
     * Sets the stroke color
     * @param color color to use for borders of shapes
     */
    public void stroke(Color color){
        this.stroke = color;
    }

    /**
     * Sets the drawing color for the internal pixel() function
     * @param c color for pixel() to use
     * @return bool false if no color is set (if c==null) and true otherwise
     */
    private boolean color(Color c){
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
    private void pixel(int x, int y){

        // draw single pixel
        // glVertex2i(x, y);

        // draw rectangle
        glVertex2i(x * this.scale, y * this.scale);
        glVertex2i(x * this.scale + this.scale, y * this.scale);
        glVertex2i(x * this.scale + this.scale, y * this.scale + this.scale);
        glVertex2i(x * this.scale, y * this.scale + this.scale);

    }

    /**
     * Paints a point (pixel) with the fill color
     * @param v Vertex with x and y coordinates
     */
    public void point(Vertex v){
        color(this.fill);
        v = v.transform(transformations.lastElement());
        pixel(v.getX(), v.getY());
    }


//    /**
//     * Paints a rectangle with the fill and stroke colors.
//     * @param x1 x coordinate of vertex 1
//     * @param y1 y coordinate of vertex 1
//     * @param x2 x coordinate of vertex 2
//     * @param y2 y coordinate of vertex 2
//     */
//    public void rect(int x1, int y1, int x2, int y2){
//
//        // swap x1 & x2 and/or y1 & y2 so 1 < 2
//        if(x1 > x2){
//            int xt = x1;
//            x1 = x2;
//            x2 = xt;
//        }
//        if(y1 > y2){
//            int yt = y1;
//            y1 = y2;
//            y2 = yt;
//        }
//
//        for(int a = x1; a <= x2; a++){
//            for(int b = y1; b <= y2; b++){
//                boolean is_edge = a == x1 || a == x2 || b == y1 || b == y2;
//                color(is_edge && this.stroke != null ? this.stroke : this.fill);
//                pixel(a, b);
//            }
//        }
//    }
//
//    /**
//     * Paints a rectangle with the fill and stroke colors.
//     * @param a vertex 1
//     * @param b vertex 2
//     */
//    public void rect(Vertex a, Vertex b){
//        rect(a.getX(), a.getY(), b.getX(), b.getY());
//    }

    /**
     * Paints a line between two vertices using the stroke color.
     * @param a vertex 1
     * @param b vertex 2
     */
    public void line(Vertex a, Vertex b){
        paintLine(a.transform(transformations.lastElement()), b.transform(transformations.lastElement()));
    }

    private void paintLine(Vertex a, Vertex b){

        color(this.stroke);

        int x1 = a.getX();
        int y1 = a.getY();
        int x2 = b.getX();
        int y2 = b.getY();

        int xd = x2 - x1;
        int yd = y2 - y1;

        float s = Math.abs(xd) > Math.abs(yd) ? Math.abs(xd) : Math.abs(yd);
        float xs = xd / s;
        float ys = yd / s;

        for(int i = 0; i <= s; i++)
            pixel(Math.round(x1 + xs * i), Math.round(y1 + ys * i));
    }

//    public void circle(int x, int y, int r){
//
//        int n_points_octant = (int)Math.ceil(Math.sqrt(2) * r / 2) + 1;
//
//        for(int i = 0; i < n_points_octant; i++){
//            int j = (int) Math.round(Math.sqrt(r * r - i * i));
//
//            if(this.fill != null) {
//                color(this.fill);
//                for (int k = -i; k <= i; k++) {
//                    pixel(x + k, y + j);
//                    pixel(x + k, y - j);
//                }
//                for (int k = -j; k <= j; k++) {
//                    pixel(x + k, y + i);
//                    pixel(x + k, y - i);
//                }
//            }
//
//            if(this.stroke != null){
//                color(new Color(0x88FF0000L));
//                pixel(x+i,y+j); pixel(x-i,y+j);
//                color(new Color(0x8800FF00L));
//                pixel(x+j,y+i); pixel(x-j,y+i);
//                color(new Color(0x880000FFL));
//                pixel(x+j,y-i); pixel(x-j,y-i);
//                color(new Color(0x88FF00FFL));
//                pixel(x+i,y-j); pixel(x-i,y-j);
//            }
//
//        }
//
//    }
//
//    public void ellipse(int x, int y, int rx, int ry){
//        color(this.stroke);
//
//        int n_points = (int)Math.ceil(rx * Math.PI / 4 + ry * Math.PI / 4);
//        int[][] points = new int[n_points][2];
//        n_points = 0;
//
//        int steps = (int)Math.ceil(rx * Math.PI / 4 + ry * Math.PI / 4) * 4;
//        int last_a = x+rx;
//        int last_b = y;
//
//        for(int s = 0; s < steps; s++){
//            double t = Math.PI * 2 / steps * s;
//            int a = x + (int)Math.round(rx * Math.cos(t));
//            int b = y + (int)Math.round(ry * Math.sin(t));
//            line(last_a, last_b, a, b);
//            last_a = a;
//            last_b = b;
//            // pixel(x + a, y + b);
//        }
//
//    }

    /**
     * Paints a triangle from three vertices using fill and stroke color.
     * @param _v0 vertex 1
     * @param _v1 vertex 2
     * @param _v2 vertex 3
     */
    public void triangle(Vertex _v0, Vertex _v1, Vertex _v2){

        _v0 = _v0.transform(transformations.lastElement());
        _v1 = _v1.transform(transformations.lastElement());
        _v2 = _v2.transform(transformations.lastElement());

        Vertex[] vertices = {_v0, _v1, _v2};
        if(color(this.fill)){

            Boundary boundary = new Boundary(vertices);

            for(int y = boundary.bottom(); y <= boundary.top(); y++) {
                for(int x = boundary.left(); x <= boundary.right(); x++) {
                    boolean fill = true;
                    boolean stroke = false;
                    for(int i = 0; i < 3; i++){
                        Vertex v1 = vertices[i];
                        Vertex v2 = vertices[(i + 1) % 3];
                        Vertex p = vertices[(i + 2) % 3];


                        int d1 =  (x - v1.getX()) * (v2.getY() - v1.getY()) - (v2.getX() - v1.getX()) * (y - v1.getY());
                        int d2 =  (p.getX() - v1.getX()) * (v2.getY() - v1.getY()) - (v2.getX() - v1.getX()) * (p.getY() - v1.getY());
                        fill &= (d1 > 0) == (d2 > 0);
                        // stroke |= (-1 <= d1) && (d1 <= 1);
                        // stroke |= d1 == 0;
                    }

//                    if(stroke && (color(this.stroke) || color(this.fill)))
//                        pixel(x, y);
//                    if(!stroke && fill && color(this.fill))
//                        pixel(x, y);
                    if(fill && color(this.fill))
                        pixel(x, y);
                }
            }
        }

        if(color(this.stroke)){
            paintLine(_v0, _v1);
            paintLine(_v1, _v2);
            paintLine(_v2, _v0);

        }
    }


    public void fillTriangle(Vertex _v0, Vertex _v1, Vertex _v2){

        _v0 = _v0.transform(transformations.lastElement());
        _v1 = _v1.transform(transformations.lastElement());
        _v2 = _v2.transform(transformations.lastElement());

        Vertex[] vertices = {_v0, _v1, _v2};
        if(color(this.fill)){

            Boundary boundary = new Boundary(vertices);

            for(int y = boundary.bottom(); y <= boundary.top(); y++) {
                for(int x = boundary.left(); x <= boundary.right(); x++) {
                    boolean fill = true;
                    for(int i = 0; i < 3; i++){
                        Vertex v1 = vertices[i];
                        Vertex v2 = vertices[(i + 1) % 3];
                        Vertex p = vertices[(i + 2) % 3];


                        int d1 =  (x - v1.getX()) * (v2.getY() - v1.getY()) - (v2.getX() - v1.getX()) * (y - v1.getY());
                        int d2 =  (p.getX() - v1.getX()) * (v2.getY() - v1.getY()) - (v2.getX() - v1.getX()) * (p.getY() - v1.getY());
                        fill &= (d1 > 0) == (d2 > 0);
                    }

                    if(fill)
                        pixel(x, y);
                }
            }
        }
    }

//    public void polygon(Vertex... vertices){
//
//        Vertex[][] lines = new Vertex[vertices.length][2];
//        for(int i = 1; i < vertices.length; i++){
//            Vertex a = vertices[i-1];
//            Vertex b = vertices[i];
//            if(b.getY() < a.getY()){
//                Vertex t = a;
//                a = b;
//                b = t;
//            }
//            lines[i][0] = a;
//            lines[i][1] = b;
//        }
//        Vertex a = vertices[vertices.length-1];
//        Vertex b = vertices[0];
//        if(b.getY() < a.getY()){
//            Vertex t = a;
//            a = b;
//            b = t;
//        }
//        lines[0][0] = a;
//        lines[0][1] = b;
//
//        if(color(this.fill)){
//
//            Boundary boundary = new Boundary(vertices);
//
//            for(int y = boundary.bottom(); y <= boundary.top(); y++){
//                boolean draw = false;
//                for(int x = boundary.left(); x <= boundary.right(); x++){
//                    boolean intersect = false;
//                    for(Vertex[] line : lines){
//                        boolean is_between_y = line[0].getY() <= y && y <= line[1].getY();
//                        int yd = line[1].getY() - line[0].getY();
//                        int xd = line[1].getX() - line[0].getX();
//                        double ratio = yd == 0 ? 0 : xd / yd;
//                        double xi = line[0].getX() + ratio * (y - line[0].getY());
//                        if(is_between_y && x >= xi){
//                            draw = !draw;
//                            intersect = true;
//                        }
//                    }
//                    if(draw)
//                        pixel(x, y);
//                }
//            }
//
//            // rect(x1, y1, x2, y2);
//        }
//
//        if(color(this.stroke)){
//            for(Vertex[] line : lines)
//                line(line[0], line[1]);
//        }
//    }

    public void polygon(Vertex... vertices){
        polygon(new Polygon(this.fill, this.stroke, vertices));
    }

    public void polygon(Polygon polygon){
        fill(polygon.fill);
        stroke(polygon.stroke);
        if(color(fill)){
            for (Vertex[] t : polygon.triangles)
                fillTriangle(t[2], t[1], t[0]);
        }

        if(color(stroke)){
            for(Vertex[] l : polygon.lines)
                line(l[0], l[1]);
        }
    }

    public void bitmap(boolean[][] bitmap){
        if (color(fill)) {
            for(int y = 0; y < bitmap.length; y++){
                for(int x = 0; x < bitmap[0].length; x++){
                    if(bitmap[y][x])
                    point(new Vertex(x, bitmap.length-y-1));
                }
            }
        }
    }


    private static double[][] matmul(double[][] a, double[][] b){
        double[][] m = new double[b.length][a[0].length];

        for(int r = 0; r < m.length; r++){
            for(int c = 0; c < m[r].length; c++){
                for(int i = 0; i < a.length; i++)
                    m[r][c] += a[r][i] * b[i][c];
            }
        }

        return m;
    }

    public void transform(double[][] matrix){
        transformations.push(matmul(matrix, transformations.lastElement()));
    }

    public void undoTransform(){
        transformations.pop();
    }

    public void rotateZ(double r){
        transform(new double[][]{
                {Math.cos(r), Math.sin(r), 0, 0},
                {-Math.sin(r), Math.cos(r), 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1},
        });
    }

    public void translate(double x, double y){
        translate(x, y, 0);
    }

    public void translate(double x, double y, double z){
        transform(new double[][]{
                {1, 0, 0, x},
                {0, 1, 0, y},
                {0, 0, 1, z},
                {0, 0, 0, 1},
        });
    }

    public void scale(double x, double y, double z){
        transform(new double[][]{
                {x, 0, 0, 0},
                {0, y, 0, 0},
                {0, 0, z, 0},
                {0, 0, 0, 1},
        });
    }

    /**
     * Holds code for redraw events. onDraw is called to draw each frame.
     */
    public static abstract class DrawEventListener{
        /**
         * Called on each redraw of the window.
         * @param window OpenGLWindow object to draw onto.
         * @param millis time in millis since the window opened.
         */
        public abstract void onDraw(OpenGlWindow window, long millis, long delta);
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
