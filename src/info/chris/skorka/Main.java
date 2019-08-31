package info.chris.skorka;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;

public class Main {
    private static Random random = new Random();

    // Configurations and settings

    // display properties
    private static final int WIDTH = 1500;
    private static final int HEIGHT = 900;
    private static final int SCALE = 1;

    // asteroids generations and management
    private static final int MIN_ASTEROID_N = 4;
    private static final int MAX_ASTEROID_N = 9;
    private static final int MIN_ASTEROID_RADIUS = 10;
    private static final int MAX_ASTEROID_RADIUS = 20;
    private static final int MIN_ASTEROID_SPAWN_TIME = 500;
    private static final int MAX_ASTEROID_SPAWN_TIME = 2000;
    private static final int MAX_ASTEROID_COUNT = 25;
    private static final int MAX_ASTEROID_SPEED = 100;
    private static final int MAX_ASTEROID_ROTATION_SPEED = 3;

    // spaceship controls
    private static final int SPACESHIP_ACCELERATION = 800;
    private static final int SPACESHIP_PASSIVE_DRAG = 10;
    private static final int SPACESHIP_ACTIVE_DRAG = 30;
    private static final int SPACESHIP_ROTATION_SPEED = 5;

    // other
    private static boolean playSounds = true;
    private static final int SCORE_SCALE = 3;


    // global state variables
    private static long nextAsteroidTime = System.currentTimeMillis();
    private static int score = 0;
    private static final Boundary space = new Boundary(0,0,WIDTH, HEIGHT);


    /**
     * Main function, loads sounds and bitmaps and creates the openGL window
     * @param args
     */
    public static void main(String[] args) {

        // spaceship definition
        Entity spaceship = new Entity(50, 50,
            new Polygon(
                new Color(0xFF8833),
                new Color(0xFF5500),
                new Vertex(15, 0),
                new Vertex(10, -8),
                new Vertex(15, -12)
            ), new Polygon(
                new Color(0xFF8833),
                new Color(0xFF5500),
                new Vertex(-15, 0),
                new Vertex(-10, -8),
                new Vertex(-15, -12)
            ), new Polygon(
                new Color(0xFF8833),
                new Color(0xFF5500),
                new Vertex(5, -25),
                new Vertex(0, -28),
                new Vertex(3, -32)
            ), new Polygon(
                new Color(0xFF8833),
                new Color(0xFF5500),
                new Vertex(-5, -25),
                new Vertex(-0, -28),
                new Vertex(-3, -32)
            ), new Polygon (
                new Color(0xAA9988),
                new Color(0x885533),
                new Vertex(0,22),
                new Vertex(3,20),
                new Vertex(5, 15),
                new Vertex(5, -5),
                new Vertex(15, 0),
                new Vertex(5, -15),
                new Vertex(5, -20),
                new Vertex(10, -30),
                new Vertex(5, -25),
                new Vertex(-0, -28),
                new Vertex(-5, -25),
                new Vertex(-10, -30),
                new Vertex(-5, -20),
                new Vertex(-5, -15),
                new Vertex(-15, 0),
                new Vertex(-5, -5),
                new Vertex(-5, 15),
                new Vertex(-3,20)
                )
        );
        spaceship.d = SPACESHIP_PASSIVE_DRAG;

        // soft/bouncy boundary space edges
        spaceship.bouncyBoundarySpace = space;

        // list of currently existing asteroids
        LinkedList<Entity> asteroids = new LinkedList<Entity>();

        // boolean bitmap for digits 0-9
        boolean[][][] numberBitMaps = {
                BitMap.scale(SCORE_SCALE, BitMap.from7Segment(new boolean[]{true,  true,  true,  true,  true,  true,  false})),
                BitMap.scale(SCORE_SCALE, BitMap.from7Segment(new boolean[]{false, true,  true,  false, false, false, false})),
                BitMap.scale(SCORE_SCALE, BitMap.from7Segment(new boolean[]{true,  true,  false, true,  true,  false, true })),
                BitMap.scale(SCORE_SCALE, BitMap.from7Segment(new boolean[]{true,  true,  true,  true,  false, false, true })),
                BitMap.scale(SCORE_SCALE, BitMap.from7Segment(new boolean[]{false, true,  true,  false, false, true,  true })),
                BitMap.scale(SCORE_SCALE, BitMap.from7Segment(new boolean[]{true,  false, true,  true,  false, true,  true })),
                BitMap.scale(SCORE_SCALE, BitMap.from7Segment(new boolean[]{true,  false, true,  true,  true,  true,  true })),
                BitMap.scale(SCORE_SCALE, BitMap.from7Segment(new boolean[]{true,  true,  true,  false, false, false, false})),
                BitMap.scale(SCORE_SCALE, BitMap.from7Segment(new boolean[]{true,  true,  true,  true,  true,  true,  true })),
                BitMap.scale(SCORE_SCALE, BitMap.from7Segment(new boolean[]{true,  true,  true,  true,  false, true,  true }))
        };

        // audio objects
        Audio scoreAudio = new Audio("/Pickup_Coin.wav");
        Audio collisionAudio = new Audio("/Explosion.wav");

        // window object
        OpenGlWindow window;
        window = new OpenGlWindow(
                WIDTH,
                HEIGHT,
                SCALE,
                "Rameroids",
                new OpenGlWindow.DrawEventListener() {
                    @Override
                    public void onDraw(Context c, long millis, long delta) {
                        // System.out.println(delta);

                        // clear screen
                        c.fill(new Color(0x000000));
                        c.stroke(null);
                        c.clear(0,0,0,0);

                        // if its time for a new asteroid, generate a new one and re-randomize next asteroid time
                        if(System.currentTimeMillis() > nextAsteroidTime && asteroids.size() < MAX_ASTEROID_COUNT){
                            asteroids.add(newAsteroid(asteroids));
                            nextAsteroidTime =
                                    System.currentTimeMillis() +
                                    MIN_ASTEROID_SPAWN_TIME +
                                    random.nextInt(MAX_ASTEROID_SPAWN_TIME - MIN_ASTEROID_SPAWN_TIME);
                        }

                        // update and draw spaceship
                        CircularBoundary spaceshipBoundary = spaceship.getCircularBoundary();
                        spaceship.update(millis, delta);
                        spaceship.draw(c);

                        // update asteroids and check for collisions with spaceship and other asteroids
                        LinkedList<Entity> toBeRemoved = new LinkedList<Entity>();
                        for(Entity e : asteroids){

                            // update positions and draw
                            e.update(millis, delta);
                            e.draw(c);
                            CircularBoundary eBoundary = e.getCircularBoundary();

                            // if intersects with spaceship, mark it for deletion
                            if(eBoundary.intersects(spaceshipBoundary)){
                                toBeRemoved.add(e);
                                score += 1;

                                // play score sound
                                if(playSounds)
                                    scoreAudio.play();
                            }

                            // check for collisions against all other asteroids
                            for(Entity f : asteroids){
                                CircularBoundary fBoundary = f.getCircularBoundary();

                                // if asteroid e intersects asteroid f, undo 1 time step, swap velocities and randomize rotations
                                if(e != f && eBoundary.intersects(fBoundary)){
                                    e.update(millis, -delta);
                                    f.update(millis, -delta);
                                    double fvx = f.vx;
                                    double fvy = f.vy;
                                    f.vx = e.vx;
                                    f.vy = e.vy;
                                    e.vx = fvx;
                                    e.vy = fvy;
                                    e.vr = random.nextDouble() * 2 * MAX_ASTEROID_ROTATION_SPEED - MAX_ASTEROID_ROTATION_SPEED;
                                    f.vr = random.nextDouble() * 2 * MAX_ASTEROID_ROTATION_SPEED - MAX_ASTEROID_ROTATION_SPEED;

                                    // play collision sound
                                    if(playSounds)
                                        collisionAudio.play();
                                    // new Audio("/Explosion.wav").play();
                                }
                            }
                        }

                        // remove all asteroids marked for deletion
                        asteroids.removeAll(toBeRemoved);

                        // generate array of digits for score
                        String scoreStr = Integer.toString(score);
                        int[] digits = new int[scoreStr.length()];
                        for(int i = 0; i < scoreStr.length(); i++)
                            digits[i] = scoreStr.charAt(i) - '0';

                        // extra pretty transformation
//                        c.rotateZ(0.3);
//                        c.scale(3,3,0);
//                        c.translate(0,-50);

                        // draw score by drawing digit bitmaps
                        c.fill(new Color(0x88FFFFFFL));
                        c.translate(5, c.getHeight() - 7*SCORE_SCALE - 10);
                        for(int i = 0; i < digits.length; i++){
                            c.translate(5*SCORE_SCALE /*+ 30*/, 0);
                            c.bitmap(numberBitMaps[digits[i]]);
                        }

                        // TODO undo transformation if there's anything after this

                    }
                },
                new OpenGlWindow.KeyboardEventListener() {
                    @Override
                    public void onKeyDown(int key) {
                        switch(key){
                            case GLFW_KEY_UP:
                            case GLFW_KEY_W:
                                spaceship.a = SPACESHIP_ACCELERATION;
                                break;
                            case GLFW_KEY_DOWN:
                            case GLFW_KEY_S:
                                spaceship.d = SPACESHIP_ACTIVE_DRAG;
                                break;
                            case GLFW_KEY_LEFT:
                            case GLFW_KEY_A:
                                spaceship.vr = -SPACESHIP_ROTATION_SPEED;
                                break;
                            case GLFW_KEY_RIGHT:
                            case GLFW_KEY_D:
                                spaceship.vr = SPACESHIP_ROTATION_SPEED;
                                break;
                        }
                    }

                    @Override
                    public void onKeyUp(int key) {
                        switch(key){
                            case GLFW_KEY_UP:
                            case GLFW_KEY_W:
                                spaceship.a = 0;
                                break;
                            case GLFW_KEY_DOWN:
                            case GLFW_KEY_S:
                                spaceship.d = SPACESHIP_PASSIVE_DRAG;
                                break;
                            case GLFW_KEY_LEFT:
                            case GLFW_KEY_A:
                            case GLFW_KEY_RIGHT:
                            case GLFW_KEY_D:
                                spaceship.vr = 0;
                                break;
                            case GLFW_KEY_R:
                                Entity.drawBoundaries = !Entity.drawBoundaries;
                                break;
                            case GLFW_KEY_F:
                                playSounds = !playSounds;
                                break;
                        }
                    }
                },
                new OpenGlWindow.MouseEventListener() {
                    @Override
                    public void onMouseDown() {

                    }

                    @Override
                    public void onMouseUp() {

                    }
                }
        );
        window.open();
    }

    /**
     * Generates a new asteroid that is fully inside the bounding space and does not intersect any existing asteroids
     * @param asteroids List of asteroids currently on the screen that the new one should not instersect
     * @return Entity with random position, rotation and simple polygon
     */
    private static Entity newAsteroid(List<Entity> asteroids){

        // number of vertices
        int n = MIN_ASTEROID_N + random.nextInt(MAX_ASTEROID_N - MIN_ASTEROID_N);

        // generate n vertices with a random distance from the center
        Vertex[] vertices = new Vertex[n];
        for(int i = 0; i < n; i++){
            double d = MIN_ASTEROID_RADIUS + random.nextDouble() * (MAX_ASTEROID_RADIUS - MIN_ASTEROID_RADIUS);
            double r = Math.PI*2*i/n;
            vertices[i] = new Vertex(Math.cos(r) * d, Math.sin(r) * d);
        }

        // search for a position where this asteroid does not intersect any other asteroids
        double x = 0;
        double y = 0;
        boolean positionFound = false;
        while(!positionFound){

            // random x, y coordinates
            x = random.nextDouble() * (WIDTH - 2 * MAX_ASTEROID_RADIUS) + MAX_ASTEROID_RADIUS;
            y = random.nextDouble() * (HEIGHT - 2 * MAX_ASTEROID_RADIUS) + MAX_ASTEROID_RADIUS;
            CircularBoundary boundary = new CircularBoundary((int)x, (int)y, MAX_ASTEROID_RADIUS);
            positionFound = true;

            // check against all asteroids
            for(Entity e : asteroids){
                if(boundary.intersects(e.getCircularBoundary())){
                    positionFound = false;
                    break;
                }
            }
        }

        // create Entity object with polygon and position
        Entity asteroid = new Entity(x, y, new Polygon (new Color(0x555555), new Color(0x888888), vertices));

        // generate random speeds
        asteroid.vr = random.nextDouble() * 2 * MAX_ASTEROID_ROTATION_SPEED - MAX_ASTEROID_ROTATION_SPEED;
        asteroid.vx = random.nextDouble() * 2 * MAX_ASTEROID_SPEED - MAX_ASTEROID_SPEED;
        asteroid.vy = random.nextDouble() * 2 * MAX_ASTEROID_SPEED - MAX_ASTEROID_SPEED;

        // hard boundary space edges
        asteroid.boundarySpace = space;

        return asteroid;
    }


}
