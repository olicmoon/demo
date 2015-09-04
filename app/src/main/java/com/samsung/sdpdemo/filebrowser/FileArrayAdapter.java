package com.samsung.sdpdemo.filebrowser;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.samsung.sdpdemo.R;

public class FileArrayAdapter extends ArrayAdapter<Item> {

    private Context c;
    private int id;
    private List<Item> items;

    public FileArrayAdapter(Context context, int textViewResourceId,
                            List<Item> objects) {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
    }

    public Item getItem(int i) {
        return items.get(i);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = null;

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(id, null, false);

            holder = new Holder();
            holder.fileNameTv = (TextView) convertView.findViewById(R.id.TextView01);
            holder.itemSizeTv = (TextView) convertView.findViewById(R.id.TextView02);
            holder.dateTv = (TextView) convertView.findViewById(R.id.TextViewDate);
            holder.fileImageView = (ImageView) convertView.findViewById(R.id.fd_Icon1);
            convertView.setTag(holder);
        }
        holder = (Holder) convertView.getTag();


        final Item rowItem = items.get(position);
        if (rowItem != null) {
            if ("directory_icon".equals(rowItem.getImage())) {
                holder.fileImageView.setBackgroundResource(R.drawable.directory_icon);
            } else if ("sensitive_file_icon".equals(rowItem.getImage())) {
                holder.fileImageView.setBackgroundResource(R.drawable.sensitive_file);
            } else {
                holder.fileImageView.setBackgroundResource(R.drawable.file_icon);
            }

            if (holder.fileNameTv != null)
                holder.fileNameTv.setText(rowItem.getName());
            if (holder.itemSizeTv != null)
                holder.itemSizeTv.setText(rowItem.getData());
            if (holder.dateTv != null)
                holder.dateTv.setText(rowItem.getDate());
        }
        return convertView;
    }

    private class Holder {
        public TextView fileNameTv;
        public TextView itemSizeTv;
        public TextView dateTv;
        public ImageView fileImageView;
    }
}