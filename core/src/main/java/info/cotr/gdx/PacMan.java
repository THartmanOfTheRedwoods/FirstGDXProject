package info.cotr.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class PacMan {
    private float x, y;
    private int size = 9;
    private Color color = Color.YELLOW;
    private Maze maze;
    private int tileSize; // Size of each tile in the maze
    private float deltaX, deltaY; // Movement deltas
    private boolean isMovingX; // Whether Pac-Man is currently moving horizontally

    public PacMan(float x, float y, Maze maze) {
        this.x = x;
        this.y = y;
        this.maze = maze;
        this.tileSize = maze.getTileSize();
        this.isMovingX = true; // Start by moving horizontally
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(color);
        shapeRenderer.circle(x, y, size);
    }

    public void move(float deltaX, float deltaY) {
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
        } else if (Math.abs(deltaY) > Math.abs(deltaX)) {
            // Moving vertically
            if (isMovingX) {
                // Snap to the nearest column before changing direction
                x = snapToTileCenter(x);
                isMovingX = false;
            }
            this.deltaY = deltaY;
            this.deltaX = 0; // Restrict horizontal movement
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

    // Snap a coordinate to the center of the nearest tile
    private float snapToTileCenter(float coordinate) {
        int tileIndex = (int) (coordinate / tileSize);
        return tileIndex * tileSize + tileSize / 2;
    }

    public float getX() { return x; }
    public float getY() { return y; }
}
