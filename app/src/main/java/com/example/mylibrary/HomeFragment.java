package com.example.mylibrary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mylibrary.BookContract.BookEntry;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private BookDbHelper dbHelper;
    private BookAdapter adapter;
    private RecyclerView recyclerView;

    private String currentKeyword = "";
    private int currentStatusFilter = -1; // -1:全部, 0:想看, 1:阅读中, 2:已读
    private String currentSortOrder = "time"; // time, rating, status

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dbHelper = new BookDbHelper(requireContext());
        recyclerView = view.findViewById(R.id.recycler_view_home);
        SearchView searchView = view.findViewById(R.id.search_view);
        Toolbar toolbar = view.findViewById(R.id.toolbar_home);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BookAdapter();

        adapter.setOnItemClickListener(book -> {
            Intent intent = new Intent(requireContext(), BookDetailActivity.class);
            intent.putExtra("book_id", book.getId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        // 设置 Toolbar 菜单
        if (toolbar != null) {
            toolbar.setOnMenuItemClickListener(this::onToolbarMenuItemClick);
        }

        // 搜索逻辑
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentKeyword = query;
                loadBooks();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                currentKeyword = newText;
                loadBooks();
                return true;
            }
        });

        return view;
    }

    private boolean onToolbarMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sort_time) {
            currentSortOrder = "time";
        } else if (id == R.id.sort_rating) {
            currentSortOrder = "rating";
        } else if (id == R.id.sort_status) {
            currentSortOrder = "status";
        } else if (id == R.id.filter_all) {
            currentStatusFilter = -1;
        } else if (id == R.id.filter_todo) {
            currentStatusFilter = 0;
        } else if (id == R.id.filter_reading) {
            currentStatusFilter = 1;
        } else if (id == R.id.filter_read) {
            currentStatusFilter = 2;
        }
        loadBooks();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBooks();
    }

    private void loadBooks() {
        if (getContext() == null) return;
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        long currentUserId = prefs.getLong("current_user_id", -1);

        List<Book> books = new ArrayList<>();
        // 确保调用的是支持所有参数的 queryBooks
        Cursor cursor = dbHelper.queryBooks(currentUserId, currentKeyword, currentStatusFilter, currentSortOrder);

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(BookEntry._ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_TITLE));
            String author = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_AUTHOR));
            float rating = cursor.getFloat(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_RATING));
            String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_IMAGE_URI));

            // 兼容读取 status
            int status = 0;
            try {
                status = cursor.getInt(cursor.getColumnIndexOrThrow("status"));
            } catch (Exception e) {
                // Ignore
            }

            // [新增] 兼容读取 filePath
            String filePath = "";
            try {
                filePath = cursor.getString(cursor.getColumnIndexOrThrow("file_path"));
            } catch (Exception e) {
                // Ignore
            }

            // [修改] 使用完整的 7 参数构造函数
            books.add(new Book(id, title, author, rating, imageUri, status, filePath));
        }
        cursor.close();
        adapter.setBooks(books);
    }
}