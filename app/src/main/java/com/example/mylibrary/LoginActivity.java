package com.example.mylibrary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private BookDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // 请确保创建对应的 XML

        dbHelper = new BookDbHelper(this);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnRegister = findViewById(R.id.btn_go_register);

        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString();
            String pass = etPassword.getText().toString();
            long userId = dbHelper.loginUser(user, pass);
            if (userId != -1) {
                // 登录成功，保存状态
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                prefs.edit().putLong("current_user_id", userId).apply();

                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
            }
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}