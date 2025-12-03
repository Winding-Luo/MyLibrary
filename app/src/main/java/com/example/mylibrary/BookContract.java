package com.example.mylibrary;

import android.provider.BaseColumns;

public final class BookContract {
    private BookContract() {}

    // 书籍表
    public static class BookEntry implements BaseColumns {
        public static final String TABLE_NAME = "library";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_IMAGE_URI = "image_uri";
    }

    // 用户表
    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_AVATAR_URI = "avatar_uri"; // [新增] 头像路径
    }

    // 收藏表
    public static class FavoriteEntry implements BaseColumns {
        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_BOOK_ID = "book_id";
    }

    // 评论表 (旧表，保留定义以防万一，但主要逻辑已迁移到广场评论)
    public static class ReviewEntry implements BaseColumns {
        public static final String TABLE_NAME = "reviews";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_BOOK_ID = "book_id";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_COMMENT = "comment";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
}