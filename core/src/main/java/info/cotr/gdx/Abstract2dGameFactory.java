package info.cotr.gdx;

public interface Abstract2dGameFactory {
    // TODO: add create2dEnvironment() factory to this family
    // The family of objects this will create is the 2D Game board and proper family of characters for this game.
    // So, a PacMan game will get a PacMan Maze + PacMan and Ghosts, other types of games will be different.
    // Eventually Maze should be a different interface like Environment.
    // TODO: Handle dependent character either through setter or via Dependency Injection via Google GUICE DI framework.
    Character createCharacter(float x, float y, CharacterType type, Maze maze, Character dependent);
    Character[] createCharacters(Maze maze);
}
