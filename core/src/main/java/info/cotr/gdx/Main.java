package info.cotr.gdx;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter implements CharacterObserver {
    private SpriteBatch batch;
    private Texture image;
    private Stage stage;
    private Skin skin;
    // Re-Usable lock that multiple threads can use to wait on multiple lock conditions.
    private final ReentrantLock lock = new ReentrantLock();
    private Condition condition1; // This is a condition from a ReentrantLock used to trigger some game thread.
    private final GameThread[] gameThreads = new GameThread[2];
    // Start of PacMan instance variables
    private ShapeRenderer shapeRenderer;
    private Maze maze;
    //private PacMan pacMan;
    private Dots dots;
    //private PacManInputProcessor pacManInputProcessor;
    private Character[] characters;
    // Scoring variables
    private static final int DOT_VALUE = 10;
    private int score;
    private Label lblScore;

    @Override
    public void create() {
        batch = new SpriteBatch();
        //image = new Texture("libgdx.png");
        image = new Texture("pacman_bg.png");
        stage = new Stage(new ScreenViewport());
        /*
        // Debug statements that helped me determine if uiskin atlas and json files were correctly defined.
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("uiskin.atlas"));
        for (TextureAtlas.AtlasRegion region : atlas.getRegions()) { System.out.println("Atlas contains region: " + region.name); }
        System.out.println(Gdx.files.internal("uiskin.atlas").file().getAbsolutePath());
        */

        // Load the default UI skin
        skin = new Skin();
        skin.addRegions(new TextureAtlas(Gdx.files.internal("uiskin.atlas")));
        // Generate a BitmapFont from the TTF file
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("UnifrakturMaguntia-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 24;  // Set font size
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();  // Dispose to avoid memory leaks
        // Add the generated font to the skin
        skin.add("default-font", font);
        skin.load(Gdx.files.internal("uiskin.json"));

        // Create a window (i.e. more like a draggable component inside the game window)
        Window window = new Window("Super Duper", skin);
        window.setSize(600, 50);
        // Centering Graphic
        //window.setPosition(Gdx.graphics.getWidth() / 2f - 300, Gdx.graphics.getHeight() / 2f - 25); // Center it
        window.setPosition(Gdx.graphics.getWidth() / 2f - 295, Gdx.graphics.getHeight() - 70);

        // Let's set up the Game threads and thread control mechanisms as a demo.
        condition1 = lock.newCondition();
        for(int i=0; i < this.gameThreads.length; i++) {
            gameThreads[i] = new GameThread(lock, condition1, "gt" + i);
            gameThreads[i].start();
        }

        // Create a button
        TextButton button = new TextButton("Demo Button!", skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Demo to signal other threads to run.
                signalGameThreads(condition1);  // Signal all threads waiting on condition1
                // Close the window when the button is clicked
                window.remove();
                // Check if the stage is no longer needed
                if (stage.getActors().size == 0) {
                    // System.out.println("Disposing of stage.");
                    stage.dispose();
                    stage = null;
                }
            }
        });

        // Add the button to the window
        window.add(button).pad(20);
        // Create scoring label
        lblScore = new Label("Score: 0", skin);
        // Add the label to the window
        window.add(lblScore);

        //window.row(); // Move to next row in row layout

        // Make the window draggable
        //window.setMovable(true);

        // Make the ENTIRE window draggable, not just the title bar
        window.addListener(new InputListener() {
            private float startX, startY;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                startX = x;
                startY = y;
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                window.moveBy(x - startX, y - startY);
            }
        });

        // Add window to the stage
        stage.addActor(window);

        // Pacman stuff
        shapeRenderer = new ShapeRenderer();
        // TODO: Move the environment creation to the PacMan2dGameFactory as well.
        // Create the Environment
        maze = new Maze();
        // Initialize dots based on the maze layout
        dots = new Dots(maze.getMazeLayout(), maze.getTileSize());
        // Initialize Game characters
        Abstract2dGameFactory pacManFactory = new PacMan2dGameFactory();
        characters = pacManFactory.createCharacters(maze);

        // Register this Main game class as a character watcher so we can react to character events.
        for(Character c : characters) {
            c.watchCharacter(this);
        }

        // Create an InputMultiplexer
        // Class allowing UI input processing to stage and game control processing to PacmanInputProcessor
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage); // Add the stage first (UI has priority)
        inputMultiplexer.addProcessor((InputAdapter)characters[0]); // Add Pac-Man controls

        // Set the InputMultiplexer as the global input processor
        Gdx.input.setInputProcessor(inputMultiplexer);
        // Set the scoring variables
        score = 0;
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(image, 0, 0);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        dots.render(shapeRenderer);
        maze.render(shapeRenderer);
        for (Character c : characters) {
            c.render(shapeRenderer);
        }
        shapeRenderer.end();

        float deltaTime = Gdx.graphics.getDeltaTime();
        // Update Pac-Man's position based on key states
        // Update the ghost position (e.g. Polymorphism).
        for(Character c : characters) { c.update(deltaTime); }

        // Check if Pac-Man collects a dot
        if (dots.collectDot((int)characters[0].getX(), (int)characters[0].getY())) {
            this.score += DOT_VALUE;
            this.lblScore.setText(this.score);
        }

        if( stage != null) {  // I close the stage with the button, so test to see if I should show it.
            stage.act(deltaTime);
            stage.draw();
        }
    }

    @Override
    public void update(Character character, PacManEvent event) {
        System.out.println("A Ghost got PacMan.");
        this.dispose();
        this.create();
    }

    private void signalGameThreads(Condition condition) {
        lock.lock();  // Acquire the ReentrantLock so we can signal waiting threads. Prevents illegal monitor ex
        try {
            condition.signalAll(); // Since each condition is a Q, this signals all waiting threads in the Q
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void dispose() {
        // Notify all game threads that the game is over
        for (GameThread gameThread : this.gameThreads) { gameThread.gameOver(); }
        signalGameThreads(condition1);  // Signal all threads waiting on condition1 to check if the game is over.
        for (Thread gameThread : this.gameThreads) {  // Wait for game threads to re-join main thread.
            try { gameThread.join(); }
            catch (InterruptedException ignored) { }
        }
        shapeRenderer.dispose();
        batch.dispose();
        image.dispose();
        if(stage != null) {
            // System.out.println("Stage should be null");
            stage.dispose();
        }
        skin.dispose();
    }

}
