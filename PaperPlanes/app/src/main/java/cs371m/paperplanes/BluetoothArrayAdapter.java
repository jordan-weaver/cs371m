package cs371m.paperplanes;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import static java.security.AccessController.getContext;

/**
 * Created by Jordan on 11/4/2016.
 */

public class BluetoothArrayAdapter extends BaseAdapter {

    List<BluetoothDevice> list;
    LayoutInflater inflater;
    Context context;

    public BluetoothArrayAdapter(Context context, List<BluetoothDevice> list) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public int getCount() {
        if(list != null) {
            return list.size();
        }
        else
            return 0;
    }

    @Override
    public Object getItem(int position) {
        if(list != null) {
            return list.get(position);
        }
        else
            return 0;
    }

    @Override
    public long getItemId(int position) {
        if(list != null) {
            return list.get(position).hashCode();
        }
        else
            return 0;
    }

    public void bindView(final BluetoothDevice data, View view, ViewGroup parent) {
        TextView textView = (TextView) view.findViewById(R.id.available_lobbies_text);

        String name = data.getName();
        if(name == null) {
            textView.setText("NAME == NULL");
        }

        String[] nameTokens = name.split(" ");

        if(nameTokens[0].equals(context.getResources().getString(R.string.usernameTag))) {
            textView.setText(nameTokens[1]);
        }
        else {
            textView.setText("nameTokens[0] != tag");
        }

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BluetoothDevice device = (BluetoothDevice) getItem(position);
        if(device == null) {
            throw new IllegalStateException("this should be called when list is not null");
        }
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.available_lobbies, parent, false);
        }
        bindView(device, convertView, parent);
        return convertView;
    }
}
