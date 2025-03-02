package info.cotr.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class PacMan implements Character {
    private CharacterType type;
    private float x, y;
    private final int size = 9;
    private final Color color = Color.YELLOW;
    private float speed; // Adjust speed as needed
    private Maze maze;
    private int tileSize; // Size of each tile in the maze
    private float deltaX, deltaY; // Movement deltas
    private boolean isMovingX; // Whether Pac-Man is currently moving horizontally
    private Vector2 direction; // Current direction of Pac-Man
    private List<CharacterObserver> characterObservers;

    // PacMan is a Singleton object as there is ever only 1 PacMan
    private PacMan(){}

    private static class SingletonHelper {
        private static final PacMan INSTANCE = new PacMan();
    }

    public static PacMan getInstance() {
        return SingletonHelper.INSTANCE;
    }

    // TODO: Create a PacMan initializaer that takes a path to a json file and configures the PacMan object.
    // Method to initialize the singleton with parameters since we can't pass to a constructor for the Bill Pugh style.
    public PacMan initialize(float x, float y, Maze maze) {
        if (this.characterObservers == null) { // Only instantiate these once on the object.
            this.x = x;
            this.y = y;
            this.speed = 70;
            this.maze = maze;
            this.tileSize = maze.getTileSize();
            this.type = CharacterType.PACMAN;
            this.isMovingX = true; // Start by moving horizontally
            this.direction = new Vector2(1, 0); // Default direction: right
            this.characterObservers = new ArrayList<>();
        }
        // I return myself on the initialize so that I can put the getInstance().initialize() calls together
        return this;
    }
    // End Singleton Code

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(color);
        shapeRenderer.circle(x, y, size);
    }

    public void move(float deltaX, float deltaY,  float deltaTime) {
        // Determine the primary movement direction
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            // Moving horizontally
            if (!isMovingX) {
                // Snap to the nearest row before changing direction
                y = snapToTileCenter(y);
                isMovingX = true;
            }
            this.deltaX = deltaX;
            this.deltaY = 0; // Restrict vertical movement
            updateDirection(deltaX, 0); // Update direction based on horizontal movement
        } else if (Math.abs(deltaY) > Math.abs(deltaX)) {
            // Moving vertically
            if (isMovingX) {
                // Snap to the nearest column before changing direction
                x = snapToTileCenter(x);
                isMovingX = false;
            }
            this.deltaY = deltaY;
            this.deltaX = 0; // Restrict horizontal movement
            updateDirection(0, deltaY); // Update direction based on vertical movement
        }

        // Calculate the new position
        float newX = x + this.deltaX;
        float newY = y + this.deltaY;

        // Check if the new position collides with a wall
        if (!maze.collidesWithWall(newX, newY, size)) {
            x = newX;
            y = newY;
        }
    }

    // Update Pac-Man's direction based on movement deltas
    private void updateDirection(float deltaX, float deltaY) {
        if (deltaX > 0) {
            direction.set(1, 0); // Right
        } else if (deltaX < 0) {
            direction.set(-1, 0); // Left
        } else if (deltaY > 0) {
            direction.set(0, 1); // Up
        } else if (deltaY < 0) {
            direction.set(0, -1); // Down
        }
    }

    // Get Pac-Man's current direction
    public Vector2 getDirection() {
        return direction;
    }

    @Override
    public void update(float delta) {

    }

    @Override
    public float getSpeed() {
        return this.speed;
    }

    @Override
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    // Snap a coordinate to the center of the nearest tile
    private float snapToTileCenter(float coordinate) {
        int tileIndex = (int) (coordinate / tileSize);
        return tileIndex * tileSize + tileSize / 2.0f;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getSize() { return this.size; }

    @Override
    public void watchCharacter(CharacterObserver characterObserver) {
        this.characterObservers.add(characterObserver);
    }

    @Override
    public void stopWatchingCharacter(CharacterObserver characterObserver) {
        this.characterObservers.remove(characterObserver);
    }

    @Override
    public void notifyWatchers(PacManEvent event) {
        for(CharacterObserver g : this.characterObservers) {
            g.update(this, event);
        }
    }
}
