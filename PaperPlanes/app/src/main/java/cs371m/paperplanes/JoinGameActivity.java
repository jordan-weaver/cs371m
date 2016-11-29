package cs371m.paperplanes;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Jordan on 10/14/2016.
 */

public class JoinGameActivity extends AppCompatActivity {

    ArrayList<BluetoothDevice> deviceList;
    ArrayList<String> alldeviceList;
    BluetoothArrayAdapter mArrayAdapter;
    String username;
    String hostUUID;

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String name = device.getName();
                Log.d("BroadcastReciever", "found " + name);
                if(name == null)
                    return;
                String[] tokens = device.getName().split(" ");
                if(tokens[0].equals(context.getResources().getString(R.string.usernameTag))) {
                    Log.d("BroadcastReciever", "Added " + name);
                    deviceList.add(device);
                    mArrayAdapter.notifyDataSetChanged();
                }

                // Add the name and address to an array adapter to show in a ListView
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);

        InitVars();

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        IntentFilter secondfilter = new IntentFilter(BluetoothDevice.ACTION_UUID);
        registerReceiver(mReceiver, secondfilter);

        BluetoothAdapter.getDefaultAdapter().startDiscovery();

        mArrayAdapter = new BluetoothArrayAdapter (this, deviceList);
        ListView listHosts = (ListView) findViewById(R.id.list_hosts);
        listHosts.setAdapter(mArrayAdapter);

        listHosts.setClickable(true);
        listHosts.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = (BluetoothDevice) mArrayAdapter.getItem(position);
                ConnectThread connectThread = new ConnectThread(device);
                connectThread.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    protected void InitVars() {
        deviceList = new ArrayList<>();
        alldeviceList = new ArrayList<>();
        username = getIntent().getStringExtra("user");
        Button cancelButton = (Button) findViewById(R.id.join_game_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                device.fetchUuidsWithSdp();
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(getString(R.string.uuid)));
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            SocketHandler.setSocket(mmSocket);
            Intent intent = new Intent(getApplicationContext(), Lobby.class);
            intent.putExtra("user", username);
            intent.putExtra("isHost", false);
            startActivity(intent);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}
