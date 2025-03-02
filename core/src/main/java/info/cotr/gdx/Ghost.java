package info.cotr.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import java.util.Objects;  // Used in Node hashcode comparison.
import java.util.Comparator;  // Used in comparing PriorityQueue Node fScores.
import java.util.Collections;
import java.util.List;  // Interface for ArrayList and LinkedList.
import java.util.ArrayList;  // Returns North, South, East, and West neighbor tiles.
import java.util.LinkedList;  // Used to return Path for ghost to follow toward PacMan.
import java.util.Map;  // Interface for HashMap.
import java.util.HashMap;  // Used to track fScore, gScore, and cameFrom in A* shortest path algorithm.
import java.util.Queue;  // Interface for PriorityQueue.
import java.util.PriorityQueue;  // A Queue data-structure prioritized by fScore in A* algorithm.

public class Ghost implements Character {
    private final CharacterType type;
    private float x, y;
    private final int size = 9;
    private final Color color;
    private final Maze maze;
    private final int tileSize;
    private boolean isMovingX;
    private final Character pacMan;
    private Vector2 targetTile;
    private Queue<Vector2> path;
    private Vector2 currentTarget; // Current target tile the ghost is moving toward
    private float speed; // Adjust speed as needed
    private final Character redGhost; // Some ghost movement is dependent on the redGhost (Shadow)
    private final List<CharacterObserver> characterObservers;

    // TODO: Create a Ghost constructor that takes a path to a json file that configures the ghost object.
    public Ghost(float x, float y, CharacterType type, Maze maze, Character pacMan, Character redGhost) {
        this.x = x;
        this.y = y;
        this.speed = 60;
        this.maze = maze;
        this.tileSize = maze.getTileSize();
        this.pacMan = pacMan;
        this.type = type;
        this.path = new LinkedList<>();
        this.color = getColorByType(type);
        this.isMovingX = true; // Initialize ghost moving horizontally.
        this.currentTarget = null; // No target initially
        this.redGhost = redGhost;
        this.characterObservers = new ArrayList<>();
    }

    public Ghost(float x, float y, CharacterType type, Maze maze, Character pacMan) {
        this(x, y, type, maze, pacMan,null);
    }

    private Color getColorByType(CharacterType type) {
        switch (type) {
            case SHADOW: return Color.RED;
            case SPEEDY: return Color.PINK;
            case BASHFUL: return Color.CYAN;
            case POKEY: return Color.ORANGE;
            default: return Color.WHITE;
        }
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(color);
        shapeRenderer.circle(x, y, size);
    }

    @Override
    public void update(float deltaTime) {
        // If there's no path or the ghost has reached the final goal, calculate a new path
        if (path.isEmpty() || (currentTarget == null && hasReachedTarget(targetTile))) {
            calculateTargetTile();
            path = findPathToTarget();
            currentTarget = path.poll(); // Get the first tile in the new path
        }

        // Move toward the current target
        if (currentTarget != null) {
            move(currentTarget.x, currentTarget.y, deltaTime);

            // Check if the ghost has reached the current target
            if (hasReachedTarget(currentTarget)) {
                if (!path.isEmpty()) {
                    currentTarget = path.poll(); // Get the next tile in the path
                } else {
                    currentTarget = null; // No more tiles in the path
                }
            }
        }
    }

    private void calculateTargetTile() {
        Vector2 originalTarget = null;
        Vector2 pacManPos;
        Vector2 pacManDirection;

        switch (type) {
            case SHADOW:
                // Target PacMan's current tile
                originalTarget = new Vector2(pacMan.getX(), pacMan.getY());
                break;
            case SPEEDY:
                // Step 1: Get Pac-Man's position
                pacManPos = new Vector2(pacMan.getX(), pacMan.getY());
                // Step 2: Determine the tile four spaces ahead of Pac-Man in his current direction
                pacManDirection = pacMan.getDirection();
                originalTarget = pacManPos.add(pacManDirection.scl(4 * tileSize));
                break;
            case BASHFUL:
                // Step 1: Get Pac-Man's position
                pacManPos = new Vector2(pacMan.getX(), pacMan.getY());
                // Step 2: Determine the tile two spaces ahead of Pac-Man in his current direction
                pacManDirection = pacMan.getDirection();
                Vector2 twoTilesAhead = pacManPos.add(pacManDirection.scl(2 * tileSize));
                // Step 3: Mirror the calculated tile relative to Blinky's position
                Vector2 redGhostPos = new Vector2(redGhost.getX(), redGhost.getY());
                originalTarget = redGhostPos.sub(twoTilesAhead).scl(2).add(twoTilesAhead);
                break;
            case POKEY:
                if (Vector2.dst(x, y, pacMan.getX(), pacMan.getY()) > 8 * tileSize) {
                    originalTarget = new Vector2(pacMan.getX(), pacMan.getY());
                } else {
                    originalTarget = new Vector2(0, 0); // Bottom-left corner
                }
                break;
        }

        // Ensure the target tile is walkable
        targetTile = findNearestWalkableTile(originalTarget);
    }

    // Spiral search for walkable targetTile
    private Vector2 findNearestWalkableTile(Vector2 target) {
        int targetX = (int) (target.x / tileSize);
        int targetY = (int) (target.y / tileSize);

        // If the target tile is walkable, return it
        if(maze.isInMaze(targetX, targetY) && maze.isWalkable(targetX, targetY)) {
            return target;
        }

        // Search for the nearest walkable tile in a spiral pattern
        int maxRadius = 5; // Maximum search radius (adjust as needed)
        for (int radius = 1; radius <= maxRadius; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    int newX = targetX + dx;
                    int newY = targetY + dy;

                    // Check if the new position is within the maze bounds
                    // Check if the tile is walkable
                    if(maze.isInMaze(newX, newY) && maze.isWalkable(newX, newY)) {
                        return new Vector2(newX * tileSize + tileSize / 2.0f, newY * tileSize + tileSize / 2.0f);
                    }
                }
            }
        }

        // If no walkable tile is found, return the original target (this should not happen in a valid maze)
        return target;
    }

    /**
     * A* algorithm for finding the shortest path for a ghost.
     */
    private Queue<Vector2> findPathToTarget() {
        // Convert current position and target position to grid coordinates
        int startX = (int) (x / tileSize);
        int startY = (int) (y / tileSize);
        int targetX = (int) (targetTile.x / tileSize);
        int targetY = (int) (targetTile.y / tileSize);

        // Map of current Node Key to parent Node Value allowing to track current nodes back to where they originated.
        Map<Node, Node> cameFrom = new HashMap<>();
        // gScore is the cost from the start node to the current node
        // i.e. the sum of the costs of all nodes taken to reach the current node.
        // In my implementation all nodes have a cost of 1, so gScore simply becomes the number of steps.
        // gScore ensures that we find the ACTUAL shortest path by tracking ACTUAL cost in reaching each node.
        Map<Node, Integer> gScore = new HashMap<>();
        // fScore is the estimated total cost from start to goal through the node
        // i.e. the sum of gScore and heuristic estimate (heuristic estimate being the manhattan distance to the goal)
        // My heuristic function merely guides the algorithm toward the goal.
        Map<Node, Integer> fScore = new HashMap<>();
        // Compare and Prioritize nodes based on their fScore.
        // Nodes with the lowest fScore (i.e. estimated total cost) are prioritized.
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(n -> fScore.getOrDefault(n, Integer.MAX_VALUE)));

        Node startNode = new Node(startX, startY);
        Node targetNode = new Node(targetX, targetY);

        gScore.put(startNode, 0);
        fScore.put(startNode, heuristic(startNode, targetNode));
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.equals(targetNode)) {  // We found the target node, so there is a path back to the ghost's node.
                // Reconstruct the path
                return reconstructPath(cameFrom, current);
            }

            for (Node neighbor : getNeighbors(current)) {
                // Cost from start to neighbor, +1 because each neighbor is simply 1 more cost step in PacMan.
                int tentativeGScore = gScore.get(current) + 1;
                // If this score is better than any previous one, update the scores.
                if (tentativeGScore < gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, tentativeGScore + heuristic(neighbor, targetNode));

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return new LinkedList<>(); // No path found
    }

    private Queue<Vector2> reconstructPath(Map<Node, Node> cameFrom, Node current) {
        Queue<Vector2> path = new LinkedList<>();
        while (cameFrom.containsKey(current)) {
            path.add(new Vector2(current.x * tileSize + tileSize / 2.0f, current.y * tileSize + tileSize / 2.0f));
            current = cameFrom.get(current);
        }
        Collections.reverse((List<?>) path);
        return path;
    }

    private List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();
        int[] dx = { -1, 1, 0, 0 };
        int[] dy = { 0, 0, -1, 1 };

        for (int i = 0; i < 4; i++) {
            int newX = node.x + dx[i];
            int newY = node.y + dy[i];

            if (maze.isInMaze(newX, newY) && maze.isWalkable(newX, newY)) {
                neighbors.add(new Node(newX, newY));
            }
        }

        return neighbors;
    }

    private int heuristic(Node a, Node b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y); // Manhattan distance
    }

    public void move(float targetX, float targetY,  float deltaTime) {
        // Calculate direction to the target
        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            dx /= distance;
            dy /= distance;
        }

        // Determine the primary movement direction
        float deltaX;
        float deltaY;
        if (Math.abs(dx) > Math.abs(dy)) {
            // Moving horizontally
            if (!isMovingX) {
                // Snap to the nearest row before changing direction
                y = snapToTileCenter(y);
                isMovingX = true;
            }
            deltaX = dx;
            deltaY = 0; // Restrict vertical movement
        } else {
            // Moving vertically
            if (isMovingX) {
                // Snap to the nearest column before changing direction
                x = snapToTileCenter(x);
                isMovingX = false;
            }
            deltaY = dy;
            deltaX = 0; // Restrict horizontal movement
        }

        // Calculate the new position
        float newX = x + deltaX * speed * deltaTime;
        float newY = y + deltaY * speed * deltaTime;

        // Check if the new position collides with a wall
        if (!maze.collidesWithWall(newX, newY, size)) {
            x = newX;
            y = newY;
        }
        if(collidesWithPacman(size)) {
            notifyWatchers(PacManEvent.GHOST_HIT_PACMAN);  // Notify of collision
        }
    }

    // Check if the ghost has reached the current target tile
    private boolean hasReachedTarget(Vector2 target) {
        float distance = Vector2.dst(x, y, target.x, target.y);
        return distance < tileSize / 2.0f; // Consider the ghost "reached" if it's close enough
    }

    // Snap a coordinate to the center of the nearest tile
    private float snapToTileCenter(float coordinate) {
        int tileIndex = (int) (coordinate / tileSize);
        return tileIndex * tileSize + tileSize / 2.0f;
    }

    // Check ghost's collision with Pac-Man's bounds
    public boolean collidesWithPacman(float radius) {
        // Check multiple points around ghost's circle
        float[] pointsX = {x - radius, x + radius, x, x};
        float[] pointsY = {y, y, y - radius, y + radius};
        for (int i = 0; i < pointsX.length; i++) {
            if (hitPacman(pointsX[i], pointsY[i])) {
                return true;
            }
        }
        return false;
    }

    public boolean hitPacman(float x, float y) {
        int radius = pacMan.getSize();
        float px = pacMan.getX();
        float py = pacMan.getY();
        double distance = Math.sqrt(Math.pow(x-px, 2) + Math.pow(y-py, 2));
        return distance < radius;
    }

    @Override
    public float getX() { return x; }
    @Override
    public float getY() { return y; }
    @Override
    public int getSize() { return this.size; }

    @Override
    public Vector2 getDirection() { return null; }  // Don't really care about this for Ghost objects.

    @Override
    public float getSpeed() {
        return this.speed;
    }

    @Override
    public void setSpeed(float speed) { this.speed = speed; }

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

    // Node class for A* algorithm
    private static class Node {
        int x, y;

        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return x == node.x && y == node.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
}
