package info.chris.skorka;

public class Entity {

    private Polygon polygons[];
    private Boundary boundary;

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
    }

    public void update(long t, long dt){
        r += vr * dt / 1000;

        vy += Math.cos(r) * a * dt / 1000;
        vx += Math.sin(r) * a * dt / 1000;

        if(d != 0){
            vy /= Math.pow(d, dt / 1000f);
            vx /= Math.pow(d, dt / 1000f);
        }

        y += vy * dt / 1000;
        x += vx * dt / 1000;
    }

    public void draw(OpenGlWindow w){
        w.rotateZ(r);
        w.translate(x, y);

        for(Polygon polygon : polygons)
            w.polygon(polygon);

        w.undoTransform();
        w.undoTransform();
    }

    public Boundary getBoundary(){
        return boundary.translate((int)x, (int)y);
    }

}
