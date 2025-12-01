package com.example.mylibrary;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dbHelper = new BookDbHelper(requireContext());
        recyclerView = view.findViewById(R.id.recycler_view_home);
        SearchView searchView = view.findViewById(R.id.search_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BookAdapter();

        // 点击跳转详情
        adapter.setOnItemClickListener(book -> {
            Intent intent = new Intent(requireContext(), BookDetailActivity.class);
            intent.putExtra("book_id", book.getId());
            intent.putExtra("book_title", book.getTitle());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        // [修改点] 这里必须使用 setOnBookLongClickListener，与 BookAdapter 中的定义保持一致
        adapter.setOnBookLongClickListener(book -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("删除书籍")
                    .setMessage("确定要删除《" + book.getTitle() + "》吗？此操作不可恢复。")
                    .setPositiveButton("删除", (dialog, which) -> {
                        dbHelper.deleteBook(book.getId());
                        loadBooks(""); // 刷新列表
                        android.widget.Toast.makeText(requireContext(), "已删除", android.widget.Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        // 搜索功能逻辑
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadBooks(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loadBooks(newText);
                return true;
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBooks(""); // 默认加载所有
    }

    private void loadBooks(String queryStr) {
        if (getContext() == null) return;

        List<Book> books = new ArrayList<>();
        Cursor cursor;

        if (queryStr == null || queryStr.trim().isEmpty()) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = db.query(BookEntry.TABLE_NAME, null, null, null, null, null, BookEntry._ID + " DESC");
        } else {
            cursor = dbHelper.searchBooks(queryStr);
        }

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(BookEntry._ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_TITLE));
            String author = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_AUTHOR));
            float rating = cursor.getFloat(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_RATING));
            String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_IMAGE_URI));
            books.add(new Book(id, title, author, rating, imageUri));
        }
        cursor.close();
        adapter.setBooks(books);
    }
}