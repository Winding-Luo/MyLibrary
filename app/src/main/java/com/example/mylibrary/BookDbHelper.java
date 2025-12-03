package com.example.mylibrary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.mylibrary.BookContract.*;

public class BookDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 9; // [版本升级]
    public static final String DATABASE_NAME = "MyLibrary.db";

    // --- 表定义 ---
    private static final String SQL_CREATE_BOOK_TABLE = "CREATE TABLE " + BookEntry.TABLE_NAME + " (" + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + BookEntry.COLUMN_TITLE + " TEXT," + BookEntry.COLUMN_AUTHOR + " TEXT," + BookEntry.COLUMN_RATING + " REAL," + BookEntry.COLUMN_IMAGE_URI + " TEXT," + "owner_id INTEGER," + "status INTEGER DEFAULT 0," + "file_path TEXT)";

    // [修改] 增加 avatar_uri
    private static final String SQL_CREATE_USER_TABLE = "CREATE TABLE " + UserEntry.TABLE_NAME + " (" + UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + UserEntry.COLUMN_USERNAME + " TEXT UNIQUE, " + UserEntry.COLUMN_PASSWORD + " TEXT, " + UserEntry.COLUMN_AVATAR_URI + " TEXT)";

    private static final String SQL_CREATE_FAVORITE_TABLE = "CREATE TABLE " + FavoriteEntry.TABLE_NAME + " (" + FavoriteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FavoriteEntry.COLUMN_USER_ID + " INTEGER, " + FavoriteEntry.COLUMN_BOOK_ID + " INTEGER)";
    public static final String TABLE_PUBLIC_BOOKS = "public_books";
    private static final String SQL_CREATE_PUBLIC_BOOKS = "CREATE TABLE " + TABLE_PUBLIC_BOOKS + " (" + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "title TEXT, author TEXT, image_uri TEXT, " + "shared_by_user_id INTEGER, " + "shared_time DATETIME DEFAULT CURRENT_TIMESTAMP)";
    public static final String TABLE_PUBLIC_REVIEWS = "public_reviews";
    private static final String SQL_CREATE_PUBLIC_REVIEWS = "CREATE TABLE " + TABLE_PUBLIC_REVIEWS + " (" + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "public_book_id INTEGER, " + "user_id INTEGER, " + "rating REAL, " + "comment TEXT, " + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
    public static final String TABLE_REVIEW_LIKES = "review_likes";
    private static final String SQL_CREATE_REVIEW_LIKES = "CREATE TABLE " + TABLE_REVIEW_LIKES + " (" + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "review_id INTEGER, " + "user_id INTEGER)";
    public static final String TABLE_REVIEW_REPLIES = "review_replies";
    private static final String SQL_CREATE_REVIEW_REPLIES = "CREATE TABLE " + TABLE_REVIEW_REPLIES + " (" + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "review_id INTEGER, " + "user_id INTEGER, " + "content TEXT, " + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";

    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_BOOK_TABLE);
        db.execSQL(SQL_CREATE_USER_TABLE);
        db.execSQL(SQL_CREATE_FAVORITE_TABLE);
        db.execSQL(SQL_CREATE_PUBLIC_BOOKS);
        db.execSQL(SQL_CREATE_PUBLIC_REVIEWS);
        db.execSQL(SQL_CREATE_REVIEW_LIKES);
        db.execSQL(SQL_CREATE_REVIEW_REPLIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BookEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FavoriteEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS reviews");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PUBLIC_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PUBLIC_REVIEWS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEW_LIKES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEW_REPLIES);
        onCreate(db);
    }

    // --- [新增] 用户管理 ---
    public Cursor getUser(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(UserEntry.TABLE_NAME, null, UserEntry._ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
    }

    public void updateUser(long userId, String username, String password, String avatarUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_USERNAME, username);
        if (password != null && !password.isEmpty()) {
            values.put(UserEntry.COLUMN_PASSWORD, password);
        }
        values.put(UserEntry.COLUMN_AVATAR_URI, avatarUri);
        db.update(UserEntry.TABLE_NAME, values, UserEntry._ID + "=?", new String[]{String.valueOf(userId)});
    }

    // --- 广场查重添加 ---
    public boolean addBookFromSquare(long userId, long publicBookId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_PUBLIC_BOOKS, null, "_id=?", new String[]{String.valueOf(publicBookId)}, null, null, null);
        if (cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String author = cursor.getString(cursor.getColumnIndexOrThrow("author"));
            String imageUri = cursor.getString(cursor.getColumnIndexOrThrow("image_uri"));
            cursor.close();

            // 查重：私有库是否已有该书
            Cursor checkCursor = db.query(BookEntry.TABLE_NAME, null,
                    "owner_id=? AND " + BookEntry.COLUMN_TITLE + "=? AND " + BookEntry.COLUMN_AUTHOR + "=?",
                    new String[]{String.valueOf(userId), title, author}, null, null, null);

            boolean exists = checkCursor.moveToFirst();
            checkCursor.close();

            if (exists) return false;

            addBook(userId, title, author, 0, imageUri, 0, "");
            return true;
        }
        cursor.close();
        return false;
    }

    // --- 其他核心功能 ---
    public boolean shareBookToSquare(long privateBookId, long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Book book = getBook(privateBookId);
        if (book == null) return false;
        Cursor cursor = db.query(TABLE_PUBLIC_BOOKS, new String[]{"_id"}, "title = ? AND author = ?", new String[]{book.getTitle(), book.getAuthor()}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        if (exists) return false;
        ContentValues values = new ContentValues();
        values.put("title", book.getTitle());
        values.put("author", book.getAuthor());
        values.put("image_uri", book.getImageUri());
        values.put("shared_by_user_id", userId);
        db.insert(TABLE_PUBLIC_BOOKS, null, values);
        return true;
    }

    public long addBook(long userId, String title, String author, float rating, String imageUri, int status, String filePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_TITLE, title);
        values.put(BookEntry.COLUMN_AUTHOR, author);
        values.put(BookEntry.COLUMN_RATING, rating);
        values.put(BookEntry.COLUMN_IMAGE_URI, imageUri);
        values.put("owner_id", userId);
        values.put("status", status);
        values.put("file_path", filePath);
        return db.insert(BookEntry.TABLE_NAME, null, values);
    }

    public void updateBook(long id, String title, String author, float rating, String imageUri, int status, String filePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_TITLE, title);
        values.put(BookEntry.COLUMN_AUTHOR, author);
        values.put(BookEntry.COLUMN_RATING, rating);
        values.put(BookEntry.COLUMN_IMAGE_URI, imageUri);
        values.put("status", status);
        values.put("file_path", filePath);
        db.update(BookEntry.TABLE_NAME, values, BookEntry._ID + "=?", new String[]{String.valueOf(id)});
    }

    public Book getBook(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(BookEntry.TABLE_NAME, null, BookEntry._ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        Book book = null;
        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_TITLE));
            String author = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_AUTHOR));
            float rating = cursor.getFloat(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_RATING));
            String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_IMAGE_URI));
            int status = 0; try { status = cursor.getInt(cursor.getColumnIndexOrThrow("status")); } catch (Exception e) {}
            String filePath = ""; try { filePath = cursor.getString(cursor.getColumnIndexOrThrow("file_path")); } catch (Exception e) {}
            book = new Book(id, title, author, rating, imageUri, status, filePath);
            cursor.close();
        }
        return book;
    }

    public Cursor queryBooks(long userId, String keyword, int statusFilter, String sortOrder) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder selection = new StringBuilder("owner_id = ?");
        java.util.List<String> argsList = new java.util.ArrayList<>();
        argsList.add(String.valueOf(userId));
        if (keyword != null && !keyword.trim().isEmpty()) {
            selection.append(" AND (").append(BookEntry.COLUMN_TITLE).append(" LIKE ? OR ").append(BookEntry.COLUMN_AUTHOR).append(" LIKE ?)");
            argsList.add("%" + keyword + "%");
            argsList.add("%" + keyword + "%");
        }
        if (statusFilter != -1) {
            selection.append(" AND status = ?");
            argsList.add(String.valueOf(statusFilter));
        }
        String orderBy;
        if ("rating".equals(sortOrder)) orderBy = BookEntry.COLUMN_RATING + " DESC";
        else if ("status".equals(sortOrder)) orderBy = "status DESC";
        else orderBy = BookEntry._ID + " DESC";
        return db.query(BookEntry.TABLE_NAME, null, selection.toString(), argsList.toArray(new String[0]), null, null, orderBy);
    }

    // 用户、评论、点赞、回复等辅助方法
    public long registerUser(String username, String password) { SQLiteDatabase db = this.getWritableDatabase(); ContentValues values = new ContentValues(); values.put(UserEntry.COLUMN_USERNAME, username); values.put(UserEntry.COLUMN_PASSWORD, password); return db.insert(UserEntry.TABLE_NAME, null, values); }
    public long loginUser(String username, String password) { SQLiteDatabase db = this.getReadableDatabase(); Cursor cursor = db.query(UserEntry.TABLE_NAME, new String[]{UserEntry._ID}, UserEntry.COLUMN_USERNAME + "=? AND " + UserEntry.COLUMN_PASSWORD + "=?", new String[]{username, password}, null, null, null); long userId = -1; if (cursor.moveToFirst()) userId = cursor.getLong(0); cursor.close(); return userId; }
    public Cursor getSquareBooks() { SQLiteDatabase db = this.getReadableDatabase(); return db.rawQuery("SELECT pb.*, u." + UserEntry.COLUMN_USERNAME + " as shared_by_name FROM " + TABLE_PUBLIC_BOOKS + " pb LEFT JOIN " + UserEntry.TABLE_NAME + " u ON pb.shared_by_user_id = u." + UserEntry._ID + " ORDER BY pb._id DESC", null); }
    public void addPublicReview(long userId, long publicBookId, float rating, String comment) { SQLiteDatabase db = this.getWritableDatabase(); ContentValues values = new ContentValues(); values.put("user_id", userId); values.put("public_book_id", publicBookId); values.put("rating", rating); values.put("comment", comment); db.insert(TABLE_PUBLIC_REVIEWS, null, values); }
    public Cursor getPublicReviews(long publicBookId) { SQLiteDatabase db = this.getReadableDatabase(); return db.rawQuery("SELECT pr.*, u." + UserEntry.COLUMN_USERNAME + " FROM " + TABLE_PUBLIC_REVIEWS + " pr LEFT JOIN " + UserEntry.TABLE_NAME + " u ON pr.user_id = u." + UserEntry._ID + " WHERE pr.public_book_id = ? ORDER BY pr._id DESC", new String[]{String.valueOf(publicBookId)}); }
    public boolean toggleReviewLike(long userId, long reviewId) { SQLiteDatabase db = this.getWritableDatabase(); Cursor cursor = db.query(TABLE_REVIEW_LIKES, null, "user_id=? AND review_id=?", new String[]{String.valueOf(userId), String.valueOf(reviewId)}, null, null, null); boolean exists = cursor.moveToFirst(); cursor.close(); if (exists) { db.delete(TABLE_REVIEW_LIKES, "user_id=? AND review_id=?", new String[]{String.valueOf(userId), String.valueOf(reviewId)}); return false; } else { ContentValues values = new ContentValues(); values.put("user_id", userId); values.put("review_id", reviewId); db.insert(TABLE_REVIEW_LIKES, null, values); return true; } }
    public int getReviewLikeCount(long reviewId) { SQLiteDatabase db = this.getReadableDatabase(); Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_REVIEW_LIKES + " WHERE review_id=?", new String[]{String.valueOf(reviewId)}); int count = 0; if (cursor.moveToFirst()) count = cursor.getInt(0); cursor.close(); return count; }
    public boolean isReviewLikedByMe(long userId, long reviewId) { SQLiteDatabase db = this.getReadableDatabase(); Cursor cursor = db.query(TABLE_REVIEW_LIKES, null, "user_id=? AND review_id=?", new String[]{String.valueOf(userId), String.valueOf(reviewId)}, null, null, null); boolean exists = cursor.getCount() > 0; cursor.close(); return exists; }
    public void addReviewReply(long userId, long reviewId, String content) { SQLiteDatabase db = this.getWritableDatabase(); ContentValues values = new ContentValues(); values.put("user_id", userId); values.put("review_id", reviewId); values.put("content", content); db.insert(TABLE_REVIEW_REPLIES, null, values); }
    public Cursor getReviewReplies(long reviewId) { SQLiteDatabase db = this.getReadableDatabase(); return db.rawQuery("SELECT rr.*, u." + UserEntry.COLUMN_USERNAME + " FROM " + TABLE_REVIEW_REPLIES + " rr LEFT JOIN " + UserEntry.TABLE_NAME + " u ON rr.user_id = u." + UserEntry._ID + " WHERE rr.review_id = ? ORDER BY rr._id ASC", new String[]{String.valueOf(reviewId)}); }
    public Cursor getFavoriteBooks(long userId) { SQLiteDatabase db = this.getReadableDatabase(); String query = "SELECT b.* FROM " + BookEntry.TABLE_NAME + " b INNER JOIN " + FavoriteEntry.TABLE_NAME + " f ON b." + BookEntry._ID + " = f." + FavoriteEntry.COLUMN_BOOK_ID + " WHERE f." + FavoriteEntry.COLUMN_USER_ID + " = ? ORDER BY f." + FavoriteEntry._ID + " DESC"; return db.rawQuery(query, new String[]{String.valueOf(userId)}); }
    public Cursor getUserReviewHistory(long userId) { SQLiteDatabase db = this.getReadableDatabase(); String query = "SELECT r.*, b.title as book_title FROM " + TABLE_PUBLIC_REVIEWS + " r LEFT JOIN " + TABLE_PUBLIC_BOOKS + " b ON r.public_book_id = b._id WHERE r.user_id = ? ORDER BY r._id DESC"; return db.rawQuery(query, new String[]{String.valueOf(userId)}); }
    public void deleteBook(long id) { SQLiteDatabase db = this.getWritableDatabase(); db.delete(BookEntry.TABLE_NAME, BookEntry._ID + "=?", new String[]{String.valueOf(id)}); db.delete(FavoriteEntry.TABLE_NAME, FavoriteEntry.COLUMN_BOOK_ID + "=?", new String[]{String.valueOf(id)}); }
    public boolean isFavorite(long userId, long bookId) { SQLiteDatabase db = this.getReadableDatabase(); Cursor cursor = db.query(FavoriteEntry.TABLE_NAME, new String[]{FavoriteEntry._ID}, FavoriteEntry.COLUMN_USER_ID + "=? AND " + FavoriteEntry.COLUMN_BOOK_ID + "=?", new String[]{String.valueOf(userId), String.valueOf(bookId)}, null, null, null); boolean exists = cursor.getCount() > 0; cursor.close(); return exists; }
    public boolean toggleFavorite(long userId, long bookId) { SQLiteDatabase db = this.getWritableDatabase(); if (isFavorite(userId, bookId)) { db.delete(FavoriteEntry.TABLE_NAME, FavoriteEntry.COLUMN_USER_ID + "=? AND " + FavoriteEntry.COLUMN_BOOK_ID + "=?", new String[]{String.valueOf(userId), String.valueOf(bookId)}); return false; } else { ContentValues values = new ContentValues(); values.put(FavoriteEntry.COLUMN_USER_ID, userId); values.put(FavoriteEntry.COLUMN_BOOK_ID, bookId); db.insert(FavoriteEntry.TABLE_NAME, null, values); return true; } }
    public int getFavoriteCount(long userId) { SQLiteDatabase db = this.getReadableDatabase(); String countQuery = "SELECT COUNT(*) FROM " + FavoriteEntry.TABLE_NAME + " WHERE " + FavoriteEntry.COLUMN_USER_ID + " = ?"; Cursor cursor = db.rawQuery(countQuery, new String[]{String.valueOf(userId)}); int count = 0; if (cursor.moveToFirst()) count = cursor.getInt(0); cursor.close(); return count; }
    public int getReviewCount(long userId) { SQLiteDatabase db = this.getReadableDatabase(); String countQuery = "SELECT COUNT(*) FROM " + TABLE_PUBLIC_REVIEWS + " WHERE user_id = ?"; Cursor cursor = db.rawQuery(countQuery, new String[]{String.valueOf(userId)}); int count = 0; if (cursor.moveToFirst()) count = cursor.getInt(0); cursor.close(); return count; }
}