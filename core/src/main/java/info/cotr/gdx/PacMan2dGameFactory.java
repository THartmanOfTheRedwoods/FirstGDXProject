package info.cotr.gdx;

public class PacMan2dGameFactory implements Abstract2dGameFactory {
    @Override
    public Character createCharacter(float x, float y, CharacterType type, Maze maze, Character dependent) {
        switch (type) {
            // Decorate PacMan with an InputProcessor for Keyboard Movement.
            case M_PACMAN: return new PacManInputProcessor(PacMan.getInstance().initialize(x, y, maze));
            case PACMAN:   return PacMan.getInstance().initialize(x, y, maze);
            case SHADOW:   return new Ghost(x, y, CharacterType.SHADOW, maze, PacMan.getInstance());
            case SPEEDY:   return new Ghost(x, y, CharacterType.SPEEDY, maze, PacMan.getInstance());
            case BASHFUL:  return new Ghost(x, y, CharacterType.BASHFUL, maze, PacMan.getInstance(), dependent);
            case POKEY:    return new Ghost(x, y, CharacterType.POKEY, maze, PacMan.getInstance());
            default: return null;
        }
    }

    // TODO: Implement the createEnvironment function to make the Maze
    // TODO: Make this function take a json file and create the characters as defined in the json file.
    // TODO: maze and dependent should either be DI injected or set on characters via setters.
    @Override
    public Character[] createCharacters(Maze maze) {
        Character redGhost = this.createCharacter(130, 200, CharacterType.SHADOW, maze, null);
        return new Character[]{
            this.createCharacter(400, 400, CharacterType.M_PACMAN, maze, null),
            redGhost,
            this.createCharacter(300, 300, CharacterType.SPEEDY, maze, null),
            this.createCharacter(130, 200, CharacterType.BASHFUL, maze, redGhost),
            this.createCharacter(310, 300, CharacterType.POKEY, maze, null),
        };
    }
}
