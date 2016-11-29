package cs371m.paperplanes;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Jordan on 11/15/2016.
 */


public class LeagueMinion {
    Point position;
    Point direction;
    LeagueMinionType type;
    int color;

    public LeagueMinion(Point position, Point direction,
                        LeagueMinionType type, int color) {
        this.position = position;
        this.direction = direction;
        this.type = type;
        this.color = color;
    }

    public void move(int deltaTime) {
        position.set(position.x + direction.x * deltaTime,
                (int) (position.y + direction.y * deltaTime));
    }
}
