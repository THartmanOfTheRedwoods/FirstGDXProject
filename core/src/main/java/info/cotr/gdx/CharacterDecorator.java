package info.cotr.gdx;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.InputAdapter;

public abstract class CharacterDecorator extends InputAdapter implements Character {
    protected final Character character;

    public CharacterDecorator(Character character) {
        this.character = character;
    }
    @Override
    public void watchCharacter(CharacterObserver characterObserver) {
        character.watchCharacter(characterObserver);
    }

    @Override
    public void stopWatchingCharacter(CharacterObserver characterObserver) {
        character.stopWatchingCharacter(characterObserver);
    }

    @Override
    public void notifyWatchers(PacManEvent event) {
        character.notifyWatchers(event);
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        character.render(shapeRenderer);
    }

    @Override
    public float getX() {
        return character.getX();
    }

    @Override
    public float getY() {
        return character.getY();
    }

    @Override
    public int getSize() {
        return character.getSize();
    }

    @Override
    public Vector2 getDirection() {
        return character.getDirection();
    }

    @Override
    public float getSpeed() {
        return this.character.getSpeed();
    }

    @Override
    public void setSpeed(float speed) {
        this.character.setSpeed(speed);
    }

    @Override
    public void move(float deltaX, float deltaY,  float deltaTime) {
        this.character.move(deltaX, deltaY, deltaTime);
    }

    @Override
    public abstract boolean keyDown(int keycode);

    @Override
    public abstract boolean keyUp(int keycode);
}
