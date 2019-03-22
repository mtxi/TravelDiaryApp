package com.example.murryxi.traveldiaryapp;

public class UserInfo
{
    private String username;
    private String user_id;
    private String user_email;

    public UserInfo()
    {
        // empty constructor
    }

    public UserInfo(String uname, String uid, String email)
    {
        this.username = uname;
        this.user_id = uid;
        this.user_email = email;
    }

    public String getUsername()
    {
        return username;
    }

    public String getUser_id()
    {
        return user_id;
    }

    public String getUser_email()
    {
        return user_email;
    }

    public void setUser_email(String user_email)
    {
        this.user_email = user_email;
    }

    public void setUser_id(String user_id)
    {
        this.user_id = user_id;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }
}
