package info.chris.skorka;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;

public class Main {
    private static Random random = new Random();
    private static long nextAsteroidTime = System.currentTimeMillis();
    private static int score = 0;

    private static final int WIDTH = 1500;
    private static final int HEIGHT = 900;
    private static final int SCALE = 1;

    private static final int MIN_ASTEROID_N = 4;
    private static final int MAX_ASTEROID_N = 9;
    private static final int MIN_ASTEROID_RADIUS = 10;
    private static final int MAX_ASTEROID_RADIUS = 20;
    private static final int MIN_ASTEROID_SPAWN_TIME = 500;
    private static final int MAX_ASTEROID_SPAWN_TIME = 2000;
    private static final int MAX_ASTEROID_COUNT = 15;
    private static final int MAX_ASTEROID_SPEED = 100;
    private static final int MAX_ASTEROID_ROTATION_SPEED = 3;
    private static final int SCORE_SCALE = 3;

    private static final int SPACESHIP_ACCELERATION = 800;
    private static final int SPACESHIP_PASSIVE_DRAG = 10;
    private static final int SPACESHIP_ACTIVE_DRAG = 30;
    private static final int SPACESHIP_ROTATION_SPEED = 5;

    private static final Boundary space = new Boundary(0,0,WIDTH, HEIGHT);

    public static void main(String[] args) {

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
//        new Polygon (
//                new Color(0x8899AA),
//                new Color(0x335588),
//                new Vertex(0,15),
//                new Vertex(15, -15),
//                new Vertex(0, -5),
//                new Vertex(-15, -15)
//        ), new Polygon(
//                new Color(0x5588FF),
//                new Color(0x0055FF),
//                new Vertex(-10, -13),
//                new Vertex(-4, -9),
//                new Vertex(-8, -20)
//        ), new Polygon(
//                new Color(0x5588FF),
//                new Color(0x0055FF),
//                new Vertex(10, -13),
//                new Vertex(4, -9),
//                new Vertex(8, -20)
//        )
        );
        spaceship.d = SPACESHIP_PASSIVE_DRAG;
        spaceship.bouncyBoundarySpace = space;

        LinkedList<Entity> asteroids = new LinkedList<Entity>();
        
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

        Audio scoreAudio = new Audio("/Pickup_Coin.wav");
        Audio collisionAudio = new Audio("/Explosion.wav");

        OpenGlWindow window;
        window = new OpenGlWindow(
                WIDTH,
                HEIGHT,
                SCALE,
                "Pixels",
                new OpenGlWindow.DrawEventListener() {
                    @Override
                    public void onDraw(Context c, long millis, long delta) {
                        // System.out.println(delta);

                        // clear screen
                        c.fill(new Color(0x000000));
                        c.stroke(null);
                        c.clear(0,0,0,0);

                        if(System.currentTimeMillis() > nextAsteroidTime && asteroids.size() < MAX_ASTEROID_COUNT){
                            asteroids.add(newAsteroid(asteroids));
                            nextAsteroidTime =
                                    System.currentTimeMillis() +
                                    MIN_ASTEROID_SPAWN_TIME +
                                    random.nextInt(MAX_ASTEROID_SPAWN_TIME - MIN_ASTEROID_SPAWN_TIME);
                        }

                        // update spaceship
                        CircularBoundary spaceshipBoundary = spaceship.getCircularBoundary();
                        spaceship.update(millis, delta);
                        spaceship.draw(c);

                        // update asteroids and check for collisions with spaceship and other asteroids
                        LinkedList<Entity> toBeRemoved = new LinkedList<Entity>();
                        for(Entity e : asteroids){
                            e.update(millis, delta);
                            CircularBoundary eBoundary = e.getCircularBoundary();

                            if(eBoundary.intersects(spaceshipBoundary)){
                                toBeRemoved.add(e);
                                score += 1;
                                scoreAudio.play();
                            }else{
                                e.draw(c);
                            }

                            for(Entity f : asteroids){
                                CircularBoundary fBoundary = f.getCircularBoundary();

                                if(e != f && eBoundary.intersects(fBoundary)){
                                    double fvx = f.vx;
                                    double fvy = f.vy;
                                    f.vx = e.vx;
                                    f.vy = e.vy;
                                    e.vx = fvx;
                                    e.vy = fvy;
                                    e.vr = random.nextDouble() * 2 * MAX_ASTEROID_ROTATION_SPEED - MAX_ASTEROID_ROTATION_SPEED;
                                    f.vr = random.nextDouble() * 2 * MAX_ASTEROID_ROTATION_SPEED - MAX_ASTEROID_ROTATION_SPEED;

                                    collisionAudio.play();
                                    // new Audio("/Explosion.wav").play();
                                }
                            }
                        }
                        asteroids.removeAll(toBeRemoved);

                        // draw score
                        String scoreStr = Integer.toString(score);
                        int[] digits = new int[scoreStr.length()];
                        for(int i = 0; i < scoreStr.length(); i++)
                            digits[i] = scoreStr.charAt(i) - '0';

                        c.fill(new Color(0x88FFFFFFL));

//                        c.rotateZ(0.3);
//                        c.scale(3,3,0);
//                        c.translate(0,-50);

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

    private static Entity newAsteroid(List<Entity> asteroids){

        int n = MIN_ASTEROID_N + random.nextInt(MAX_ASTEROID_N - MIN_ASTEROID_N);
        Vertex[] vertices = new Vertex[n];
        for(int i = 0; i < n; i++){
            double d = MIN_ASTEROID_RADIUS + random.nextDouble() * (MAX_ASTEROID_RADIUS - MIN_ASTEROID_RADIUS);
            double r = Math.PI*2*i/n;
            vertices[i] = new Vertex(Math.cos(r) * d, Math.sin(r) * d);
        }

        double x = 0;
        double y = 0;
        boolean positionFound = false;
        while(!positionFound){
            x = random.nextDouble() * (WIDTH - 2 * MAX_ASTEROID_RADIUS) + MAX_ASTEROID_RADIUS;
            y = random.nextDouble() * (HEIGHT - 2 * MAX_ASTEROID_RADIUS) + MAX_ASTEROID_RADIUS;
            CircularBoundary boundary = new CircularBoundary((int)x, (int)y, MAX_ASTEROID_RADIUS);
            positionFound = true;
            for(Entity e : asteroids){
                if(boundary.intersects(e.getCircularBoundary())){
                    positionFound = false;
                    break;
                }
            }
        }

        Entity asteroid = new Entity(x, y, new Polygon (new Color(0x555555), new Color(0x888888), vertices));
        asteroid.vr = random.nextDouble() * 2 * MAX_ASTEROID_ROTATION_SPEED - MAX_ASTEROID_ROTATION_SPEED;
        asteroid.vx = random.nextDouble() * 2 * MAX_ASTEROID_SPEED - MAX_ASTEROID_SPEED;
        asteroid.vy = random.nextDouble() * 2 * MAX_ASTEROID_SPEED - MAX_ASTEROID_SPEED;
        asteroid.boundarySpace = space;

        return asteroid;
    }


}
