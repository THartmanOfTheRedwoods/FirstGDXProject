package info.cotr.gdx;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Dots {
    private boolean[][] dots;
    private int dotSize = 5; // Size of each dot
    private int rows, cols;
    private int tileSize; // Size of each tile in the maze

    public Dots(int[][] maze, int tileSize) {
        this.rows = maze.length;
        this.cols = maze[0].length;
        this.tileSize = tileSize;
        dots = new boolean[rows][cols];
        generateDots(maze);
    }

    private void generateDots(int[][] maze) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Add a dot only if there is no wall
                if (maze[i][j] == 0) {
                    dots[i][j] = true;
                } else {
                    dots[i][j] = false;
                }
            }
        }
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(1, 1, 1, 1); // White color for dots
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (dots[i][j]) {
                    // Draw the dot at the center of the tile
                    shapeRenderer.circle(j * tileSize + tileSize / 2, i * tileSize + tileSize / 2, dotSize);
                }
            }
        }
    }

    // Check if a dot exists at a specific position and remove it
    public boolean collectDot(int x, int y) {
        int row = y / tileSize;
        int col = x / tileSize;
        if (row >= 0 && row < rows && col >= 0 && col < cols && dots[row][col]) {
            dots[row][col] = false; // Remove the dot
            return true;
        }
        return false;
    }
}
