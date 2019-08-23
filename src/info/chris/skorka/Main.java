package info.chris.skorka;
import java.util.LinkedList;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;

public class Main {
    private static Random random = new Random();
    private static long nextAsteroidTime = System.currentTimeMillis();
    private static int score = 0;

    private static final int MIN_ASTEROID_N = 8;
    private static final int MAX_ASTEROID_N = 15;
    private static final int MIN_ASTEROID_RADIUS = 10;
    private static final int MAX_ASTEROID_RADIUS = 20;
    private static final int MIN_ASTEROID_SPAWN_TIME = 500;
    private static final int MAX_ASTEROID_SPAWN_TIME = 2000;
    private static final int MAX_ASTEROID_COUNT = 25;
    private static final int SCORE_SCALE = 3;

    private static final int SPACESHIP_ACCELERATION = 800;
    private static final int SPACESHIP_PASSIVE_DRAG = 10;
    private static final int SPACESHIP_ACTIVE_DRAG = 30;
    private static final int SPACESHIP_ROTATION_SPEED = 5;

    public static void main(String[] args) {


        Entity spaceship = new Entity(50, 50, new Polygon (
                new Color(0x8899AA),
                new Color(0x335588),
                new Vertex(0,15),
                new Vertex(15, -15),
                new Vertex(0, -5),
                new Vertex(-15, -15)
        ), new Polygon(
                new Color(0x5588FF),
                new Color(0x0055FF),
                new Vertex(-10, -13),
                new Vertex(-4, -9),
                new Vertex(-8, -20)
        ), new Polygon(
                new Color(0x5588FF),
                new Color(0x0055FF),
                new Vertex(10, -13),
                new Vertex(4, -9),
                new Vertex(8, -20)
        ));
        spaceship.d = 10;

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

        OpenGlWindow window;
        window = new OpenGlWindow(
                510,
                290,
                1,
                "Pixels",
                new OpenGlWindow.DrawEventListener() {
                    @Override
                    public void onDraw(OpenGlWindow w, long millis, long delta) {
                        // System.out.println(delta);

                        //w.clear(1,0,0,1);
                        w.fill(new Color(0x000000));
                        w.stroke(null);
                        w.clear(0,0,0,0);

                        if(System.currentTimeMillis() > nextAsteroidTime && asteroids.size() < MAX_ASTEROID_COUNT){
                            asteroids.add(newAsteroid(w));
                            nextAsteroidTime =
                                    System.currentTimeMillis() +
                                    MIN_ASTEROID_SPAWN_TIME +
                                    random.nextInt(MAX_ASTEROID_SPAWN_TIME - MIN_ASTEROID_SPAWN_TIME);
                        }

                        Boundary spaceshipBoundary = spaceship.getBoundary();
                        if(spaceshipBoundary.getX1() < 0 || spaceshipBoundary.getX2() > w.getWidth())
                            spaceship.vx = -spaceship.vx;
                        if(spaceshipBoundary.getY1() < 0 || spaceshipBoundary.getY2() > w.getHeight())
                            spaceship.vy = -spaceship.vy;
                        spaceship.update(millis, delta);
                        spaceship.draw(w);

                        LinkedList<Entity> toBeRemoved = new LinkedList<Entity>();
                        for(Entity e : asteroids){
                            e.update(millis, delta);
                            Boundary asteroidBoundary = e.getBoundary();
                            if(asteroidBoundary.getX1() < 0 || asteroidBoundary.getX2() > w.getWidth())
                                e.vx = -e.vx;
                            if(asteroidBoundary.getY1() < 0 || asteroidBoundary.getY2() > w.getHeight())
                                e.vy = -e.vy;

                            if(asteroidBoundary.intersects(spaceshipBoundary)){
                                toBeRemoved.add(e);
                                score += 1;
                            }else{
                                e.draw(w);
                            }
                        }
                        for(Entity e : toBeRemoved)
                            asteroids.remove(e);

                        // draw score
                        String scoreStr = Integer.toString(score);
                        int[] digits = new int[scoreStr.length()];
                        for(int i = 0; i < scoreStr.length(); i++)
                            digits[i] = scoreStr.charAt(i) - '0';

                        w.fill(new Color(0x88FFFFFFL));

//                        w.rotateZ(0.3);
//                        w.scale(3,3,0);
//                        w.translate(0,-50);

                        w.translate(5, w.getHeight() - 7*SCORE_SCALE - 10);
                        for(int i = 0; i < digits.length; i++){
                            w.translate(5*SCORE_SCALE, 0);
                            w.bitmap(numberBitMaps[digits[i]]);
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

    private static Entity newAsteroid(OpenGlWindow window){

        int n = MIN_ASTEROID_N + random.nextInt(MAX_ASTEROID_N - MIN_ASTEROID_N);
        Vertex[] vertices = new Vertex[n];
        for(int i = 0; i < n; i++){
            double d = MIN_ASTEROID_RADIUS + random.nextDouble() * (MAX_ASTEROID_RADIUS - MIN_ASTEROID_RADIUS);
            double r = Math.PI*2*i/n;
            vertices[i] = new Vertex(Math.cos(r) * d, Math.sin(r) * d);
        }

        double x = random.nextDouble() * (window.getWidth() - 2 * MAX_ASTEROID_RADIUS) + MAX_ASTEROID_RADIUS;
        double y = random.nextDouble() * (window.getHeight() - 2 * MAX_ASTEROID_RADIUS) + MAX_ASTEROID_RADIUS;

        Entity asteroid = new Entity(x, y, new Polygon (new Color(0x555555), new Color(0x888888), vertices));
        asteroid.vr = random.nextDouble() * 2;
        asteroid.vx = random.nextDouble() * 20 - 10;
        asteroid.vy = random.nextDouble() * 20 - 10;

        return asteroid;
    }


}
