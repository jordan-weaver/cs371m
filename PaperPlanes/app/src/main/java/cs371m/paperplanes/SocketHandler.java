package cs371m.paperplanes;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

/**
 * Created by Jordan on 11/4/2016.
 */

public class SocketHandler {

    private static BluetoothSocket socket;

    public static synchronized BluetoothSocket getSocket(){
        return socket;
    }

    public static synchronized void setSocket(BluetoothSocket newSocket){
        socket = newSocket;
    }
}
