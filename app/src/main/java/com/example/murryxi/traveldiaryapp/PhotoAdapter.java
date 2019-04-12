package com.example.murryxi.traveldiaryapp;

import android.arch.persistence.room.Database;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import afu.org.checkerframework.checker.oigj.qual.O;

/* This class handles the display of the text and images in recycleView. onBindViewHolder handles how the image should be displayed in
    the recycleView using Glide.
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ImgViewHolder>
{
    private Context imgContext;
    private List<JournalEntry> imgUploads;
    public String url;
    public DatabaseReference dbRef;
    private FirebaseAuth fbAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = fbAuth.getCurrentUser();


    public PhotoAdapter(Context context, List<JournalEntry> uploads)
    {
        imgContext = context;
        imgUploads = uploads;
    }
    @NonNull
    @Override
    public ImgViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View v;
        v = LayoutInflater.from(imgContext).inflate(R.layout.img_item, viewGroup, false);
        ImgViewHolder viewHolder = new ImgViewHolder(v);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ImgViewHolder imgViewHolder, int i)
    {
        JournalEntry uplCurrent = imgUploads.get(i);
        imgViewHolder.location.setText(uplCurrent.getPlaceName());
        GlideApp.with(imgContext)
                .load(uplCurrent.getImgUrl())
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imgViewHolder.imgView);
        imgViewHolder.dateOfEntry.setText(uplCurrent.getEntryDate());

        imgViewHolder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                TextView placeName = v.findViewById(R.id.jentry_location);
                //TextView entryText = v.findViewById(R.id.entry_caption);

                String place = placeName.getText().toString();
                //String text = entryText.getText().toString();
                // pass to new activity
                String key = imgUploads.get(i).getEntryID();
                Intent in = new Intent(v.getContext(), JournalEntryDetail.class);
                // need to directly reference the current List position to display correct image
                in.putExtra("image", imgUploads.get(i).getImgUrl());
                in.putExtra("location", place);
                in.putExtra("text", imgUploads.get(i).getImgName());
                in.putExtra("date", imgUploads.get(i).getEntryDate());
                in.putExtra("id", key);
                Bundle b = new Bundle();
                Bundle c = new Bundle();
                b.putDouble("lat", imgUploads.get(i).showLat());
                c.putDouble("lng", imgUploads.get(i).showLong());
                in.putExtras(b);
                in.putExtras(c);
                Toast.makeText(imgContext,"Key: " + key + imgUploads.get(i).getPlaceLat(), Toast.LENGTH_SHORT).show();
                imgContext.startActivity(in);
            }

        });
    }

    @Override
    public int getItemCount() {
        return imgUploads.size();
    }

    class ImgViewHolder extends RecyclerView.ViewHolder
    {
        TextView location;
        ImageView imgView;
        TextView dateOfEntry;
        public ImgViewHolder(View itemView)
        {
            super(itemView);

            dateOfEntry = itemView.findViewById(R.id.jentry_date);
            location = itemView.findViewById(R.id.jentry_location);
            imgView = itemView.findViewById(R.id.preview_image);
        }

    }



}
