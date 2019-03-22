package com.example.murryxi.traveldiaryapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;


public class TripListAdapter extends RecyclerView.Adapter<TripListAdapter.TripViewHolder>
{
    private Context imgContext;
    private List<TripInfo> tripInfoList;

    public TripListAdapter(Context context, List<TripInfo> tripInfos)
    {
        imgContext = context;
        tripInfoList = tripInfos;
    }
    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View v;
        v = LayoutInflater.from(imgContext).inflate(R.layout.trip_item, viewGroup, false);
        return new TripViewHolder(v);

    }

    @Override
    public void onBindViewHolder (@NonNull final TripViewHolder tripViewHolder, int i)
    {
        final TripInfo trips = tripInfoList.get(i);
        tripViewHolder.cityName.setText(trips.getCity());
        tripViewHolder.date.setText(trips.getDate());
        GlideApp.with(imgContext)
                .load(trips.getPicUrl())
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(tripViewHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return tripInfoList.size();
    }

    class TripViewHolder extends RecyclerView.ViewHolder
    {
        TextView cityName;
        TextView date;
        ImageView imageView;
        public TripViewHolder(View itemView)
        {
            super(itemView);

            cityName = itemView.findViewById(R.id.city_name);
            date = itemView.findViewById(R.id.trip_date);
            imageView = itemView.findViewById(R.id.card_bgImage);
        }

    }

}
