package com.example.mylibrary;

import android.provider.BaseColumns;

public final class BookContract {
    private BookContract() {}

    // 原有的图书表
    public static class BookEntry implements BaseColumns {
        public static final String TABLE_NAME = "library";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_RATING = "rating"; // 这是书籍的平均评分
    }

    // 新增：用户表
    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD = "password"; // 实际项目中请存储哈希值
    }

    // 新增：收藏表 (用户ID <-> 书籍ID)
    public static class FavoriteEntry implements BaseColumns {
        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_BOOK_ID = "book_id";
    }

    // 新增：评价表
    public static class ReviewEntry implements BaseColumns {
        public static final String TABLE_NAME = "reviews";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_BOOK_ID = "book_id";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_COMMENT = "comment";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
}