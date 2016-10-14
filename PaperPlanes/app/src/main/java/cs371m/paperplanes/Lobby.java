package cs371m.paperplanes;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Yuanjie on 10/14/16.
 */


public class Lobby extends Activity {

    private ListView gameList;
    private List<String> playerList;
    private boolean isHost;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lobby);
        isHost = true;

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
        // If Host, broadcast the game is over
        // If not Host, just leave and broadcast that you have left.

        // Begin loop for checking for new players / players leaving

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }
}

