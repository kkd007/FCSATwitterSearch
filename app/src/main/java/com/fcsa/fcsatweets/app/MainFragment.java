package com.fcsa.fcsatweets.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

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
import java.util.Arrays;
import java.util.List;


public class MainFragment extends Fragment {

    private MainActivity mMainActivity;
    private SharedPreferences savedSearches;
    private TableLayout queryTableLayout;
    private EditText queryEditText;
    private EditText tagEditText;
    private ProgressDialog pDialog;

    public MainFragment(MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        savedSearches = mMainActivity.getSharedPreferences("searches", Context.MODE_PRIVATE);

        queryTableLayout = (TableLayout) rootView.findViewById(R.id.twitterQueryTableLayout);

        queryEditText = (EditText) rootView.findViewById(R.id.twitterQueryEditText);
        tagEditText = (EditText) rootView.findViewById(R.id.twitterTagQueryEditText);

        Button saveButton = (Button) rootView.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(saveButtonListner);

        Button clearTagsButton = (Button) rootView.findViewById(R.id.clearTagsButton);
        clearTagsButton.setOnClickListener(clearTagsButtonListener);

        refreshButtons(null);


        return rootView;
    }



    View.OnClickListener queryButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String buttonText = ((Button) view).getText().toString();
            String query = savedSearches.getString(buttonText, null);
              new FetchBearerTokenTask2().execute(query);
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
                ((InputMethodManager) mMainActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(tagEditText.getWindowToken(), 0);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        LayoutInflater layoutInflater = (LayoutInflater) mMainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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


    private class FetchBearerTokenTask2 extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            try
            {
                pDialog = ProgressDialog.show(getActivity(), "Searching Twitter....", "Please wait...", true);
            }
            catch (Exception ex)
            {
                String msg = ex.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String responseString)
        {

           // Start TwitterSearchActivity
            Intent i = new Intent(getActivity(),TwitterSearchActivity.class);
            if(responseString==null || responseString.length()==0)
            {
                i.putExtra(TwitterSearchFragment.EXTRA_SEARCH_QUERY,stResposne);
            }
            else
            {
                i.putExtra(TwitterSearchFragment.EXTRA_SEARCH_QUERY,responseString);
            }
            if (pDialog.isShowing())
                pDialog.dismiss();

            startActivity(i);



            // ArrayAdapter<Status> adapter = new ArrayAdapter<Status>(getActivity())
            // Start another activity with Fragment.
            // Pass in the data.
        }

        @Override
        protected String doInBackground(String... params) {


            String searchString = Uri.encode(params[0]);
            String uriStr = "https://api.twitter.com/1.1/search/tweets.json?q="+searchString;
            String bearerToken = "AAAAAAAAAAAAAAAAAAAAABwWWQAAAAAAUNyabju6tSElgeurJtQVkUUEfGE%3DVXA9ukvoFha46s4ffdByKCnbeoHJxzsu1HZNMNEygP0NRtGf4O";

            URL url = null;
            String responseString = null;
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
                    responseString  = out.toString();
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

            return responseString;
        }

    }

    String stResposne = "{\"statuses\":[{\"metadata\":{\"result_type\":\"recent\",\"iso_language_code\":\"en\"},\"created_at\":\"Tue Feb 25 22:31:50 +0000 2014\",\"id\":438441232331964416,\"id_str\":\"438441232331964416\",\"text\":\"farmcreditIL: IT'S NOT TOO LATE! Application deadline for Farm Credit Illinois's AGRICULTURE SCHOLARSHIPS is thi... http:\\/\\/t.co\\/arUolMpS9T\",\"source\":\"\\u003ca href=\\\"http:\\/\\/ifttt.com\\\" rel=\\\"nofollow\\\"\\u003eIFTTT\\u003c\\/a\\u003e\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":1402533342,\"id_str\":\"1402533342\",\"name\":\"Illinois Tracker\",\"screen_name\":\"Iltracker2\",\"location\":\"\",\"description\":\"\",\"url\":null,\"entities\":{\"description\":{\"urls\":[]}},\"protected\":false,\"followers_count\":59,\"friends_count\":26,\"listed_count\":3,\"created_at\":\"Sat May 04 14:58:18 +0000 2013\",\"favourites_count\":125,\"utc_offset\":null,\"time_zone\":null,\"geo_enabled\":false,\"verified\":false,\"statuses_count\":65936,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"is_translation_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_image_url\":\"http:\\/\\/abs.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"profile_background_image_url_https\":\"https:\\/\\/abs.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/abs.twimg.com\\/sticky\\/default_profile_images\\/default_profile_0_normal.png\",\"profile_image_url_https\":\"https:\\/\\/abs.twimg.com\\/sticky\\/default_profile_images\\/default_profile_0_normal.png\",\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":true,\"default_profile_image\":true,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[{\"url\":\"http:\\/\\/t.co\\/arUolMpS9T\",\"expanded_url\":\"http:\\/\\/ift.tt\\/1hQ7Ojw\",\"display_url\":\"ift.tt\\/1hQ7Ojw\",\"indices\":[116,138]}],\"user_mentions\":[]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"lang\":\"en\"},{\"metadata\":{\"result_type\":\"recent\",\"iso_language_code\":\"en\"},\"created_at\":\"Tue Feb 25 22:27:31 +0000 2014\",\"id\":438440146665353216,\"id_str\":\"438440146665353216\",\"text\":\"Well, look here @reddricer made the Farm Credit Annual meeting program. #concerning http:\\/\\/t.co\\/eOeYQGXtTR\",\"source\":\"\\u003ca href=\\\"http:\\/\\/twitter.com\\/download\\/iphone\\\" rel=\\\"nofollow\\\"\\u003eTwitter for iPhone\\u003c\\/a\\u003e\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":201765305,\"id_str\":\"201765305\",\"name\":\"Patrick H. Lenderman\",\"screen_name\":\"phlenderman\",\"location\":\"Paragould, AR\",\"description\":\"The H stands for Hootie..1 of the founding members of KNOT HOLE HUNTING CLUB. I'm the BANKER that always says NO. #timesarehard http:\\/\\/t.co\\/TIOoeOjEUY\",\"url\":\"http:\\/\\/t.co\\/oJKL2CtWDt\",\"entities\":{\"url\":{\"urls\":[{\"url\":\"http:\\/\\/t.co\\/oJKL2CtWDt\",\"expanded_url\":\"http:\\/\\/www.timbermallard.com\",\"display_url\":\"timbermallard.com\",\"indices\":[0,22]}]},\"description\":{\"urls\":[{\"url\":\"http:\\/\\/t.co\\/TIOoeOjEUY\",\"expanded_url\":\"http:\\/\\/www.gotducks.com\",\"display_url\":\"gotducks.com\",\"indices\":[128,150]}]}},\"protected\":false,\"followers_count\":193,\"friends_count\":443,\"listed_count\":2,\"created_at\":\"Tue Oct 12 15:36:19 +0000 2010\",\"favourites_count\":17,\"utc_offset\":null,\"time_zone\":null,\"geo_enabled\":false,\"verified\":false,\"statuses_count\":3098,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"is_translation_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_image_url\":\"http:\\/\\/abs.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"profile_background_image_url_https\":\"https:\\/\\/abs.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/2476353765\\/image_normal.jpg\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/2476353765\\/image_normal.jpg\",\"profile_banner_url\":\"https:\\/\\/pbs.twimg.com\\/profile_banners\\/201765305\\/1362351691\",\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":true,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[{\"text\":\"concerning\",\"indices\":[72,83]}],\"symbols\":[],\"urls\":[],\"user_mentions\":[{\"screen_name\":\"reddricer\",\"name\":\"Brad Gray\",\"id\":475411848,\"id_str\":\"475411848\",\"indices\":[16,26]}],\"media\":[{\"id\":438440146501775361,\"id_str\":\"438440146501775361\",\"indices\":[84,106],\"media_url\":\"http:\\/\\/pbs.twimg.com\\/media\\/BhWm_YOCMAE6zxc.jpg\",\"media_url_https\":\"https:\\/\\/pbs.twimg.com\\/media\\/BhWm_YOCMAE6zxc.jpg\",\"url\":\"http:\\/\\/t.co\\/eOeYQGXtTR\",\"display_url\":\"pic.twitter.com\\/eOeYQGXtTR\",\"expanded_url\":\"http:\\/\\/twitter.com\\/phlenderman\\/status\\/438440146665353216\\/photo\\/1\",\"type\":\"photo\",\"sizes\":{\"thumb\":{\"w\":150,\"h\":150,\"resize\":\"crop\"},\"small\":{\"w\":340,\"h\":453,\"resize\":\"fit\"},\"medium\":{\"w\":600,\"h\":800,\"resize\":\"fit\"},\"large\":{\"w\":768,\"h\":1024,\"resize\":\"fit\"}}}]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"lang\":\"en\"},{\"metadata\":{\"result_type\":\"recent\",\"iso_language_code\":\"en\"},\"created_at\":\"Tue Feb 25 22:22:47 +0000 2014\",\"id\":438438956565532673,\"id_str\":\"438438956565532673\",\"text\":\"IT'S NOT TOO LATE! Application deadline for Farm Credit Illinois's AGRICULTURE SCHOLARSHIPS is this Friday,... http:\\/\\/t.co\\/WdhHiFzEDW\",\"source\":\"\\u003ca href=\\\"http:\\/\\/www.facebook.com\\/twitter\\\" rel=\\\"nofollow\\\"\\u003eFacebook\\u003c\\/a\\u003e\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":138092869,\"id_str\":\"138092869\",\"name\":\"Farm Credit Illinois\",\"screen_name\":\"farmcreditIL\",\"location\":\"Illinois\",\"description\":\"We're a farmer-owned cooperative with a single purpose in mind - to serve the financial, credit and insurance needs of Illinois farmers and agribusinesses.\",\"url\":\"http:\\/\\/t.co\\/z4dSwS05OX\",\"entities\":{\"url\":{\"urls\":[{\"url\":\"http:\\/\\/t.co\\/z4dSwS05OX\",\"expanded_url\":\"http:\\/\\/www.farmcreditIL.com\",\"display_url\":\"farmcreditIL.com\",\"indices\":[0,22]}]},\"description\":{\"urls\":[]}},\"protected\":false,\"followers_count\":345,\"friends_count\":32,\"listed_count\":10,\"created_at\":\"Wed Apr 28 16:08:57 +0000 2010\",\"favourites_count\":0,\"utc_offset\":-21600,\"time_zone\":\"Central Time (US & Canada)\",\"geo_enabled\":true,\"verified\":false,\"statuses_count\":534,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"is_translation_enabled\":false,\"profile_background_color\":\"EDECE9\",\"profile_background_image_url\":\"http:\\/\\/abs.twimg.com\\/images\\/themes\\/theme3\\/bg.gif\",\"profile_background_image_url_https\":\"https:\\/\\/abs.twimg.com\\/images\\/themes\\/theme3\\/bg.gif\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/378800000825366210\\/5984af6ab8d47912877faa5a5b4faf0c_normal.jpeg\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/378800000825366210\\/5984af6ab8d47912877faa5a5b4faf0c_normal.jpeg\",\"profile_link_color\":\"088253\",\"profile_sidebar_border_color\":\"D3D2CF\",\"profile_sidebar_fill_color\":\"E3E2DE\",\"profile_text_color\":\"634047\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[{\"url\":\"http:\\/\\/t.co\\/WdhHiFzEDW\",\"expanded_url\":\"http:\\/\\/fb.me\\/2P6euNlqi\",\"display_url\":\"fb.me\\/2P6euNlqi\",\"indices\":[111,133]}],\"user_mentions\":[]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"lang\":\"en\"},{\"metadata\":{\"result_type\":\"recent\",\"iso_language_code\":\"en\"},\"created_at\":\"Tue Feb 25 21:24:19 +0000 2014\",\"id\":438424241021538304,\"id_str\":\"438424241021538304\",\"text\":\"#Job #Omaha Credit Review Specialist (Level 23B, 25C) Closing Date: 2-25-2014 at Farm Credit Services of Ameri... http:\\/\\/t.co\\/AN6SWPmBUI\",\"source\":\"\\u003ca href=\\\"http:\\/\\/twitterfeed.com\\\" rel=\\\"nofollow\\\"\\u003etwitterfeed\\u003c\\/a\\u003e\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":2343402674,\"id_str\":\"2343402674\",\"name\":\"Jobs in Omaha\",\"screen_name\":\"JobsinOmaha1\",\"location\":\"Omaha, NE\",\"description\":\"Tweets with latest #Jobs #Offers in #Omaha !!\",\"url\":null,\"entities\":{\"description\":{\"urls\":[]}},\"protected\":false,\"followers_count\":57,\"friends_count\":580,\"listed_count\":0,\"created_at\":\"Fri Feb 14 10:39:34 +0000 2014\",\"favourites_count\":0,\"utc_offset\":3600,\"time_zone\":\"Amsterdam\",\"geo_enabled\":false,\"verified\":false,\"statuses_count\":2246,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"is_translation_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_image_url\":\"http:\\/\\/abs.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"profile_background_image_url_https\":\"https:\\/\\/abs.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/434275907012812800\\/sUnxDsUw_normal.jpeg\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/434275907012812800\\/sUnxDsUw_normal.jpeg\",\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":true,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[{\"text\":\"Job\",\"indices\":[0,4]},{\"text\":\"Omaha\",\"indices\":[5,11]}],\"symbols\":[],\"urls\":[{\"url\":\"http:\\/\\/t.co\\/AN6SWPmBUI\",\"expanded_url\":\"http:\\/\\/q.gs\\/5gOVr\",\"display_url\":\"q.gs\\/5gOVr\",\"indices\":[114,136]}],\"user_mentions\":[]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"lang\":\"en\"},{\"metadata\":{\"result_type\":\"recent\",\"iso_language_code\":\"en\"},\"created_at\":\"Tue Feb 25 19:39:46 +0000 2014\",\"id\":438397931838660609,\"id_str\":\"438397931838660609\",\"text\":\"Also need to thank Farm Credit &amp; Bayer Crop Science for sponsorship foe SoyPac auction !\",\"source\":\"\\u003ca href=\\\"http:\\/\\/twitter.com\\/download\\/android\\\" rel=\\\"nofollow\\\"\\u003eTwitter for Android\\u003c\\/a\\u003e\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":63344223,\"id_str\":\"63344223\",\"name\":\"Mike Cunningham\",\"screen_name\":\"soyfarmer62\",\"location\":\"east-central Illinois\",\"description\":\"corn-soybean farmer in east-central Illinois\",\"url\":null,\"entities\":{\"description\":{\"urls\":[]}},\"protected\":false,\"followers_count\":282,\"friends_count\":355,\"listed_count\":2,\"created_at\":\"Thu Aug 06 02:41:28 +0000 2009\",\"favourites_count\":17,\"utc_offset\":-21600,\"time_zone\":\"Central Time (US & Canada)\",\"geo_enabled\":true,\"verified\":false,\"statuses_count\":481,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"is_translation_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_image_url\":\"http:\\/\\/abs.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"profile_background_image_url_https\":\"https:\\/\\/abs.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/418169193536557056\\/51ForwHj_normal.jpeg\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/418169193536557056\\/51ForwHj_normal.jpeg\",\"profile_banner_url\":\"https:\\/\\/pbs.twimg.com\\/profile_banners\\/63344223\\/1384180904\",\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":true,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorite_count\":1,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[],\"user_mentions\":[]},\"favorited\":false,\"retweeted\":false,\"lang\":\"en\"},{\"metadata\":{\"result_type\":\"recent\",\"iso_language_code\":\"en\"},\"created_at\":\"Tue Feb 25 19:02:35 +0000 2014\",\"id\":438388571960254464,\"id_str\":\"438388571960254464\",\"text\":\"#ATTRA News: Events: #Farm Credit East Webinar: Navigating the #Grants Process Feb 27 - http:\\/\\/t.co\\/dbr4brBkbr #ag #farmcredit\",\"source\":\"\\u003ca href=\\\"http:\\/\\/www.hootsuite.com\\\" rel=\\\"nofollow\\\"\\u003eHootSuite\\u003c\\/a\\u003e\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":1524712279,\"id_str\":\"1524712279\",\"name\":\"NCAT\",\"screen_name\":\"NCAT_ORG\",\"location\":\"Butte, MT\",\"description\":\"National Center for Appropriate Technology - championing small-scale, local, sustainable solutions - #ag #energy #eco Also: @ATTRASustainAg\",\"url\":\"http:\\/\\/t.co\\/WChn01R462\",\"entities\":{\"url\":{\"urls\":[{\"url\":\"http:\\/\\/t.co\\/WChn01R462\",\"expanded_url\":\"http:\\/\\/www.ncat.org\",\"display_url\":\"ncat.org\",\"indices\":[0,22]}]},\"description\":{\"urls\":[]}},\"protected\":false,\"followers_count\":524,\"friends_count\":480,\"listed_count\":12,\"created_at\":\"Mon Jun 17 12:10:13 +0000 2013\",\"favourites_count\":4,\"utc_offset\":-21600,\"time_zone\":\"Central Time (US & Canada)\",\"geo_enabled\":true,\"verified\":false,\"statuses_count\":2078,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"is_translation_enabled\":false,\"profile_background_color\":\"0099B9\",\"profile_background_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_background_images\\/378800000171779204\\/7uUcVP6R.png\",\"profile_background_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_background_images\\/378800000171779204\\/7uUcVP6R.png\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/425421727133233153\\/IAc3NKOp_normal.png\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/425421727133233153\\/IAc3NKOp_normal.png\",\"profile_banner_url\":\"https:\\/\\/pbs.twimg.com\\/profile_banners\\/1524712279\\/1372165092\",\"profile_link_color\":\"0099B9\",\"profile_sidebar_border_color\":\"FFFFFF\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[{\"text\":\"ATTRA\",\"indices\":[0,6]},{\"text\":\"Farm\",\"indices\":[21,26]},{\"text\":\"Grants\",\"indices\":[63,70]},{\"text\":\"ag\",\"indices\":[111,114]},{\"text\":\"farmcredit\",\"indices\":[115,126]}],\"symbols\":[],\"urls\":[{\"url\":\"http:\\/\\/t.co\\/dbr4brBkbr\",\"expanded_url\":\"http:\\/\\/ow.ly\\/t9yFu\",\"display_url\":\"ow.ly\\/t9yFu\",\"indices\":[88,110]}],\"user_mentions\":[]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"lang\":\"en\"},{\"metadata\":{\"result_type\":\"recent\",\"iso_language_code\":\"en\"},\"created_at\":\"Tue Feb 25 19:01:56 +0000 2014\",\"id\":438388411691716608,\"id_str\":\"438388411691716608\",\"text\":\"Strong global and domestic demand for U.S. agriculture products drive numbers. http:\\/\\/t.co\\/X0p957iNQN\",\"source\":\"\\u003ca href=\\\"http:\\/\\/www.hootsuite.com\\\" rel=\\\"nofollow\\\"\\u003eHootSuite\\u003c\\/a\\u003e\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":501199310,\"id_str\":\"501199310\",\"name\":\"Babcock Institute\",\"screen_name\":\"BabcockInst\",\"location\":\"UW-Madison\",\"description\":\"We link WI and U.S. dairy industries to industries around the world! Research. Development. Education.\",\"url\":\"http:\\/\\/t.co\\/ey0e2FdBV2\",\"entities\":{\"url\":{\"urls\":[{\"url\":\"http:\\/\\/t.co\\/ey0e2FdBV2\",\"expanded_url\":\"http:\\/\\/babcock.wisc.edu\",\"display_url\":\"babcock.wisc.edu\",\"indices\":[0,22]}]},\"description\":{\"urls\":[]}},\"protected\":false,\"followers_count\":225,\"friends_count\":200,\"listed_count\":9,\"created_at\":\"Thu Feb 23 22:18:21 +0000 2012\",\"favourites_count\":57,\"utc_offset\":null,\"time_zone\":null,\"geo_enabled\":false,\"verified\":false,\"statuses_count\":346,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"is_translation_enabled\":false,\"profile_background_color\":\"000000\",\"profile_background_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_background_images\\/438878703\\/twitterbannertrial.jpg\",\"profile_background_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_background_images\\/438878703\\/twitterbannertrial.jpg\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/1851022384\\/babcocklogo1_normal.jpg\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/1851022384\\/babcocklogo1_normal.jpg\",\"profile_link_color\":\"C24641\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[{\"url\":\"http:\\/\\/t.co\\/X0p957iNQN\",\"expanded_url\":\"http:\\/\\/ow.ly\\/tSgDc\",\"display_url\":\"ow.ly\\/tSgDc\",\"indices\":[79,101]}],\"user_mentions\":[]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"lang\":\"en\"},{\"metadata\":{\"result_type\":\"recent\",\"iso_language_code\":\"en\"},\"created_at\":\"Tue Feb 25 17:53:30 +0000 2014\",\"id\":438371188184592384,\"id_str\":\"438371188184592384\",\"text\":\"Congrats to CSU profs Cleon Kimberling &amp; Norman Dalsted inducted into Farm Credit Colorado Agriculture Hall of Fame http:\\/\\/t.co\\/0zhPbktZUf\",\"source\":\"\\u003ca href=\\\"https:\\/\\/about.twitter.com\\/products\\/tweetdeck\\\" rel=\\\"nofollow\\\"\\u003eTweetDeck\\u003c\\/a\\u003e\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":27098495,\"id_str\":\"27098495\",\"name\":\"Colorado State Univ\",\"screen_name\":\"ColoradoStateU\",\"location\":\"Fort Collins, Colo. \",\"description\":\"Colorado State University, home of the Rams, where Green & Gold pride runs deep. World-class researchers, passionate alumni & top-notch students make us CSU!\",\"url\":\"http:\\/\\/t.co\\/qb0lcVmior\",\"entities\":{\"url\":{\"urls\":[{\"url\":\"http:\\/\\/t.co\\/qb0lcVmior\",\"expanded_url\":\"http:\\/\\/www.colostate.edu\",\"display_url\":\"colostate.edu\",\"indices\":[0,22]}]},\"description\":{\"urls\":[]}},\"protected\":false,\"followers_count\":18391,\"friends_count\":4963,\"listed_count\":375,\"created_at\":\"Fri Mar 27 21:12:20 +0000 2009\",\"favourites_count\":5808,\"utc_offset\":-25200,\"time_zone\":\"Mountain Time (US & Canada)\",\"geo_enabled\":true,\"verified\":false,\"statuses_count\":19877,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"is_translation_enabled\":false,\"profile_background_color\":\"BFB87F\",\"profile_background_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_background_images\\/378800000047397358\\/b2a65249db1305794a79174457b75ee6.jpeg\",\"profile_background_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_background_images\\/378800000047397358\\/b2a65249db1305794a79174457b75ee6.jpeg\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/426107313854767105\\/hqRXZmwP_normal.jpeg\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/426107313854767105\\/hqRXZmwP_normal.jpeg\",\"profile_banner_url\":\"https:\\/\\/pbs.twimg.com\\/profile_banners\\/27098495\\/1391471324\",\"profile_link_color\":\"2A8F13\",\"profile_sidebar_border_color\":\"FFFFFF\",\"profile_sidebar_fill_color\":\"D4CFA5\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorite_count\":1,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[{\"url\":\"http:\\/\\/t.co\\/0zhPbktZUf\",\"expanded_url\":\"http:\\/\\/col.st\\/Mrw8tn\",\"display_url\":\"col.st\\/Mrw8tn\",\"indices\":[120,142]}],\"user_mentions\":[]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"lang\":\"en\"},{\"metadata\":{\"result_type\":\"recent\",\"iso_language_code\":\"en\"},\"created_at\":\"Tue Feb 25 14:15:09 +0000 2014\",\"id\":438316240759980032,\"id_str\":\"438316240759980032\",\"text\":\"BARN's Opening Ag Mkt Rpt: TUE FEB 25th btyb Gov's Forum, Farm Credit CO Ag Hall of Fame, Pletcher &amp; HPLE- http:\\/\\/t.co\\/kVrypscpIC...\",\"source\":\"\\u003ca href=\\\"http:\\/\\/www.hootsuite.com\\\" rel=\\\"nofollow\\\"\\u003eHootSuite\\u003c\\/a\\u003e\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":17392020,\"id_str\":\"17392020\",\"name\":\"BarnMedia\",\"screen_name\":\"barnmedia\",\"location\":\"Briggsdale, Colorado\",\"description\":\"Ag News, Markets & More!\",\"url\":\"http:\\/\\/t.co\\/i6vF3Ksfhm\",\"entities\":{\"url\":{\"urls\":[{\"url\":\"http:\\/\\/t.co\\/i6vF3Ksfhm\",\"expanded_url\":\"http:\\/\\/www.barnmedia.net\",\"display_url\":\"barnmedia.net\",\"indices\":[0,22]}]},\"description\":{\"urls\":[]}},\"protected\":false,\"followers_count\":2320,\"friends_count\":906,\"listed_count\":102,\"created_at\":\"Fri Nov 14 17:56:06 +0000 2008\",\"favourites_count\":5,\"utc_offset\":-25200,\"time_zone\":\"Mountain Time (US & Canada)\",\"geo_enabled\":false,\"verified\":false,\"statuses_count\":31778,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"is_translation_enabled\":false,\"profile_background_color\":\"9AE4E8\",\"profile_background_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_background_images\\/4281211\\/BARNsmalllogo.jpg\",\"profile_background_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_background_images\\/4281211\\/BARNsmalllogo.jpg\",\"profile_background_tile\":true,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/520022936\\/twitterProfilePhoto_normal.jpg\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/520022936\\/twitterProfilePhoto_normal.jpg\",\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"BDDCAD\",\"profile_sidebar_fill_color\":\"DDFFCC\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[{\"url\":\"http:\\/\\/t.co\\/kVrypscpIC\",\"expanded_url\":\"http:\\/\\/ow.ly\\/tYEfE\",\"display_url\":\"ow.ly\\/tYEfE\",\"indices\":[111,133]}],\"user_mentions\":[]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"lang\":\"en\"},{\"metadata\":{\"result_type\":\"recent\",\"iso_language_code\":\"en\"},\"created_at\":\"Tue Feb 25 10:25:29 +0000 2014\",\"id\":438258441048821760,\"id_str\":\"438258441048821760\",\"text\":\"Farm Credit Shares Financial Tips with Local Sustainable Food Producers http:\\/\\/t.co\\/rydXWpNK8k\",\"source\":\"\\u003ca href=\\\"http:\\/\\/www.hootsuite.com\\\" rel=\\\"nofollow\\\"\\u003eHootSuite\\u003c\\/a\\u003e\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":807195475,\"id_str\":\"807195475\",\"name\":\"Debt Relief Training\",\"screen_name\":\"DebtReliefTrain\",\"location\":\"Los Angeles\",\"description\":\"Free Online Tips Tricks Tools and Techniques for Getting Out of Debt and Improving Credit\",\"url\":\"http:\\/\\/t.co\\/nPinPwf2Uc\",\"entities\":{\"url\":{\"urls\":[{\"url\":\"http:\\/\\/t.co\\/nPinPwf2Uc\",\"expanded_url\":\"http:\\/\\/www.debtrelieftraining.com\\/\",\"display_url\":\"debtrelieftraining.com\",\"indices\":[0,22]}]},\"description\":{\"urls\":[]}},\"protected\":false,\"followers_count\":2511,\"friends_count\":2583,\"listed_count\":5,\"created_at\":\"Thu Sep 06 16:47:44 +0000 2012\",\"favourites_count\":0,\"utc_offset\":null,\"time_zone\":null,\"geo_enabled\":false,\"verified\":false,\"statuses_count\":2531,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"is_translation_enabled\":false,\"profile_background_color\":\"EBEBEB\",\"profile_background_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_background_images\\/652733747\\/2gqol97t12859vx12ksj.jpeg\",\"profile_background_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_background_images\\/652733747\\/2gqol97t12859vx12ksj.jpeg\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/2583575782\\/Debt_Relief_Training_Logo_normal.jpg\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/2583575782\\/Debt_Relief_Training_Logo_normal.jpg\",\"profile_link_color\":\"990000\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[{\"url\":\"http:\\/\\/t.co\\/rydXWpNK8k\",\"expanded_url\":\"http:\\/\\/ow.ly\\/2Ebwse\",\"display_url\":\"ow.ly\\/2Ebwse\",\"indices\":[72,94]}],\"user_mentions\":[]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"lang\":\"en\"},{\"metadata\":{\"result_type\":\"recent\",\"iso_language_code\":\"en\"},\"created_at\":\"Tue Feb 25 08:41:59 +0000 2014\",\"id\":438232394169344000,\"id_str\":\"438232394169344000\",\"text\":\"RT @majorsrikanth: 1. Another huge scam made, in the making etc etc. by @Devinder_Sharma on farmer loans waiver. http:\\/\\/t.co\\/iRobXAu2su\",\"source\":\"\\u003ca href=\\\"https:\\/\\/roundteam.co\\\" rel=\\\"nofollow\\\"\\u003eRoundTeam\\u003c\\/a\\u003e\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":1910721170,\"id_str\":\"1910721170\",\"name\":\"x militaire\",\"screen_name\":\"xmilitaire\",\"location\":\"Asia\",\"description\":\"Tweets from X Military Personnel\",\"url\":null,\"entities\":{\"description\":{\"urls\":[]}},\"protected\":false,\"followers_count\":144,\"friends_count\":17,\"listed_count\":1,\"created_at\":\"Fri Sep 27 10:14:09 +0000 2013\",\"favourites_count\":0,\"utc_offset\":null,\"time_zone\":null,\"geo_enabled\":false,\"verified\":false,\"statuses_count\":46183,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"is_translation_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_background_images\\/378800000123078040\\/0f661ef61cb87ce26199ce486fa10219.jpeg\",\"profile_background_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_background_images\\/378800000123078040\\/0f661ef61cb87ce26199ce486fa10219.jpeg\",\"profile_background_tile\":true,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/378800000514048632\\/9d31a1283a8c00babd12b3ebd5d5d038_normal.jpeg\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/378800000514048632\\/9d31a1283a8c00babd12b3ebd5d5d038_normal.jpeg\",\"profile_banner_url\":\"https:\\/\\/pbs.twimg.com\\/profile_banners\\/1910721170\\/1385321274\",\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"000000\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweeted_status\":{\"metadata\":{\"result_type\":\"recent\",\"iso_language_code\":\"en\"},\"created_at\":\"Tue Feb 25 08:36:18 +0000 2014\",\"id\":438230966348554240,\"id_str\":\"438230966348554240\",\"text\":\"1. Another huge scam made, in the making etc etc. by @Devinder_Sharma on farmer loans waiver. http:\\/\\/t.co\\/iRobXAu2su\",\"source\":\"web\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":45635832,\"id_str\":\"45635832\",\"name\":\"Major srikanth\",\"screen_name\":\"majorsrikanth\",\"location\":\"india\",\"description\":\"Ex Fauji,  atheist,  becoming a logical fundamentalist.Loves India much more than Indians\",\"url\":\"http:\\/\\/t.co\\/FWGGvEQOaH\",\"entities\":{\"url\":{\"urls\":[{\"url\":\"http:\\/\\/t.co\\/FWGGvEQOaH\",\"expanded_url\":\"http:\\/\\/majorsrikanth.blogspot.com\\/\",\"display_url\":\"majorsrikanth.blogspot.com\",\"indices\":[0,22]}]},\"description\":{\"urls\":[]}},\"protected\":false,\"followers_count\":687,\"friends_count\":187,\"listed_count\":22,\"created_at\":\"Mon Jun 08 18:42:25 +0000 2009\",\"favourites_count\":1210,\"utc_offset\":19800,\"time_zone\":\"Chennai\",\"geo_enabled\":true,\"verified\":false,\"statuses_count\":11877,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"is_translation_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_image_url\":\"http:\\/\\/abs.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"profile_background_image_url_https\":\"https:\\/\\/abs.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/416149635359465472\\/McFieB5X_normal.jpeg\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/416149635359465472\\/McFieB5X_normal.jpeg\",\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":true,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":1,\"favorite_count\":0,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[{\"url\":\"http:\\/\\/t.co\\/iRobXAu2su\",\"expanded_url\":\"http:\\/\\/devinder-sharma.blogspot.in\\/2013\\/03\\/the-rs-65-lakh-crore-farm-credit.html\",\"display_url\":\"devinder-sharma.blogspot.in\\/2013\\/03\\/the-rs\\u2026\",\"indices\":[94,116]}],\"user_mentions\":[{\"screen_name\":\"Devinder_Sharma\",\"name\":\"Devinder Sharma\",\"id\":92773212,\"id_str\":\"92773212\",\"indices\":[53,69]}]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"lang\":\"en\"},\"retweet_count\":1,\"favorite_count\":0,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[{\"url\":\"http:\\/\\/t.co\\/iRobXAu2su\",\"expanded_url\":\"http:\\/\\/devinder-sharma.blogspot.in\\/2013\\/03\\/the-rs-65-lakh-crore-farm-credit.html\",\"display_url\":\"devinder-sharma.blogspot.in\\/2013\\/03\\/the-rs\\u2026\",\"indices\":[113,135]}],\"user_mentions\":[{\"screen_name\":\"majorsrikanth\",\"name\":\"Major srikanth\",\"id\":45635832,\"id_str\":\"45635832\",\"indices\":[3,17]},{\"screen_name\":\"Devinder_Sharma\",\"name\":\"Devinder Sharma\",\"id\":92773212,\"id_str\":\"92773212\",\"indices\":[72,88]}]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"lang\":\"en\"},{\"metadata\":{\"result_type\":\"recent\",\"iso_language_code\":\"en\"},\"created_at\":\"Tue Feb 25 08:36:18 +0000 2014\",\"id\":438230966348554240,\"id_str\":\"438230966348554240\",\"text\":\"1. Another huge scam made, in the making etc etc. by @Devinder_Sharma on farmer loans waiver. http:\\/\\/t.co\\/iRobXAu2su\",\"source\":\"web\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":45635832,\"id_str\":\"45635832\",\"name\":\"Major srikanth\",\"screen_name\":\"majorsrikanth\",\"location\":\"india\",\"description\":\"Ex Fauji,  atheist,  becoming a logical fundamentalist.Loves India much more than Indians\",\"url\":\"http:\\/\\/t.co\\/FWGGvEQOaH\",\"entities\":{\"url\":{\"urls\":[{\"url\":\"http:\\/\\/t.co\\/FWGGvEQOaH\",\"expanded_url\":\"http:\\/\\/majorsrikanth.blogspot.com\\/\",\"display_url\":\"majorsrikanth.blogspot.com\",\"indices\":[0,22]}]},\"description\":{\"urls\":[]}},\"protected\":false,\"followers_count\":687,\"friends_count\":187,\"listed_count\":22,\"created_at\":\"Mon Jun 08 18:42:25 +0000 2009\",\"favourites_count\":1210,\"utc_offset\":19800,\"time_zone\":\"Chennai\",\"geo_enabled\":true,\"verified\":false,\"statuses_count\":11877,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"is_translation_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_image_url\":\"http:\\/\\/abs.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"profile_background_image_url_https\":\"https:\\/\\/abs.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/416149635359465472\\/McFieB5X_normal.jpeg\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/416149635359465472\\/McFieB5X_normal.jpeg\",\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":true,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":1,\"favorite_count\":0,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[{\"url\":\"http:\\/\\/t.co\\/iRobXAu2su\",\"expanded_url\":\"http:\\/\\/devinder-sharma.blogspot.in\\/2013\\/03\\/the-rs-65-lakh-crore-farm-credit.html\",\"display_url\":\"devinder-sharma.blogspot.in\\/2013\\/03\\/the-rs\\u2026\",\"indices\":[94,116]}],\"user_mentions\":[{\"screen_name\":\"Devinder_Sharma\",\"name\":\"Devinder Sharma\",\"id\":92773212,\"id_str\":\"92773212\",\"indices\":[53,69]}]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"lang\":\"en\"},{\"metadata\":{\"result_type\":\"recent\",\"iso_language_code\":\"en\"},\"created_at\":\"Mon Feb 24 21:49:37 +0000 2014\",\"id\":438068222185570304,\"id_str\":\"438068222185570304\",\"text\":\"Customer Service Representative: Farm Credit Canada \\/ FCC (Yorkton) \\\"Lending and... http:\\/\\/t.co\\/2adHTojqJG #saskatchewan #jobs\",\"source\":\"\\u003ca href=\\\"http:\\/\\/dlvr.it\\\" rel=\\\"nofollow\\\"\\u003edlvr.it\\u003c\\/a\\u003e\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":249881819,\"id_str\":\"249881819\",\"name\":\"Saskatchewan Jobs\",\"screen_name\":\"SK_Jobs\",\"location\":\"Saskatchewan, Canada\",\"description\":\"New jobs across Saskatchewan directly from employer websites\",\"url\":\"http:\\/\\/t.co\\/pG2A2EronP\",\"entities\":{\"url\":{\"urls\":[{\"url\":\"http:\\/\\/t.co\\/pG2A2EronP\",\"expanded_url\":\"http:\\/\\/www.eluta.ca\\/saskatchewan-jobs\",\"display_url\":\"eluta.ca\\/saskatchewan-j\\u2026\",\"indices\":[0,22]}]},\"description\":{\"urls\":[]}},\"protected\":false,\"followers_count\":2370,\"friends_count\":2449,\"listed_count\":30,\"created_at\":\"Wed Feb 09 23:58:25 +0000 2011\",\"favourites_count\":0,\"utc_offset\":-21600,\"time_zone\":\"Saskatchewan\",\"geo_enabled\":false,\"verified\":false,\"statuses_count\":35188,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"is_translation_enabled\":false,\"profile_background_color\":\"02070A\",\"profile_background_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_background_images\\/446655394\\/skjobs_twitter_background.jpg\",\"profile_background_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_background_images\\/446655394\\/skjobs_twitter_background.jpg\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/1239761428\\/SK_jobs_normal.png\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/1239761428\\/SK_jobs_normal.png\",\"profile_banner_url\":\"https:\\/\\/pbs.twimg.com\\/profile_banners\\/249881819\\/1387296411\",\"profile_link_color\":\"33281B\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[{\"text\":\"saskatchewan\",\"indices\":[107,120]},{\"text\":\"jobs\",\"indices\":[121,126]}],\"symbols\":[],\"urls\":[{\"url\":\"http:\\/\\/t.co\\/2adHTojqJG\",\"expanded_url\":\"http:\\/\\/bit.ly\\/1hqPwkt\",\"display_url\":\"bit.ly\\/1hqPwkt\",\"indices\":[84,106]}],\"user_mentions\":[]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"lang\":\"en\"},{\"metadata\":{\"result_type\":\"recent\",\"iso_language_code\":\"en\"},\"created_at\":\"Mon Feb 24 21:00:30 +0000 2014\",\"id\":438055859067695105,\"id_str\":\"438055859067695105\",\"text\":\"March 3rd deadline for #4H and #FFA in #illinois to apply for 1st Farm Credit Community Improvement Grant. \\n\\nhttps:\\/\\/t.co\\/ugWUpSLek4\",\"source\":\"\\u003ca href=\\\"https:\\/\\/about.twitter.com\\/products\\/tweetdeck\\\" rel=\\\"nofollow\\\"\\u003eTweetDeck\\u003c\\/a\\u003e\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":2260335517,\"id_str\":\"2260335517\",\"name\":\"1st Farm Credit \",\"screen_name\":\"1stFarmCredit\",\"location\":\"Northern 42 Counties in IL\",\"description\":\"1st Farm Credit Services leads the industry in agriculture loans, risk management products and various services for clients in 42 northern Illinois counties.\",\"url\":\"http:\\/\\/t.co\\/kxqT40IEVv\",\"entities\":{\"url\":{\"urls\":[{\"url\":\"http:\\/\\/t.co\\/kxqT40IEVv\",\"expanded_url\":\"http:\\/\\/www.1stfarmcredit.com\",\"display_url\":\"1stfarmcredit.com\",\"indices\":[0,22]}]},\"description\":{\"urls\":[]}},\"protected\":false,\"followers_count\":33,\"friends_count\":45,\"listed_count\":0,\"created_at\":\"Tue Dec 24 14:25:45 +0000 2013\",\"favourites_count\":0,\"utc_offset\":null,\"time_zone\":null,\"geo_enabled\":false,\"verified\":false,\"statuses_count\":27,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"is_translation_enabled\":false,\"profile_background_color\":\"F5FAF7\",\"profile_background_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_background_images\\/378800000156494700\\/sSCs9ZW3.jpeg\",\"profile_background_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_background_images\\/378800000156494700\\/sSCs9ZW3.jpeg\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/415513440120082432\\/XnR4M8u3_normal.jpeg\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/415513440120082432\\/XnR4M8u3_normal.jpeg\",\"profile_banner_url\":\"https:\\/\\/pbs.twimg.com\\/profile_banners\\/2260335517\\/1391205381\",\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"000000\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[{\"text\":\"4H\",\"indices\":[23,26]},{\"text\":\"FFA\",\"indices\":[31,35]},{\"text\":\"illinois\",\"indices\":[39,48]}],\"symbols\":[],\"urls\":[{\"url\":\"https:\\/\\/t.co\\/ugWUpSLek4\",\"expanded_url\":\"https:\\/\\/www.1stfarmcredit.com\\/about-us\\/community\\/profile\\/community-improvement-grants\",\"display_url\":\"1stfarmcredit.com\\/about-us\\/commu\\u2026\",\"indices\":[109,132]}],\"user_mentions\":[]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"lang\":\"en\"},{\"metadata\":{\"result_type\":\"recent\",\"iso_language_code\":\"fr\"},\"created_at\":\"Mon Feb 24 20:46:44 +0000 2014\",\"id\":438052395939950592,\"id_str\":\"438052395939950592\",\"text\":\"Adjoint ou adjointe au service \\u00e0 la client\\u00e8le: Farm Credit Canada \\/ FCC (Rosetown) \\\"Aptitudes... http:\\/\\/t.co\\/K9jeLYpTpV #saskatchewan #jobs\",\"source\":\"\\u003ca href=\\\"http:\\/\\/dlvr.it\\\" rel=\\\"nofollow\\\"\\u003edlvr.it\\u003c\\/a\\u003e\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":249881819,\"id_str\":\"249881819\",\"name\":\"Saskatchewan Jobs\",\"screen_name\":\"SK_Jobs\",\"location\":\"Saskatchewan, Canada\",\"description\":\"New jobs across Saskatchewan directly from employer websites\",\"url\":\"http:\\/\\/t.co\\/pG2A2EronP\",\"entities\":{\"url\":{\"urls\":[{\"url\":\"http:\\/\\/t.co\\/pG2A2EronP\",\"expanded_url\":\"http:\\/\\/www.eluta.ca\\/saskatchewan-jobs\",\"display_url\":\"eluta.ca\\/saskatchewan-j\\u2026\",\"indices\":[0,22]}]},\"description\":{\"urls\":[]}},\"protected\":false,\"followers_count\":2370,\"friends_count\":2449,\"listed_count\":30,\"created_at\":\"Wed Feb 09 23:58:25 +0000 2011\",\"favourites_count\":0,\"utc_offset\":-21600,\"time_zone\":\"Saskatchewan\",\"geo_enabled\":false,\"verified\":false,\"statuses_count\":35188,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"is_translation_enabled\":false,\"profile_background_color\":\"02070A\",\"profile_background_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_background_images\\/446655394\\/skjobs_twitter_background.jpg\",\"profile_background_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_background_images\\/446655394\\/skjobs_twitter_background.jpg\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/1239761428\\/SK_jobs_normal.png\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/1239761428\\/SK_jobs_normal.png\",\"profile_banner_url\":\"https:\\/\\/pbs.twimg.com\\/profile_banners\\/249881819\\/1387296411\",\"profile_link_color\":\"33281B\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[{\"text\":\"saskatchewan\",\"indices\":[120,133]},{\"text\":\"jobs\",\"indices\":[134,139]}],\"symbols\":[],\"urls\":[{\"url\":\"http:\\/\\/t.co\\/K9jeLYpTpV\",\"expanded_url\":\"http:\\/\\/bit.ly\\/1hqBT4N\",\"display_url\":\"bit.ly\\/1hqBT4N\",\"indices\":[97,119]}],\"user_mentions\":[]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"lang\":\"fr\"}],\"search_metadata\":{\"completed_in\":0.023,\"max_id\":438441232331964416,\"max_id_str\":\"438441232331964416\",\"next_results\":\"?max_id=438052395939950591&q=%22Farm%20Credit%22&include_entities=1\",\"query\":\"%22Farm+Credit%22\",\"refresh_url\":\"?since_id=438441232331964416&q=%22Farm%20Credit%22&include_entities=1\",\"count\":15,\"since_id\":0,\"since_id_str\":\"0\"}}";

}
