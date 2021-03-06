package com.example.murryxi.traveldiaryapp;

public class TripInfo
{
    private String country;
    private String city;
    private String date;
    private String picUrl;
    private String notes;
    private String tripKey;

    public TripInfo()
    {
         //empty constructor
    }

    public TripInfo(String key, String country, String city, String date, String notes, String url)
    {
        this.country = country;
        this.city = city;
        this.date = date;
        this.notes = notes;
        this.tripKey = key;
        picUrl = url;
    }

    public String getCountry()
    {
        return country;
    }

    public String getCity()
    {
        return city;
    }

    public String getDate()
    {
        return date;
    }

    public String getPicUrl()
    {
        return picUrl;
    }

    public String getTripKey()
    {
        return tripKey;
    }

    public String getNotes()
    {
        return notes;
    }

    public void setCountry(String countryName)
    {
        this.country = countryName;
    }

    public void setCity(String cityName)
    {
        this.city = cityName;
    }

    public void setDate(String dateOfTrip)
    {
        this.date = dateOfTrip;
    }

    public void setPicUrl(String url)
    {
        this.picUrl = url;
    }
}
