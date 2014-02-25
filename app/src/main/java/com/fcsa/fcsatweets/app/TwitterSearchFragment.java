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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */

public class TwitterSearchFragment extends ListFragment {

    public static final String EXTRA_SEARCH_QUERY = "com.fcsa.fcsatweets.app.searchquery";

    private TwitterSearchActivity mTwitterSearchActivity;

    private ListView mListView;
    private Statuses statusesList;
    private ProgressDialog pDialog;

    public TwitterSearchFragment(TwitterSearchActivity twitterSearchActivity) {
        mTwitterSearchActivity = twitterSearchActivity;
    }

    public TwitterSearchFragment() { }


    @Override
    public void onActivityCreated(Bundle saveInstanceState)
    {
        super.onCreate(saveInstanceState);
        setRetainInstance(true);
        initLayout();
        int layout = R.layout.list_item_twittersearch;

        String searchQuery = (String)getArguments().getString(EXTRA_SEARCH_QUERY);
        new FetchBearerTokenTask2().execute(searchQuery);

        setListAdapter(new TwitterSearchAdapter(getActivity(), layout, statusesList) );

    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//            Bundle savedInstanceState) {
//
//
//        View rootView = inflater.inflate(R.layout.fragment_twitter_search, container, false);
//
//        String searchQuery = (String)getArguments().getString(EXTRA_SEARCH_QUERY);
//
//        //mListView = (ListView)(rootView.findViewById(R.layout.list_item_twittersearch));
//
//
//
//        //setAdapter();
//
//        return rootView;
//    }


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
        public TwitterSearchAdapter(FragmentActivity activity, int layout, Statuses statuses)
        {
            super(getActivity(),layout, (List<Status>) statuses);
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

    private class FetchBearerTokenTask2 extends AsyncTask<String, Void, Statuses> {

        @Override
        protected void onPreExecute() {
            try
            {
            pDialog = ProgressDialog.show(getActivity(),"Loading Data","Loading. Please wait...", true);
            }
            catch (Exception ex)
            {
                String msg = ex.getMessage();
            }
        }

        @Override
        protected void onPostExecute(Statuses statuses)
        {

            if (pDialog.isShowing())
                pDialog.dismiss();

            Statuses statuses2 = statuses;

            setListAdapter(new TwitterSearchAdapter(getActivity(),  R.layout.list_item_twittersearch, statuses));
            // ArrayAdapter<Status> adapter = new ArrayAdapter<Status>(getActivity())
            // Start another activity with Fragment.
            // Pass in the data.
        }

        @Override
        protected Statuses doInBackground(String... params) {
            String uriStr = "https://api.twitter.com/1.1/search/tweets.json?q=Farm%20Credit";
            String bearerToken = "AAAAAAAAAAAAAAAAAAAAABwWWQAAAAAAUNyabju6tSElgeurJtQVkUUEfGE%3DVXA9ukvoFha46s4ffdByKCnbeoHJxzsu1HZNMNEygP0NRtGf4O";

            URL url = null;
            Statuses statusesResults = null;
            try {

                HttpClient httpclient = new DefaultHttpClient();

                HttpGet httpGet = new HttpGet(uriStr);
                httpGet.addHeader("User-Agent", "FCSA Tweets");
                httpGet.addHeader("Authorization", "Bearer " + bearerToken);
                httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded");
                //httpGet.addHeader("Accept-Encoding","gzip");
                httpGet.addHeader("Content-Length", "0");


                HttpResponse response = httpclient.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    String responseString = out.toString();

                    final GsonBuilder builder = new GsonBuilder();
                    final Gson gson = builder.create();

                    //    JSONObject jso = new JSONObject(responseString);
                    //   JSONArray ja = jso.getJSONArray("statuses");
                    //String jsonObjectString  = responseString.replace("statuses","twittersearchresults");

                    try
                    {
                        statusesResults =  gson.fromJson(responseString,Statuses.class);

                        //Log.i("FCSA Tweets", statusesResults.toString());


                    }
                    catch (Exception e)
                    {

                        String msg = e.getMessage();

                    }



                }
                else
                {
                    //Closes the connection.

                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }


            }
            catch (Exception ex)
            {
                String msg = ex.getMessage();
            }

            return statusesResults;
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
