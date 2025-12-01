package com.example.mylibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText; // TextInputEditText 是 EditText 的子类，这里兼容
import android.widget.TextView; // [修改点 1] 引入 TextView
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private BookDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new BookDbHelper(this);

        // 虽然 XML 里是 TextInputEditText，但它是 EditText 的子类，所以这里不用改
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);

        Button btnLogin = findViewById(R.id.btn_login);

        // [修改点 2] 将 Button 改为 TextView
        TextView btnRegister = findViewById(R.id.btn_go_register);

        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString();
            String pass = etPassword.getText().toString();
            long userId = dbHelper.loginUser(user, pass);
            if (userId != -1) {
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                // [修改] 同时保存 userId 和 username
                prefs.edit()
                        .putLong("current_user_id", userId)
                        .putString("username", user) // 新增这一行
                        .apply();

                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}