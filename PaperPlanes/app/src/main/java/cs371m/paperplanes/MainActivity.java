package cs371m.paperplanes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.nio.BufferUnderflowException;

public class MainActivity extends AppCompatActivity {

    public EditText username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitVars();
    }

    private void InitVars() {
        username = (EditText) findViewById(R.id.username_edittext);

        Button hostGameButton = (Button) findViewById(R.id.host_game_button);
        hostGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Lobby.class);
                intent.putExtra("user", username.getText().toString());
                intent.putExtra("isHost", true);
                startActivity(intent);
            }
        });

        Button joinGameButton = (Button) findViewById(R.id.join_game_button);
        joinGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), JoinGameActivity.class);
                intent.putExtra("user", username.getText().toString());
                startActivity(intent);
            }
        });

        Button exitButton = (Button) findViewById(R.id.exit_button);
    }
}
