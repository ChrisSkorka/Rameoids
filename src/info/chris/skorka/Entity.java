package info.chris.skorka;

public class Entity {

    // private static final boolean drawBoundaries = true;
    public static boolean drawBoundaries = false;

    private Polygon polygons[];
    private Boundary boundary;
    public Boundary boundarySpace;
    public Boundary bouncyBoundarySpace;
    public Boundary infiniteSpace;
    private CircularBoundary circularBoundary;

    public double x, y;
    public double vx = 0;
    public double vy = 0;
    public double a = 0;
    public double d = 0;
    public double r = 0;
    public double vr = 0;

    public Entity(double x, double y, Polygon... polygons){
        this.x = x;
        this.y = y;
        this.polygons = polygons;

        // boundary of entire entity
        Boundary[] boundaries = new Boundary[polygons.length];
        for(int i = 0; i < polygons.length; i++)
            boundaries[i] = new Boundary(polygons[i].vertices);
        boundary = new Boundary(boundaries);

//        int vertexCount = 0;
//        for(Polygon p : polygons)
//            vertexCount += p.vertices.length;
//        Vertex[] vertices = new Vertex[vertexCount];
//        vertexCount = 0;
//        for(Polygon p : polygons){
//            for(int i = 0; i < p.vertices.length; i++)
//                vertices[vertexCount + i] = p.vertices[i];
//
//            vertexCount += p.vertices.length;
//        }
//
//        circularBoundary = new CircularBoundary(vertices);

        if(polygons.length == 1)
            circularBoundary = new CircularBoundary(polygons[0].vertices);
        else
            circularBoundary = new CircularBoundary(boundary);
    }

    public void update(long t, long dt){
        r += vr * dt / 1000;

        vy += Math.cos(r) * a * dt / 1000;
        vx += Math.sin(r) * a * dt / 1000;

        if(bouncyBoundarySpace != null){
            CircularBoundary circularBoundary = getCircularBoundary();
            int dLeft = circularBoundary.left() - bouncyBoundarySpace.left();
            int dRight = circularBoundary.right() - bouncyBoundarySpace.right();
            int dBottom = circularBoundary.bottom() - bouncyBoundarySpace.bottom();
            int dTop = circularBoundary.top() - bouncyBoundarySpace.top();
            if (dLeft < 0) {
                vx -= 100 * dLeft * dt / 1000;
            }
            if (dRight > 0) {
                vx -= 100 * dRight * dt / 1000;
            }
            if (dBottom < 0) {
                vy -= 100 * dBottom * dt / 1000;
            }
            if (dTop > 0) {
                vy -= 100 * dTop * dt / 1000;
            }
        }

        if(d != 0){
            vy /= Math.pow(d, dt / 1000f);
            vx /= Math.pow(d, dt / 1000f);
        }

        y += vy * dt / 1000;
        x += vx * dt / 1000;

        if(boundarySpace != null) {
            CircularBoundary circularBoundary = getCircularBoundary();
            if (circularBoundary.left() < boundarySpace.left()) {
                vx = -vx;
                x = boundarySpace.left() + circularBoundary.radius() + 1;
            }
            if (circularBoundary.right() > boundarySpace.right()) {
                vx = -vx;
                x = boundarySpace.right() - circularBoundary.radius() - 1;
            }
            if (circularBoundary.bottom() < boundarySpace.bottom()) {
                vy = -vy;
                y = boundarySpace.bottom() + circularBoundary.radius() + 2; // TODO WTF DOES IT GET STUCK
            }
            if (circularBoundary.top() > boundarySpace.top()) {
                vy = -vy;
                y = boundarySpace.top() - circularBoundary.radius() - 1;
            }
        }

        if(infiniteSpace != null){
            CircularBoundary circularBoundary = getCircularBoundary();
            if (circularBoundary.left() < infiniteSpace.left()) {
                x += infiniteSpace.right() - infiniteSpace.left();
            }
            if (circularBoundary.right() > infiniteSpace.right()) {
                x -= infiniteSpace.right() - infiniteSpace.left();
            }
            if (circularBoundary.bottom() < infiniteSpace.bottom()) {
                y += infiniteSpace.top() - infiniteSpace.bottom();
            }
            if (circularBoundary.top() > infiniteSpace.top()) {
                y -= infiniteSpace.top() - infiniteSpace.bottom();
            }
        }
    }

    public void draw(Context c){
        c.rotateZ(r);

        c.translate(x, y);
        for(Polygon polygon : polygons)
            c.polygon(polygon);

        if(infiniteSpace != null) {
            c.translate(-c.getWidth(), 0);
            for (Polygon polygon : polygons)
                c.polygon(polygon);
            c.undoTransform();

            c.translate(c.getWidth(), 0);
            for (Polygon polygon : polygons)
                c.polygon(polygon);
            c.undoTransform();

            c.translate(0, -c.getHeight());
            for (Polygon polygon : polygons)
                c.polygon(polygon);
            c.undoTransform();

            c.translate(0, c.getHeight());
            for (Polygon polygon : polygons)
                c.polygon(polygon);
            c.undoTransform();

        }

//        c.fill(null);
//        c.stroke(new Color(0x88FF0000L));
//        c.rect(boundary.left(), boundary.bottom(), boundary.right(), boundary.top());
//        c.stroke(new Color(0x8800ff00L));
//        c.rect(circularBoundary.left(), circularBoundary.bottom(), circularBoundary.right(), circularBoundary.top());

        c.undoTransform();
        c.undoTransform();

        if(drawBoundaries) {
            Boundary b = getBoundary();
            CircularBoundary a = getCircularBoundary();
            c.fill(null);
            c.stroke(new Color(0x88FF0000L));
            c.rect(b.left(), b.bottom(), b.right(), b.top());
            c.stroke(new Color(0x8800FF00L));
            c.circle(new Vertex(this.x, this.y), a.radius());

            c.stroke(new Color(0x8800FFFFL));
            c.line(new Vertex(this.x, this.y), new Vertex(this.x+this.vx, this.y+this.vy));
            c.stroke(new Color(0x88FF00FFL));
            c.line(new Vertex(this.x, this.y), new Vertex(this.x+Math.sin(this.r)*this.a/10, this.y+Math.cos(this.r)*this.a/10));
            c.stroke(new Color(0x88FFFF00L));
            double v = Math.sqrt(this.vx*this.vx+this.vy*this.vy);
            c.line(new Vertex(this.x, this.y), new Vertex(this.x-Math.sin(this.r)*this.d*v/50, this.y-Math.cos(this.r)*this.d*v/50));
        }
    }

    public Boundary getBoundary(){
        return boundary.translate((int)x, (int)y);
    }

    public CircularBoundary getCircularBoundary(){
        return circularBoundary.translate((int)x, (int)y);
    }

}
