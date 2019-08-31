package info.chris.skorka;

/**
 * Represents a vertex/point in 3D space in terms of z, y, z double values with an additional w term.
 */
public class Vertex {

    private double x, y, z;
    private double w = 1;

    /**
     * Create vertex from integer coordinates
     * @param x x-coordinate
     * @param y y-coordinate
     * @param z z-coordinate
     */
    public Vertex(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Create vertex from doubles coordinates
     * @param x x-coordinate
     * @param y y-coordinate
     * @param z z-coordinate
     */
    public Vertex(double x, double y, double z, double w){
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * Create vertex from integer coordinates with z=0
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public Vertex(int x, int y){
        this.x = x;
        this.y = y;
        this.z = 0;
    }

    /**
     * Create vertex from integer coordinates with z=0.0
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public Vertex(double x, double y){
        this.x = x;
        this.y = y;
        this.z = 0;
    }

    /**
     * Transform this vertex according to a transformation matrix
     * @param t 4x4 Transformation matrix
     * @return Transformed Vertex
     */
    public Vertex transform(double[][] t){
        double[] vi = {x,y,z,w};
        double[] vf = {0,0,0,0};
        for(int r = 0; r < t.length; r++){
            for(int c = 0; c < t[r].length; c++)
                vf[r] += t[r][c] * vi[c];
        }

        return new Vertex(vf[0],vf[1],vf[2],vf[3]);
    }

    /**
     * @return x component as rounded to the nearest integer
     */
    public int getX() {
        return (int)Math.round(x);
    }

    /**
     * @return y component as rounded to the nearest integer
     */
    public int getY() {
        return (int)Math.round(y);
    }

    /**
     * @return z component as rounded to the nearest integer
     */
    public int getZ() {
        return (int)Math.round(z);
    }
}
