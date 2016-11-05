package cs371m.paperplanes;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by Yuanjie on 10/14/16.
 */


public class Lobby extends AppCompatActivity {

    private ListView gameList;
    private List<String> playerList;
    private boolean isHost;
    private BluetoothAdapter mBluetoothAdapter;
    String MY_UUID;
    String NAME;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lobby);

        // bluetooth set up
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        MY_UUID = BluetoothDevice.EXTRA_UUID;
        NAME = BluetoothDevice.EXTRA_NAME;
        // get the info for who is joining
        Intent intent = getIntent();
        String user = intent.getStringExtra("user");
        isHost = intent.getBooleanExtra("isHost", false);

        // Set up the list of players
        gameList = (ListView) findViewById(R.id.gameList);
        playerList = new ArrayList<String>();
        if (isHost) {
            playerList.add(user);
            playerList.add("Waiting for Player0");
            playerList.add("Waiting for Player1");
            playerList.add("Waiting for Player2");

            // Set the lobby name
            TextView gameName = (TextView) findViewById(R.id.gameName);
            gameName.setText(user + "'s Game");
        }
        else {
            // Tell host you joined, wait for host to send this shit
            playerList.add("Waiting for Player0");
            playerList.add("Waiting for Player1");
            playerList.add("Waiting for Player2");
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                playerList);
        gameList.setAdapter(arrayAdapter);

        // If start is pressed, start the game with X players
        Button startGame = (Button) findViewById(R.id.startGame);
        if (!isHost) {
            startGame.setEnabled(false);
        }
        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check number of players
                // Start the game
                Intent intent = new Intent(getApplicationContext(), GameState.class);
                startActivity(intent);
            }
        });
        // If Cancel is pressed, check to see if host.
        Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        if(isHost) {
            Intent discoverableIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, UUID.fromString(getString(R.string.uuid)));
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    //manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }
}

