package info.chris.skorka;

public class Entity {

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

    public void draw(OpenGlWindow w){
        w.rotateZ(r);

        w.translate(x, y);
        for(Polygon polygon : polygons)
            w.polygon(polygon);

        if(infiniteSpace != null) {
            w.translate(-w.getWidth(), 0);
            for (Polygon polygon : polygons)
                w.polygon(polygon);
            w.undoTransform();

            w.translate(w.getWidth(), 0);
            for (Polygon polygon : polygons)
                w.polygon(polygon);
            w.undoTransform();

            w.translate(0, -w.getHeight());
            for (Polygon polygon : polygons)
                w.polygon(polygon);
            w.undoTransform();

            w.translate(0, w.getHeight());
            for (Polygon polygon : polygons)
                w.polygon(polygon);
            w.undoTransform();

        }

        w.undoTransform();
        w.undoTransform();
    }

    public Boundary getBoundary(){
        return boundary.translate((int)x, (int)y);
    }

    public CircularBoundary getCircularBoundary(){
        return circularBoundary.translate((int)x, (int)y);
    }

}
