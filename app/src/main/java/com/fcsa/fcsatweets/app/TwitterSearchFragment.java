package com.fcsa.fcsatweets.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */

public class TwitterSearchFragment extends ListFragment {

    public static final String EXTRA_SEARCH_QUERY = "com.fcsa.fcsatweets.app.searchquery";

    private TwitterSearchActivity mTwitterSearchActivity;

    private ListView mListView;
    private Statuses statusesList;


    public TwitterSearchFragment(TwitterSearchActivity twitterSearchActivity) {
        mTwitterSearchActivity = twitterSearchActivity;
    }

    public TwitterSearchFragment() { }


    @Override
    public void onCreate(Bundle saveInstanceState)
    {
        super.onCreate(saveInstanceState);

        String searchQuery = (String)getArguments().getString(EXTRA_SEARCH_QUERY);

        final GsonBuilder builder = new GsonBuilder();
        final Gson gson = builder.create();
        Statuses statusesObj =  gson.fromJson(searchQuery, Statuses.class);


           TwitterSearchAdapter adapter = new TwitterSearchAdapter(statusesObj.mStatuses);
           setListAdapter(adapter);
//          ListView v =  getListView(adapter);
//        View footerView =  ((LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_layout, null, false);
//        v.addFooterView(footerView);
////
        //code to set adapter to populate list
//        View footerView =  ((LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_layout, null, false);
//
//        ListView view = (ListView)(getActivity().findViewById(R.layout.fragment_twitter_search));
//        view.addFooterView(footerView);

    }


   public static TwitterSearchFragment newInstance(String searchQuery)
    {
        Bundle args = new Bundle();
        args.putString(EXTRA_SEARCH_QUERY,searchQuery);

        TwitterSearchFragment fragment = new TwitterSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }


    private class TwitterSearchAdapter extends ArrayAdapter<Status>
    {
        public TwitterSearchAdapter(ArrayList<Status> statuses)
        {
            super(getActivity(),0, statuses);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if(convertView == null)
            {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_twittersearch,null);
            }

            Status item = getItem(position);

            TextView screenTextView = (TextView)convertView.findViewById(R.id.twitter_search_screen_name);
            screenTextView.setText(item.getUser().getScreen_name());

            TextView locationTextView = (TextView)convertView.findViewById(R.id.twitter_search_location);
            locationTextView.setText(item.getUser().getLocation());

            TextView textTextView = (TextView)convertView.findViewById(R.id.twitter_search_text);
            textTextView.setText(item.getText());

//            TextView dateTextView = (TextView)convertView.findViewById(R.id.twitter_search_date);
//            textTextView.setText(item.getCreated_at());

            return convertView;
        }
    }



    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

}
