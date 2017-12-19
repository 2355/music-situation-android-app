package com.example.tlabuser.musicapplication.View.Root;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.tlabuser.musicapplication.Model.Situation;
import com.example.tlabuser.musicapplication.R;

import java.util.List;

/**
 * UNNECESSARY FILE
 * set values to Views in item_artist
 */

public class ListSituationAdapter extends ArrayAdapter<Situation> {

    LayoutInflater mInflater;

    public ListSituationAdapter(Context context, List<Situation> item){
        super(context, 0, item);
        mInflater =  (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        Situation item = getItem(position);
        ViewHolder holder;

        if(convertView==null){
            convertView = mInflater.inflate(R.layout.item_situation, null);
            holder = new ViewHolder();
            holder.situationTextView = (TextView)convertView.findViewById(R.id.tv_situation);
            holder.tracksTextView    = (TextView)convertView.findViewById(R.id.tv_tracks);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.situationTextView.setText(item.name);
        holder.tracksTextView.setText(String.format("%d tracks", item.tracks));

        return convertView;
    }

    static class ViewHolder{
        TextView  situationTextView;
        TextView  tracksTextView;
    }

}