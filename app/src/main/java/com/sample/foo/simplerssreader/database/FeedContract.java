package com.sample.foo.simplerssreader.database;

import android.provider.BaseColumns;

public final class FeedContract {
    private FeedContract() {}

    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "feeds";
        public static final String TITLE = "title";
    }
}
