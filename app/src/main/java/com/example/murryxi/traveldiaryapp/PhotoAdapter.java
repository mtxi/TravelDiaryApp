package com.example.murryxi.traveldiaryapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;
/* This class handles the display of the text and images in recycleView. onBindViewHolder handles how the image should be displayed in
    the recycleView using Glide.
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ImgViewHolder>
{
    private Context imgContext;
    private List<JournalEntry> imgUploads;


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
        return new ImgViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImgViewHolder imgViewHolder, int i)
    {
        JournalEntry uplCurrent = imgUploads.get(i);
        String lng = Double.toString(uplCurrent.showLat());
        imgViewHolder.location.setText(lng);
        GlideApp.with(imgContext)
                .load(uplCurrent.getImgUrl())
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imgViewHolder.imgView);
    }

    @Override
    public int getItemCount() {
        return imgUploads.size();
    }

    class ImgViewHolder extends RecyclerView.ViewHolder
    {
        TextView location;
        ImageView imgView;
        public ImgViewHolder(View itemView)
        {
            super(itemView);

            location = itemView.findViewById(R.id.jentry_location);
            imgView = itemView.findViewById(R.id.img_view_posts);
        }

    }



}
