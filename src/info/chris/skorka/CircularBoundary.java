package info.chris.skorka;

public class CircularBoundary {

    private int x, y, r;

    public CircularBoundary(int x, int y, int r){
        this.x = x;
        this.y = y;
        this.r = r;
    }

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

    public CircularBoundary(Boundary boundary){
        this.x = (boundary.left() + boundary.right()) / 2;
        this.y = (boundary.bottom() + boundary.top()) / 2;
        this.r = (int) Math.round(Math.max(boundary.right() - boundary.left(), boundary.top() - boundary.bottom()) / 2);
    }

    public boolean intersects(CircularBoundary circularBoundary){
        return Math.pow(this.x - circularBoundary.x, 2) + Math.pow(this.y - circularBoundary.y, 2)
                <= Math.pow(this.r + circularBoundary.r, 2);
    }

    public CircularBoundary translate(int x, int y){
        return new CircularBoundary(this.x+x, this.y+y, this.r);
    }

    public int right(){ return this.x + this.r; }
    public int left(){ return this.x - this.r; }
    public int top(){ return this.y + this.r; }
    public int bottom(){ return this.y - this.r; }
    public int radius(){ return this.r; }

}
