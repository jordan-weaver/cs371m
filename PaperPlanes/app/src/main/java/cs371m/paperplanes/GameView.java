package cs371m.paperplanes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

/**
 * Created by Jordan on 11/15/2016.
 */

public class GameView extends View {

    ArrayList<LeagueMinion> minions;

    private int height;
    private int width;

    public GameView(Context context) {
        super(context);
        minions = new ArrayList<>();
        height = this.getHeight();
        width = this.getWidth();
    }
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        minions = new ArrayList<>();
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        minions = new ArrayList<>();
    }

    public void setList(ArrayList<LeagueMinion> list) {
        minions = list;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int height = canvas.getHeight();
        int width = canvas.getWidth();
        for(LeagueMinion m : minions) {
            Paint p = new Paint();
            p.setColor(m.color);
            switch(m.type) {
                case Circle:
                    canvas.drawCircle(m.position.x, m.position.y, 50, p);
                    Log.d("Draw", "Draw new circle");
                    break;
                case Square:
                    break;
                case Line:
                    Paint paint = new Paint();
                    paint.setColor(m.color);
                    paint.setStrokeWidth(4.0f);
                    canvas.drawLine(m.position.x, m.position.y,
                            (m.direction.x * width + m.position.x) * 10,
                            (m.direction.y * height + m.position.y) * 10, paint);
                    break;
                default:
                    Log.d("onDraw", "Error, invalid minion type");
                    break;
            }
        }
    }
}
