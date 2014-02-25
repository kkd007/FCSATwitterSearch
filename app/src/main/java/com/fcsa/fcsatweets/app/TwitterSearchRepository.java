package com.fcsa.fcsatweets.app;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by DhulipalaK on 2/24/14.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TwitterSearchRepository {

    List<TwitterSearchResult> mTwitterSearchResults;

    public List readJsonStream(InputStream in) throws IOException
    {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        reader.setLenient(true);

        try
        {
            return readMessagesArray(reader);
        }
        finally
        {
            reader.close();
        }
    }


    public List readMessagesArray(JsonReader reader) throws IOException {

        try
        {
            reader.beginArray();
            reader.endArray();
        }
        catch (Exception ex)
        {
            String msg = ex.getMessage();

        }

//        while (reader.hasNext())
//        {
//            mTwitterSearchResults.add(ReadTwitterSearch(reader));
//        }
//        reader.endArray();
        return mTwitterSearchResults;
    }





        public TwitterSearchResult ReadTwitterSearch(JsonReader jsonReader) throws IOException
        {
            String _Name = null;

        String _CreatedDate = null;

        String _Text = null;

        String _ScreenName = null;

        jsonReader.beginObject();

        while(jsonReader.hasNext())
        {
            String xmlTagName = jsonReader.nextName();
            if(xmlTagName.equals("name"))
            {
                _Name = jsonReader.nextString();
            }
            else
            if(xmlTagName.equals("created_at"))
            {
                _CreatedDate = jsonReader.nextString();
            }
            else
            if(xmlTagName.equals("screen_name"))
            {
                _ScreenName = jsonReader.nextString();
            }
            else
            if(xmlTagName.equals("text"))
            {
                _Text = jsonReader.nextString();
            }
            else
            {
                jsonReader.skipValue();
            }

        }
        jsonReader.endObject();

        TwitterSearchResult twitterSearchResult = new TwitterSearchResult();
            twitterSearchResult.setName(_Name);
            twitterSearchResult.setCreatedDate(_CreatedDate);
            twitterSearchResult.setScreenName(_ScreenName);
            twitterSearchResult.setText(_Text);

         return twitterSearchResult;
    }
}

/*
* {
  sections:
      [
          {
             "SectionId": 1,
             "SectionName": "Android"
          }
      ]
}
This is a Json object with your array data set to sections property of a JSon object.

Now do it as follows:

JSONObject jso = new JSONObject(jsonString);
JSONArray ja = jso.getJSONArray("sections");

Data sections = new Data();

for (int i = 0; i < ja.length(); i++) {
    Section s = new Section();
    JSONObject jsonSection = ja.getJSONObject(i);

    s.SectionId = Integer.ValueOf(jsonSection.getString("SectionId"));
    s.SectionName = jsonSection.getString("SectionName");

   //add it to sections list
   sections.add(s);
}

return sections;
*
*
* */
