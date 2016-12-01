package cs371m.paperplanes;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import cs371m.paperplanes.R;

/**
 * Created by Yuanjie on 12/1/16.
 */

public class EndGame extends AppCompatActivity {

    private boolean isHost;
    private Context context;
    private Handler mHandler;
    private DataTransferThread dtThread;
    private boolean gameStarted;

    final private int HANDLER_RESTART       = 0;
    final private int HANDLER_QUIT          = 1;
    final private int BUFFER_START_GAME     = 0;
    private final int BUFFER_CANCEL_LOBBY   = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.end_game);



        int winner = getIntent().getIntExtra("winner", 0);
        isHost = getIntent().getBooleanExtra("host", false);
        if (winner == 0) {
            Toast.makeText(this, "Uhoh, it looks like there was no winner",
                    Toast.LENGTH_SHORT).show();
        }
        TextView dispWin = (TextView) findViewById(R.id.winner);
        if (winner == 3) {
            dispWin.setText("It was a draw!  Unlucky.");
        }
        else if (winner == 1 || winner == 2) {
            dispWin.setText("Player " + winner + " won!");
        }

        Button replay = (Button) findViewById(R.id.replay);
        if (!isHost) {
            replay.setEnabled(false);
        }
        replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Just go ahead and restart the game state.  Tell the client such.
                DataTransferThread dtThread2 = new DataTransferThread();
                dtThread2.start();
                byte[] byteArray = new byte[2];
                byteArray[0] = BUFFER_START_GAME;
                byte[] byteName = (byte []) "".getBytes();
                for (int i = 0; i < byteName.length; i++) {
                    byteArray[i+1] = byteName[i];
                }
                dtThread2.write(byteArray);

                gameStarted = true;

                Toast.makeText(context, "Starting game!", Toast.LENGTH_SHORT).show();
                SystemClock.sleep(2000);
                Intent intent = new Intent(getApplicationContext(), GameState.class);
                intent.putExtra("isHost", true);
                startActivity(intent);
            }
        });
        Button quit = (Button) findViewById(R.id.quit);
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataTransferThread dtThread2 = new DataTransferThread();
                dtThread2.start();
                byte[] byteArray = new byte[2];
                byteArray[0] = BUFFER_CANCEL_LOBBY;
                byte[] byteName = (byte []) "".getBytes();
                for (int i = 0; i < byteName.length; i++) {
                    byteArray[i+1] = byteName[i];
                }
                dtThread2.write(byteArray);
                dtThread2.resetConnection();
                dtThread.resetConnection();
                Intent intent = new Intent(context, MainActivity.class);
                gameStarted = true;

                Toast.makeText(context, "You have left the game!", Toast.LENGTH_SHORT).show();

                startActivity(intent);

            }
        });

        gameStarted = false;
        context = this;
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message m) {
                if (m.what == HANDLER_QUIT) {
                    Toast.makeText(context, "Other player has left!", Toast.LENGTH_SHORT).show();
                    dtThread.resetConnection();
                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                }
                else if (m.what == HANDLER_RESTART) {
                    Intent intent = new Intent(context, GameState.class);
                    intent.putExtra("isHost", false);
                    Toast.makeText(context, "Host has restarted the game!",
                            Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                }
            }
        };
        dtThread = new DataTransferThread();
        dtThread.start();
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

            // Keep listening to the InputStream until an exception occurs
            while (!gameStarted) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    if (bytes <= 0)
                        continue;

                    if(isHost) {
                        switch (buffer[0]) {
                            case BUFFER_CANCEL_LOBBY:
                                // Remove player from lobby list and inform other players of new list

                                // haha jk theres no other players its hard coded for 2 players
                                mHandler.obtainMessage(HANDLER_QUIT, " ").sendToTarget();
                                gameStarted = true;
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
                                mHandler.obtainMessage(HANDLER_RESTART, " ").sendToTarget();
                                gameStarted = true;
                                break;

                            case BUFFER_CANCEL_LOBBY:
                                // quit out of activity
                                // go to main or join? probably main
                                Log.d("Lobby", "Host canceled lobby");
                                mHandler.obtainMessage(HANDLER_QUIT, " ").sendToTarget();
                                gameStarted = true;
                                break;
                            default:
                                Log.d("EndGame", "Invalid buffer[0] value for client read");
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

        public void resetConnection() {
            if (mmInStream != null) {
                try {mmInStream.close();} catch (Exception e) {}
            }

            if (mmOutStream != null) {
                try {mmOutStream.close();} catch (Exception e) {}
            }

            if (mmSocket != null) {
                try {mmSocket.close();} catch (Exception e) {}
            }

        }
    }


}
