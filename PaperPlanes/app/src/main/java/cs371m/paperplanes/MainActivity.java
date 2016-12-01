package cs371m.paperplanes;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.nio.BufferUnderflowException;

public class MainActivity extends AppCompatActivity {

    public EditText username;
    public int usernameLen;
    private Context context;
    private final static int REQUEST_ENABLE_BT = 888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        InitVars();
        EstablishBluetooth();
    }

    private void InitVars() {
        username = (EditText) findViewById(R.id.username_edittext);
        usernameLen = getResources().getInteger(R.integer.USERNAME_LENGTH);

        Button hostGameButton = (Button) findViewById(R.id.host_game_button);
        hostGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Lobby.class);
                String user = username.getText().toString();
                if (user.length() == 0) {
                    Toast.makeText(context, "Please enter a name!", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (user.length() > usernameLen) {
                        user = user.substring(0, usernameLen);
                    }
                    intent.putExtra("user", getString(R.string.usernameTag) + " " + user);
                    intent.putExtra("isHost", true);
                    startActivity(intent);
                }
            }
        });

        Button joinGameButton = (Button) findViewById(R.id.join_game_button);
        joinGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), JoinGameActivity.class);
                String user = username.getText().toString();
                if (user.length() == 0) {
                    Toast.makeText(context, "Please enter a name!", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (user.length() > usernameLen) {
                        user = user.substring(0, usernameLen);
                    }
                    intent.putExtra("user", getString(R.string.usernameTag) + " " + user);
                    startActivity(intent);
                }
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
