package com.antonylhz.photosearch.google;

import com.antonylhz.photosearch.GalleryItem;

/**
 * Created by antonylhz on 2/9/16.
 */
public class GoogleGalleryItem implements GalleryItem {

    private String url;

    public GoogleGalleryItem(String url) {
        this.url = url;
    }

    @Override
    public String getUrl() {
        return url;
    }
}
