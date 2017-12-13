package com.example.tlabuser.musicapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
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
            holder.situationTextView = (TextView)convertView.findViewById(R.id.situation);
            holder.tracksTextView    = (TextView)convertView.findViewById(R.id.tracks);
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