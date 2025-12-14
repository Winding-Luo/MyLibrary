package com.example.mylibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private ImageView ivAvatar;
    private BookDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new BookDbHelper(this);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        ivAvatar = findViewById(R.id.iv_login_avatar);
        Button btnLogin = findViewById(R.id.btn_login);
        TextView btnRegister = findViewById(R.id.btn_go_register);

        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String username = s.toString().trim();
                if (!username.isEmpty()) {
                    loadUserAvatar(username);
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_default_avatar);
                }
            }
        });

        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString();
            String pass = etPassword.getText().toString();
            long userId = dbHelper.loginUser(user, pass);
            if (userId != -1) {
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                prefs.edit()
                        .putLong("current_user_id", userId)
                        .putString("username", user)
                        .apply();

                Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, R.string.login_fail, Toast.LENGTH_SHORT).show();
            }
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void loadUserAvatar(String username) {
        String avatarUri = dbHelper.getAvatarByUsername(username);
        if (avatarUri != null && !avatarUri.isEmpty()) {
            Glide.with(this)
                    .load(avatarUri)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
    }
}