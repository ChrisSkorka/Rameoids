package info.chris.skorka;

public class Boundary {

    private int x1, x2, y1, y2;

    /**
     * Creates a 2D boundary from an array of vertices.
     * Find min and max x and y values from vertices to construct minimum surrounding rectangular bounding box
     * @param vertices array of vertices to be contained within bounding box
     */
    public Boundary(Vertex... vertices){

        x1 = vertices[0].getX();
        y1 = vertices[0].getY();
        x2 = vertices[0].getX();
        y2 = vertices[0].getY();

        for(Vertex v : vertices) {
            if (v.getX() < x1)
                x1 = v.getX();
            if (v.getY() < y1)
                y1 = v.getY();
            if (v.getX() > x2)
                x2 = v.getX();
            if (v.getY() > y2)
                y2 = v.getY();
        }
    }
    /**
     * Creates a 2D boundary from an array of boundaries.
     * Find min and max left, top, right and bottom values from boundaries to construct minimum surrounding rectangular bounding box
     * @param boundaries array of boundaries to be contained within bounding box
     */
    public Boundary(Boundary... boundaries){

        x1 = boundaries[0].left();
        y1 = boundaries[0].bottom();
        x2 = boundaries[0].right();
        y2 = boundaries[0].top();

        for(Boundary b : boundaries) {
            if (b.left() < x1)
                x1 = b.left();
            if (b.bottom() < y1)
                y1 = b.bottom();
            if (b.right() > x2)
                x2 = b.right();
            if (b.top() > y2)
                y2 = b.top();
        }
    }

    /**
     * Create rectangular boundary from the coordinates
     * @param left min x value
     * @param bottom min y value
     * @param right max x value
     * @param top max y value
     */
    public Boundary(int left, int bottom, int right, int top){
        this.x1 = left;
        this.y1 = bottom;
        this.x2 = right;
        this.y2 = top;
    }

    /**
     * Check if this boundary intersects another boundary.
     * @param b other boundary
     * @return true of this and b intersect
     */
    public boolean intersects(Boundary b){
        return Math.max(x1, b.x1) < Math.min(x2, b.x2) && Math.max(y1, b.y1) < Math.min(y2, b.y2);
    }

    /**
     * Check if this boundary is completely contained with in another boundary
     * @param b other boundary
     * @return true of this is completely and entirely inside of b
     */
    public boolean inside(Boundary b){
        return x1 >= b.x1 && x2 <= b.x2 && y1 >= b.y1 && y2 <= b.y2;
    }

    /**
     * Translates the bounding box by the values
     * @param x Amount to translate by along the x axis
     * @param y Amount to translate by along the y axis
     * @return new Boundary object with translated boundary values
     */
    public Boundary translate(int x, int y){
        return new Boundary(x1 + x, y1 + y, x2 + x, y2 + y);
    }

    /**
     * @return left coordinate
     */
    public int left() {
        return x1;
    }

    /**
     * @return right coordinate
     */
    public int right() {
        return x2;
    }

    /**
     * @return bottom coordinate
     */
    public int bottom() {
        return y1;
    }

    /**
     * @return top coordinate
     */
    public int top() {
        return y2;
    }
}
