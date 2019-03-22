package com.example.murryxi.traveldiaryapp;

import android.app.Application;

import com.mapbox.mapboxsdk.Mapbox;

public class MapBoxFeature extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        String mapBox_access_token = BuildConfig.MAPBOX_ACCESS_TOKEN;

        Mapbox.getInstance(this, mapBox_access_token);
    }
}