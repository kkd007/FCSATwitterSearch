package com.fcsa.fcsatweets.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.Arrays;
import java.util.List;


public class MainFragment extends Fragment {

    private MainActivity mMainActivity;
    private SharedPreferences savedSearches;
    private TableLayout queryTableLayout;
    private EditText queryEditText;
    private EditText tagEditText;


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

            // Start TwitterSearchActivity
            Intent i = new Intent(getActivity(),TwitterSearchActivity.class);
            i.putExtra(TwitterSearchFragment.EXTRA_SEARCH_QUERY,query);
            startActivity(i);


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


}
