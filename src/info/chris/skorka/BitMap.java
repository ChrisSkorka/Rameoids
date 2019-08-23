package info.chris.skorka;

public class BitMap{

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

    public static boolean[][] scale(int scale, boolean[][] b){
        boolean bitmap[][] = new boolean[b.length * scale][b[0].length * scale];

        for(int y = 0; y < bitmap.length; y++) {
            for (int x = 0; x < bitmap[0].length; x++)
                bitmap[y][x] = b[y/scale][x/scale];
        }

        return bitmap;
    }


}
