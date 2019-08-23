package info.chris.skorka;

public class Polygon {

    public Vertex[] vertices;
    public Vertex[][] triangles;
    public Vertex[][] lines;
    public Color fill;
    public Color stroke;

    public Polygon(Color fill, Color stroke, Vertex... vertices){
        this.vertices = vertices;
        this.fill = fill;
        this.stroke = stroke;


        triangles = decomposePolygon(vertices);
        lines = linesFromVertices(vertices);
    }

    /**
     * Converts an array of vertices to an array of lines (array of Vertex pairs).
     * The line linking form the last vertex to the first is also included forming a complete polygon.
     * @param vertices array of vertices
     * @return Vertex[][2] array of pairs of vertices
     */
    public static Vertex[][] linesFromVertices(Vertex[] vertices){
        Vertex[][] lines = new Vertex[vertices.length][2];
        for(int i = 1; i < vertices.length; i++){
//            Vertex a = vertices[i-1];
//            Vertex b = vertices[i];
//            if(b.getY() < a.getY()){
//                Vertex t = a;
//                a = b;
//                b = t;
//            }
//            lines[i][0] = a;
//            lines[i][1] = b;

            lines[i][0] = vertices[i-1];
            lines[i][1] = vertices[i];
        }
        lines[0][0] = vertices[vertices.length-1];
        lines[0][1] = vertices[0];

        return lines;
    }

    /**
     * Decomposes an array of vertices into triangles (array of 3 vertices).
     * @param vertices array of vertices that compose the polygon
     * @return Vertex[][3] array of triangles
     */
    public static Vertex[][] decomposePolygon(Vertex... vertices){
        Vertex[][] lines = linesFromVertices(vertices);
        double cw = 0;
        for(Vertex[] line : lines)
            cw += (line[1].getX() - line[0].getX()) * (line[1].getY() + line[0].getY());

        if(cw < 0){
            Vertex[] new_vertices = new Vertex[vertices.length];

            for(int i = 0; i < vertices.length; i++)
                new_vertices[i] = vertices[vertices.length - i - 1];

            vertices = new_vertices;
        }

        return decomposeCWPolygon(vertices);
    }

    private static Vertex[][] decomposeCWPolygon(Vertex... vertices){

        // base case
        if(vertices.length == 3)
            return new Vertex[][] {vertices};
        else{

            Vertex[][] lines = linesFromVertices(vertices);
            int indexOfCut1 = 0;
            int indexOfCut2 = 2;
            boolean cutFound = false;

            // search for cut through polygon that doesn't cut lines
            for(int i = 0; i < vertices.length-2 && !cutFound; i++){
                Vertex p1 = vertices[i];
                indexOfCut1 = i;
                for(int j = i + 2; j < vertices.length && !cutFound; j++) {
                    if(i==0 && j==vertices.length-1)
                        continue;

                    Vertex p2 = vertices[j];
                    indexOfCut2 = j;

                    Vertex[] cut = new Vertex[]{p1, p2};

                    Vertex p1a = vertices[(indexOfCut1+1)%vertices.length];
                    Vertex p1b = vertices[(indexOfCut1+vertices.length-1)%vertices.length];
                    Vertex p2a = vertices[(indexOfCut2+1)%vertices.length];
                    Vertex p2b = vertices[(indexOfCut2+vertices.length-1)%vertices.length];

                    // check line p1:p2 is between lines p1:p1a and p1b:p1
                    boolean isBetween =
                            ((p2.getX() - p1.getX()) * (p1a.getY() - p1.getY()) - (p1a.getX() - p1.getX()) * (p2.getY() - p1.getY()) > 0 &&
                                    (p2.getX() - p1b.getX()) * (p1.getY() - p1b.getY()) - (p1.getX() - p1b.getX()) * (p2.getY() - p1b.getY()) > 0) ||
                                    ((p1.getX() - p2.getX()) * (p2a.getY() - p2.getY()) - (p2a.getX() - p2.getX()) * (p1.getY() - p2.getY()) > 0 &&
                                            (p1.getX() - p2b.getX()) * (p2.getY() - p2b.getY()) - (p2.getX() - p2b.getX()) * (p1.getY() - p2b.getY()) > 0);
                    //(a.getX() - cut[0].getX()) * (cut[1].getY() - cut[0].getY()) - (cut[1].getX() - cut[0].getX()) * (a.getY() - cut[0].getY()) *
                    //(b.getX() - cut[0].getX()) * (cut[1].getY() - cut[0].getY()) - (cut[1].getX() - cut[0].getX()) * (b.getY() - cut[0].getY()) < 0; // &&
                    //(c.getX() - cut[0].getX()) * (cut[1].getY() - cut[0].getY()) - (cut[1].getX() - cut[0].getX()) * (c.getY() - cut[0].getY()) *
                    //(d.getX() - cut[0].getX()) * (cut[1].getY() - cut[0].getY()) - (cut[1].getX() - cut[0].getX()) * (d.getY() - cut[0].getY()) < 0;

                    if(!isBetween)
                        continue;

                    boolean intersects = false;
                    for (Vertex[] line : lines) {
                        // check if cut and line intersect

                        float x, y;

                        if(
                                cut[0]==line[0] ||
                                        cut[1]==line[0] ||
                                        cut[0]==line[1] ||
                                        cut[1]==line[1]
                        ) continue;

                        // if both line are vertical
                        if (cut[0].getX() == cut[1].getX() && line[0].getX() == line[1].getX()) {
                            continue;
                        } else if (cut[0].getX() == cut[1].getX()) {
                            float m = (line[0].getY() - line[1].getY()) / (line[0].getX() - line[1].getX());
                            x = cut[0].getX();
                            y = -m * line[0].getX() + line[0].getY() + m * x;
                        } else if (line[0].getX() == line[1].getX()) {
                            float m = (cut[0].getY() - cut[1].getY()) / (cut[0].getX() - cut[1].getX());
                            x = line[0].getX();
                            y = -m * cut[0].getX() + cut[0].getY() + m * x;
                        } else {
                            float m1 = (cut[0].getY() - cut[1].getY()) / (cut[0].getX() - cut[1].getX());
                            float m2 = (line[0].getY() - line[1].getY()) / (line[0].getX() - line[1].getX());

                            if (m1 == m2)// parallel
                                continue;

                            x = (-m1 * cut[0].getX() + cut[0].getY() + m2 * line[0].getX() - line[0].getY()) / (m2 - m1);
                            y = -m1 * cut[0].getX() + cut[0].getY() + m1 * x;
                        }

                        // if in both bounding boxes, cut intersect line
                        intersects =
                                (x - cut[0].getX()) * (x - cut[1].getX()) <= 0 &&
                                        (y - cut[0].getY()) * (y - cut[1].getY()) <= 0 &&
                                        (x - line[0].getX()) * (x - line[1].getX()) <= 0 &&
                                        (y - line[0].getY()) * (y - line[1].getY()) <= 0;

                        if (intersects) {
//                            System.out.printf("%d:%d-%d:%d intersects %d:%d-%d:%d\n",
//                                    cut[0].getX(), cut[0].getY(), cut[1].getX(), cut[1].getY(),
//                                    line[0].getX(), line[0].getY(), line[1].getX(), line[1].getY());
                            break;
                        }
                    }

                    if (!intersects)
                        cutFound = true;
                }
            }

//            System.out.printf("cut polygon: ");
//            for(Vertex v : vertices)
//                System.out.printf("%d:%d ", v.getX(), v.getY());
//            System.out.printf("at: %d,%d\n", indexOfCut1, indexOfCut2);

            // divide polygon
            Vertex[] polygon1 = new Vertex[indexOfCut2-indexOfCut1 + 1];
            Vertex[] polygon2 = new Vertex[vertices.length - polygon1.length + 2];

            for(int i = 0; i < polygon1.length; i++)
                polygon1[i] = vertices[indexOfCut1 + i];
            for(int i = 0; i < polygon2.length; i++)
                polygon2[i] = vertices[(indexOfCut2+i)%vertices.length];

            Vertex[][] triangles1 = decomposeCWPolygon(polygon1);
            Vertex[][] triangles2 = decomposeCWPolygon(polygon2);

            // merge arrays
            Vertex[][] trianglesTotal = new Vertex[triangles1.length+triangles2.length][3];
            for(int i = 0; i < triangles1.length; i++)
                trianglesTotal[i] = triangles1[i];
            for(int i = 0; i < triangles2.length; i++)
                trianglesTotal[triangles1.length+i] = triangles2[i];

            return trianglesTotal;
        }

        // convex only polygon decomposition
//        Vertex[][] triangles = new Vertex[vertices.length-2][3];
//
//        for(int i = 0; i < triangles.length; i++) {
//            triangles[i][0] = vertices[0];
//            triangles[i][1] = vertices[i + 1];
//            triangles[i][2] = vertices[i + 2];
//        }

    }

}
