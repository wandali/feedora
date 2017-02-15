package com.sample.foo.simplerssreader;

import android.net.Uri;

/**
 * Created by obaro on 27/11/2016.
 */

public class RssFeedModel {

    public String title;
    public String link;
    public String description;
    public String newDescription = "";
    public Uri thumbnailUri;

    public RssFeedModel(String title, String link, String description, String thumbnailUri) {
        int x = 0;
        this.title = title;
        this.link = link;
        String xmlChange = "";

        for (char c : description.toCharArray()) {

            if (c == '<') {
                x = 1;
            } else if (c == '>') {
                x = 0;
            } else if (c == '&') {
                x = 2;
                xmlChange = xmlChange + c;
            } else if (x == 0) {
                newDescription = newDescription + c;
            } else if (x == 2) {
                if (c != ';') {
                    xmlChange = xmlChange + c;
                } else {
                    x = 0;

                    if (xmlChange.equals("&#8217") || xmlChange.equals("&#039")) {
                        newDescription = newDescription + "'";
                        xmlChange = "";
                        x = 0;
                    } else if (xmlChange.equals("&#8220") || xmlChange.equals("&#8221")) {
                        newDescription = newDescription + '"';
                        xmlChange = "";
                        x = 0;
                    } else if (xmlChange.equals("&nbsp")) {
                        xmlChange = "";
                        x = 0;
                    } else {
                        xmlChange = "";
                        x = 0;
                    }
                }

            }

        }
        this.description = newDescription;
        if (thumbnailUri != null) this.thumbnailUri = Uri.parse(thumbnailUri);
    }
}
