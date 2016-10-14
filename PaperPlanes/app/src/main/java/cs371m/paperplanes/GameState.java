package cs371m.paperplanes;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by Yuanjie on 10/14/16.
 */

public class GameState extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_state);
        Button bankLeft = (Button) findViewById(R.id.bankLeft);
        Button bankRight = (Button) findViewById(R.id.bankRight);

        // Set the onClicks for these buttons
        bankLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        bankRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // Start game loop
    }
}
