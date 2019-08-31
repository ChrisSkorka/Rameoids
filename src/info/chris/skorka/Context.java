package info.chris.skorka;

import java.util.Stack;

/**
 * Context for drawing onto an OpenGlWindow. A new instance of this is provided for each draw event.
 * Contains all functionality required draw simple graphics.
 */
public class Context {


    private OpenGlWindow openGlWindow;
    private Stack<double[][]> transformations = new Stack<>();
    private Color fill, stroke;

    /**
     * Create drawing context object to draw into a window.
     * Starts with the default transformation (identity).
     * @param openGlWindow window to draw onto
     */
    public Context(OpenGlWindow openGlWindow){
        this.openGlWindow = openGlWindow;
        transformations.push(new double[][]{{1,0,0,0},{0,1,0,0},{0,0,1,0},{0,0,0,1}});
    }

    /**
     * Get the width of the display.
     * Note this is the number of pixels of the game not the screen.
     * @return width of the display
     */
    public int getWidth(){
        return openGlWindow.getWidth();
    }

    /**
     * Get the height of the display.
     * Note this is the number of pixels of the game not the screen.
     * @return height of the display
     */
    public int getHeight(){
        return openGlWindow.getHeight();
    }

    /**
     * Sets the drawing color for the internal pixel() function.
     * @param color color for pixel() to use
     * @return bool false if no color is set (if c==null) and true otherwise
     */
    private boolean color(Color color){
        return openGlWindow.color(color);
    }

    /**
     * paints an individual pixel with the color set by color().
     * @param x x coordinate
     * @param y y coordinate
     */
    private void pixel(int x, int y){
        openGlWindow.pixel(x, y);
    }

    /**
     * Clears the screen, should be called at the start of each frame.
     * @param red 0.0 - 1.0 red value
     * @param green 0.0 - 1.0 green value
     * @param blue 0.0 - 1.0 blue value
     * @param alpha 0.0 - 1.0 alpha value
     */
    public void clear(float red, float green, float blue, float alpha){
        openGlWindow.clear(red, green, blue, alpha);
    }

    /**
     * Sets the fill color
     * @param color color to use for filling operations
     */
    public void fill(Color color){
        this.fill = color;
    }

    /**
     * Sets the stroke color.
     * @param color color to use for borders of shapes
     */
    public void stroke(Color color){
        this.stroke = color;
    }

    /**
     * Paints a point (pixel) with the fill color.
     * @param v Vertex with x and y coordinates
     */
    public void point(Vertex v){
        color(this.fill);
        v = v.transform(transformations.lastElement());
        pixel(v.getX(), v.getY());
    }

    /**
     * Paints a rectangle with the fill and stroke colors.
     * @param x1 x coordinate of vertex 1
     * @param y1 y coordinate of vertex 1
     * @param x2 x coordinate of vertex 2
     * @param y2 y coordinate of vertex 2
     */
    public void rect(int x1, int y1, int x2, int y2){
        polygon(new Vertex(x1, y1), new Vertex(x1, y2), new Vertex(x2, y2), new Vertex(x2, y1));
    }

    /**
     * Paints a rectangle with the fill and stroke colors.
     * @param a vertex 1
     * @param b vertex 2
     */
    public void rect(Vertex a, Vertex b){
        rect(a.getX(), a.getY(), b.getX(), b.getY());
    }

    /**
     * Paints a line between two vertices using the stroke color.
     * @param a vertex 1
     * @param b vertex 2
     */
    public void line(Vertex a, Vertex b){
        paintLine(a.transform(transformations.lastElement()), b.transform(transformations.lastElement()));
    }

    /**
     * Paints the actual pixels for a line from Vertex a to b.
     * Note this assumes the transformation has already been applied to the vertices.
     * @param a starting vertex
     * @param b ending vertex
     */
    private void paintLine(Vertex a, Vertex b){

        color(this.stroke);

        // get coordinates
        int x1 = a.getX();
        int y1 = a.getY();
        int x2 = b.getX();
        int y2 = b.getY();

        // get differences
        int xd = x2 - x1;
        int yd = y2 - y1;

        // calculate number of steps required
        float s = Math.abs(xd) > Math.abs(yd) ? Math.abs(xd) : Math.abs(yd);
        float xs = xd / s;
        float ys = yd / s;

        // paint pixels
        for(int i = 0; i <= s; i++)
            pixel(Math.round(x1 + xs * i), Math.round(y1 + ys * i));
    }

    /**
     * Draws a circle with a given center vertex and radius.
     * Note only the center point is transformed according to the stored transformation.
     * The shape or size of the circle is NOT affected by the transformation.
     * @param vertex center Vertex
     * @param r Radius
     */
    public void circle(Vertex vertex, int r){

        // transform center point
        vertex = vertex.transform(transformations.lastElement());
        int x = vertex.getX();
        int y = vertex.getY();

        // number of points along and octet of the circle
        int n_points_octant = (int)Math.ceil(Math.sqrt(2) * r / 2) + 1;

        // for each pixel within one octet (octet is mirrored 3 times to fill the circle)
        for(int i = 0; i < n_points_octant; i++){
            int j = (int) Math.round(Math.sqrt(r * r - i * i));

            // fill entire circle
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

            // draw outline of all 8 octets
            if(color(this.stroke)){
                pixel(x+i,y+j); pixel(x-i,y+j);
                pixel(x+j,y+i); pixel(x-j,y+i);
                pixel(x+j,y-i); pixel(x-j,y-i);
                pixel(x+i,y-j); pixel(x-i,y-j);
            }

        }

    }

    /**
     * Paints a triangle from three vertices using fill and stroke color.
     * @param v0 vertex 1
     * @param v1 vertex 2
     * @param v2 vertex 3
     */
    public void triangle(Vertex v0, Vertex v1, Vertex v2){
        polygon(v0, v1, v2);
    }

    /**
     * Fills a triangle, vertices should be defined in a clock wise order.
     * This does not paint the outline.
     * @param _v0 Vertex 1
     * @param _v1 Vertex 2
     * @param _v2 Vertex 3
     */
    private void fillTriangle(Vertex _v0, Vertex _v1, Vertex _v2){

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

    /**
     * Draw a polygon as defined by the vertices.
     * @param vertices array of vertices
     */
    public void polygon(Vertex... vertices){
        polygon(new Polygon(this.fill, this.stroke, vertices));
    }

    /**
     * Draw a polygon as defined by the polygon object.
     * @param polygon The polygon to be drawn
     */
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

    /**
     * Draw a bitmap where each pixel is painted with the fill color if its corresponding value in the bitmap is true.
     * @param bitmap bitmap to be drawn
     */
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

    /**
     * Perform a matrix multiplication.
     * a_n_cols must equal b_n_rows.
     * @param a Matrix a
     * @param b Matrix b
     * @return
     */
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

    /**
     * Apply a new transformation, new transformation matrix is placed on the stack.
     * This transformation is applied on top of all previous transformations.
     * @param matrix 4x4 transformation matrix
     */
    public void transform(double[][] matrix){
        transformations.push(matmul(matrix, transformations.lastElement()));
    }

    /**
     * Undo the last transformation (remove from the transformation stack).
     */
    public void undoTransform(){
        if(transformations.size() > 1)
            transformations.pop();
    }

    /**
     * Get the current transformation matrix (All currently applied transformations combined).
     * @return 4x4 double transformation matrix
     */
    public double [][] getTransformation(){
        return transformations.lastElement();
    }

    /**
     * Get the entire transformation stack.
     * @return Stack of 4x4 transformation matrices
     */
    public Stack<double[][]> getTransformationStack(){
        return transformations;
    }

    /**
     * Apply a rotation around the z axis transformation.
     * @param r Angle in radians to rotate about the z axis
     */
    public void rotateZ(double r){
        transform(new double[][]{
                {Math.cos(r), Math.sin(r), 0, 0},
                {-Math.sin(r), Math.cos(r), 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1},
        });
    }

    /**
     * Applies a translation transformation, translates 0.0 along the z axis.
     * @param x X axis translation
     * @param y Y axis translation
     */
    public void translate(double x, double y){
        translate(x, y, 0);
    }

    /**
     * Applies a translation transformation.
     * @param x X axis translation
     * @param y Y axis translation
     * @param z Z axis translation
     */
    public void translate(double x, double y, double z){
        transform(new double[][]{
                {1, 0, 0, x},
                {0, 1, 0, y},
                {0, 0, 1, z},
                {0, 0, 0, 1},
        });
    }

    /**
     * Applies a scaling transformation.
     * @param x X axis scale
     * @param y Y axis scale
     * @param z Z axis scale
     */
    public void scale(double x, double y, double z){
        transform(new double[][]{
                {x, 0, 0, 0},
                {0, y, 0, 0},
                {0, 0, z, 0},
                {0, 0, 0, 1},
        });
    }
}
