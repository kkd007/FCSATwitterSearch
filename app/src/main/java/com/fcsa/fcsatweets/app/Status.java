package com.fcsa.fcsatweets.app;

import android.os.AsyncTask;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by DhulipalaK on 2/24/14.
 */

class  Statuses
{
    @SerializedName("statuses")
    public ArrayList<Status> mStatuses;
}

public class Status
{
    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    private String created_at;

    private String text;

    private User user;

}

