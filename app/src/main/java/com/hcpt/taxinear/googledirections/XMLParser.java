package com.hcpt.taxinear.googledirections;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

public class XMLParser {
	
	protected static final String MARKERS = "markers";
    protected static final String MARKER = "marker";

    protected URL feedUrl;

    protected XMLParser(final String feedUrl) {
        try {
            this.feedUrl = new URL(feedUrl);
        } catch (MalformedURLException e) {
            Log.e("Routing Error", e.getMessage());
        }
    }

    protected InputStream getInputStream() {
        try {
            return feedUrl.openConnection().getInputStream();
        } catch (IOException e) {
            Log.e("Routing Error", e.getMessage());
            return null;
        }
    }
}
