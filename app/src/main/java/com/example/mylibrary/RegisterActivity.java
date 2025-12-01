package com.example.mylibrary;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private BookDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // 请确保创建对应的 XML

        dbHelper = new BookDbHelper(this);
        etUsername = findViewById(R.id.et_reg_username);
        etPassword = findViewById(R.id.et_reg_password);
        Button btnRegister = findViewById(R.id.btn_register_confirm);

        btnRegister.setOnClickListener(v -> {
            String user = etUsername.getText().toString();
            String pass = etPassword.getText().toString();
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "请输入完整信息", Toast.LENGTH_SHORT).show();
                return;
            }

            long id = dbHelper.registerUser(user, pass);
            if (id != -1) {
                Toast.makeText(this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                finish(); // 返回登录页
            } else {
                Toast.makeText(this, "注册失败，用户名可能已存在", Toast.LENGTH_SHORT).show();
            }
        });
    }
}