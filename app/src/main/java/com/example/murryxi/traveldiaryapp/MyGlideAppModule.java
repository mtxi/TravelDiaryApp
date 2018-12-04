package com.example.murryxi.traveldiaryapp;

import android.content.Context;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

@GlideModule
public final class MyGlideAppModule extends AppGlideModule
{
    @Override
    public void applyOptions(Context context, GlideBuilder builder)
    {
        /* increase memory usage by changing Bitmap format from standard RGB_565 to ARGB_8888 to improve image quality */
        builder.setDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888));
    }
}