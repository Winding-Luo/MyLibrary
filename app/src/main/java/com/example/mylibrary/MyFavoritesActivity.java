package com.example.mylibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mylibrary.BookContract.BookEntry;
import java.util.ArrayList;
import java.util.List;

public class MyFavoritesActivity extends AppCompatActivity {
    private BookDbHelper dbHelper;
    private BookAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);
        setTitle("我的收藏");

        dbHelper = new BookDbHelper(this);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookAdapter();
        adapter.setOnItemClickListener(book -> {
            Intent intent = new Intent(this, BookDetailActivity.class);
            intent.putExtra("book_id", book.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        long userId = prefs.getLong("current_user_id", -1);
        List<Book> books = new ArrayList<>();
        Cursor cursor = dbHelper.getFavoriteBooks(userId);

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(BookEntry._ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_TITLE));
            String author = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_AUTHOR));
            float rating = cursor.getFloat(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_RATING));
            String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_IMAGE_URI));
            int status = 0; try { status = cursor.getInt(cursor.getColumnIndexOrThrow("status")); } catch (Exception e) {}
            String path = ""; try { path = cursor.getString(cursor.getColumnIndexOrThrow("file_path")); } catch (Exception e) {}

            books.add(new Book(id, title, author, rating, imageUri, status, path));
        }
        cursor.close();
        adapter.setBooks(books);
    }
}