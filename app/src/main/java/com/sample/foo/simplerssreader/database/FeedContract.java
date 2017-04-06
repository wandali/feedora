package com.sample.foo.simplerssreader.database;

import android.provider.BaseColumns;

/* Date: 05/04/2017
Incoming #3052
Francis: This establishes the columns and table for the sql and tree to interact with each
other. */
public final class FeedContract {
    private FeedContract() {}

    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "feeds";
        public static final String URL = "url";
        public static final String FOLDER_ID = "folder_id";
        public static final String FEED_TITLE = "feed_title";
    }
}
