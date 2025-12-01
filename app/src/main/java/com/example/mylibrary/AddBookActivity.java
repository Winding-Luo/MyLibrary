package com.example.mylibrary;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.mylibrary.BookContract.BookEntry;

public class AddBookActivity extends AppCompatActivity {

    private EditText etTitle, etAuthor;
    private RatingBar ratingBar;
    private ImageView ivPreview;
    private TextView tvHeader; // 标题文本
    private Button btnSave;
    private BookDbHelper dbHelper;
    private Uri selectedImageUri = null;

    private long mBookId = -1; // 如果是 -1 表示添加模式，否则是编辑模式

    private final ActivityResultLauncher<String[]> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).into(ivPreview);

                    // 获取持久化权限 (这对 OpenDocument 是有效的)
                    try {
                        getContentResolver().takePersistableUriPermission(uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        dbHelper = new BookDbHelper(this);
        etTitle = findViewById(R.id.et_title);
        etAuthor = findViewById(R.id.et_author);
        ratingBar = findViewById(R.id.rating_bar_input);
        ivPreview = findViewById(R.id.iv_add_preview);
        btnSave = findViewById(R.id.btn_save);

        // 这里的 ID 需要你去 activity_add_book.xml 里给顶部的 "Add New Book" TextView 加一个 id="@+id/tv_header_title"
        // 如果懒得加，可以把下面这行注释掉
        // tvHeader = findViewById(R.id.tv_header_title);

        // [修改点 2] 启动参数改为 String 数组
        ivPreview.setOnClickListener(v -> pickImageLauncher.launch(new String[]{"image/*"}));
        btnSave.setOnClickListener(v -> saveBook());

        // --- 检查是否是编辑模式 ---
        mBookId = getIntent().getLongExtra("book_id", -1);
        if (mBookId != -1) {
            // 编辑模式：加载已有数据
            loadBookData(mBookId);
            btnSave.setText("更新书籍"); // 按钮文字变一下
            // if (tvHeader != null) tvHeader.setText("编辑书籍");
        }
    }

    private void loadBookData(long id) {
        Book book = dbHelper.getBook(id);
        if (book != null) {
            etTitle.setText(book.getTitle());
            etAuthor.setText(book.getAuthor());
            ratingBar.setRating(book.getRating());
            if (book.getImageUri() != null && !book.getImageUri().isEmpty()) {
                selectedImageUri = Uri.parse(book.getImageUri());
                Glide.with(this).load(selectedImageUri).into(ivPreview);
            }
        }
    }

    private void saveBook() {
        String title = etTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        float rating = ratingBar.getRating();
        String imageUriStr = selectedImageUri != null ? selectedImageUri.toString() : "";

        if (title.isEmpty() || author.isEmpty()) {
            Toast.makeText(this, "请输入完整信息", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mBookId == -1) {
            // 添加模式
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(BookEntry.COLUMN_TITLE, title);
            values.put(BookEntry.COLUMN_AUTHOR, author);
            values.put(BookEntry.COLUMN_RATING, rating);
            values.put(BookEntry.COLUMN_IMAGE_URI, imageUriStr);
            db.insert(BookEntry.TABLE_NAME, null, values);
            Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
        } else {
            // 编辑模式
            dbHelper.updateBook(mBookId, title, author, rating, imageUriStr);
            Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}