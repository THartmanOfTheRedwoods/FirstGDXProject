package info.cotr.gdx;

import com.badlogic.gdx.InputAdapter;

public class PacManInputProcessor extends InputAdapter {
    private PacMan pacMan;
    private float speed = 70; // Movement speed
    private boolean upPressed, downPressed, leftPressed, rightPressed;

    public PacManInputProcessor(PacMan pacMan) {
        this.pacMan = pacMan;
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case com.badlogic.gdx.Input.Keys.UP:
                upPressed = true;
                break;
            case com.badlogic.gdx.Input.Keys.DOWN:
                downPressed = true;
                break;
            case com.badlogic.gdx.Input.Keys.LEFT:
                leftPressed = true;
                break;
            case com.badlogic.gdx.Input.Keys.RIGHT:
                rightPressed = true;
                break;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case com.badlogic.gdx.Input.Keys.UP:
                upPressed = false;
                break;
            case com.badlogic.gdx.Input.Keys.DOWN:
                downPressed = false;
                break;
            case com.badlogic.gdx.Input.Keys.LEFT:
                leftPressed = false;
                break;
            case com.badlogic.gdx.Input.Keys.RIGHT:
                rightPressed = false;
                break;
        }
        return true;
    }

    public void update(float delta) {
        float deltaX = 0, deltaY = 0;

        if (upPressed) deltaY += speed * delta;
        if (downPressed) deltaY -= speed * delta;
        if (leftPressed) deltaX -= speed * delta;
        if (rightPressed) deltaX += speed * delta;

        pacMan.move(deltaX, deltaY);
    }
}
