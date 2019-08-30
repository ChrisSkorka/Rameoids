package info.chris.skorka;

import java.util.Stack;

public class Context {


    private OpenGlWindow openGlWindow;
    private Stack<double[][]> transformations = new Stack<>();
    private Color fill, stroke;

    public Context(OpenGlWindow openGlWindow){
        this.openGlWindow = openGlWindow;
        transformations.push(new double[][]{{1,0,0,0},{0,1,0,0},{0,0,1,0},{0,0,0,1}});
    }


    public int getWidth(){
        return openGlWindow.getWidth();
    }

    public int getHeight(){
        return openGlWindow.getHeight();
    }

    private boolean color(Color color){
        return openGlWindow.color(color);
    }

    private void pixel(int x, int y){
        openGlWindow.pixel(x, y);
    }

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
     * Sets the stroke color
     * @param color color to use for borders of shapes
     */
    public void stroke(Color color){
        this.stroke = color;
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

    public void circle(Vertex vertex, int r){
        vertex = vertex.transform(transformations.lastElement());
        int x = vertex.getX();
        int y = vertex.getY();

        int n_points_octant = (int)Math.ceil(Math.sqrt(2) * r / 2) + 1;

        for(int i = 0; i < n_points_octant; i++){
            int j = (int) Math.round(Math.sqrt(r * r - i * i));

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
}
