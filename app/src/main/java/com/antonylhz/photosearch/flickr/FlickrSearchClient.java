package com.antonylhz.photosearch.flickr;

import android.net.Uri;
import android.util.Log;
import com.antonylhz.photosearch.GalleryItem;
import com.antonylhz.photosearch.SearchClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Author  : KAILIANG CHEN
 * Version : 0.1
 * Date    : 1/17/16
 *
 *
     Flickr API

     You can make this call to the Flickr API to return a JSON object with a list of photos.

     https://api.flickr.com/services/rest/?method=flickr.photos.search&
           api_key=178069b03af62f5735258c0a10a14d6e&format=json&nojsoncallback=1&text=kittens

     The text parameter should be replaced with the query that the user enters into the app.

     The JSON response you'll receive will have items described like this example.

     {
         "id": "23451156376",
             "owner": "28017113@N08",
             "secret": "8983a8ebc7",
             "server": "578",
             "farm": 1,
             "title": "Merry Christmas!",
             "ispublic": 1,
             "isfriend": 0,
             "isfamily": 0
     },
     You can use these parameters to get the full URL of the photo:

     http://farm{farm}.static.flickr.com/{server}/{id}_{secret}.jpg
     So, using our example from before, the URL would be

     http://farm1.static.flickr.com/578/23451156376_8983a8ebc7.jpg

     If interested, more documentation about the search endpoint can be found at
     https://www.flickr.com/services/api/explore/flickr.photos.search.

     You can generate your own at https://www.flickr.com/services/api/misc.api_keys.html.
 *
 */
public class FlickrSearchClient implements SearchClient {
    private static final String LOG_TAG = FlickrSearchClient.class.getSimpleName();

    private static final int PAGE_CAPACITY = 100;

    private static final String ENDPOINT = "https://api.flickr.com/services/rest/";
    private static final String API_KEY = "178069b03af62f5735258c0a10a14d6e";
    private static final String METHOD_GETRECENT = "flickr.photos.getRecent";
    private static final String METHOD_SEARCH = "flickr.photos.search";

    private static volatile FlickrSearchClient instance = null;
    private FlickrSearchClient() {

    }

    public static FlickrSearchClient getInstance() {
        if (instance == null) {
            synchronized (FlickrSearchClient.class) {
                if (instance == null) {
                    instance = new FlickrSearchClient();
                }
            }
        }
        return instance;
    }

    public String getUrl(String query, int count) {
        String url;
        int page = count / PAGE_CAPACITY + 1;
        if (query != null) {
            url = Uri.parse(ENDPOINT).buildUpon()
                    .appendQueryParameter("method", METHOD_SEARCH)
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("text", query)
                    .appendQueryParameter("page", String.valueOf(page))
                    .build().toString();
        } else {
            url = Uri.parse(ENDPOINT).buildUpon()
                    .appendQueryParameter("method", METHOD_GETRECENT)
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("page", String.valueOf(page))
                    .build().toString();
        }
        return url;
    }

    public List<GalleryItem> parseJsonResponse(JSONObject response) {
        List<GalleryItem> result = new ArrayList<>();
        try {
            JSONObject photos = response.getJSONObject("photos");
            JSONArray photoArr = photos.getJSONArray("photo");
            for (int i = 0; i < photoArr.length(); i++) {
                JSONObject itemObj = photoArr.getJSONObject(i);
                FlickrGalleryItem item = new FlickrGalleryItem(
                        itemObj.getString("id"),
                        itemObj.getString("secret"),
                        itemObj.getString("server"),
                        itemObj.getString("farm")
                );
                result.add(item);
            }
        } catch (JSONException e) {
            Log.d(LOG_TAG, "Error in Json response!");
            e.printStackTrace();
        }
        return result;
    }



}
