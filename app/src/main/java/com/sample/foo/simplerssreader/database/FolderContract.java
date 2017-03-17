package com.sample.foo.simplerssreader.database;

import android.provider.BaseColumns;

public final class FolderContract {
    private FolderContract() {}

    public static class FolderEntry implements BaseColumns {
        public static final String TABLE_NAME = "folders";
        public static final String TITLE = "title";
    }
}
