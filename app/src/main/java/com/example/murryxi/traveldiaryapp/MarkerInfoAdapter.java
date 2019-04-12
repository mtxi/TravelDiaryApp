package com.example.murryxi.traveldiaryapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;

public class MarkerInfoAdapter extends RecyclerView.Adapter<MarkerInfoAdapter.ViewHolder>
{
    private List<JournalEntry> listOfPlaces;
    private Context context;

    public MarkerInfoAdapter (Context context, List<JournalEntry> list)
    {
        this.context = context;
        listOfPlaces = list;
}
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View v;
        v = LayoutInflater.from(context).inflate(R.layout.map_rv_card, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MarkerInfoAdapter.ViewHolder viewHolder, int i)
    {
        JournalEntry j = listOfPlaces.get(i);
        viewHolder.location.setText(j.getPlaceName());
        GlideApp.with(context)
                .load(j.getImgUrl())
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(viewHolder.imgView);
    }

    @Override
    public int getItemCount() {
        return listOfPlaces.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView location;
        ImageView imgView;
        public ViewHolder(View itemView)
        {
            super(itemView);

            location = itemView.findViewById(R.id.cv_title_location);
            imgView = itemView.findViewById(R.id.cv_location_image);
        }

    }
}
