package com.example.murryxi.traveldiaryapp;

import android.provider.ContactsContract;

import javax.security.auth.callback.Callback;

public class JournalEntry
{
    private String imgName;
    private String imgUrl;
    private String entryLocation; // variable used to retrieve data from Firebase
    private static String place; // static version of 'entryLocation' to pass values between classes
    public static double locationLat;
    public static double locationLong;
    private double placeLat;
    private double placeLong;
    private String entryDate;

    public JournalEntry()
    {
        // empty constructor
    }

    public JournalEntry(String name, String url, String location, double lat, double lng)
    {
        if (name.trim().equals(""))
        {
            name = "Journal Entry";
        }
        imgName = name;
        imgUrl = url;
        place = location;
        locationLat = lat;
        locationLong = lng;
    }

    public String getImgName()
    {
        return imgName;
    }

    public void setImgName(String name)
    {
        imgName = name;
    }

    public String getImgUrl()
    {
        return imgUrl;
    }

    public String getPlaceName()
    {
        // getter used to retrieve data from Firebase
        return entryLocation;
    }

    public void setPlaceName(String location)
    {
        place = location;
    }

    public String getEntryLocation()
    {
        // getter only used to pass value between classes
        return place;
    }

    public void setLatitude(double latitude)
    {
        locationLat = latitude;
    }

    public double showLat()
    {
        return placeLat;
    }

    public double showLong()
    {
        return placeLong;
    }

    public double getPlaceLong()
    {
        return locationLong;
    }

    public void setLongitude(double longitude)
    {
        locationLong = longitude;
    }

    public double getPlaceLat()
    {
        // to pass between classes
        return locationLat;
    }

}