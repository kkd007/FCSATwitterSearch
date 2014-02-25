package com.fcsa.fcsatweets.app;

import android.app.ProgressDialog;
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


//    @Override
//    public void onActivityCreated(Bundle saveInstanceState)
//    {
//        super.onCreate(saveInstanceState);
//        setRetainInstance(true);
//        initLayout();
//        int layout = R.layout.list_item_twittersearch;
//
//        String searchQuery = (String)getArguments().getString(EXTRA_SEARCH_QUERY);
//        setListAdapter(new TwitterSearchAdapter(getActivity(), layout, statusesList) );
//
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_twitter_search, container, false);

        String searchQuery = (String)getArguments().getString(EXTRA_SEARCH_QUERY);

        //    JSONObject jso = new JSONObject(responseString);
        //   JSONArray ja = jso.getJSONArray("statuses");
        //String jsonObjectString  = responseString.replace("statuses","twittersearchresults");



        try
        {
            final GsonBuilder builder = new GsonBuilder();
            final Gson gson = builder.create();
            Statuses statusesObj =  gson.fromJson(searchQuery, Statuses.class);

            int layout = R.layout.list_item_twittersearch;

            //setListAdapter(new TwitterSearchAdapter(getActivity(), layout,  statusesObj.mStatuses) );

            mListView = (ListView)(rootView.findViewById(R.layout.list_item_twittersearch));
            mListView.setAdapter(new TwitterSearchAdapter(getActivity(), layout,  statusesObj.mStatuses));

            String msg = "";

        }
        catch (Exception e)
        {

            String msg = e.getMessage();

        }



        return rootView;
    }


    private void initLayout() {

        mListView = (ListView)getListView();
        statusesList = new Statuses();
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
        public TwitterSearchAdapter(FragmentActivity activity, int layout, ArrayList<Status> statuses)
        {
            super(getActivity(),layout, statuses);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if(convertView == null)
            {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_twittersearch,null);
            }

            Status item = getItem(position);

            TextView textTextView = (TextView)convertView.findViewById(R.id.twittersearch_list_item_text);
            textTextView.setText(item.getText());
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
