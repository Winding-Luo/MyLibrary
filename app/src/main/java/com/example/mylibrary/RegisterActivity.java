package com.example.mylibrary;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText etUsername, etPassword, etConfirmPassword;
    private BookDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new BookDbHelper(this);
        etUsername = findViewById(R.id.et_reg_username);
        etPassword = findViewById(R.id.et_reg_password);
        etConfirmPassword = findViewById(R.id.et_reg_confirm_password);
        Button btnRegister = findViewById(R.id.btn_register_confirm);

        btnRegister.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString();
            String confirmPass = etConfirmPassword.getText().toString();

            if (user.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, R.string.input_complete_info, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirmPass)) {
                Toast.makeText(this, R.string.password_mismatch, Toast.LENGTH_SHORT).show();
                return;
            }

            long id = dbHelper.registerUser(user, pass);
            if (id != -1) {
                Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.register_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }
}