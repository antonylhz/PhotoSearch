package com.antonylhz.photosearch.flickr;

import com.antonylhz.photosearch.GalleryItem;

/**
 * Author  : KAILIANG CHEN
 * Version : 0.1
 * Date    : 1/17/16
 */
public class FlickrGalleryItem implements GalleryItem {

    private String id;
    private String secret;
    private String server;
    private String farm;

    public FlickrGalleryItem(String id, String secret, String server, String farm) {
        this.id = id;
        this.secret = secret;
        this.server = server;
        this.farm = farm;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return "http://farm" + farm + ".static.flickr.com/" + server + "/" + id + "_" + secret + ".jpg";
    }
}
