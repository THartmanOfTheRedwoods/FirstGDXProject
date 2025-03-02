package info.cotr.gdx;

public class PacManInputProcessor extends CharacterDecorator {
    private boolean upPressed, downPressed, leftPressed, rightPressed;

    public PacManInputProcessor(Character character) {
        super(character);
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

    @Override
    public void update(float delta) {
        float deltaX = 0, deltaY = 0;

        float speed = this.character.getSpeed();

        if (upPressed) deltaY += speed * delta;
        if (downPressed) deltaY -= speed * delta;
        if (leftPressed) deltaX -= speed * delta;
        if (rightPressed) deltaX += speed * delta;

        this.character.move(deltaX, deltaY, delta);
    }
}
