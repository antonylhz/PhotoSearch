package com.antonylhz.photosearch;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.net.URL;

public class GalleryActivity extends AppCompatActivity {

    private static final String TAG = GalleryActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d(TAG, "Received a new search query: " + query);
            try {
                URL url = new URL(query);
                handleUrl(url);
            } catch (Exception e) {
                handleQuery(query);
            }
        } else if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                Log.d(TAG, "Received a new URL to extract images");
            } else if (type.startsWith("image/")) {
                Log.d(TAG, "Received a new image to display");
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                Log.d(TAG, "Received a new image group to display");
            }
        }
    }

    private void handleQuery(String query) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(SearchClient.PREF_SEARCH_QUERY, query)
                .commit();
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.gallery_fragment);
        if (fragment != null) {
            ((GalleryFragment) fragment).refresh();
        }
    }

    private void handleUrl(URL url) {

    }

}
