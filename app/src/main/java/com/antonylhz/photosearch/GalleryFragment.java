package com.antonylhz.photosearch;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.antonylhz.photosearch.flickr.FlickrSearchClient;
import com.antonylhz.photosearch.google.GoogleSearchClient;
import com.reginald.swiperefresh.CustomSwipeRefreshLayout;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class GalleryFragment extends Fragment {
    private static final String TAG = GalleryFragment.class.getSimpleName();

    private static final int COLUMN_NUM = 3;

    private SearchClient mSearchClient;

    private RequestQueue mRequestQueue;
    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private CustomSwipeRefreshLayout mCustomSwipeRefreshLayout;

    private GalleryAdapter mAdapter;

    private boolean mLoading = false;

    private SearchView mSearchView;

    public GalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mSearchClient = GoogleSearchClient.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        mRequestQueue = Volley.newRequestQueue(getActivity());

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int totalItem = mLayoutManager.getItemCount();
                int lastItemPos = mLayoutManager.findLastVisibleItemPosition();
                if (!mLoading && totalItem - 1 != lastItemPos) {
                    startLoading();
                }
            }
        });

        mLayoutManager = new GridLayoutManager(getActivity(), COLUMN_NUM);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new GalleryAdapter(getActivity(), new ArrayList<GalleryItem>());
        mRecyclerView.setAdapter(mAdapter);

        mCustomSwipeRefreshLayout = (CustomSwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        mCustomSwipeRefreshLayout.setOnRefreshListener(
                new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        startLoading();
        return view;
    }

    public void refresh() {
        mAdapter.clear();
        startLoading();
    }

    private void startLoading() {
        Log.d(TAG, "startLoading");
        mLoading = true;
        int count = mLayoutManager.getItemCount();
        String query = PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getString(SearchClient.PREF_SEARCH_QUERY, null);

        String url = mSearchClient.getUrl(query, count);
        if (url == null) {
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse " + response);
                        List<GalleryItem> result = mSearchClient.parseJsonResponse(response);
                        if(result != null) {
                            mAdapter.addAll(result);
                            mAdapter.notifyDataSetChanged();
                        }
                        mLoading = false;
                        mCustomSwipeRefreshLayout.refreshComplete();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley failed", error);
                    }
                }
        );
        request.setTag(TAG);
        mRequestQueue.add(request);
    }

    private void stopLoading() {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLoading();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        setupSearchEngineSpinnerMenu(menu.findItem(R.id.menu_search_engine_spinner));
        setupSearchMenu(menu.findItem(R.id.menu_item_search));
    }

    private void setupSearchEngineSpinnerMenu(MenuItem spinnerItem) {
        View view = spinnerItem.getActionView();
        if(view instanceof Spinner) {
            Spinner spinner = (Spinner) view;
            spinner.setAdapter(ArrayAdapter.createFromResource(
                    getContext(), R.array.pref_search_engine,
                    android.R.layout.simple_spinner_dropdown_item
            ));
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    switch (position) {
                        case 0:
                            if (!(mSearchClient instanceof GoogleSearchClient)) {
                                mSearchClient = GoogleSearchClient.getInstance();
                                refresh();
                            }
                            break;
                        case 1:
                            if (!(mSearchClient instanceof FlickrSearchClient)) {
                                mSearchClient = FlickrSearchClient.getInstance();
                                refresh();
                            }
                            break;
                        default:

                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    private void setupSearchMenu(MenuItem searchItem) {
        mSearchView = (SearchView) searchItem.getActionView();

        SearchManager searchManager = (SearchManager) getActivity()
                .getSystemService(Context.SEARCH_SERVICE);
        ComponentName name = getActivity().getComponentName();
        SearchableInfo searchInfo = searchManager.getSearchableInfo(name);
        mSearchView.setSearchableInfo(searchInfo);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean selectionHandled;
        switch (item.getItemId()) {
            case R.id.menu_search_engine_spinner:

            case R.id.menu_item_search:
                getActivity().onSearchRequested();
                selectionHandled = true;
                break;
            case R.id.menu_item_move:
                if(mRecyclerView != null) {
                    mRecyclerView.smoothScrollToPosition(0);
                }
                selectionHandled = true;
                break;
            case R.id.menu_item_clear:
                if(mSearchView != null) {
                    mSearchView.setQuery("", false);
                    mSearchView.setIconified(false);
                }

                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(SearchClient.PREF_SEARCH_QUERY, null)
                        .commit();
                refresh();
                selectionHandled = true;
                break;
            default:
                selectionHandled = super.onOptionsItemSelected(item);
                break;
        }
        return selectionHandled;
    }
}
