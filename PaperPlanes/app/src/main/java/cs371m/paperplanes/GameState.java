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
import android.text.TextUtils;
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
            try {
                mmInStream.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }

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
            LeagueMinion minion3 = new LeagueMinion(
                    new Point(0, 0),
                    new Point(0, 0), LeagueMinionType.Line, Color.BLUE
            );
            minions.add(minion3);
            LeagueMinion minion4 = new LeagueMinion(
                    new Point(0, 0),
                    new Point(0, 0), LeagueMinionType.Line, Color.RED
            );
            minions.add(minion4);
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Render time stuff
            long blueLaser = 0l;
            long redLaser = 0l;

            try {
                if (mmInStream.available() > 0) {
                    mmInStream.reset();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Keep listening to the InputStream until an exception occurs
            while (gameover != 2) {
                SystemClock.sleep(37);
                try {
                    // Read from the InputStream
                    bytes = 0;
                    if (mmInStream.available() > 0) {
                        bytes = mmInStream.read(buffer);
                    }

                    switch(playerNumber) {
                        case 1:
                            int newTime = (int) System.currentTimeMillis();
                            deltaTime = (int)((newTime - time) * 0.33);
                            time = newTime;

                            String s;
                            if(bytes > 0) {
                                // Read client inputs
                                s = new String(buffer, StandardCharsets.UTF_8);
                                Log.d("HOST READING", s);

                                if (!TextUtils.isGraphic(s.substring(0, 3))) {
                                    buffer[bytes] = '\0';
                                } else {
                                    String[] clientIn = s.split("\\|");

                                    // Parse
                                    minions.get(1).direction.x = Integer.parseInt(clientIn[0]);
                                    minions.get(1).direction.y = Integer.parseInt(clientIn[1]);
                                    if (Integer.parseInt(clientIn[2]) == 1) {
                                        playerShoot = true;
                                    }
                                }
                            }

                            // Run move/shoot loop
                            for (int i = 0; i < 4; ++i) {


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
                            int hit = checkHits();
                            if (hit != 0) {
                                Log.d("GAME OVER", "THE GAME HAS ENDED");
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
                            } else {
                                output = output + "0|";
                            }
                            if (playerShoot) {
                                output = output + "1|";
                            } else {
                                output = output + "0|";
                            }
                            // Fuck.  I need to send the host's direction vector
                            output = output + minions.get(0).direction.x + "|"
                                    + minions.get(0).direction.y + "|";

                            byte[] writableOut = output.getBytes();
                            Log.d("output", output);
                            write(writableOut);

                            // Draw
                            blueLaser = fireGuns(blueLaser);
                            redLaser = fireGunsc(redLaser);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    gameView.invalidate();
                                }
                            });
                            break;
                        case 2:
                            // Read in the stufffffffff
                            s = new String(buffer, StandardCharsets.UTF_8);

                            // Parse the stufffffffff
                            Log.d("STRING", s);
                            String[] tokens = s.split("\\|");
                            int hostWidth = Integer.parseInt(tokens[0]);
                            int hostHeight = Integer.parseInt(tokens[1]);
                            minions.get(0).position.x = Integer.parseInt(tokens[2]);
                            minions.get(0).position.y = Integer.parseInt(tokens[3]);
                            minions.get(1).position.x = Integer.parseInt(tokens[4]);
                            minions.get(1).position.y = Integer.parseInt(tokens[5]);
                            minions.get(0).direction.x = Integer.parseInt(tokens[9]);
                            minions.get(0).direction.y = Integer.parseInt(tokens[10]);
                            gameover = Integer.parseInt(tokens[6]);
                            if (Integer.parseInt(tokens[7]) == 1)
                                hostShoot = true;
                            if (Integer.parseInt(tokens[8]) == 1)
                                playerShoot = true;
                            blueLaser = fireGuns(blueLaser);
                            redLaser = fireGunsc(redLaser);

                            // Send to Host like this:
                            // 1. Direction Vector
                            // 2. Shooting?
                            s = minions.get(1).direction.x + "|" + minions.get(1).direction.y
                                    + "|";
                            if (playerShoot) {
                                s = s + "1|";
                            }
                            else {
                                s = s + "0|";
                            }
                            byte[] writable = s.getBytes();
                            write(writable);
                            playerShoot = false;

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
                } catch (IOException e) {
                    Log.d("IOExcept","IOException");
                    break;
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

        if (Math.abs(button1Press - button2Press) < 75) {
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
            minions.get(i).direction.x = (int) (x * Math.cos(Math.toRadians(-3.0))
                    - y * Math.sin(Math.toRadians(-3.0)));
            minions.get(i).direction.y = (int) (y * Math.cos(Math.toRadians(-3.0))
                    + x * Math.sin(Math.toRadians(-3.0)));
            button2Press = SystemClock.elapsedRealtime();
        }
        // Bank right
        if (button == 2) {
            minions.get(i).direction.x = (int) (x * Math.cos(Math.toRadians(3.0))
                    - y * Math.sin(Math.toRadians(3.0)));
            minions.get(i).direction.y = (int) (y * Math.cos(Math.toRadians(3.0))
                    + x * Math.sin(Math.toRadians(3.0)));
            button1Press = SystemClock.elapsedRealtime();
        }

        int oldx = minions.get(i).direction.x;
        int oldy = minions.get(i).direction.y;
        double dist = Math.sqrt(Math.pow(x,2) + Math.pow(y,2));
        if (dist < 100.0) {
            double frac = 100.0 / dist;
            minions.get(i).direction.x = (int)(oldx * frac) + 1;
            minions.get(i).direction.y = (int)(oldy * frac) + 1;
        }
        return true;
    }

    public long fireGuns(long blue) {
        if (hostShoot) {
            minions.get(2).position.x = minions.get(0).position.x;
            minions.get(2).position.y = minions.get(0).position.y;
            minions.get(2).direction.x = minions.get(0).direction.x;
            minions.get(2).direction.y = minions.get(0).direction.y;
            hostShoot = false;
            return SystemClock.elapsedRealtime();
        }
        if (SystemClock.elapsedRealtime() - blue > 50) {
            minions.get(2).position.x = 0;
            minions.get(2).position.y = 0;
            minions.get(2).direction.x = 0;
            minions.get(2).direction.y = 0;
        }
        return blue;
    }
    public long fireGunsc(long red) {
        if (playerShoot) {
            minions.get(3).position.x = minions.get(1).position.x;
            minions.get(3).position.y = minions.get(1).position.y;
            minions.get(3).direction.x = minions.get(1).direction.x;
            minions.get(3).direction.y = minions.get(1).direction.y;
            playerShoot = false;
            return SystemClock.elapsedRealtime();
        }
        if (SystemClock.elapsedRealtime() - red > 50){
            minions.get(3).position.x = 0;
            minions.get(3).position.y = 0;
            minions.get(3).direction.x = 0;
            minions.get(3).direction.y = 0;
        }
        return red;
    }

    // 0 if none, 1 if host is hit, 2 if player is hit, 3 if tie
    public int checkHits() {
        // host shot x, host shot y, etc.
        int hsx = minions.get(2).position.x;
        int hsy = minions.get(2).position.y;
        int csx = minions.get(3).position.x;
        int csy = minions.get(3).position.y;

        boolean hostWin = false;
        boolean clientWin = false;
        // Shots only valid if not near origin
        if (hsx > 10 || hsy > 10) {
            int clientx = minions.get(1).position.x;
            int clienty = minions.get(1).position.y;
            double slope = (minions.get(2).position.y - minions.get(2).direction.y * height) /
                    (minions.get(2).position.x - minions.get(2).direction.x * width);
            double yint = (hsy - slope * hsx);
            double a = slope;
            int b = -1;
            double c = yint;

            double dist = (Math.abs(a * clientx + b * clienty + c)) /
                    (Math.sqrt(Math.pow(a,2) + Math.pow(b,2)));

            if (dist < 60) {
                hostWin = true;
            }
        }
        if (csx > 10 || csy > 10) {
            int hostx = minions.get(0).position.x;
            int hosty = minions.get(0).position.y;
            double slope = (minions.get(3).position.y - minions.get(3).direction.y * height) /
                    (minions.get(3).position.x - minions.get(3).direction.x * width);
            double yint = (csy - slope * csx);
            double a = slope;
            int b = -1;
            double c = yint;

            double dist = (Math.abs(a * hostx + b * hosty + c)) /
                    (Math.sqrt(Math.pow(a,2) + Math.pow(b,2)));

            if (dist < 60) {
                clientWin = true;
            }
        }

        if (hostWin && clientWin)
            return 3;
        if (hostWin)
            return 1;
        if (clientWin)
            return 2;
        else
            return 0;
    }
}
