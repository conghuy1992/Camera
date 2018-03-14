package com.conghuy.example.adapters;

import android.content.Context;
import android.hardware.Camera;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.conghuy.example.R;

import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by maidinh on 05-Oct-17.
 */

public class SpinnerResolutionAdapter extends BaseAdapter {
    private Context context;
    private List<Camera.Size> sizes;

    public SpinnerResolutionAdapter(Context context, List<Camera.Size> sizes) {
        this.context = context;
        this.sizes = sizes;
    }
    public void update(List<Camera.Size> sizes){
        this.sizes=sizes;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return sizes.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    class ViewHolder {
        private TextView tvResolution;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.spinner_resolution_layout, null);
            holder = new ViewHolder();
            holder.tvResolution = (TextView) convertView.findViewById(R.id.tvResolution);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Camera.Size size = sizes.get(position);
        String msg = size.width + " X " + size.height;
        holder.tvResolution.setText(msg);

        return convertView;
    }
}
