package com.schoolmgmtsys.root.ssg.utils;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;

import java.net.URL;

public class GlideUrlAdv extends GlideUrl {
    private String cacheKey;

    public GlideUrlAdv(String mUrl,String cacheKey) {
        super(mUrl);
        this.cacheKey = cacheKey;
    }

    public GlideUrlAdv(String mUrl,String cacheKey, Headers headers) {
        super(mUrl, headers);
        this.cacheKey = cacheKey;
    }

    public GlideUrlAdv(URL url) {
        super(url);
        this.cacheKey = url.getPath();
    }

    public GlideUrlAdv(URL url, Headers headers) {
        super(url, headers);
        this.cacheKey = url.getPath();
    }

    @Override
    public String getCacheKey() {
        return cacheKey;
    }
}