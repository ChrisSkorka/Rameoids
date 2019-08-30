package info.chris.skorka;

public class Boundary {

    private int x1, x2, y1, y2;

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

    public Boundary(int x1, int y1, int x2, int y2){
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public int left() {
        return x1;
    }

    public int right() {
        return x2;
    }

    public int bottom() {
        return y1;
    }

    public int top() {
        return y2;
    }

    public boolean intersects(Boundary b){
        return Math.max(x1, b.x1) < Math.min(x2, b.x2) && Math.max(y1, b.y1) < Math.min(y2, b.y2);
    }

    public boolean inside(Boundary b){
        return x1 >= b.x1 && x2 <= b.x2 && y1 >= b.y1 && y2 <= b.y2;
    }

    public Boundary translate(int x, int y){
        return new Boundary(x1 + x, y1 + y, x2 + x, y2 + y);
    }
}
