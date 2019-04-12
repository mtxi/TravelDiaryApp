package com.example.murryxi.traveldiaryapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class TripListAdapter extends RecyclerView.Adapter<TripListAdapter.TripViewHolder>
{
    private Context imgContext;
    private List<TripInfo> tripInfoList;
    public String daysLeft = "";
    public static int SECONDS_IN_A_DAY = 24 * 60 * 60;

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

        String dateOfTrip = trips.getDate();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
        try
        {
            Date date = format.parse(dateOfTrip);
            Calendar tripDate = Calendar.getInstance();
            tripDate.setTime(date);

            Calendar today = Calendar.getInstance();
            long diff = tripDate.getTimeInMillis() - today.getTimeInMillis();
            long diffSec = diff / 1000;

            long days = diffSec / SECONDS_IN_A_DAY;

            daysLeft = Long.toString(days);

            tripViewHolder.date.setText(daysLeft);

        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        tripViewHolder.cityName.setText(trips.getCity());
        GlideApp.with(imgContext)
                .load(trips.getPicUrl())
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(tripViewHolder.imageView);

        tripViewHolder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                TextView cityName = v.findViewById(R.id.city_name);
                //TextView entryText = v.findViewById(R.id.entry_caption);

                String place = cityName.getText().toString();
                //String text = entryText.getText().toString();
                // pass to new activity
                String key = tripInfoList.get(i).getTripKey();
                Intent in = new Intent(v.getContext(), TripDetail.class);
                // need to directly reference the current List position to display correct image
                in.putExtra("image", tripInfoList.get(i).getPicUrl());
                in.putExtra("destination", place);
                in.putExtra("notes", tripInfoList.get(i).getNotes());
                in.putExtra("id", key);
                in.putExtra("date", tripInfoList.get(i).getDate());
                in.putExtra("countdown", daysLeft);
                in.putExtra("country", tripInfoList.get(i).getCountry());
                imgContext.startActivity(in);
            }

        });


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
