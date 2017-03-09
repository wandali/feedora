package com.sample.foo.simplerssreader;

import android.net.Uri;

import com.google.common.base.CharMatcher;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.Date;

class RssFeedModel {

    public String title;
    public String link;
    public String description;
    Uri thumbnailUri;
    public String author;
    public Date date;
    /*
    * need date Created and date Modified
     */


    /**
     * Unescapes and trims title for feed item.
     *
     * @param title the title
     * @return the title, unescaped and trimmed
     */
    private String parseTitle(String title) {
        String parse = title;
        parse = StringEscapeUtils.unescapeXml(parse);
        parse = StringEscapeUtils.unescapeHtml(parse);
        parse = CharMatcher.WHITESPACE.trimFrom(parse);
        return parse;
    }

    /**
     * Parses html description of the feed item for text.
     *
     * @param description plain text description or html description
     * @return the text content of the description
     */
    private String parseDescription(String description) {
        Document doc = Jsoup.parse(description);
        String parse = doc.text();
        parse = CharMatcher.WHITESPACE.trimFrom(parse);
        return parse;
    }
    /* Date: 08/03/2017
    Jack: Added more parameters and added assigning of the passed in variables to the class variables */
    RssFeedModel(String title, String link, String description, String thumbnailUri, String author, Date date) {
        this.title = this.parseTitle(title);
        this.link = link;
        this.author = author;
        this.date = date;
        if (description != null) this.description = this.parseDescription(description);
        if (thumbnailUri != null) this.thumbnailUri = Uri.parse(thumbnailUri);
    }
}