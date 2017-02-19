package com.sample.foo.simplerssreader;

import android.net.Uri;

import com.google.common.base.CharMatcher;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

class RssFeedModel {

    public String title;
    String link;
    String description;
    Uri thumbnailUri;

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

    RssFeedModel(String title, String link, String description, String thumbnailUri) {
        this.title = this.parseTitle(title);
        this.link = link;
        if (description != null) this.description = this.parseDescription(description);
        if (thumbnailUri != null) this.thumbnailUri = Uri.parse(thumbnailUri);
    }
}