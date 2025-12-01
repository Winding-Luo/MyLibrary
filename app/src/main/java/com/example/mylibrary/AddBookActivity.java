package com.example.mylibrary;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mylibrary.BookContract.BookEntry;

public class AddBookActivity extends AppCompatActivity {

    private EditText etTitle, etAuthor;
    private RatingBar ratingBar;
    private BookDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        // 初始化控件
        dbHelper = new BookDbHelper(this);
        etTitle = findViewById(R.id.et_title);
        etAuthor = findViewById(R.id.et_author);
        ratingBar = findViewById(R.id.rating_bar_input);
        Button btnSave = findViewById(R.id.btn_save);

        // 点击保存按钮
        btnSave.setOnClickListener(v -> saveBook());
    }

    private void saveBook() {
        String title = etTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        float rating = ratingBar.getRating();

        if (title.isEmpty() || author.isEmpty()) {
            Toast.makeText(this, "Please enter title and author", Toast.LENGTH_SHORT).show();
            return;
        }

        // 写入数据库
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_TITLE, title);
        values.put(BookEntry.COLUMN_AUTHOR, author);
        values.put(BookEntry.COLUMN_RATING, rating);

        long newRowId = db.insert(BookEntry.TABLE_NAME, null, values);

        if (newRowId != -1) {
            Toast.makeText(this, "Book Saved!", Toast.LENGTH_SHORT).show();
            finish(); // 关闭页面，返回主页
        } else {
            Toast.makeText(this, "Error saving book", Toast.LENGTH_SHORT).show();
        }
    }
}