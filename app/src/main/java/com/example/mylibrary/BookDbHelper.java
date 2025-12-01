package com.example.mylibrary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.mylibrary.BookContract.*;

public class BookDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 3; // [修改] 版本号升级，触发重建
    public static final String DATABASE_NAME = "MyLibrary.db";

    // [修改] SQL 语句增加 image_uri 字段
    private static final String SQL_CREATE_BOOK_TABLE =
            "CREATE TABLE " + BookEntry.TABLE_NAME + " (" +
                    BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    BookEntry.COLUMN_TITLE + " TEXT," +
                    BookEntry.COLUMN_AUTHOR + " TEXT," +
                    BookEntry.COLUMN_RATING + " REAL," +
                    BookEntry.COLUMN_IMAGE_URI + " TEXT)";

    private static final String SQL_CREATE_USER_TABLE =
            "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                    UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    UserEntry.COLUMN_USERNAME + " TEXT UNIQUE, " +
                    UserEntry.COLUMN_PASSWORD + " TEXT)";

    private static final String SQL_CREATE_FAVORITE_TABLE =
            "CREATE TABLE " + FavoriteEntry.TABLE_NAME + " (" +
                    FavoriteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    FavoriteEntry.COLUMN_USER_ID + " INTEGER, " +
                    FavoriteEntry.COLUMN_BOOK_ID + " INTEGER)";

    private static final String SQL_CREATE_REVIEW_TABLE =
            "CREATE TABLE " + ReviewEntry.TABLE_NAME + " (" +
                    ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ReviewEntry.COLUMN_USER_ID + " INTEGER, " +
                    ReviewEntry.COLUMN_BOOK_ID + " INTEGER, " +
                    ReviewEntry.COLUMN_RATING + " REAL, " +
                    ReviewEntry.COLUMN_COMMENT + " TEXT, " +
                    ReviewEntry.COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP)";

    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_BOOK_TABLE);
        db.execSQL(SQL_CREATE_USER_TABLE);
        db.execSQL(SQL_CREATE_FAVORITE_TABLE);
        db.execSQL(SQL_CREATE_REVIEW_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 简单处理：删除重建
        db.execSQL("DROP TABLE IF EXISTS " + BookEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FavoriteEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME);
        onCreate(db);
    }

    // --- 用户相关方法 ---

    public long registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_USERNAME, username);
        values.put(UserEntry.COLUMN_PASSWORD, password);
        return db.insert(UserEntry.TABLE_NAME, null, values);
    }

    public long loginUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(UserEntry.TABLE_NAME,
                new String[]{UserEntry._ID},
                UserEntry.COLUMN_USERNAME + "=? AND " + UserEntry.COLUMN_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null);

        long userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getLong(0);
        }
        cursor.close();
        return userId;
    }

    // --- 收藏相关方法 ---

    public boolean isFavorite(long userId, long bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(FavoriteEntry.TABLE_NAME,
                new String[]{FavoriteEntry._ID},
                FavoriteEntry.COLUMN_USER_ID + "=? AND " + FavoriteEntry.COLUMN_BOOK_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(bookId)},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean toggleFavorite(long userId, long bookId) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (isFavorite(userId, bookId)) {
            db.delete(FavoriteEntry.TABLE_NAME,
                    FavoriteEntry.COLUMN_USER_ID + "=? AND " + FavoriteEntry.COLUMN_BOOK_ID + "=?",
                    new String[]{String.valueOf(userId), String.valueOf(bookId)});
            return false; // 取消收藏
        } else {
            ContentValues values = new ContentValues();
            values.put(FavoriteEntry.COLUMN_USER_ID, userId);
            values.put(FavoriteEntry.COLUMN_BOOK_ID, bookId);
            db.insert(FavoriteEntry.TABLE_NAME, null, values);
            return true; // 添加收藏
        }
    }

    // --- 评论相关方法 ---

    public void addReview(long userId, long bookId, float rating, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ReviewEntry.COLUMN_USER_ID, userId);
        values.put(ReviewEntry.COLUMN_BOOK_ID, bookId);
        values.put(ReviewEntry.COLUMN_RATING, rating);
        values.put(ReviewEntry.COLUMN_COMMENT, comment);
        db.insert(ReviewEntry.TABLE_NAME, null, values);
    }

    public Cursor getReviews(long bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT r.*, u." + UserEntry.COLUMN_USERNAME +
                " FROM " + ReviewEntry.TABLE_NAME + " r " +
                " LEFT JOIN " + UserEntry.TABLE_NAME + " u " +
                " ON r." + ReviewEntry.COLUMN_USER_ID + " = u." + UserEntry._ID +
                " WHERE r." + ReviewEntry.COLUMN_BOOK_ID + " = ?" +
                " ORDER BY r." + ReviewEntry._ID + " DESC";
        return db.rawQuery(query, new String[]{String.valueOf(bookId)});
    }

    // 在 BookDbHelper 类中添加以下方法：

    // 1. 搜索书籍 (支持书名或作者模糊搜索)
    public Cursor searchBooks(String queryStr) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = BookEntry.COLUMN_TITLE + " LIKE ? OR " + BookEntry.COLUMN_AUTHOR + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + queryStr + "%", "%" + queryStr + "%"};
        return db.query(BookEntry.TABLE_NAME, null, selection, selectionArgs, null, null, BookEntry._ID + " DESC");
    }

    // 2. 统计用户收藏数量
    public int getFavoriteCount(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT COUNT(*) FROM " + FavoriteEntry.TABLE_NAME +
                " WHERE " + FavoriteEntry.COLUMN_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(countQuery, new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    // 3. 统计用户评论数量
    public int getReviewCount(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT COUNT(*) FROM " + ReviewEntry.TABLE_NAME +
                " WHERE " + ReviewEntry.COLUMN_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(countQuery, new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    // --- 新增：CRUD 完整支持 ---

    // 1. 根据 ID 获取单本书的所有信息 (用于编辑和详情页加载)
    public Book getBook(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(BookEntry.TABLE_NAME, null,
                BookEntry._ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);

        Book book = null;
        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_TITLE));
            String author = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_AUTHOR));
            float rating = cursor.getFloat(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_RATING));
            String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_IMAGE_URI));
            book = new Book(id, title, author, rating, imageUri);
            cursor.close();
        }
        return book;
    }

    // 2. 更新书籍信息
    public void updateBook(long id, String title, String author, float rating, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_TITLE, title);
        values.put(BookEntry.COLUMN_AUTHOR, author);
        values.put(BookEntry.COLUMN_RATING, rating);
        values.put(BookEntry.COLUMN_IMAGE_URI, imageUri);

        db.update(BookEntry.TABLE_NAME, values, BookEntry._ID + "=?", new String[]{String.valueOf(id)});
    }

    // 3. 删除书籍
    public void deleteBook(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // 删除书籍记录
        db.delete(BookEntry.TABLE_NAME, BookEntry._ID + "=?", new String[]{String.valueOf(id)});
        // 同时删除关联的评论和收藏，保持数据清洁
        db.delete(ReviewEntry.TABLE_NAME, ReviewEntry.COLUMN_BOOK_ID + "=?", new String[]{String.valueOf(id)});
        db.delete(FavoriteEntry.TABLE_NAME, FavoriteEntry.COLUMN_BOOK_ID + "=?", new String[]{String.valueOf(id)});
    }
}