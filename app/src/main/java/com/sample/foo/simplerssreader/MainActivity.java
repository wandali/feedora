package com.sample.foo.simplerssreader;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private RecyclerView mRecyclerView;
    private EditText mEditText;
    private Button mFetchFeedButton;
    private Button subFeedButton;
    private SwipeRefreshLayout mSwipeLayout;
    private TextView mFeedTitleTextView;
    private TextView mFeedDescriptionTextView;

    private List<RssFeedModel> mFeedModelList;
    private String mFeedTitle;
    private String mFeedDescription;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mEditText = (EditText) findViewById(R.id.rssFeedEditText);
        mFetchFeedButton = (Button) findViewById(R.id.fetchFeedButton);
        subFeedButton = (Button) findViewById(R.id.subFeedButton);
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mFeedTitleTextView = (TextView) findViewById(R.id.feedTitle);
        mFeedDescriptionTextView = (TextView) findViewById(R.id.feedDescription);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mFetchFeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FetchFeedTask().execute((Void) null);
            }
        });
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FetchFeedTask().execute((Void) null);
            }
        });
        subFeedButton.setOnClickListener(new View.OnClickListener()
        {
            /* Date: 16/02/2017
            Francis: IMPORTANT NOTE: READ THIS FOR ADDING NEW MENU ITEMS PROGRAMATICALLY
            popup.getMenu().add(groupId, itemId, order, title); for each menuItem you want to add.
            This comment is left in for the other group members. Depending on if I wind up
            not working on adding new items to the drop down menu. */
            @Override
            public void onClick(View view)
            {
                PopupMenu popup = new PopupMenu(MainActivity.this,subFeedButton);
                /* Date: 16/02/2017
                Francis: Inflating the Popup through the xml file */
                popup.getMenuInflater().inflate(R.menu.subscribe_menu, popup.getMenu());
                /* Date: 16/02/2017
                Francis: Registering popup with OnMenuItemClickListener. So you can click on the
                options */
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        return true;
                    }
                });
                popup.show();
            }
        });
        /* Date: 16/02/2017
        Francis: ATTENTION: This was auto-generated to implement the App Indexing API.
        See https://g.co/AppIndexing/AndroidStudio for more information. */

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Date: 16/02/2017
        Francis: Inflate the menu; this adds items to the action bar if it is present.
        menu_main shares the name of menu_main.xml Inflating the menus inside it. */
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        /* Date: 16/02/2017
        Francis: Handle action bar item clicks here. (The top right menu) */
        int id = item.getItemId();


        /* Date: 16/02/2017
        For no functionality, the below if statement is sufficient. */
        if(mToggle.onOptionsItemSelected(item)){
            return(true);
        }

        /* Date: 16/02/2017
        Francis: A row of if statements to give each button their own functionality later.
        May as well do it now. */
        if (id == R.id.dateCreated) {
            return true;
        }
        if (id == R.id.dateModified) {
            return true;
        }
        if (id == R.id.articleTitle) {
            return true;
        }
        if (id == R.id.Author) {
            return true;
        }
        if (id == R.id.Random) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException, IOException {
        String title = null;
        String link = null;
        String description = null;
        String thumbUrl = null;
        boolean isItem = false;
        boolean endItem = false;
        boolean isStart = true;
        int numTitle = 0;
        List<RssFeedModel> items = new ArrayList<>();
        List <String> artTitles = new ArrayList<>();

        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                int eventType = xmlPullParser.getEventType();

                String name = xmlPullParser.getName();
                if (name == null)
                    continue;

                if (eventType == XmlPullParser.END_TAG) {
                    if (name.equalsIgnoreCase("item")) {
                        isItem = false;
                        endItem = true;
                    }
                    continue;
                }
                if (eventType == XmlPullParser.START_TAG) {
                    if (name.equalsIgnoreCase("item")) {
                        isItem = true;
                        continue;
                    }
                }

                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }

                name = name.toLowerCase();
                switch (name) {
                    case "title":
                        title = result;
                        artTitles.add(title);
                        numTitle ++;
                        break;
                    case "link":
                        link = result;
                        break;
                    case "description":
                        description = result;
                        break;
                    /* Date: 16/02/2017
                    Wanda: grabs the attribute of the url*/
                    case "media:thumbnail":
                        thumbUrl = xmlPullParser.getAttributeValue(null, "url");
                        break;
                }

                if(isStart && isItem){
                    if(numTitle > 1){
                            title = artTitles.get(0);
                            numTitle = 0;
                    }
                    else if(numTitle == artTitles.size()){
                        title = "";
                    }
                    else{
                        Log.d("MainActivity", "ERROR: PARSING FEED TITLE");
                    }
                    mFeedTitle = title;
                    mFeedDescription = description;
                    isStart = false;
                    title = null;
                    link = null;
                    description = null;
                }
                
                if (title != null && link != null && description != null && endItem) {
                    if (isItem) {
                        if(numTitle == artTitles.size()){
                            title = artTitles.get(numTitle - 2);
                        }
                        else{
                            title = artTitles.get(numTitle);
                        }
                        Log.d("MainActivity",title+ " " + link + " "+ description + " " + thumbUrl);
                        RssFeedModel item = new RssFeedModel(title, link, description, thumbUrl);
                        item.printArticle();
                        items.add(item);
                    } else {
                        mFeedTitle = title;
                        mFeedDescription = description;
                    }

                    title = null;
                    link = null;
                    description = null;
                    thumbUrl = null;
                    isItem = false;
                    endItem = false;
                }
            }

            return items;
        } finally {
            inputStream.close();
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    private class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {

        private String urlLink;

        @Override
        protected void onPreExecute() {
            mSwipeLayout.setRefreshing(true);
            if (!mEditText.getText().toString().matches("")) {
                mFeedTitle = null;
                mFeedDescription = null;
            }
            mFeedTitleTextView.setText("Feed Title: " + mFeedTitle);
            mFeedDescriptionTextView.setText("Feed Description: " + mFeedDescription);
            urlLink = mEditText.getText().toString();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (TextUtils.isEmpty(urlLink))
                return false;

            /* Date: 16/02/2017
            Wanda: If the URL entered does not have an http or https associated with it, it will
            load the proper one for the website so the articles can be pulled and it does not get
            displayed as invalid RSS feed url */
            try {
                InputStream stream;
                Boolean hasNoProtocol = !urlLink.startsWith("http://") && !urlLink.startsWith("https://");
                if (hasNoProtocol) {
                    URL httpUrl = new URL("http://" + urlLink);
                    URL httpsUrl = new URL("https://" + urlLink);
                    try {
                        URLConnection connection = httpsUrl.openConnection();
                        connection.setConnectTimeout(3000);
                        stream = connection.getInputStream();
                    } catch (Exception e) {
                        URLConnection connection = httpUrl.openConnection();
                        connection.setConnectTimeout(3000);
                        stream = connection.getInputStream();
                    }
                } else {
                    URL url = new URL(urlLink);
                    stream = url.openConnection().getInputStream();
                }
                mFeedModelList = parseFeed(stream);
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error", e);
            } catch (XmlPullParserException e) {
                Log.e(TAG, "Error", e);
            }
            return false;
        }


        @Override
        protected void onPostExecute(Boolean success) {
            mSwipeLayout.setRefreshing(false);
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            if (success) {
                mFeedTitleTextView.setText("Feed Title: " + mFeedTitle);
                mFeedDescriptionTextView.setText("Feed Description: " + mFeedDescription);
                mRecyclerView.setAdapter(new RssFeedListAdapter(mFeedModelList));
            } else {
                Toast.makeText(MainActivity.this,
                        "Enter a Valid RSS Feed URL",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
