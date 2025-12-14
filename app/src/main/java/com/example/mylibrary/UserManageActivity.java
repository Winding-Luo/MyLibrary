package com.example.mylibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class UserManageActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etConfirm;
    private ImageView ivAvatar;
    private Button btnSave;
    private BookDbHelper dbHelper;
    private long userId;
    private String currentAvatarUri;
    private Uri selectedUri;

    private final ActivityResultLauncher<String[]> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    selectedUri = uri; // 暂存，不立即复制
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

        ivAvatar.setOnClickListener(v -> pickImageLauncher.launch(new String[]{"image/*"}));
        btnSave.setOnClickListener(v -> saveUser());
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

        // 子线程处理 IO
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