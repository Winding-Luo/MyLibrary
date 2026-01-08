package com.example.mylibrary;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserManageActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etConfirm;
    private ImageView ivAvatar;
    private Button btnSave;
    private BookDbHelper dbHelper;
    private long userId;
    private String currentAvatarUri;
    private Uri selectedUri;
    private Uri photoUri;

    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && photoUri != null) {
                    selectedUri = photoUri;
                    loadAvatar(photoUri.toString());
                }
            }
    );

    private final ActivityResultLauncher<String[]> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    selectedUri = uri;
                    loadAvatar(uri.toString());
                    try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Exception e) {}
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manage);

        dbHelper = new BookDbHelper(this);
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getLong("current_user_id", -1);

        etUsername = findViewById(R.id.et_manage_username);
        etPassword = findViewById(R.id.et_manage_password);
        etConfirm = findViewById(R.id.et_manage_confirm_password);
        ivAvatar = findViewById(R.id.iv_manage_avatar);
        btnSave = findViewById(R.id.btn_manage_save);

        loadUserData();

        ivAvatar.setOnClickListener(v -> showImageSourceDialog());
        btnSave.setOnClickListener(v -> saveUser());
    }

    private void showImageSourceDialog() {
        String[] options = {"拍照", "从相册选择"};
        new AlertDialog.Builder(this)
                .setTitle("设置头像")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermission();
                    } else {
                        pickImageLauncher.launch(new String[]{"image/*"});
                    }
                })
                .show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 200);
        } else {
            launchCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
        String imageFileName = "AVATAR_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void loadUserData() {
        Cursor cursor = dbHelper.getUser(userId);
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            currentAvatarUri = cursor.getString(cursor.getColumnIndexOrThrow("avatar_uri"));
            etUsername.setText(name);
            loadAvatar(currentAvatarUri);
        }
        cursor.close();
    }

    private void loadAvatar(String uri) {
        if (uri != null && !uri.isEmpty()) {
            Glide.with(this)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
    }

    private void saveUser() {
        String name = etUsername.getText().toString().trim();
        String pass = etPassword.getText().toString();
        String confirm = etConfirm.getText().toString();

        if (name.isEmpty()) {
            Toast.makeText(this, R.string.username_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.isEmpty() && !pass.equals(confirm)) {
            Toast.makeText(this, R.string.password_mismatch, Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            if (selectedUri != null) {
                currentAvatarUri = copyImageToInternalStorage(selectedUri);
            }

            dbHelper.updateUser(userId, name, pass, currentAvatarUri);

            getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    .edit().putString("username", name).apply();

            runOnUiThread(() -> {
                Toast.makeText(this, R.string.modify_success, Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private String copyImageToInternalStorage(Uri uri) {
        try {
            String filename = "AVATAR_" + System.currentTimeMillis() + ".jpg";
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