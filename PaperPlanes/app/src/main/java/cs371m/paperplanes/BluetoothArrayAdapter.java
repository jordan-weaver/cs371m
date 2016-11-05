package cs371m.paperplanes;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Jordan on 11/4/2016.
 */

public class BluetoothArrayAdapter extends BaseAdapter {

    List<BluetoothDevice> list;
    LayoutInflater inflater;

    public BluetoothArrayAdapter(Context context, List<BluetoothDevice> list) {
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
        String name = data.getName();
        TextView textView = (TextView) view.findViewById(R.id.available_lobbies_text);
        textView.setText(name);
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
