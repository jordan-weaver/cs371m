package cs371m.paperplanes;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by Yuanjie on 10/14/16.
 */

public class GameState extends AppCompatActivity {

    private boolean isHost;
    private int playerNumber;
    private GameView gameView;
    private ArrayList<LeagueMinion> minions;
    private int height;
    private int width;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_state);

        InitVars();

        GameLogicThread gameLogicThread = new GameLogicThread();
        gameLogicThread.start();
    }

    protected void InitVars() {

        isHost = getIntent().getBooleanExtra("isHost", false);
        if(isHost)
            playerNumber = 1;
        else
            playerNumber = 2;

        minions = new ArrayList<>();
        gameView = (GameView) findViewById(R.id.gameview);
        gameView.setList(minions);

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        height = outMetrics.heightPixels;
        width = outMetrics.widthPixels;

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
    }

    private class GameLogicThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        private int time;

        public GameLogicThread() {
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

            time = (int) System.currentTimeMillis();
            LeagueMinion minion = new LeagueMinion(
                    new Point(height / 2, width / 2),
                    new Point(0, 1), LeagueMinionType.Circle, Color.BLUE
            );
            minions.add(minion);
        }

        public void run() {
            Log.d("RUN FUNCTION", "HOORAY");
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = 0;
                    if (mmInStream.available() > 0) {
                        bytes = mmInStream.read(buffer);
                    }
                    switch(playerNumber) {
                        case 1:
                            if(bytes > 0) {
                                // handle player input
                                Log.d("GameThread", "Client Input");
                            }
                            int newTime = (int) System.currentTimeMillis();
                            int deltaTime = (int)((newTime - time) * 0.33);
                            time = newTime;
                            for(int i = 0; i < minions.size(); ++i) {
                                minions.get(i).move(deltaTime);
                            }
                            // Write to clients

                            // Draw
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    gameView.invalidate();
                                }
                            });
                            break;
                        case 2:
                            break;
                        default:
                            Log.d("GameState", "Error, invalid player number");
                            break;
                    }
                    sleep(20);
                } catch (IOException e) {
                    Log.d("IOExcept","IOException");
                    break;
                } catch (InterruptedException e) {
                    Log.d("Exception", "interrupt on sleep");
                    e.printStackTrace();
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            Log.d("GameLogicThread: write", new String(bytes, StandardCharsets.UTF_8));
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
}
