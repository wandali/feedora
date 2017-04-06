package com.sample.foo.simplerssreader;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
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
import android.view.Gravity;
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

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.sample.foo.simplerssreader.database.DBHelper;
import com.sample.foo.simplerssreader.database.FeedContract.FeedEntry;
import com.sample.foo.simplerssreader.database.FolderContract.FolderEntry;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String historyFile = "History_Pref";

    private View mInfoView;
    private ActionBarDrawerToggle mToggle;
    private RecyclerView mRecyclerView;
    private AutoCompleteTextView mEditText;
    private ArrayAdapter<String> mAdapter;
    private List<String> mHistoryList;
    private Button mSubscribeButton;
    private SwipeRefreshLayout mSwipeLayout;
    private TextView mFeedTitleTextView;
    private TextView mFeedDescriptionTextView;
    private SharedPreferences sharedPref;
    private List<RssFeedModel> mFeedModelList;
    private DrawerLayout mDrawerLayout;

    /* Date: 03/26/2017
    Wanda: Data for a successfully fetched feed. */
    private String mFeedTitle = "";
    private String mFeedDescription = "";
    private String mFeedUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MainActivity self = this;
        setContentView(R.layout.layout_main_activity);

        mInfoView = findViewById(R.id.feedInfo);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
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
        setupAutocomplete();

        Button mFetchFeedButton = (Button) findViewById(R.id.fetchFeedButton);
        mSubscribeButton = (Button) findViewById(R.id.subFeedButton);
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mFeedTitleTextView = (TextView) findViewById(R.id.feedTitle);
        mFeedDescriptionTextView = (TextView) findViewById(R.id.feedDescription);

        mHomeButton.setOnClickListener(new View.OnClickListener() {
            /* Date: 13/03/2017
            Incoming #3013
            Used to set an action listener to the home button to direct the user to the home screen. */
            @Override
            public void onClick(View view) {
                Log.v(TAG, "Clicked Home.");
                self.navigateToHome();
            }
        });

        /* Date: 22/03/2017
        Incoming: #3591
        Apurv: Making sure the Subscribe button remains disabled (we only want to enable it unless there is a legitimate link posted). */
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

        mFetchFeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "Clicked fetch.");
                mFeedUrl = mEditText.getText().toString().toLowerCase();
                new FetchFeedTask().execute();
            }
        });

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.v(TAG, "Pulled to refresh.");
                new FetchFeedTask().execute();
            }
        });

        mHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                self.navigateToHome();
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
                Log.v(TAG, "Clicked subscribe.");

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
                        int folderId = item.getItemId();
                        if (folderId == R.id.Add) {
                            self.openCreateFolderDialog();
                            return true;
                        } else {
                             /* Date: 19/04/2017
                             Incoming #3010
                             Wanda: Add the feed to the db. */
                            DBHelper mDbHelper = new DBHelper(getApplicationContext());
                            SQLiteDatabase db = mDbHelper.getWritableDatabase();
                            ContentValues feedValues = new ContentValues();
                            feedValues.put(FeedEntry.URL, mFeedUrl);
                            feedValues.put(FeedEntry.FEED_TITLE, mFeedTitle);
                            feedValues.put(FeedEntry.FOLDER_ID, folderId);
                            db.insert(FeedEntry.TABLE_NAME, null, feedValues);
                            self.refreshFolders();
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        self.refreshFolders();
    }

    private void refreshFolders() {
        LinearLayout foldersContainer = (LinearLayout) findViewById(R.id.foldersContainer);

        /* Date: 19/04/2017
        Incoming: #3010
        Wanda: Clear the container. */
        foldersContainer.removeAllViews();

        DBHelper mDbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        /* Date 03/04/2017
        Incoming: #3827
        Wanda: Need to do a full outer join to get empty folders. */
        final String query =
                "SELECT " +
                        "feeds.feed_title, " +
                        "feeds.folder_id, " +
                        "feeds.url, " +
                        "folders.title, " +
                        "folders._id " +
                        "FROM " + FeedEntry.TABLE_NAME + " feeds " +
                        "LEFT JOIN " + FolderEntry.TABLE_NAME + " folders " +
                        "ON feeds.folder_id=folders._id " +
                        "UNION ALL " +
                        "SELECT " +
                        "feeds.feed_title, " +
                        "feeds.folder_id, " +
                        "feeds.url, " +
                        "folders.title, " +
                        "folders._id " +
                        "FROM " + FolderEntry.TABLE_NAME + " folders " +
                        "LEFT JOIN " + FeedEntry.TABLE_NAME + " feeds " +
                        "ON folders._id=feeds.folder_id " +
                        "WHERE feeds.feed_title IS NULL ORDER BY folders.title COLLATE NOCASE";

        Cursor cursor = db.rawQuery(query, new String[]{});

        /* Date: 19/04/2017
        Incoming #3010
        Wanda: Push folders onto the tree. */
        int prevFolderId = -1;
        TreeNode root = TreeNode.root();
        Context context = this;
        TreeNode folderNode = null;
        while (cursor.moveToNext()) {
            final String folderName = cursor.getString(3);
            final String feedUrl = cursor.getString(2);
            final String feedTitle = cursor.getString(0);
            final int folderID = cursor.getInt(4);

            /* Date: 22/03/2017
            Incoming #3012
            Kendra: Check if folder name is empty, if so do not display folder, statement added
            to help with deleting folders*/
            if (folderName != null && !folderName.isEmpty()) {
                /* Date: 19/04/2017
                Incoming: #3010
                Wanda: Push a new folder if it's a new folderID. */
                if (folderID != prevFolderId) {
                    FolderTreeItemHolder.IconTreeItem item = new FolderTreeItemHolder.IconTreeItem(folderName);
                    /* Date: 03/26/2017
                    Incoming: #3041
                    Wanda: Set a click listener on the folder that will open the edit/delete dialog. */
                    View.OnClickListener clickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openEditFolderDialog(folderID);
                        }
                    };
                    FolderTreeItemHolder itemHolder = new FolderTreeItemHolder(context, clickListener);
                    folderNode = new TreeNode(item).setViewHolder(itemHolder);
                    root.addChild(folderNode);
                    prevFolderId = folderID;
                }

                /* Date: 19/04/2017
                Incoming #3023
                Wanda: Push the feed onto the folder. */
                if (feedUrl != null) {
                    final FeedTreeItemHolder.FeedTreeItem iconTreeItem = new FeedTreeItemHolder.FeedTreeItem(feedTitle, feedUrl);
                    final FeedTreeItemHolder feedTreeItemHolder = new FeedTreeItemHolder(this);
                    final TreeNode feedNode = new TreeNode(iconTreeItem)
                            .setViewHolder(feedTreeItemHolder);
                    /* Date 04/02/2017
                    Incoming #3017
                    Kendra: On a long click, user can delete a feed from under a folder */
                    feedNode.setLongClickListener(new TreeNode.TreeNodeLongClickListener() {
                        @Override
                        public boolean onLongClick(TreeNode feedNode, Object object) {
                            Toast.makeText(MainActivity.this, feedUrl, Toast.LENGTH_LONG).show();
                            /* Date 04/02/2017
                            Incoming #3017
                            Kendra: Send selected folder and feedURL information to method for deletion of feed. */
                            openDeleteFeedDialog(folderID, feedUrl, feedTreeItemHolder, feedNode);
                            return true;
                        }
                    });
                    /* Date: 04/04/17
                    Incoming: #3011
                    Joline: listener for the subscribed feeds */
                    feedNode.setClickListener(new TreeNode.TreeNodeClickListener() {
                        @Override
                        public void onClick(TreeNode treeNode, Object object) {
                            navigateToFeed(feedTitle, feedUrl);
                        }
                    });
                    assert folderNode != null;
                    folderNode.addChildren(feedNode);
                }
            }

        }
        cursor.close();

        AndroidTreeView treeView = new AndroidTreeView(this, root);
        treeView.setDefaultAnimation(true);
        treeView.setDefaultContainerStyle(R.style.FolderContainerStyle, true);
        View treeNodeView = treeView.getView();
        treeNodeView.setBackgroundColor(Color.WHITE);
        foldersContainer.addView(treeNodeView);
    }

    /* Date: 04/04/17
    Incoming: #3011
    Joline: Starting the activity for viewing a subscribed feed */
    private void navigateToFeed(String feedTitle, String feedURL) {

        /* Date: 05/04/2017
        Incoming: #3011
        Wanda: save history before hiding the autocomplete field.*/
        setHistory();

        /* Date: 05/04/2017
        Incoming: #3011
        Wanda: Hide info. */
        mInfoView.setVisibility(View.GONE);
        /* Date: 05/04/2017
        Incoming: #3011
        Wanda: Close the side menu. */
        mDrawerLayout.closeDrawer(Gravity.START);
        /* Date: 05/04/2017
        Incoming: #3011
        Wanda: Set the ActionBar title to the feed title. */
        setTitle(feedTitle);
        mFeedUrl = feedURL;
        new FetchFeedTask().execute();
    }

    private void setupAutocomplete() {
        mHistoryList = new ArrayList<>();
        getHistory();
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
    }

    /* Date: 22/03/2017
    Incoming: #3013
    Kendra: Listener for home button, once clicked main activity is
    refreshed and user is brought home */
    private void navigateToHome() {
        mInfoView.setVisibility(View.VISIBLE);
        mDrawerLayout.closeDrawer(Gravity.START);
        setTitle("Feedora");
        /* Date: 05/04/2017
        Incoming: #3011
        Wanda: Redo this after showing it again for results to show up. */
        setupAutocomplete();
    }


    /* Date: 22/03/2017
    Incoming: #3014
    Kendra: Opens a dialog that asks if user wants to delete a selected feed,
    if they click OK feed will be removed from database*/
    private void openDeleteFeedDialog(final int folderID, final String FeedUrl, final TreeNode.BaseNodeViewHolder holder, final TreeNode node) {
        new AlertDialog.Builder(this)
                .setTitle("Are you sure you want to delete this feed?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(MainActivity.this, "Deleted feed.", Toast.LENGTH_LONG).show();

                        DBHelper mDbHelper = new DBHelper(getApplicationContext());
                        SQLiteDatabase db = mDbHelper.getReadableDatabase();

                        /* Date: 22/03/2017
                        Incoming: #3014
                        Kendra: Delete feed from db based on which folderID and feedURL was selected */
                        String[] whereArguments = new String[]{String.valueOf(folderID), FeedUrl};
                        db.delete(
                                FeedEntry.TABLE_NAME,
                                FeedEntry.FOLDER_ID + "=? and " + FeedEntry.URL + "=?",
                                whereArguments);

                        /* Date 03/04/2017
                        Incoming: #3827
                        Wanda: Delete the feed in the UI. */
                        holder.getTreeView().removeNode(node);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /* Date: 22/03/2017
    Incoming: #3014
    Kendra: Listener for edit button for the folders menu, allows user to edit folders they created
    Pre: folderID is passed in, which holds the folder info the user has chosen to edit*/
    private void openEditFolderDialog(final int folderID) {
        View viewInflated = LayoutInflater
                .from(this)
                .inflate(R.layout.layout_dialog_text_input, (ViewGroup) findViewById(android.R.id.content), false);
        final EditText input = (EditText) viewInflated.findViewById(R.id.input);
        final MainActivity self = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Edit Folder")
                .setView(viewInflated)
                .setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                                String[] arguments = new String[]{String.valueOf(folderID)};
                                db.delete(
                                        FolderEntry.TABLE_NAME,
                                        "_id = ?",
                                        arguments);
                                /* Date: 26/03/2017
                                Incoming: #3722
                                Wanda: Also delete feeds with this folder id. */
                                db.delete(
                                        FeedEntry.TABLE_NAME,
                                        FeedEntry.FOLDER_ID + "=?",
                                        arguments);
                                self.refreshFolders();

                            }
                        })
                .setNegativeButton("Cancel", null);

        final AlertDialog dialog = builder.create();

        final View.OnClickListener onClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                /* Date: 22/03/2017
                Incoming: #3014
                Kendra: Get the name the user inputs and save it */
                String newFolderName = input.getText().toString().trim();
                if (newFolderName.length() == 0) {
                    Toast.makeText(MainActivity.this,
                            "Folder name cannot be blank. Try Again.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                DBHelper mDbHelper = new DBHelper(getApplicationContext());
                SQLiteDatabase db = mDbHelper.getReadableDatabase();

                boolean folderExists = folderExistsInDatabase(db, newFolderName);
                if (folderExists) {
                    Toast.makeText(MainActivity.this,
                            "Folder already exists.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

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
                dialog.dismiss();
            }
        };

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(onClickListener);
            }
        });

        dialog.show();
    }

    private void openCreateFolderDialog() {
        View viewInflated = LayoutInflater
                .from(this)
                .inflate(R.layout.layout_dialog_text_input, (ViewGroup) findViewById(android.R.id.content), false);
        final EditText input = (EditText) viewInflated.findViewById(R.id.input);

        final MainActivity self = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Create a Folder")
                .setView(viewInflated)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

        final AlertDialog dialog = builder.create();

        final View.OnClickListener onClickListener = new View.OnClickListener() {
            public void onClick(View v) {

                /* Date: 10/03/2017
                Francis: Adds the user input to the list of folders. To be established
                later. */
                String folderName = input.getText().toString().trim();
                if (folderName.length() == 0) {
                    Toast.makeText(MainActivity.this,
                            "Enter a folder name.",
                            Toast.LENGTH_LONG).show();
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

                /* 04/04/2017
                Incoming: #3043
                Joline: error handling for duplicate folder. Doesn't allow dup to be saved */
                boolean folderExists = folderExistsInDatabase(db, folderName);
                if (folderExists) {
                    Toast.makeText(MainActivity.this,
                            "Folder already exists.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                long folderID = db.insert(FolderEntry.TABLE_NAME, null, folderValues);

                /* Date: 19/03/2017
                Incoming #3023
                Wanda: Add feed to the db. */
                ContentValues feedValues = new ContentValues();
                feedValues.put(FeedEntry.URL, mFeedUrl);
                feedValues.put(FeedEntry.FEED_TITLE, mFeedTitle);
                feedValues.put(FeedEntry.FOLDER_ID, folderID);
                db.insert(FeedEntry.TABLE_NAME, null, feedValues);

                /* Date: 19/03/2017
                Incoming #3023
                Wanda: Refresh UI. */
                self.refreshFolders();
                dialog.dismiss();
            }
        };

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(onClickListener);
            }
        });
        dialog.show();
    }

    /* Date: 04/04/17
    Incoming: #3043
    Joline: using this function to check the SQLite database to see if the folder the user entered
    in create folder already exists */
    private boolean folderExistsInDatabase(SQLiteDatabase db, String folderName) {
        Cursor cursor;
        String checkDB = "SELECT TITLE FROM " + FolderEntry.TABLE_NAME + " WHERE TITLE=?";
        cursor = db.rawQuery(checkDB, new String[]{folderName});
        boolean exists = false;
        cursor.moveToFirst();
        if (cursor.getCount() > 0) exists = true;
        cursor.close();
        return exists;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Date: 16/02/2017
        Francis: Inflate the menu; this adds items to the action bar if it is present.
        menu_main shares the name of menu_main.xml Inflating the menus inside it. */
        getMenuInflater().inflate(R.menu.sort_menu, menu);
        Drawable iconDrawable = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_sort)
                .color(Color.DKGRAY)
                .sizeDp(24);
        menu.getItem(0).setIcon(iconDrawable);
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

    public String innerElementTextOrNull(Element element, String elementName) {
        try {
            return element.select(elementName).first().text();
        } catch (Exception e) {
            return null;
        }
    }

    /* Date: 03/22/2017
    Joline: Uses shared preferences to get the saved history from another instance of the app */
    private void getHistory() {
        sharedPref = getSharedPreferences(historyFile, 0);
        int size = sharedPref.getInt("list_size", 0);
        for (int i = 0; i < size; i++)
            mHistoryList.add(sharedPref.getString("url_" + i, null));
    }

    /* Date: 03/22/2017
    Joline: saves the users url list via shared preferences */
    private void setHistory() {
        sharedPref = getSharedPreferences(historyFile, 0);
        SharedPreferences.Editor editor = sharedPref.edit();
        int size = mHistoryList.size();
        editor.putInt("list_size", size);
        for (int i = 0; i < size; i++)
            editor.putString("url_" + i, mHistoryList.get(i));
        editor.apply();
    }

    private void updateFeedDetails() {
        mFeedTitleTextView.setText("Feed Title: " + mFeedTitle);
        mFeedDescriptionTextView.setText("Feed Description: " + mFeedDescription);
    }

    private void clearFeedDetails() {
        mFeedTitle = "";
        mFeedDescription = "";
        updateFeedDetails();
    }

    @Override
    public void onStop() {
        super.onStop();
        setHistory();
    }

    /* Date: 16/03/2017
    Joline: This function updates the adapter and history list by adding the
    most recent accepted url submitted by the user. Shows the most recent url first. */
    private void addFeedToHistory(String feedURL) {
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

    private class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {

        private String feedTitle;
        private String feedURL;
        private String feedDescription;

        @Override
        protected void onPreExecute() {
            /* Date: 03/26/2017
            Wanda: Set the layout state to refreshing. */
            mSwipeLayout.setRefreshing(true);

            /* Date: 03/26/2017
            Wanda: Reset UI. */
            clearFeedDetails();

            /* Date: 03/26/2017
            Wanda: Get the feed url from the text input. */
            feedURL = mFeedUrl;
        }

        /* Date: 03/25/2017
        Incoming: #3334
        Wanda: Re-wrote parseFeed method to be less complex and error prone. */
        List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException, IOException {
            Document doc = Jsoup.parse(inputStream, null, "", Parser.xmlParser());
            Element rssElement = doc.select("rss").first();

            if (rssElement == null) {
                throw new XmlPullParserException("Could not find an rss element.");
            }

            feedTitle = innerElementTextOrNull(doc, "rss channel title");
            feedDescription = innerElementTextOrNull(doc, "rss channel description");

            Elements articles = doc.select("rss channel item");
            List<RssFeedModel> items = new ArrayList<>();
            DateFormat articleDateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.getDefault());
            for (Element article : articles) {
                final String title = innerElementTextOrNull(article, "title");

                final String link = innerElementTextOrNull(article, "link");

                final String description = innerElementTextOrNull(article, "description");

                String author;
                author = innerElementTextOrNull(article, "dc|creator");
                /* Date: 25/03/2017
                Incoming: #3334
                Wanda: If we didn't get an author from dc:creator try the author element. */
                if (author == null) author = innerElementTextOrNull(article, "author");

                String thumbUrl;
                try {
                    thumbUrl = article.select("media|thumbnail").first().attr("url");
                } catch (Exception e) {
                    thumbUrl = null;
                }
                /* Date: 25/03/2017
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

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (TextUtils.isEmpty(feedURL)) return false;

            /* Date: 16/02/2017
            Wanda: If the URL entered does not have an http or https and/or www. associated with it,
            it will load the proper one for the website so the articles can be pulled and it does
            not get displayed as invalid RSS feed url */
            try {
                InputStream stream;
                Boolean hasNoProtocol = !feedURL.startsWith("http://") && !feedURL.startsWith("https://");
                Boolean succeeded = false;
                Exception fetchException = null;
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
                            mFeedModelList = parseFeed(stream);
                            succeeded = true;
                            break;
                        } catch (Exception e) {
                            fetchException = e;
                        }
                    }
                } else {
                    try {
                        URL url = new URL(feedURL);
                        stream = url.openConnection().getInputStream();
                        mFeedModelList = parseFeed(stream);
                        succeeded = true;
                    } catch (Exception e) {
                        fetchException = e;
                    }
                }
                if (!succeeded) throw fetchException;
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
                if (mFeedModelList != null) mFeedModelList.clear();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            /* Date: 26/03/2017
            Wanda: Done refreshing. */
            mSwipeLayout.setRefreshing(false);

            /* Date: 26/03/2017
            Incoming: #3020
            Wanda: Hide keyboard. */
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            View focusedView = getCurrentFocus();
            if (focusedView != null) {
                IBinder binder = focusedView.getWindowToken();
                inputMethodManager.hideSoftInputFromWindow(binder, InputMethodManager.HIDE_NOT_ALWAYS);
            }

            if (success) {
                /* Date: 26/03/2017
                Incoming: #3020
                Wanda: Commit changes to the UI. */
                mFeedTitle = feedTitle;
                mFeedUrl = feedURL;
                mFeedDescription = feedDescription;
                updateFeedDetails();

                mRecyclerView.setAdapter(new RssFeedListAdapter(mFeedModelList));
                addFeedToHistory(feedURL);

                /* Date: 22/03/2017
                Incoming: #3591
                Apurv: Making sure the Subscribe button is enabled since we found proper link */
                mSubscribeButton.setEnabled(true);
            } else {
                Toast.makeText(MainActivity.this, "Enter a Valid RSS Feed URL", Toast.LENGTH_LONG).show();
            }
        }
    }
}
