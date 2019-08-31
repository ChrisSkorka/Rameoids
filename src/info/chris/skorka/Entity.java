package info.chris.skorka;

public class Entity {

    public static boolean drawBoundaries = false;

    private Polygon polygons[];
    private Boundary boundary;
    public Boundary boundarySpace;
    public Boundary bouncyBoundarySpace;
    public Boundary infiniteSpace;
    private CircularBoundary circularBoundary;

    // physics configuration/parameters
    public double x, y;
    public double vx = 0;
    public double vy = 0;
    public double a = 0;
    public double d = 0;
    public double r = 0;
    public double vr = 0;


    /**
     * Create and Entity object that represents an object in the game, it has physics attribute as well as polygons
     * defining the look of the entity
     * @param x initial x-coordinate of the
     * @param y initial y-coordinate of the
     * @param polygons One or more Polygon objects defining the shape of the entity
     */
    public Entity(double x, double y, Polygon... polygons){
        this.x = x;
        this.y = y;
        this.polygons = polygons;

        // rectangular boundary of entire entity (all polygons)
        Boundary[] boundaries = new Boundary[polygons.length];
        for(int i = 0; i < polygons.length; i++)
            boundaries[i] = new Boundary(polygons[i].vertices);
        boundary = new Boundary(boundaries);

        // combine all polygons vertices into one list to compute circular boundary
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

        // compute circular boundary from either one polygon or the rectangular boundary of many polygons
        if(polygons.length == 1)
            circularBoundary = new CircularBoundary(polygons[0].vertices);
        else
            circularBoundary = new CircularBoundary(boundary);
    }

    /**
     * Updates the entity, processes 1 time step
     * @param t Time in milli seconds
     * @param dt Delta time since last update
     */
    public void update(long t, long dt){

        // change rotation
        r += vr * dt / 1000;

        // apply acceleration in the direction of r
        vy += Math.cos(r) * a * dt / 1000;
        vx += Math.sin(r) * a * dt / 1000;

        // apply bouncy inward force
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

        // apply drag
        if(d != 0){
            vy /= Math.pow(d, dt / 1000f);
            vx /= Math.pow(d, dt / 1000f);
        }

        // change speed according to velocities
        y += vy * dt / 1000;
        x += vx * dt / 1000;

        // change velocity if intersecting hard boundary
        if(boundarySpace != null) {
            CircularBoundary circularBoundary = getCircularBoundary();
            if (circularBoundary.left() < boundarySpace.left()) {
                vx = -vx;
                x += vx * dt / 1000;
            }
            if (circularBoundary.right() > boundarySpace.right()) {
                vx = -vx;
                x += vx * dt / 1000;
            }
            if (circularBoundary.bottom() < boundarySpace.bottom()) {
                vy = -vy;
                y += vy * dt / 1000;
            }
            if (circularBoundary.top() > boundarySpace.top()) {
                vy = -vy;
                y += vy * dt / 1000;
            }
        }

        // if infinite space teleport to opposite site of boundary
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

    /**
     * Draws this entity onto the context
     * @param c Context object
     */
    public void draw(Context c){

        // rotate
        c.rotateZ(r);

        // translate its position and draw all its polygons
        c.translate(x, y);
        for(Polygon polygon : polygons)
            c.polygon(polygon);

        // if infinite space draw duplicates on opposite sites of the display
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

        // undo rotation and translation
        c.undoTransform();
        c.undoTransform();

        // draw boundaries and velocity information
        if(drawBoundaries) {
            Boundary b = getBoundary();
            CircularBoundary a = getCircularBoundary();

            // rectangular bounding box
            c.fill(null);
            c.stroke(new Color(0x88FF0000L));
            c.rect(b.left(), b.bottom(), b.right(), b.top());

            // circular bounding circle
            c.stroke(new Color(0x8800FF00L));
            c.circle(new Vertex(this.x, this.y), a.radius());

            // velocity
            c.stroke(new Color(0x8800FFFFL));
            c.line(new Vertex(this.x, this.y), new Vertex(this.x+this.vx, this.y+this.vy));

            // acceleration
            c.stroke(new Color(0x88FF00FFL));
            c.line(new Vertex(this.x, this.y), new Vertex(this.x+Math.sin(this.r)*this.a/10, this.y+Math.cos(this.r)*this.a/10));

            // drag
            c.stroke(new Color(0x88FFFF00L));
            double v = Math.sqrt(this.vx*this.vx+this.vy*this.vy);
            c.line(new Vertex(this.x, this.y), new Vertex(this.x-Math.sin(this.r)*this.d*v/50, this.y-Math.cos(this.r)*this.d*v/50));
        }
    }

    /**
     * Get rectangular boundary adjusted for the current position
     * Note that the bounding box does not adjust for rotations
     * @return Boundary of the translated entity
     */
    public Boundary getBoundary(){
        return boundary.translate((int)x, (int)y);
    }

    /**
     * Get circular boundary adjusted for the current position
     * @return CircularBoundary of the translated entity
     */
    public CircularBoundary getCircularBoundary(){
        return circularBoundary.translate((int)x, (int)y);
    }

}
