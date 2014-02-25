package com.fcsa.fcsatweets.app;

import android.app.AlertDialog;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Base64;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


public class SearchActivity extends Activity {

    private SharedPreferences savedSearches;
    private TableLayout queryTableLayout;
    private EditText queryEditText;
    private EditText tagEditText;

    private class FetchBearerTokenTask extends AsyncTask<String, Void, Statuses> {

//        @Override
//        protected ArrayList<Status> doInBackground(Object... objects) {
//
//            String uriStr = "https://api.twitter.com/1.1/search/tweets.json?q=Farm%20Credit";
//            String bearerToken = "AAAAAAAAAAAAAAAAAAAAABwWWQAAAAAAUNyabju6tSElgeurJtQVkUUEfGE%3DVXA9ukvoFha46s4ffdByKCnbeoHJxzsu1HZNMNEygP0NRtGf4O";
//
//            URL url = null;
//            try {
//
//                HttpClient httpclient = new DefaultHttpClient();
//
//                HttpGet httpGet = new HttpGet(uriStr);
//                httpGet.addHeader("User-Agent", "FCSA Tweets");
//                httpGet.addHeader("Authorization", "Bearer " + bearerToken);
//                httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded");
//                //httpGet.addHeader("Accept-Encoding","gzip");
//                httpGet.addHeader("Content-Length", "0");
//
//
//                HttpResponse response = httpclient.execute(httpGet);
//                StatusLine statusLine = response.getStatusLine();
//                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
//                    ByteArrayOutputStream out = new ByteArrayOutputStream();
//                    response.getEntity().writeTo(out);
//                    out.close();
//                    String responseString = out.toString();
//
//                    final GsonBuilder builder = new GsonBuilder();
//                    final Gson gson = builder.create();
//
//                //    JSONObject jso = new JSONObject(responseString);
//                 //   JSONArray ja = jso.getJSONArray("statuses");
//                    //String jsonObjectString  = responseString.replace("statuses","twittersearchresults");
//
//                    try
//                    {
//                        Statuses statusesResults = gson.fromJson(responseString,Statuses.class);
//
//
//
//                          Log.i("FCSA Tweets", statusesResults.toString());
//
//                        return statusesResults;
//                    }
//                    catch (Exception e)
//                    {
//
//                        String msg = e.getMessage();
//
//                    }
//
//
////                    String jsonObjectString ="{\"menu_fields\":[{\"id\": 22, \"menu_id\": 1, \"field_type_id\": 1, \"c4w_code\": \"1234\", \"field_label\": \"Customer No\", \"field_values\": \"\", \"date_created\": \"2012-09-16 05:11:23\", \"date_modified\": \"2013-11-20 10:33:23\", \"is_required\": 0, \"is_static\": 1, \"field_order\": 1 }, {\"id\": 23, \"menu_id\": 1, \"field_type_id\": 1, \"c4w_code\": \"1234\", \"field_label\": \"Company Name\", \"field_values\": \"\", \"date_created\": \"2012-09-16 05:11:56\", \"date_modified\": \"2013-11-20 10:33:23\", \"is_required\": 1, \"is_static\": 1, \"field_order\": 3 }]}";
////                    try {
////                        MenuFieldHolder menuFieldHolder= gson.fromJson(jsonObjectString, MenuFieldHolder.class);
////                        Log.i("TAG", "Result: " + menuFieldHolder.toString());
////                    } catch (Throwable t) {
////                        t.printStackTrace();
//
//
//
//                        InputStream stream = new ByteArrayInputStream(responseString.getBytes("UTF-8"));
//                    Log.i("FCSA Tweets", responseString);
//
//
//                    TwitterSearchRepository twitterSearchRepository = new TwitterSearchRepository();
//                    List<TwitterSearchResult> twitterSearchResults = twitterSearchRepository.readJsonStream(stream);
//                    int count = twitterSearchResults.size();
//
//
//                }
//                else
//                {
//                    //Closes the connection.
//
//                    response.getEntity().getContent().close();
//                    throw new IOException(statusLine.getReasonPhrase());
//                }
//
////
////                url = new URL(uriStr);
////                InputStream inputStream = null;
////
////                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
////                try {
////                    connection.setRequestMethod("GET");
////                    connection.setRequestProperty("User-Agent", "FCSA Tweets");
////                    connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
////                    connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
////                    //connection.addRequestProperty("Content-Length", );
////                    //connection.addRequestProperty("Accept-Encoding","gzip");
////                    inputStream = (InputStream) connection.getInputStream();
////
////
////
////
////
//////                    byte[] outputInBytes = str.getBytes("UTF-8");
//////                    OutputStream os = connection.getOutputStream();
//////                    os.write( outputInBytes );
//////                    os.close();
////
////
//////                    String TAG = "FCSA Tweets";
//////
//////                    Log.i(TAG, String.valueOf(connection.getResponseCode()));
//////                    Log.i(TAG, connection.getResponseMessage());
//////
//////                    StringBuilder jsonString = new StringBuilder();
//////                    String line = null;
//////
//////                    int i = 0;
//////
//////                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
//////
//////                    while ((line = reader.readLine()) != null) {
//////                        jsonString.append(line);
//////                        i = i + 1;
//////                        Log.i("FCSA Tweets", "appending JSON: " + i);
//////                    }
//////                    // Parse the JSON using JSONTokener
//////
//////                    JSONArray array;
//////                    array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();
//////                    reader.close();
////
////
////
//////                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
//////
//////                    Gson gson = new Gson();
//////
//////                   TwitterSearchResult twitterSearchResult =   gson.fromJson(reader, TwitterSearchResult.class);
////
////
//////
////                } catch (Exception ex) {
////                    String msg = ex.toString();
////                    throw ex;
////
////                }
//            }
//            catch (Exception ex)
//            {
//                String msg = ex.getMessage();
//            }
//
//
////
////            String APIKey = null;
////            String APISecret = null;
////            String urlEncodedAPIKey = null;
////            String urlEncodedAPISecret = null;
////            String bearerTokenCredentials = null;
////            String bearerTokenCredentials64 = null;
////
////
////            APIKey = "zOpZ76WNccftmATjOgiow";
////            APISecret = "nXOJAvO62QAPPcgdXfyIMuyUVB6Sxp57e09XniUbw";
////
////            urlEncodedAPIKey  =  Uri.encode(APIKey);
////           urlEncodedAPISecret   = Uri.encode(APISecret);
////
////           bearerTokenCredentials = urlEncodedAPIKey+":"+urlEncodedAPISecret;
////            byte[] data = bearerTokenCredentials.getBytes();
////           bearerTokenCredentials64 = Base64.encodeToString(data,Base64.DEFAULT);
////           String uriEncode =  Uri.encode(bearerTokenCredentials64);
////
////
////
////
////            String TAG = "FCSATweets";
////
////            URL url = null;
////            try
////            {
////                String buildUrl =  Uri.parse("https://api.twitter.com/oauth2/token").buildUpon()
////                        //.appendQueryParameter("grant_type","client_credentials")
////                        .build().toString();
////
////                url = new URL(buildUrl);
////
////                InputStream inputStream = null;
////                        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
////              try
////              {
////                connection.setRequestMethod("POST");
////                connection.setRequestProperty("Authorization", "Basic " + "ek9wWjc2V05jY2Z0bUFUak9naW93Om5YT0pBdk82MlFBUFBjZ2RYZnlJTXV5VVZCNlN4cDU3ZTA5WG5pVWJ3");
////                connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
////                connection.addRequestProperty("Content-Length", "29");
////                connection.addRequestProperty("Accept-Encoding","gzip");
////
////               String str =  "grant_type=client_credentials";
////               byte[] outputInBytes = str.getBytes("UTF-8");
////               OutputStream os = connection.getOutputStream();
////               os.write( outputInBytes );
////               os.close();
////
////               // connection.setDoOutput(true);
////                //connection.setDoInput(true);
////                 inputStream = (InputStream)connection.getInputStream();
////              }
////              catch (Exception ex)
////              {
////                 String msg = ex.toString();
////                  throw ex;
////
////              }
//            // connection.getOutputStream().write("grant_type=client_credentials".getBytes());
//
//
////
////                Log.i(TAG, String.valueOf(connection.getResponseCode()));
////                Log.i(TAG,connection.getResponseMessage());
////
////                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
////
////                String line;
////                while((line = bufferedReader.readLine())!=null)
////                {
////                    Log.i(TAG,line);
////                }
////
////                bufferedReader.close();
////
////
////
////            }
////            catch (Exception e)
////            {
////                Log.e(TAG,"ERROR ERROR ERROR", e);
////            }
////
//
//
//            return null;
//        }


        @Override
        protected void onPostExecute(Statuses statuses)
        {
            Statuses statuses2 = statuses;
        }

        @Override
        protected Statuses doInBackground(String... params) {
            String uriStr = "https://api.twitter.com/1.1/search/tweets.json?q=Farm%20Credit";
            String bearerToken = "AAAAAAAAAAAAAAAAAAAAABwWWQAAAAAAUNyabju6tSElgeurJtQVkUUEfGE%3DVXA9ukvoFha46s4ffdByKCnbeoHJxzsu1HZNMNEygP0NRtGf4O";

            URL url = null;
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
                        Statuses statusesResults = gson.fromJson(responseString,Statuses.class);

                        Log.i("FCSA Tweets", statusesResults.toString());

                        return statusesResults;
                    }
                    catch (Exception e)
                    {

                        String msg = e.getMessage();

                    }


                   InputStream stream = new ByteArrayInputStream(responseString.getBytes("UTF-8"));
                    Log.i("FCSA Tweets", responseString);


                    TwitterSearchRepository twitterSearchRepository = new TwitterSearchRepository();
                    List<TwitterSearchResult> twitterSearchResults = twitterSearchRepository.readJsonStream(stream);
                    int count = twitterSearchResults.size();


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

            return null;
        }
    }


    View.OnClickListener queryButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String buttonText = ((Button) view).getText().toString();
            String query = savedSearches.getString(buttonText, null);

            new FetchBearerTokenTask().execute("Hello");


//            // create the URL corresponding to the touched button's query
//            String urlString = getString(R.string.searchURL) + query;
//
//            Intent getUrl = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
//
//            startActivity(getUrl);


        }
    };

    View.OnClickListener editButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TableRow buttonTableRow = (TableRow) view.getParent();
            Button searchButton = (Button) buttonTableRow.findViewById(R.id.newTagButton);
            String tag = searchButton.getText().toString();

            tagEditText.setText(tag);
            queryEditText.setText(savedSearches.getString(tag, null));

        }
    };

    View.OnClickListener saveButtonListner = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (queryEditText.getText().length() > 0 && tagEditText.getText().length() > 0) {
                String queryStr = queryEditText.getText().toString();
                String tagStr = tagEditText.getText().toString();
                makeTag(queryStr, tagStr);
                queryEditText.setText("");
                tagEditText.setText("");
                // hide the soft keyboard
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(tagEditText.getWindowToken(), 0);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                builder.setTitle(R.string.missingTitle);
                builder.setPositiveButton(R.string.OK, null);
                builder.setMessage(R.string.missingMessage);
                AlertDialog errorDialog = builder.create();
                errorDialog.show();
            }
        }
    };

    View.OnClickListener clearTagsButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
            builder.setTitle(R.string.confirmTitle);

            builder.setPositiveButton(R.string.erase,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            clearButtons();
                            SharedPreferences.Editor preferencesEditor = savedSearches.edit();

                            preferencesEditor.clear();
                            preferencesEditor.apply();
                        }
                    }
            );

            builder.setCancelable(true);
            builder.setNegativeButton(R.string.cancel, null);
            builder.setMessage(R.string.confirmMessage);

            AlertDialog confirmDialog = builder.create();
            confirmDialog.show();

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        savedSearches = getSharedPreferences("searches", MODE_PRIVATE);

        queryTableLayout = (TableLayout) findViewById(R.id.twitterQueryTableLayout);

        queryEditText = (EditText) findViewById(R.id.twitterQueryEditText);
        tagEditText = (EditText) findViewById(R.id.twitterTagQueryEditText);

        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(saveButtonListner);

        Button clearTagsButton = (Button) findViewById(R.id.clearTagsButton);
        clearTagsButton.setOnClickListener(clearTagsButtonListener);

        refreshButtons(null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    private void refreshButtons(String newTag) {
        String[] tags = savedSearches.getAll().keySet().toArray(new String[0]);
        Arrays.sort(tags, String.CASE_INSENSITIVE_ORDER);

        if (newTag != null) {
            makeTagGUI(newTag, Arrays.binarySearch(tags, newTag));
        } else {
            for (int index = 0; index < tags.length; ++index) {
                makeTagGUI(tags[index], index);
            }
        }
    }

    private void makeTag(String query, String tag) {
        String originalQuery = savedSearches.getString(tag, null);

        // get a sharedPreferences.Edit to store new tag/query pair
        SharedPreferences.Editor preferencesEdit = savedSearches.edit();
        preferencesEdit.putString(tag, query);
        preferencesEdit.apply();

        if (originalQuery == null) {
            refreshButtons(tag);
        }
    }

    private void makeTagGUI(String tag, int index) {
        // get a reference to the LayoutInflater service
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // inflate new_tag_view.xml
        View newTagView = layoutInflater.inflate(R.layout.new_tag_view, null);

        //get newTagButton, set its text and register its listner
        Button newTagButton = (Button) newTagView.findViewById(R.id.newTagButton);
        newTagButton.setText(tag);
        newTagButton.setOnClickListener(queryButtonListener);

        Button newEditButton = (Button) newTagView.findViewById(R.id.newEditButton);
        newEditButton.setOnClickListener(editButtonListener);

        queryTableLayout.addView(newTagView, index);

    }

    private void clearButtons() {
        queryTableLayout.removeAllViews();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
