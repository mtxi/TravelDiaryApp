package com.example.murryxi.traveldiaryapp;

public class PhotoUpload
{
    private String imgName;
    private String imgUrl;

    public PhotoUpload()
    {
        // empty constructor
    }

    public PhotoUpload(String name, String url)
    {
        if (name.trim().equals(""))
        {
            name = "Journal Entry";
        }
        imgName = name;
        imgUrl = url;
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

    public void setImgUrl(String url)
    {
        imgUrl = url;
    }
}
