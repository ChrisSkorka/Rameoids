package info.chris.skorka;

/**
 * Represents a color in terms of ARGB floating point values
 */
public class Color {

    private float r, g, b, a;

    /**
     * Color from red, green and blue float values
     * @param red red component (0.0 - 1.0)
     * @param green green component (0.0 - 1.0)
     * @param blue blue component (0.0 - 1.0)
     */
    public Color(float red, float green, float blue){
        this.r = red;
        this.g = green;
        this.b = blue;
        this.a = 1f;
    }

    /**
     * Color from red, green and blue int values
     * @param red red component (0 - 255)
     * @param green green component (0 - 255)
     * @param blue blue component (0 - 255)
     */
    public Color(int red, int green, int blue){
        this.r = red/255f;
        this.g = green/255f;
        this.b = blue/255f;
        this.a = 1f;
    }

    /**
     * Color from red, green and blue byte values
     * @param red red component (0 - 255)
     * @param green green component (0 - 255)
     * @param blue blue component (0 - 255)
     */
    public Color(byte red, byte green, byte blue){
        this.r = red/255f;
        this.g = green/255f;
        this.b = blue/255f;
        this.a = 1f;
    }

    /**
     * Color from int RGB hex value
     * @param hex standard RGB hex as int
     */
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

    /**
     * Color from long ARGB hex value
     * @param hex standard ARGB hex as long
     */
    public Color(int hex){

        long r = (hex & 0x00FF0000) >> 16;
        long g = (hex & 0x0000FF00) >> 8;
        long b = (hex & 0x000000FF) >> 0;

        this.r = r/255f;
        this.g = g/255f;
        this.b = b/255f;
        this.a = 1f;

    }

    /**
     * Red component
     * @return red component as a float (0.0 - 1.0)
     */
    public float getR() {
        return r;
    }

    /**
     * Green component
     * @return green component as a float (0.0 - 1.0)
     */
    public float getG() {
        return g;
    }

    /**
     * Blue component
     * @return blue component as a float (0.0 - 1.0)
     */
    public float getB() {
        return b;
    }

    /**
     * Alpha component
     * @return alpha component as a float (0.0 - 1.0)
     */
    public float getA() {
        return a;
    }
}
