package cs371m.paperplanes;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.nio.BufferUnderflowException;

public class MainActivity extends AppCompatActivity {

    public EditText username;
    private final static int REQUEST_ENABLE_BT = 888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitVars();
        EstablishBluetooth();
    }

    private void InitVars() {
        username = (EditText) findViewById(R.id.username_edittext);
        String user = username.getText().toString();
        if (user.length() > 6) {
            user = user.substring(0, 6);
        }

        Button hostGameButton = (Button) findViewById(R.id.host_game_button);
        final String finalUser = user;
        hostGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Lobby.class);
                intent.putExtra("user", finalUser);
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
    }

    private void EstablishBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            Toast.makeText(this, "Fuck off and get a device with bluetooth", Toast.LENGTH_LONG).show();
            return;
        }

        if(!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == RESULT_OK) {
                //Toast.makeText(this, "Successfully turned on bluetooth", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Failed to turn on bluetooth", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
