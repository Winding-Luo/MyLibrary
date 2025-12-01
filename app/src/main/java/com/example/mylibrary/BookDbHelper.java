package com.example.mylibrary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.mylibrary.BookContract.*;

import java.util.ArrayList;
import java.util.List;

public class BookDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2; // 版本号升级
    public static final String DATABASE_NAME = "MyLibrary.db";

    // SQL 语句定义
    private static final String SQL_CREATE_BOOK_TABLE =
            "CREATE TABLE " + BookEntry.TABLE_NAME + " (" +
                    BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    BookEntry.COLUMN_TITLE + " TEXT," +
                    BookEntry.COLUMN_AUTHOR + " TEXT," +
                    BookEntry.COLUMN_RATING + " REAL)";

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
        // 简单处理：删除重建。生产环境需用 ALTER TABLE 保留数据
        db.execSQL("DROP TABLE IF EXISTS " + BookEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FavoriteEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME);
        onCreate(db);
    }

    // --- 用户相关方法 ---

    // 注册：返回新用户ID，失败返回 -1
    public long registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_USERNAME, username);
        values.put(UserEntry.COLUMN_PASSWORD, password);
        return db.insert(UserEntry.TABLE_NAME, null, values);
    }

    // 登录：返回用户ID，失败返回 -1
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

    // 检查是否已收藏
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

    // 切换收藏状态
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

    // 添加评论
    public void addReview(long userId, long bookId, float rating, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ReviewEntry.COLUMN_USER_ID, userId);
        values.put(ReviewEntry.COLUMN_BOOK_ID, bookId);
        values.put(ReviewEntry.COLUMN_RATING, rating);
        values.put(ReviewEntry.COLUMN_COMMENT, comment);
        db.insert(ReviewEntry.TABLE_NAME, null, values);
    }

    // 获取某本书的评论列表
    public Cursor getReviews(long bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        // 关联查询：我们需要 User 表里的用户名
        String query = "SELECT r.*, u." + UserEntry.COLUMN_USERNAME +
                " FROM " + ReviewEntry.TABLE_NAME + " r " +
                " LEFT JOIN " + UserEntry.TABLE_NAME + " u " +
                " ON r." + ReviewEntry.COLUMN_USER_ID + " = u." + UserEntry._ID +
                " WHERE r." + ReviewEntry.COLUMN_BOOK_ID + " = ?" +
                " ORDER BY r." + ReviewEntry._ID + " DESC";
        return db.rawQuery(query, new String[]{String.valueOf(bookId)});
    }
}