package info.cotr.gdx;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public interface Character {
    void watchCharacter(CharacterObserver characterObserver);
    void stopWatchingCharacter(CharacterObserver characterObserver);
    void notifyWatchers(PacManEvent event);
    void render(ShapeRenderer shapeRenderer);
    float getX();
    float getY();
    int getSize();
    Vector2 getDirection();
    void update(float delta);
    void move(float deltaX, float deltaY,  float deltaTime);
    float getSpeed();
    void setSpeed(float speed);
}
