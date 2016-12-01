package cs371m.paperplanes;

import android.app.Activity;
import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import android.os.Handler;
import android.widget.Toast;

import java.util.logging.LogRecord;


/**
 * Created by Yuanjie on 10/14/16.
 */


public class Lobby extends AppCompatActivity {

    private Handler mHandler;
    private ListView gameList;
    private DataTransferThread dtThread;
    private List<String> playerList;
    private boolean isHost;
    private String username;
    private String deviceName;
    private BluetoothAdapter mBluetoothAdapter;
    private Context context;
    ArrayAdapter<String> arrayAdapter;
    String MY_UUID;
    String NAME;

    private final int REQUEST_CODE_DISCOVERABLE = 1;

    private final int HANDLER_PLAYER_LIST = 0;

    private final int BUFFER_START_GAME     = 0;
    private final int BUFFER_CANCEL_LOBBY    = 1;
    private final int BUFFER_PLAYER_LIST    = 2;
    private final int BUFFER_JOIN_GAME      = 3;
    private final int BUFFER_LEAVE_GAME     = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lobby);

        InitVars();

        if (isHost) {
            playerList.add(0, username);
            playerList.add(1, "Waiting for Player0");

            // Set the lobby name
            String[] token = username.split(" ");
            TextView gameName = (TextView) findViewById(R.id.gameName);
            gameName.setText(token[1] + "'s Game");

            deviceName = mBluetoothAdapter.getName();
            mBluetoothAdapter.setName(username);
            AcceptThread acceptThread = new AcceptThread();
            acceptThread.start();
            Intent discoverableIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivityForResult(discoverableIntent, REQUEST_CODE_DISCOVERABLE);
        }
        else {
            // Send Host our name
            dtThread = new DataTransferThread();
            dtThread.start();
            int nameLength = getResources().getInteger(R.integer.USERNAME_LENGTH);
            byte[] byteArray = new byte[nameLength + 2];
            byteArray[0] = BUFFER_JOIN_GAME;
            byte[] byteName = (byte []) username.getBytes();
            for (int i = 0; i < byteName.length; i++) {
                byteArray[i+1] = byteName[i];
            }
            dtThread.write(byteArray);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_DISCOVERABLE) {
            if(resultCode == RESULT_CANCELED) {
                mBluetoothAdapter.setName(deviceName);
                finish();
            }
        }
    }

    protected void InitVars() {
        context = this;
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message m) {
                if(m.what == HANDLER_PLAYER_LIST) {
                    if (isHost) {
                        Log.d("handlemessage", "host");
                        String name = (String) m.obj;

                        playerList.set(1, name);
                        arrayAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("handlemessage", "client");
                        String name = (String) m.obj;
                        TextView gameName = (TextView) findViewById(R.id.gameName);
                        gameName.setText(name + "'s Game");
                        playerList.add(0, name.split(" ")[1]);
                        playerList.add(1, username.split(" ")[1]);
                    }
                }
            }
        };
        // bluetooth set up
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        MY_UUID = UUID.randomUUID().toString();
        NAME = BluetoothDevice.EXTRA_NAME;
        // get the info for who is joining
        Intent intent = getIntent();
        username = intent.getStringExtra("user");
        isHost = intent.getBooleanExtra("isHost", false);

        // Set up the list of players
        gameList = (ListView) findViewById(R.id.gameList);
        playerList = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter<String>(
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
                /*
                mBluetoothAdapter.setName(deviceName);
                byte[] byteArray = new byte[1];
                byteArray[0] = BUFFER_START_GAME;
                dtThread.write(byteArray);
                */
                // Send client start the game
                DataTransferThread dtThread = new DataTransferThread();
                dtThread.start();
                int nameLength = getResources().getInteger(R.integer.USERNAME_LENGTH);
                byte[] byteArray = new byte[nameLength + 6];
                byteArray[0] = BUFFER_START_GAME;
                byte[] byteName = (byte []) "".getBytes();
                for (int i = 0; i < byteName.length; i++) {
                    byteArray[i+1] = byteName[i];
                }
                dtThread.write(byteArray);
                Toast.makeText(context, "Starting game!", Toast.LENGTH_SHORT).show();
                SystemClock.sleep(2000);
                Intent intent = new Intent(getApplicationContext(), GameState.class);
                intent.putExtra("isHost", true);
                startActivity(intent);
            }
        });
        // If Cancel is pressed, check to see if host.
        Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isHost) {
                    mBluetoothAdapter.setName(deviceName);
                }
                finish();
            }
        });
    }

    private class DataTransferThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public DataTransferThread() {
            mmSocket = SocketHandler.getSocket();
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            boolean gameStarted = false;

            // Keep listening to the InputStream until an exception occurs
            while (!gameStarted) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    if (bytes <= 0)
                        continue;

                    if(isHost) {
                        switch (buffer[0]) {
                            case BUFFER_JOIN_GAME:
                                //read player list
                                byte[] newBuffer = new byte[1023];
                                for(int i = 0; i < 1023; ++i)
                                    newBuffer[i] = buffer[i + 1];
                                String s = new String(newBuffer, StandardCharsets.UTF_8);
                                Log.d("dtthread read ishost", s);
                                mHandler.obtainMessage(HANDLER_PLAYER_LIST, s).sendToTarget();
                                break;
                            case BUFFER_LEAVE_GAME:
                                // Remove player from lobby list and inform other players of new list

                                // haha jk theres no other players its hard coded for 2 players
                                Log.d("Lobby", "Client left game");
                                break;
                            default:
                                Log.d("Lobby", "Invalid buffer[0] value for host read");
                                break;
                        }
                    }
                    else {
                        switch (buffer[0]) {
                            case BUFFER_START_GAME:
                                // get game info and start intent for game
                                Log.d("Lobby", "Host started game");
                                gameStarted = true;
                                Intent intent = new Intent(getApplicationContext(), GameState.class);
                                intent.putExtra("isHost", false);

                                // Clear stuff
                                bytes = mmInStream.read(buffer);
                                buffer[bytes] = '\0';
                                if (mmInStream.available() > 0) {
                                   mmInStream.read();
                                }

                                startActivity(intent);
                                break;

                            case BUFFER_CANCEL_LOBBY:
                                // quit out of activity
                                // go to main or join? probably main
                                Log.d("Lobby", "Host canceled lobby");
                                break;
                            case BUFFER_PLAYER_LIST:
                                //read player list
                                byte[] newBuffer = new byte[1023];
                                for(int i = 0; i < 1023; ++i)
                                    newBuffer[i] = buffer[i + 1];
                                String s = new String(newBuffer, StandardCharsets.UTF_8);
                                Log.d("dtthread read ishost", s);
                                mHandler.obtainMessage(HANDLER_PLAYER_LIST, s).sendToTarget();
                                break;
                            default:
                                Log.d("Lobby", "Invalid buffer[0] value for client read");
                                break;
                        }
                    }

                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            Log.d("dtthread: write", new String(bytes, StandardCharsets.UTF_8));
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
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
                    SocketHandler.setSocket(socket);

                    // Send client our names
                    dtThread = new DataTransferThread();
                    dtThread.start();
                    int nameLength = getResources().getInteger(R.integer.USERNAME_LENGTH);
                    byte[] byteArray = new byte[nameLength + 6];
                    byteArray[0] = BUFFER_PLAYER_LIST;
                    byte[] byteName = (byte []) username.getBytes();
                    for (int i = 0; i < byteName.length; i++) {
                        byteArray[i+1] = byteName[i];
                    }
                    dtThread.write(byteArray);

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

