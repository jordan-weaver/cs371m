package cs371m.paperplanes;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

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
    private Context context;

    private long button1Press;
    private long button2Press;
    private boolean hostShoot = false;
    private boolean playerShoot = false;
    private int gameover;
    private static int deltaTime;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
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

        context = this;

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
        button1Press = SystemClock.elapsedRealtime();
        button2Press = SystemClock.elapsedRealtime();
        gameover = 1;

        // Set the onClicks for these buttons
        bankLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return clickEvent(1);
            }
        });
        bankRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return clickEvent(2);
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
                    new Point(width - 60, height / 2),
                    new Point(0, 100), LeagueMinionType.Circle, Color.BLUE
            );
            minions.add(minion);
            LeagueMinion minion2 = new LeagueMinion(
                    new Point(60, height / 2),
                    new Point(0, -100), LeagueMinionType.Circle, Color.RED
            );
            minions.add(minion2);
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (gameover != 2) {
                SystemClock.sleep(17);
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
                            deltaTime = (int)((newTime - time) * 0.33);
                            time = newTime;

                            // Read client inputs

                            // Update client info

                            // Run game loop
                            for(int i = 0; i < minions.size(); ++i) {
                                if (minions.get(i).position.x > width ||
                                    minions.get(i).position.x < 0) {
                                    minions.get(i).position.x = Math.abs(minions.get(i).position.x -
                                            width + 3);
                                }
                                if (minions.get(i).position.y > height ||
                                    minions.get(i).position.y < 0) {
                                    minions.get(i).position.y = Math.abs(minions.get(i).position.y -
                                            height + 3);
                                }

                                minions.get(i).move(deltaTime);
                            }
                            // Write to clients
                            /*
                                My Format, delineated by |:
                                0. Game over? No.
                                1. Send host width and height in px
                                2. Send locations in order of minion arr
                                3. Host shooting?
                                4. Player shooting?
                             */
                            String output = width + "|" + height + "|"
                                    + minions.get(0).position.x + "|" + minions.get(0).position.y
                                    + "|" + minions.get(1).position.x + "|"
                                    + minions.get(1).position.y + "|" + gameover + "|";
                            if (hostShoot) {
                                output = output + "1|";
                                hostShoot = false;
                            }
                            else {
                                output = output + "0|";
                            }
                            if (playerShoot) {
                                output = output + "1|";
                                playerShoot = false;
                            }
                            else {
                                output = output + "0|";
                            }
                            byte[] writableOut = output.getBytes();
                            Log.d("output", output);
                            write(writableOut);

                            // Draw
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    gameView.invalidate();
                                }
                            });
                            break;
                        case 2:
                            // Read in the stufffffffff
                            byte[] newBuffer = new byte[1023];
                            for(int i = 0; i < 1023; ++i)
                                newBuffer[i] = buffer[i + 1];
                            String s = new String(newBuffer, StandardCharsets.UTF_8);

                            // Parse the stufffffffff
                            Log.d("STRING", s);
                            String[] tokens = s.split("\\|");
                            int hostWidth = Integer.parseInt(tokens[0]);
                            int hostHeight = Integer.parseInt(tokens[1]);
                            minions.get(0).position.x = Integer.parseInt(tokens[2]);
                            minions.get(0).position.y = Integer.parseInt(tokens[3]);
                            minions.get(1).position.x = Integer.parseInt(tokens[4]);
                            minions.get(1).position.y = Integer.parseInt(tokens[5]);
                            gameover = Integer.parseInt(tokens[6]);

                            // Draw the stufffffffff
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    gameView.invalidate();
                                }
                            });

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

    public boolean clickEvent(int button) {
        // Double press (less than 200ms apart)

        if (Math.abs(button1Press - button2Press) < 200) {
            if (isHost) {
                hostShoot = true;
            }
            else {
                playerShoot = true;
            }
        }
        int i = 1;
        if (isHost) {
            i = 0;
        }
        int x = minions.get(i).direction.x;
        int y = minions.get(i).direction.y;

        // Bank left
        if (button == 1) {
            minions.get(i).direction.x = (int) (x * Math.cos(Math.toRadians(3.0))
                    - y * Math.sin(Math.toRadians(3.0)));
            minions.get(i).direction.y = (int) (y * Math.cos(Math.toRadians(3.0))
                    + x * Math.sin(Math.toRadians(3.0)));
            button1Press = SystemClock.elapsedRealtime();
        }
        // Bank right
        if (button == 2) {
            minions.get(i).direction.x = (int) (x * Math.cos(Math.toRadians(-3.0))
                    - y * Math.sin(Math.toRadians(-3.0)));
            minions.get(i).direction.y = (int) (y * Math.cos(Math.toRadians(-3.0))
                    + x * Math.sin(Math.toRadians(-3.0)));
            button2Press = SystemClock.elapsedRealtime();
        }

        return true;
    }

}
