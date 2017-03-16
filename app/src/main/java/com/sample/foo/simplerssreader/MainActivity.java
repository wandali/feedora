package com.sample.foo.simplerssreader;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static String[] subList = new String[15];
    private static int subTracker=0;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private RecyclerView mRecyclerView;
    private EditText mEditText;
    private Button mFetchFeedButton;
    private Button subFeedButton;
    private SwipeRefreshLayout mSwipeLayout;
    private TextView mFeedTitleTextView;
    private TextView mFeedDescriptionTextView;
    private View mPlusIconView;

    private List<RssFeedModel> mFeedModelList;
    private String mFeedTitle;
    private String mFeedDescription;

    private LinearLayout mLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MainActivity self = this;
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
        mPlusIconView = findViewById(R.id.menu).findViewById(R.id.plus);

        mPlusIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                self.openCreateFolder();
            }
        });

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
                for(int i=0;i<subTracker;++i)
                {
                    popup.getMenu().add(Menu.NONE,subTracker,Menu.NONE,subList[i]);
                }
                /* Date: 16/02/2017
                Francis: Registering popup with OnMenuItemClickListener. So you can click on the
                options */
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        int id = item.getItemId();
                        if (id == R.id.Add)
                        {
                            self.openCreateFolder();
                            return true;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    public void openCreateFolder() {
        View viewInflated = LayoutInflater
                .from(this)
                .inflate(R.layout.dialog_text_input, (ViewGroup) findViewById(android.R.id.content), false);
        final EditText input = (EditText) viewInflated.findViewById(R.id.input);

        new AlertDialog.Builder(this)
                .setTitle("Create a Folder")
                .setView(viewInflated)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* Date: 10/03/2017
                        Francis: Adds the user input to the list of folders. To be established
                        later. */
                        String folderName = input.getText().toString();

                        subList[subTracker]=folderName;
                        ++subTracker;

                        mLinearLayout = (LinearLayout)findViewById(R.id.subFeedList);
                        TextView customSub = new TextView(MainActivity.this);
                        customSub.setText(folderName);
                        customSub.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        mLinearLayout.addView(customSub);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
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
        if (id == R.id.dateOldest) {
            if (mFeedModelList != null) {
                Collections.sort(mFeedModelList, new Sorting("dateOldest"));
                mRecyclerView.setAdapter(new RssFeedListAdapter(mFeedModelList));
            }
            return true;
        }
        if (id == R.id.articleTitleAZ) {
            if (mFeedModelList != null) {
                Collections.sort(mFeedModelList, new Sorting("title"));
                mRecyclerView.setAdapter(new RssFeedListAdapter(mFeedModelList));
            }
            return true;
        }

        if (id == R.id.articleTitleZA) {
            if (mFeedModelList != null) {
                Collections.sort(mFeedModelList, new Sorting("title"));
                Collections.reverse(mFeedModelList);
                mRecyclerView.setAdapter(new RssFeedListAdapter(mFeedModelList));
            }
            return true;
        }
        if (id == R.id.Author) {
            if (mFeedModelList != null) {
                Collections.sort(mFeedModelList, new Sorting("author"));
                mRecyclerView.setAdapter(new RssFeedListAdapter(mFeedModelList));
            }
            return true;
        }
        if (id == R.id.Random) {
            if (mFeedModelList != null) {
                Collections.sort(mFeedModelList, new Sorting("random"));
                mRecyclerView.setAdapter(new RssFeedListAdapter(mFeedModelList));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException, IOException {
        String title = null;
        String link = null;
        String description = null;
        String thumbUrl = null;
        String author = null;
        Date date = new Date(Long.MIN_VALUE);

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
                    /* Date: 08/03/2017
                    Incoming #3008
                    Jack: grabs the author name */
                    case "dc:creator":
                        author = result;
                        break;
                    /* Date: 08/03/2017
                    Incoming #3007
                    Jack: grabs and parses the date */
                    case "pubdate":
                        DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
                        try {

                            date = formatter.parse(result);
                        } catch (ParseException e) {
                            date = new Date(Long.MIN_VALUE);
                        }
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
                        //Log.d("MainActivity",title+ " " + link + " "+ description + " " + thumbUrl);
                        /* Date: 08/03/2017
                        Jack: Added more parameters for creating a new item */
                        RssFeedModel item = new RssFeedModel(title, link, description, thumbUrl, author, date);
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
            urlLink = mEditText.getText().toString().toLowerCase();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (TextUtils.isEmpty(urlLink))
                return false;

            /* Date: 16/02/2017
            Wanda: If the URL entered does not have an http or https and/or www. associated with it,
            it will load the proper one for the website so the articles can be pulled and it does
            not get displayed as invalid RSS feed url */
            try {
                InputStream stream = null;
                Boolean hasNoProtocol = !urlLink.startsWith("http://") && !urlLink.startsWith("https://");
                if (hasNoProtocol) {
                    ArrayList<URL> possibleUrls = new ArrayList<>(Arrays.asList(
                            new URL("https://" + urlLink),
                            new URL("https://www." + urlLink),
                            new URL("http://" + urlLink),
                            new URL("http://www." + urlLink)));
                    for (URL url : possibleUrls) {
                        try {
                            URLConnection connection = url.openConnection();
                            connection.setConnectTimeout(500);
                            stream = connection.getInputStream();
                            /* Date: 19/02/2017
                            Wanda: If there's no exception thrown we use the stream */
                            break;
                        } catch (Exception e) {
                            Log.e(TAG, "Error", e);
                        }
                    }
                } else {
                    URL url = new URL(urlLink);
                    stream = url.openConnection().getInputStream();
                }
                mFeedModelList = parseFeed(stream);
                return true;
            } catch (IOException | XmlPullParserException e) {
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
