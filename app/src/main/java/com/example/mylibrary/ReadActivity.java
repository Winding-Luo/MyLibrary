package com.example.mylibrary;

import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReadActivity extends AppCompatActivity {

    private long startTime;
    private long bookId;
    private BookDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        dbHelper = new BookDbHelper(this);

        TextView tvContent = findViewById(R.id.tv_book_content);
        String title = getIntent().getStringExtra("book_title");
        String uriString = getIntent().getStringExtra("book_path");
        bookId = getIntent().getLongExtra("book_id", -1);

        if (title != null) setTitle(title);

        if (uriString != null && !uriString.isEmpty()) {
            loadTextContent(Uri.parse(uriString), tvContent);
        } else {
            tvContent.setText("无法打开书籍：文件路径丢失");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTime = System.currentTimeMillis();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bookId != -1) {
            long endTime = System.currentTimeMillis();
            long sessionDuration = endTime - startTime;
            // 超过5秒才记录，避免误触
            if (sessionDuration > 5000) {
                dbHelper.addReadingTime(bookId, sessionDuration);
            }
        }
    }

    private void loadTextContent(Uri uri, TextView textView) {
        new Thread(() -> {
            StringBuilder sb = new StringBuilder();
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                if (inputStream != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    reader.close();
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "读取失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                return;
            }

            String finalContent = sb.toString();
            runOnUiThread(() -> {
                if (finalContent.isEmpty()) {
                    textView.setText("文件内容为空或格式不支持");
                } else {
                    textView.setText(finalContent);
                }
            });
        }).start();
    }
}