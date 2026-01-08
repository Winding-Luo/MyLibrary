package com.example.mylibrary;

import android.provider.BaseColumns;

public final class BookContract {
    private BookContract() {}

    public static class BookEntry implements BaseColumns {
        public static final String TABLE_NAME = "library";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_IMAGE_URI = "image_uri";
        // [新增] 阅读时长字段
        public static final String COLUMN_READING_DURATION = "reading_duration";
    }

    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_AVATAR_URI = "avatar_uri";
    }

    public static class FavoriteEntry implements BaseColumns {
        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_BOOK_ID = "book_id";
    }

    // 保留旧表定义以防兼容性问题
    public static class ReviewEntry implements BaseColumns {
        public static final String TABLE_NAME = "reviews";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_BOOK_ID = "book_id";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_COMMENT = "comment";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
}