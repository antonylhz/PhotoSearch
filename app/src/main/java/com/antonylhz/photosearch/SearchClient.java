package com.antonylhz.photosearch;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by antonylhz on 2/9/16.
 */
public interface SearchClient {

    String PREF_SEARCH_QUERY ="searchQuery";

    /**
     * Given query string, and desired quantity, return the composed search url
     *
     * @param query
     * @param count
     * @return searchable url
     */
    String getUrl(String query, int count);

    /**
     * Given Json response, parse it into list of GalleryItem's
     * @param response
     * @return
     */
    List<GalleryItem> parseJsonResponse(JSONObject response);

}
