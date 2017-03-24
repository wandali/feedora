package com.sample.foo.simplerssreader;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sample.foo.simplerssreader.database.DBHelper;
import com.sample.foo.simplerssreader.database.FeedContract.FeedEntry;
import com.sample.foo.simplerssreader.database.FolderContract.FolderEntry;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.xmlpull.v1.XmlPullParser;
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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActionBarDrawerToggle mToggle;
    private RecyclerView mRecyclerView;
    private AutoCompleteTextView mEditText;
    private ArrayAdapter<String> mAdapter;
    private List<String> mHistoryList;
    private Button mSubscribeButton;
    private Button mEditButton;
    private SwipeRefreshLayout mSwipeLayout;
    private TextView mFeedTitleTextView;
    private TextView mFeedDescriptionTextView;
    private List<RssFeedModel> mFeedModelList;
    private String mFeedTitle = "";
    private String mFeedDescription = "";

    /* Date 03/22/2017
    Incoming: #3591
    Francis: Safe variable copies mFeedTitle. Use this instead and pass it around. */
    private String titlePass = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MainActivity self = this;
        setContentView(R.layout.activity_main);

        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mFeedTitleTextView = (TextView) findViewById(R.id.feedTitle);
        mFeedDescriptionTextView = (TextView) findViewById(R.id.feedDescription);
        View mHomeButton = findViewById(R.id.menu).findViewById(R.id.homeButton);



        /* Date: 16/03/2017
        Incoming #3026
        Joline: This is for the history set up, used AutoCompleteTextView. mEditText, used to be
        of type "Edit Text" kept name and tag id in case used elsewhere in program*/
        mEditText = (AutoCompleteTextView) findViewById(R.id.rssFeedEditText);
        mHistoryList = new ArrayList<>();
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mHistoryList);
        mEditText.setThreshold(0);
        mEditText.enoughToFilter();
        mEditText.setAdapter(mAdapter);
        mEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mEditText.showDropDown();
                return false;
            }
        });

        Button mFetchButton = (Button) findViewById(R.id.fetchFeedButton);
        mSubscribeButton = (Button) findViewById(R.id.subFeedButton);
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mFeedTitleTextView = (TextView) findViewById(R.id.feedTitle);
        mFeedDescriptionTextView = (TextView) findViewById(R.id.feedDescription);
        mEditButton = (Button) findViewById(R.id.edit);

        mHomeButton.setOnClickListener(new View.OnClickListener() {
            /* Date: 13/03/2017
            Incoming #3013
            Used to set an action listener to the home button to direct the user to the home screen. */
            @Override
            public void onClick(View view) {
                Log.v(TAG, "Clicked Home.");
                self.goHome();
            }
        });



           /*
            Date: 22/03/2017
            Issue: #3591
            Apurv: Making sure the Subscribe button remains disabled (we only want to enable it unless there is a legitimate link posted)
        */
        mSubscribeButton.setEnabled(false);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mSubscribeButton.setEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mFetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "Clicked Fetch.");
                new FetchFeedTask().execute((Void) null);
            }
        });

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.v(TAG, "Pulled to refresh.");
                new FetchFeedTask().execute((Void) null);
            }
        });

        mHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                self.goHome();
            }
        });

        mSubscribeButton.setOnClickListener(new View.OnClickListener() {
            /* Date: 16/02/2017
            Francis: IMPORTANT NOTE: READ THIS FOR ADDING NEW MENU ITEMS PROGRAMMATICALLY
            popup.getMenu().add(groupId, itemId, order, title); for each menuItem you want to add.
            This comment is left in for the other group members. Depending on if I wind up
            not working on adding new items to the drop down menu. */
            @Override
            public void onClick(View view) {
                Log.v(TAG, "Clicked Subscribe.");

                PopupMenu popup = new PopupMenu(MainActivity.this, mSubscribeButton);
                /* Date: 16/02/2017
                Francis: Inflating the Popup through the xml file */
                popup.getMenuInflater().inflate(R.menu.subscribe_menu, popup.getMenu());

                /* Date: 19/04/2017
                Incoming #3010
                Wanda: Get the folders from the db. */
                DBHelper mDbHelper = new DBHelper(getApplicationContext());
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                String[] projection = {
                        FolderEntry._ID,
                        FolderEntry.TITLE,
                };
                String sortOrder = FolderEntry.TITLE + " ASC";
                Cursor cursor = db.query(
                        FolderEntry.TABLE_NAME,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        sortOrder
                );

                /* Date: 19/04/2017
                Incoming #3010
                Wanda: Push folder items onto the subscribe popup menu. */
                int folderNameIndex = cursor.getColumnIndexOrThrow(FolderEntry.TITLE);
                int folderIDIndex = cursor.getColumnIndexOrThrow(FolderEntry._ID);
                while (cursor.moveToNext()) {
                    String folderName = cursor.getString(folderNameIndex);
                    int folderID = cursor.getInt(folderIDIndex);
                    popup.getMenu().add(Menu.NONE, folderID, Menu.NONE, folderName);
                }
                cursor.close();

                /* Date: 16/02/2017
                Francis: Registering popup with OnMenuItemClickListener. So you can click on the
                options */
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.Add) {
                            self.openCreateFolderDialog();
                            return true;
                        } else {
                             /* Date: 19/04/2017
                             Incoming #3010
                             Wanda: Add a folder to the db. */
                            final String subscribeUrl = mEditText.getText().toString();
                            final String urlName = titlePass;
                            DBHelper mDbHelper = new DBHelper(getApplicationContext());
                            SQLiteDatabase db = mDbHelper.getWritableDatabase();
                            ContentValues feedValues = new ContentValues();
                            feedValues.put(FeedEntry.URL, subscribeUrl);
                            feedValues.put(FeedEntry.FOLDER_ID, id);
                            if (urlName == null) {
                                feedValues.put(FeedEntry.FEED_TITLE, "null");
                            } else {
                                feedValues.put(FeedEntry.FEED_TITLE, urlName);
                            }
                            db.insert(FeedEntry.TABLE_NAME, null, feedValues);
                            self.refreshFolders();
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
        /* Date: 22/03/2017
        Incoming: #3014
        Kendra: Listener for edit button for the folders menu, allows user to edit folders */
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* Date: 22/03/2017
                Incoming: #3014
                Kendra: Inflating the Popup through the xml file, using same menu as subscribe button
                with create folder option removed*/
                PopupMenu popup = new PopupMenu(MainActivity.this, mSubscribeButton);

                /* Date: 22/03/2017
                Incoming: #3014
                Kendra: Get the folders from db, populate menu */
                DBHelper mDbHelper = new DBHelper(getApplicationContext());
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                String[] projection = {
                        FolderEntry._ID,
                        FolderEntry.TITLE,

                };
                String sortOrder = FolderEntry.TITLE + " ASC";
                Cursor cursor = db.query(
                        FolderEntry.TABLE_NAME,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        sortOrder
                );

                /* Date: 22/03/2017
                Incoming #3014
                Kendra: Push folder items onto the edit folder popup menu. */
                int folderNameIndex = cursor.getColumnIndexOrThrow(FolderEntry.TITLE);
                int folderIDIndex = cursor.getColumnIndexOrThrow(FolderEntry._ID);
                while (cursor.moveToNext()) {
                    String folderName = cursor.getString(folderNameIndex);
                    int folderID = cursor.getInt(folderIDIndex);
                    popup.getMenu().add(Menu.NONE, folderID, Menu.NONE, folderName);

                }
                cursor.close();
                /* Date: 22/03/2017
                Incoming: #3014
                Kendra: Menu listeners for each folder in the menu */
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        final int folderID = item.getItemId();
                        if (folderID == R.id.Add) {
                            self.openCreateFolderDialog();
                            return true;

                        } else {
                            self.editFolderDialog(folderID);
                            return true;
                        }
                    }
                });
                popup.show();

            }
        });
        self.refreshFolders();

    }

    public void refreshFolders() {
        LinearLayout foldersContainer = (LinearLayout) findViewById(R.id.foldersContainer);

         /* Date: 19/04/2017
         Incoming #3010
         Wanda: Clear the container. */
        foldersContainer.removeAllViews();

        DBHelper mDbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(
                FeedEntry.TABLE_NAME +
                        " feeds LEFT OUTER JOIN " +
                        FolderEntry.TABLE_NAME +
                        " folders ON " +
                        "feeds." + FeedEntry.FOLDER_ID + " = folders." + FolderEntry._ID
        );

        System.out.println(
                FeedEntry.TABLE_NAME +
                        " feeds LEFT OUTER JOIN " +
                        FolderEntry.TABLE_NAME +
                        " folders ON " +
                        "feeds." + FeedEntry.FOLDER_ID + " = folders." + FolderEntry._ID
        );


        String sortOrder = FolderEntry.TITLE + " ASC";
        Cursor cursor = queryBuilder.query(db, null, null, null, null, null, sortOrder);

         /* Date: 19/04/2017
         Incoming #3010
         Wanda: Push folders onto the tree. */
        int folderNameIndex = cursor.getColumnIndex(FolderEntry.TITLE);
        int folderIDIndex = cursor.getColumnIndex(FolderEntry._ID);
        int feedUrlIndex = cursor.getColumnIndex(FeedEntry.URL);
        int feedTitleIndex = cursor.getColumnIndex(FeedEntry.FEED_TITLE);
        int prevFolderId = -1;
        TreeNode root = TreeNode.root();
        Context context = this;
        TreeNode folderNode = null;
        while (cursor.moveToNext()) {
            String folderName = cursor.getString(folderNameIndex);
            String feedUrl = cursor.getString(feedUrlIndex);
            String feedTitle = cursor.getString(feedTitleIndex);

            int folderID = cursor.getInt(folderIDIndex);

            /* Date: 22/03/2017
            Incoming #3012
            Kendra: Check if folder name is empty, if so do not display folder, statement added
            to help with deleting folders*/
            if (folderName != null && !folderName.isEmpty()) {
                /* Date: 19/04/2017
                Incoming: #3010
                Wanda: Push a new folder if it's a new folderID. */
                if (folderID != prevFolderId) {
                    folderNode = new TreeNode(new FolderTreeItemHolder.IconTreeItem(folderName))
                            .setViewHolder(new FolderTreeItemHolder(context));
                    root.addChild(folderNode);
                    prevFolderId = folderID;
                }


                /* Date: 19/04/2017
                Incoming #3023
                Wanda: Push the feed onto the folder. */
                if (feedUrl != null) {
                    /* Date 03/22/2017
                    Incoming: #3591 Sending the feed title instead of the url to the folders. */
                    TreeNode feedNode = new TreeNode(new FeedTreeItemHolder.IconTreeItem(feedTitle))
                            .setViewHolder(new FeedTreeItemHolder(this));
                    assert folderNode != null;
                    folderNode.addChildren(feedNode);
                }
            }

        }
        cursor.close();

        AndroidTreeView treeView = new AndroidTreeView(this, root);
        treeView.setDefaultAnimation(true);
        treeView.setDefaultContainerStyle(R.style.TreeNodeStyleDivided, true);
        foldersContainer.addView(treeView.getView());
    }

    /* Date: 22/03/2017
    Incoming: #3013
    Kendra: Listener for home button, once clicked main activity is
    refreshed and user is brought home */
    public void goHome() {
        Intent homeIntent = new Intent(this, MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);

    }

    /* Date: 22/03/2017
    Incoming: #3014
    Kendra: Listener for edit button for the folders menu, allows user to edit folders they created
    Pre: folderID is passed in, which holds the folder info the user has chosen to edit*/
    public void editFolderDialog(final int folderID) {
        View viewInflated = LayoutInflater
                .from(this)
                .inflate(R.layout.dialog_text_input, (ViewGroup) findViewById(android.R.id.content), false);
        final EditText input = (EditText) viewInflated.findViewById(R.id.input);
        final MainActivity self = this;
        new AlertDialog.Builder(this)
                .setTitle("Edit Folder")
                .setView(viewInflated)
                .setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* Date: 22/03/2017
                        Incoming: #3014
                        Kendra: Get the name the user inputs and save it */
                        String newFolderName = input.getText().toString().trim();
                        if (newFolderName.length() == 0) {
                            Toast.makeText(MainActivity.this, "Folder name cannot be blank. Try Again.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        DBHelper mDbHelper = new DBHelper(getApplicationContext());
                        SQLiteDatabase db = mDbHelper.getReadableDatabase();

                        /* Date: 22/03/2017
                        Incoming: #3014
                        Kendra: Prepare to put new folder name into db */
                        ContentValues values = new ContentValues();
                        values.put(FolderEntry.TITLE, newFolderName);

                        /* Date: 22/03/2017
                        Incoming: #3014
                        Kendra: Based on folderID, update the folders title in the db to the new
                        title entered by user */
                        db.update(
                                FolderEntry.TABLE_NAME,
                                values,
                                "_id = ?",
                                new String[]{String.valueOf(folderID)});
                        self.refreshFolders();

                    }
                })
                .setNeutralButton("Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                DBHelper mDbHelper = new DBHelper(getApplicationContext());
                                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                                /* Date: 22/03/2017
                                Incoming: #3012
                                Kendra: Based on folderID, delete the current folder user has clicked */
                                db.delete(
                                        FolderEntry.TABLE_NAME,
                                        "_id = ?",
                                        new String[]{String.valueOf(folderID)});
                                self.refreshFolders();

                            }
                        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
                .show();
    }

    public void openCreateFolderDialog() {
        View viewInflated = LayoutInflater
                .from(this)
                .inflate(R.layout.dialog_text_input, (ViewGroup) findViewById(android.R.id.content), false);
        final EditText input = (EditText) viewInflated.findViewById(R.id.input);

        final MainActivity self = this;
        new AlertDialog.Builder(this)
                .setTitle("Create a Folder")
                .setView(viewInflated)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* Date: 10/03/2017
                        Francis: Adds the user input to the list of folders. To be established
                        later. */
                        String folderName = input.getText().toString().trim();
                        if (folderName.length() == 0) {
                            Toast.makeText(MainActivity.this, "Enter a folder name.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        String feedUrl = mEditText.getText().toString().toLowerCase();
                        if (feedUrl.length() == 0) {
                            Toast.makeText(MainActivity.this, "Your feed url cannot be blank.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        /* Date: 19/04/2017
                        Wanda: Get a writeable database. */
                        DBHelper mDbHelper = new DBHelper(getApplicationContext());
                        SQLiteDatabase db = mDbHelper.getWritableDatabase();

                        /* Date: 19/04/2017
                        Incoming #3010
                        Wanda: Add folder to the db. */
                        ContentValues folderValues = new ContentValues();
                        folderValues.put(FolderEntry.TITLE, folderName);
                        long folderID = db.insert(FolderEntry.TABLE_NAME, null, folderValues);

                        /* Date: 19/04/2017
                        Incoming #3023
                        Wanda: Add feed to the db. */
                        ContentValues feedValues = new ContentValues();
                        feedValues.put(FeedEntry.URL, feedUrl);
                        feedValues.put(FeedEntry.FOLDER_ID, folderID);
                        if (titlePass == null) {
                            feedValues.put(FeedEntry.FEED_TITLE, "null");
                        } else {
                            feedValues.put(FeedEntry.FEED_TITLE, titlePass);
                        }
                        db.insert(FeedEntry.TABLE_NAME, null, feedValues);

                        /* Date: 19/04/2017
                        Wanda: Refresh UI. */
                        self.refreshFolders();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
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
        List<String> artTitles = new ArrayList<>();

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
                        numTitle++;
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

                if (isStart && isItem) {
                    if (numTitle > 1) {
                        title = artTitles.get(0);
                        numTitle = 0;
                    } else if (numTitle == artTitles.size()) {
                        title = "";
                    } else {
                        Log.e(TAG, "ERROR: PARSING FEED TITLE");
                    }
                    mFeedTitle = title;
                    titlePass = title;
                    mFeedDescription = description;
                    isStart = false;
                    title = null;
                    link = null;
                    description = null;
                }

                if (title != null && link != null && description != null && endItem) {
                    if (isItem) {
                        if (numTitle == artTitles.size()) {
                            title = artTitles.get(numTitle - 2);
                        } else {
                            title = artTitles.get(numTitle);
                        }
                        //Log.d("MainActivity",title+ " " + link + " "+ description + " " + thumbUrl);
                        /* Date: 08/03/2017
                        Jack: Added more parameters for creating a new item */
                        RssFeedModel item = new RssFeedModel(title, link, description, thumbUrl, author, date);
                        items.add(item);
                    } else {
                        mFeedTitle = title;
                        titlePass = title;
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
                mFeedTitle = "";
                mFeedDescription = "";
                titlePass = null;
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
                if (stream == null) throw new IOException();
                mFeedModelList = parseFeed(stream);
                updateHistory(urlLink);
                return true;
            } catch (IOException | XmlPullParserException e) {
                Log.e(TAG, "Error", e);
                mFeedModelList.clear();
            }
            return false;
        }

        /* Date: 16/03/2017
        Incoming #3026
        Joline: This function updates the adapter and history list by adding the
        most recent accepted url submitted by the user. Shows the most recent url first */
        void updateHistory(String feedURL) {
            if (!mHistoryList.contains(feedURL)) {
                if (mHistoryList.size() == 5) {
                    mHistoryList.remove(4);
                }
                mHistoryList.add(0, feedURL);
                mAdapter.clear();
                mAdapter.addAll(mHistoryList);
                mAdapter.notifyDataSetChanged();
            }
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
                /* Date: 22/03/2017
                Issue: #3591
                Apurv: Making sure the Subscribe button is enabled since we found proper link */
                mSubscribeButton.setEnabled(true);
            } else {
                Toast.makeText(MainActivity.this,
                        "Enter a Valid RSS Feed URL",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
