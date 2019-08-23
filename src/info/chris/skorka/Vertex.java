package info.chris.skorka;

public class Vertex {

    private double x, y, z;
    private double w = 1;

    public Vertex(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vertex(double x, double y, double z, double w){
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vertex(int x, int y){
        this.x = x;
        this.y = y;
        this.z = 0;
    }

    public Vertex(double x, double y){
        this.x = x;
        this.y = y;
        this.z = 0;
    }

    public Vertex transform(double[][] t){
        double[] vi = {x,y,z,w};
        double[] vf = {0,0,0,0};
        for(int r = 0; r < t.length; r++){
            for(int c = 0; c < t[r].length; c++)
                vf[r] += t[r][c] * vi[c];
        }

        return new Vertex(vf[0],vf[1],vf[2],vf[3]);
    }

    public int getX() {
        return (int)Math.round(x);
    }

    public int getY() {
        return (int)Math.round(y);
    }

    public int getZ() {
        return (int)Math.round(z);
    }
}
