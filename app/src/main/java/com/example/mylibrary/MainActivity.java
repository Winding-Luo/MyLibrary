package com.example.mylibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import com.example.mylibrary.BookContract.BookEntry;

public class MainActivity extends AppCompatActivity {

    private BookDbHelper dbHelper;
    private BookAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- 1. 检查登录状态 ---
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        long userId = prefs.getLong("current_user_id", -1);
        if (userId == -1) {
            // 未登录，跳转到 LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        dbHelper = new BookDbHelper(this);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        FloatingActionButton fab = findViewById(R.id.fab_add);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookAdapter();

        // --- 2. 设置点击事件：跳转到详情页 ---
        adapter.setOnItemClickListener(book -> {
            Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
            intent.putExtra("book_id", book.getId());
            intent.putExtra("book_title", book.getTitle());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddBookActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 只有在已登录的情况下才加载数据
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        if (prefs.getLong("current_user_id", -1) != -1) {
            loadBooks();
        }
    }

    // --- 3. 添加注销菜单 ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu); // 需要新建 res/menu/main_menu.xml
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadBooks() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Book> books = new ArrayList<>();
        Cursor cursor = db.query(BookEntry.TABLE_NAME, null, null, null, null, null, BookEntry._ID + " DESC");
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(BookEntry._ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_TITLE));
            String author = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_AUTHOR));
            float rating = cursor.getFloat(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_RATING));
            books.add(new Book(id, title, author, rating));
        }
        cursor.close();
        adapter.setBooks(books);
    }
}