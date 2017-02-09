package com.sample.foo.simplerssreader;

/**
 * Created by obaro on 27/11/2016.
 */

public class RssFeedModel {

    public String title;
    public String link;
    public String description;
    public String newDescription = "";

    public RssFeedModel(String title, String link, String description) {
        int x = 0;
        this.title = title;
        this.link = link;
        for (char c : description.toCharArray()) {

            if (c == '<') {
                x = 1;
            } else if (c == '>') {
                x = 0;
            } else if (c == '&') {
                x = 2;
            } else if (x == 0) {
                newDescription = newDescription + c;
            } else if (x == 2) {
                if (c == 'q') {
                    x = 3;
                } else {
                    x = 4;
                }
            } else if (c == ';') {
                if (x == 3) {
                    newDescription = newDescription + '"';
                } else if (x == 4) {
                    newDescription = newDescription + "'";
                }
                x = 0;
            }

        }
        this.description = newDescription;
    }
}
