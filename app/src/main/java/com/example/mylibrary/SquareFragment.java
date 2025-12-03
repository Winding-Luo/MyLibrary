package com.example.mylibrary;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SquareFragment extends Fragment {

    private BookDbHelper dbHelper;
    private BookAdapter adapter;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_square, container, false);

        dbHelper = new BookDbHelper(requireContext());
        recyclerView = view.findViewById(R.id.recycler_view_square);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new BookAdapter();
        // 点击广场的书，进入详情页，标记为公共模式
        adapter.setOnItemClickListener(book -> {
            Intent intent = new Intent(requireContext(), BookDetailActivity.class);
            intent.putExtra("book_id", book.getId());
            intent.putExtra("is_public_mode", true);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSquareBooks();
    }

    private void loadSquareBooks() {
        List<Book> books = new ArrayList<>();
        Cursor cursor = dbHelper.getSquareBooks();

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String author = cursor.getString(cursor.getColumnIndexOrThrow("author"));
            String imageUri = cursor.getString(cursor.getColumnIndexOrThrow("image_uri"));
            String sharedBy = cursor.getString(cursor.getColumnIndexOrThrow("shared_by_name"));

            // 构造 Book 用于展示，author 字段拼上分享者，status设为-1
            Book book = new Book(id, title, author + " (来自: " + (sharedBy==null?"未知":sharedBy) + ")", 0, imageUri, -1, "");
            books.add(book);
        }
        cursor.close();
        adapter.setBooks(books);
    }
}