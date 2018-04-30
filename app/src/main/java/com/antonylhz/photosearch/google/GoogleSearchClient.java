package com.antonylhz.photosearch.google;

import android.util.Log;
import com.antonylhz.photosearch.GalleryItem;
import com.antonylhz.photosearch.SearchClient;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by antonylhz on 2/9/16.
 *
 * Example API REST call
 *
 * https://www.googleapis.com/customsearch/v1?q=cat&cx=010595824610626994902%3Azvnsp0ycbiw&searchType=image&key=AIzaSyDrH8CwM2dxEVAMkzwkbqqChaWUIHGJKhk
 *
 *
 * Due to the limited daily search allowance (100),
 * here we just show one page of images (10)
 *
 */
public class GoogleSearchClient implements SearchClient {
    private static final String LOG_TAG = GoogleSearchClient.class.getSimpleName();

    private static final String CX = "010595824610626994902:zvnsp0ycbiw";
    private static final String KEY = "AIzaSyDrH8CwM2dxEVAMkzwkbqqChaWUIHGJKhk";
    private static final String SEARCH_TYPE_IMAGE = "image";

    private static volatile GoogleSearchClient instance = null;
    private GoogleSearchClient() {

    }

    public static GoogleSearchClient getInstance() {
        if (instance == null) {
            synchronized (GoogleSearchClient.class) {
                if (instance == null) {
                    instance = new GoogleSearchClient();
                }
            }
        }
        return instance;
    }

    public String getUrl(String query, int count) {
        String url = null;
        if (query != null) {
            HttpRequestInitializer httpRequestInitializer = new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {

                }
            };
            JsonFactory jsonFactory = new JacksonFactory();
            Customsearch custom = new Customsearch(new NetHttpTransport(), jsonFactory,
                    httpRequestInitializer);
            try {
                Customsearch.Cse.List list = custom.cse().list(query)
                                                .setCx(CX)
                                                .setKey(KEY)
                                                .setSearchType(SEARCH_TYPE_IMAGE);
                url = list.buildHttpRequestUrl().toString();

            } catch (Exception e) {
                Log.e(LOG_TAG, "Cannot load images for query = " + query);
                e.printStackTrace();
            }
        }

        return url;
    }

    /**
     * Google custom search response: root -> [items] -> link
     *
     * @param response
     * @return
     */
    public List<GalleryItem> parseJsonResponse(JSONObject response) {
        List<GalleryItem> result = new ArrayList<>();
        try {
            JSONArray photoArr = response.getJSONArray("items");
            for (int i = 0; i < photoArr.length(); i++) {
                JSONObject itemObj = photoArr.getJSONObject(i);
                GoogleGalleryItem item = new GoogleGalleryItem(
                        itemObj.getString("link")
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
