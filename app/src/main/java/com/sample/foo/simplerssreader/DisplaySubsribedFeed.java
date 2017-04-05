package com.sample.foo.simplerssreader;

import android.app.Activity;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DisplaySubsribedFeed extends MainActivity {
    private String feedTitle;
    private String feedURL;
    private List<RssFeedModel> feedList;
    private RecyclerView subRecyclerView;
    RssFeedListAdapter subAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_subsribed_feed);
        /*
        * Joline: instead of performing an AsyncTask...*/
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        feedTitle = bundle.getString("FEED_TITLE");
        feedURL = bundle.getString("FEED_URL");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(feedTitle);
        /*DOES NOT WORK*/
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        subRecyclerView = (RecyclerView) findViewById(R.id.subRecycle);
        subRecyclerView.setLayoutManager(layoutManager);
        subAdapter = null;
        subRecyclerView.setAdapter(subAdapter);
        checkURL();
    }
    /*
    * Joline: Modified version of Wanda's FetchFeedTask*/
    public List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException, IOException{
        Document doc = Jsoup.parse(inputStream, null, "", Parser.xmlParser());
        Element rssElement = doc.select("rss").first();
        if (rssElement == null) throw new XmlPullParserException("Could not find an rss element.");

        Elements articles = doc.select("rss channel item");
        List<RssFeedModel> items = new ArrayList<>();
        DateFormat articleDateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.getDefault());
        for (Element article : articles) {
            final String title = innerElementTextOrNull(article, "title");

            final String link = innerElementTextOrNull(article, "link");

            final String description = innerElementTextOrNull(article, "description");

            String author;
            author = innerElementTextOrNull(article, "dc|creator");
            /* Date: 03/25/2017
            Incoming: #3334
            Wanda: If we didn't get an author from dc:creator try the author element. */
            if (author == null) author = innerElementTextOrNull(article, "author");

            String thumbUrl;
            try {
                thumbUrl = article.select("media|thumbnail").first().attr("url");
            } catch (Exception e) {
                thumbUrl = null;
            }
        /* Date: 03/25/2017
        Incoming: #3765
        Wanda: If we don't have an image try to pull one from the description html. */
            if (thumbUrl == null) {
                try {
                    final String rawDescription = article.select("description").text();
                    Document parsedDescription = Jsoup.parse(rawDescription);
                    thumbUrl = parsedDescription.select("img").attr("src");
                } catch (Exception e) {
                    thumbUrl = null;
                }
            }

            Date date;
            try {
                final String dateText = article.select("pubdate").text();
                date = articleDateFormatter.parse(dateText);
            } catch (ParseException e) {
                date = new Date(Long.MIN_VALUE);
            }
            RssFeedModel item = new RssFeedModel(title, link, description, thumbUrl, author, date);
            items.add(item);
        }
        inputStream.close();
        return items;
    }
    public Boolean checkURL(){
        try {
            InputStream stream = null;
            Boolean hasNoProtocol = !feedURL.startsWith("http://") && !feedURL.startsWith("https://");

            if (hasNoProtocol) {
                ArrayList<URL> possibleUrls = new ArrayList<>(Arrays.asList(
                        new URL("https://" + feedURL),
                        new URL("https://www." + feedURL),
                        new URL("http://" + feedURL),
                        new URL("http://www." + feedURL)));
                for (URL url : possibleUrls) {
                    try {
                        URLConnection connection = url.openConnection();
                        connection.setConnectTimeout(500);
                        stream = connection.getInputStream();
                            /* Date: 19/02/2017
                            Wanda: If there's no exception thrown we use the stream */
                        break;
                    } catch (Exception e) {
                        Log.e("DisplaySubscribedFeed", "Error", e);
                    }
                }
            } else {
                URL url = new URL(feedURL);
                stream = url.openConnection().getInputStream();
            }
            if (stream == null) throw new IOException();
            feedList = parseFeed(stream);
            setViews();
            return true;
        } catch (IOException | XmlPullParserException e) {
            Log.e("DisplaySubscribedFeed", "Error", e);
            if (feedList != null) feedList.clear();
        }
        return false;
    }
    public void setViews(){
        subAdapter = new RssFeedListAdapter(feedList);
        subRecyclerView.setAdapter(subAdapter);
    }
    @Override
    /*Date:04/04/17
    * Joline: Enabling Sort functions*/
    public boolean onOptionsItemSelected(MenuItem item) {
                /* Date: 16/02/2017
        Francis: Handle action bar item clicks here. (The top right menu) */
        int id = item.getItemId();


        /* Date: 16/02/2017
        Francis: For no functionality, the below if statement is sufficient. */

        if (mToggle.onOptionsItemSelected(item)) {
            return (true);
        }

        /* Date: 16/02/2017
        Francis: A row of if statements to give each button their own functionality later.
        May as well do it now. */
        if (id == R.id.dateOldest) {
            if (feedList != null) {
                Collections.sort(feedList, new Sorting("dateOldest"));
                subRecyclerView.setAdapter(new RssFeedListAdapter(feedList));
            }
            return true;
        }
        if (id == R.id.articleTitleAZ) {
            if (feedList != null) {
                Collections.sort(feedList, new Sorting("title"));
                subRecyclerView.setAdapter(new RssFeedListAdapter(feedList));
            }
            return true;
        }

        if (id == R.id.articleTitleZA) {
            if (feedList != null) {
                Collections.sort(feedList, new Sorting("title"));
                Collections.reverse(feedList);
                subRecyclerView.setAdapter(new RssFeedListAdapter(feedList));
            }
            return true;
        }
        if (id == R.id.Author) {
            if (feedList != null) {
                Collections.sort(feedList, new Sorting("author"));
               subRecyclerView.setAdapter(new RssFeedListAdapter(feedList));
            }
            return true;
        }
        if (id == R.id.Random) {
            if (feedList != null) {
                Collections.sort(feedList, new Sorting("random"));
                subRecyclerView.setAdapter(new RssFeedListAdapter(feedList));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
