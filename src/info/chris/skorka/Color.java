package info.chris.skorka;

public class Color {

    private float r, g, b, a;

    public Color(float red, float green, float blue){
        this.r = red;
        this.g = green;
        this.b = blue;
        this.a = 1f;
    }

    public Color(int red, int green, int blue){
        this.r = red/255f;
        this.g = green/255f;
        this.b = blue/255f;
        this.a = 1f;
    }

    public Color(byte red, byte green, byte blue){
        this.r = red/255f;
        this.g = green/255f;
        this.b = blue/255f;
        this.a = 1f;
    }

    public Color(long hex){

        long a = (hex & 0xFF000000L) >> 24;
        long r = (hex & 0x00FF0000L) >> 16;
        long g = (hex & 0x0000FF00L) >> 8;
        long b = (hex & 0x000000FFL) >> 0;

        this.r = r/255f;
        this.g = g/255f;
        this.b = b/255f;
        this.a = a/255f;

    }

    public Color(int hex){

        long r = (hex & 0x00FF0000) >> 16;
        long g = (hex & 0x0000FF00) >> 8;
        long b = (hex & 0x000000FF) >> 0;

        this.r = r/255f;
        this.g = g/255f;
        this.b = b/255f;
        this.a = 1f;

    }

    public float getR() {
        return r;
    }

    public float getG() {
        return g;
    }

    public float getB() {
        return b;
    }

    public float getA() {
        return a;
    }
}
