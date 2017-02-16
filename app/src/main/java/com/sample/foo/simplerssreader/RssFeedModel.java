package com.sample.foo.simplerssreader;

import android.net.Uri;
import android.util.Log;

import org.apache.commons.lang.StringEscapeUtils;


public class RssFeedModel {

    public String title;
    public String link;
    public String description;
    public Uri thumbnailUri;

    /* Date: 16/02/2017
    Wanda: Trims the tab spaces and parses out the image within the description
    so only the proper description is shown in the UI */
    String parseDescription(String description) {
        String parsed = description;
        parsed = parsed.replaceAll("<img.*\\/>", "");
        parsed = parsed.replaceAll("<p>", "");
        parsed = parsed.replaceAll("<\\/p>", "");
        parsed = parsed.trim();

        /* Date: 16/02/2017
        Wanda: Changes unicode to their respective escape character */
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
