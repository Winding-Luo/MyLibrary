package com.example.mylibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class AddBookActivity extends AppCompatActivity {

    private EditText etTitle, etAuthor;
    private RatingBar ratingBar;
    private Spinner spinnerStatus;
    private ImageView ivPreview;
    private Button btnSave, btnImportFile;
    private TextView tvFileStatus;

    private BookDbHelper dbHelper;
    private Uri selectedImageUri = null;
    private Uri selectedFileUri = null;
    private long mBookId = -1;

    private final ActivityResultLauncher<String[]> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).into(ivPreview);
                    try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Exception e) {}
                }
            }
    );

    private final ActivityResultLauncher<String[]> pickFileLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    tvFileStatus.setText("已选择: " + uri.getLastPathSegment());
                    try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Exception e) {}
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
        spinnerStatus = findViewById(R.id.spinner_status);
        ivPreview = findViewById(R.id.iv_add_preview);
        btnSave = findViewById(R.id.btn_save);
        btnImportFile = findViewById(R.id.btn_import_file);
        tvFileStatus = findViewById(R.id.tv_file_status);

        String[] statuses = {"想看", "阅读中", "已读"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses);
        spinnerStatus.setAdapter(adapter);

        ivPreview.setOnClickListener(v -> pickImageLauncher.launch(new String[]{"image/*"}));
        btnImportFile.setOnClickListener(v -> pickFileLauncher.launch(new String[]{"text/plain"}));
        btnSave.setOnClickListener(v -> saveBook());

        mBookId = getIntent().getLongExtra("book_id", -1);
        if (mBookId != -1) {
            loadBookData(mBookId);
            btnSave.setText("更新书籍");
        }
    }

    private void loadBookData(long id) {
        Book book = dbHelper.getBook(id);
        if (book != null) {
            etTitle.setText(book.getTitle());
            etAuthor.setText(book.getAuthor());
            ratingBar.setRating(book.getRating());
            spinnerStatus.setSelection(book.getStatus());
            if (book.getImageUri() != null && !book.getImageUri().isEmpty()) {
                selectedImageUri = Uri.parse(book.getImageUri());
                Glide.with(this).load(book.getImageUri()).into(ivPreview);
            }
            if (book.getFilePath() != null && !book.getFilePath().isEmpty()) {
                selectedFileUri = Uri.parse(book.getFilePath());
                tvFileStatus.setText("已关联电子书");
            }
        }
    }

    private void saveBook() {
        String title = etTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        float rating = ratingBar.getRating();
        int status = spinnerStatus.getSelectedItemPosition();

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        long userId = prefs.getLong("current_user_id", -1);
        if (userId == -1) { finish(); return; }

        if (title.isEmpty() || author.isEmpty()) {
            Toast.makeText(this, "请输入完整信息", Toast.LENGTH_SHORT).show();
            return;
        }

        String finalImagePath = "";
        if (selectedImageUri != null) {
            if ("content".equals(selectedImageUri.getScheme())) finalImagePath = copyImageToInternalStorage(selectedImageUri);
            else finalImagePath = selectedImageUri.toString();
        } else if (mBookId != -1) {
            Book oldBook = dbHelper.getBook(mBookId);
            if (oldBook != null) finalImagePath = oldBook.getImageUri();
        }

        String finalFilePath = "";
        if (selectedFileUri != null) {
            finalFilePath = selectedFileUri.toString();
        } else if (mBookId != -1) {
            Book oldBook = dbHelper.getBook(mBookId);
            if (oldBook != null) finalFilePath = oldBook.getFilePath();
        }

        if (mBookId == -1) {
            // [修复] 传入 finalFilePath
            dbHelper.addBook(userId, title, author, rating, finalImagePath, status, finalFilePath);
            Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
        } else {
            // [修复] 传入 finalFilePath
            dbHelper.updateBook(mBookId, title, author, rating, finalImagePath, status, finalFilePath);
            Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private String copyImageToInternalStorage(Uri uri) {
        try {
            String filename = "IMG_" + System.currentTimeMillis() + ".jpg";
            File file = new File(getFilesDir(), filename);
            InputStream inputStream = getContentResolver().openInputStream(uri);
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) outputStream.write(buffer, 0, length);
            outputStream.close();
            inputStream.close();
            return file.getAbsolutePath();
        } catch (Exception e) { e.printStackTrace(); return ""; }
    }
}