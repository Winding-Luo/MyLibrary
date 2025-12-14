package com.example.mylibrary;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddBookActivity extends AppCompatActivity {

    private EditText etTitle, etAuthor;
    private RatingBar ratingBar;
    private Spinner spinnerStatus;
    private ImageView ivPreview;
    private Button btnSave, btnImportFile, btnTakePhoto;
    private TextView tvFileStatus;

    private BookDbHelper dbHelper;
    private Uri selectedImageUri = null;
    private Uri selectedFileUri = null;
    private Uri photoUri = null;
    private long mBookId = -1;

    // 拍照回调
    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && photoUri != null) {
                    selectedImageUri = photoUri;
                    Glide.with(this).load(photoUri).into(ivPreview);
                }
            }
    );

    // 相册选择回调
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

    // 文件选择回调
    private final ActivityResultLauncher<String[]> pickFileLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    tvFileStatus.setText(String.format(getString(R.string.file_selected), uri.getLastPathSegment()));
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
        btnTakePhoto = findViewById(R.id.btn_take_photo);

        String[] statuses = {
                getString(R.string.status_todo),
                getString(R.string.status_reading),
                getString(R.string.status_read)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses);
        spinnerStatus.setAdapter(adapter);

        ivPreview.setOnClickListener(v -> pickImageLauncher.launch(new String[]{"image/*"}));
        btnTakePhoto.setOnClickListener(v -> checkCameraPermission());
        btnImportFile.setOnClickListener(v -> pickFileLauncher.launch(new String[]{"text/plain"}));
        btnSave.setOnClickListener(v -> saveBook());

        mBookId = getIntent().getLongExtra("book_id", -1);
        if (mBookId != -1) {
            loadBookData(mBookId);
            btnSave.setText(R.string.update_book);
        } else {
            btnSave.setText(R.string.save_book);
        }
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            launchCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            Toast.makeText(this, R.string.camera_permission_needed, Toast.LENGTH_SHORT).show();
        }
    }

    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                takePictureLauncher.launch(photoUri);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.camera_launch_fail, Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws java.io.IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
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
                tvFileStatus.setText(R.string.file_associated);
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
            Toast.makeText(this, R.string.input_complete_info, Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
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
                dbHelper.addBook(userId, title, author, rating, finalImagePath, status, finalFilePath);
            } else {
                dbHelper.updateBook(mBookId, title, author, rating, finalImagePath, status, finalFilePath);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, mBookId == -1 ? R.string.add_success : R.string.update_success, Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
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