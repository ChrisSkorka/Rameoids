package info.chris.skorka;

public class CircularBoundary {

    private int x, y, r;

    /**
     * Create circular boundary directly from circle values.
     * @param x x-coordinate of the center point
     * @param y y-coordinate of the center point
     * @param r radius of the circular boundary from the center point
     */
    public CircularBoundary(int x, int y, int r){
        this.x = x;
        this.y = y;
        this.r = r;
    }

    /**
     * Create circular boundary from vertices.
     * Center is the average point and the radius is the maximum distance from any vertex to the center
     * @param vertices array of vertices to construct circular boundary from
     */
    public CircularBoundary(Vertex... vertices){
        this.x = 0;
        this.y = 0;
        this.r = 0;

        for(Vertex v : vertices){
            this.x += v.getX();
            this.y += v.getY();
        }

        this.x /= vertices.length;
        this.y /= vertices.length;

        for(Vertex v : vertices){
            int d = (int) Math.round(Math.sqrt(
                (v.getX() - this.x) * (v.getX() - this.x) +
                (v.getY() - this.y) * (v.getY() - this.y)));

            if(d > this.r)
                this.r = d;
        }
    }

    /**
     * Create circular boundary from a rectangular boundary.
     * Note that the the circular boundary is so that it completely fits into the larger axis of the rectangular
     * boundary and DOES NOT contain all the space from the rectangular boundary
     * @param boundary rectangular boundary to fit circular boundary into
     */
    public CircularBoundary(Boundary boundary){
        this.x = (boundary.left() + boundary.right()) / 2;
        this.y = (boundary.bottom() + boundary.top()) / 2;
        this.r = (int) Math.round(Math.max(boundary.right() - boundary.left(), boundary.top() - boundary.bottom()) / 2);
    }

    /**
     * Check if this boundary intersects another boundary.
     * @param circularBoundary other boundary
     * @return true of this and b intersect
     */
    public boolean intersects(CircularBoundary circularBoundary){
        return Math.pow(this.x - circularBoundary.x, 2) + Math.pow(this.y - circularBoundary.y, 2)
                <= Math.pow(this.r + circularBoundary.r, 2);
    }

    /**
     * Translates the bounding box by the values
     * @param x Amount to translate by along the x axis
     * @param y Amount to translate by along the y axis
     * @return new Boundary object with translated boundary values
     */
    public CircularBoundary translate(int x, int y){
        return new CircularBoundary(this.x+x, this.y+y, this.r);
    }

    /**
     * @return right coordinate
     */
    public int right(){ return this.x + this.r; }

    /**
     * @return left coordinate
     */
    public int left(){ return this.x - this.r; }

    /**
     * @return top coordinate
     */
    public int top(){ return this.y + this.r; }

    /**
     * @return bottom coordinate
     */
    public int bottom(){ return this.y - this.r; }

    /**
     * @return radius of the bounding circle
     */
    public int radius(){ return this.r; }

}
