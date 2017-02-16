package com.sample.foo.simplerssreader;

import android.net.Uri;
import android.util.Log;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Created by obaro on 27/11/2016.
 */

public class RssFeedModel {

    public String title;
    public String link;
    public String description;
    public Uri thumbnailUri;

    String parseDescription(String description) {
        String parsed = description;
        parsed = parsed.replaceAll("<img.*\\/>", "");
        parsed = parsed.replaceAll("<p>", "");
        parsed = parsed.replaceAll("<\\/p>", "");
        parsed = parsed.trim();
        parsed = StringEscapeUtils.unescapeXml(parsed);
        parsed = StringEscapeUtils.unescapeHtml(parsed);
        return parsed;
    }

    public RssFeedModel(String title, String link, String description, String thumbnailUri) {
        this.title = title;
        this.link = link;
        this.description = this.parseDescription(description);
        if (thumbnailUri != null) this.thumbnailUri = Uri.parse(thumbnailUri);
    }
    public void printArticle(){
        Log.d("RSSReedModel",title + " " + link + " "+ description + " " + thumbnailUri);
    }
}
