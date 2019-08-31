package info.chris.skorka;

/**
 * Provides static methods for generating boolean bitmaps.
 */
public class BitMap{

    /**
     * Generate 4x7 boolean bitmap from 7 segment boolean array
     * @param s 7 segment configuration as (top, top right, bottom right, bottom, bottom left, top left, middle)
     * @return boolean[7][4] bitmap
     */
    public static boolean[][] from7Segment(boolean[] s){
        return new boolean[][]{
                {s[0]||s[5], s[0], s[0], s[0]||s[1]},
                {s[5], false, false, s[1]},
                {s[5], false, false, s[1]},
                {s[4]||s[5]||s[6], s[6], s[6], s[1]||s[2]||s[6]},
                {s[4], false, false, s[2]},
                {s[4], false, false, s[2]},
                {s[3]||s[4], s[3], s[3], s[2]||s[3]},
        };
    }

    /**
     * Scales a boolean bitmap by a positive integer scaling factor
     * @param scale integer scaling value
     * @param b 2D boolean array bitmap
     * @return boolean[nrows*scale][ncols*scale] scaled bitmap
     */
    public static boolean[][] scale(int scale, boolean[][] b){

        if(scale <= 1)
            return b;

        boolean bitmap[][] = new boolean[b.length * scale][b[0].length * scale];

        for(int y = 0; y < bitmap.length; y++) {
            for (int x = 0; x < bitmap[0].length; x++)
                bitmap[y][x] = b[y/scale][x/scale];
        }

        return bitmap;
    }


}
