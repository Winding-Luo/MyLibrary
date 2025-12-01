package com.example.mylibrary;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.appcompat.app.AppCompatActivity;

public class BookDetailActivity extends AppCompatActivity {

    private long bookId;
    private long userId;
    private BookDbHelper dbHelper;
    private TextView tvReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail); // 需创建对应 XML

        dbHelper = new BookDbHelper(this);
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getLong("current_user_id", -1);

        // 获取传递过来的书籍信息
        bookId = getIntent().getLongExtra("book_id", -1);
        String bookTitle = getIntent().getStringExtra("book_title");

        TextView tvTitle = findViewById(R.id.tv_detail_title);
        tvTitle.setText(bookTitle);

        // --- 1. 收藏功能 ---
        ToggleButton btnFav = findViewById(R.id.btn_favorite);
        // 初始化收藏状态
        btnFav.setChecked(dbHelper.isFavorite(userId, bookId));
        btnFav.setOnClickListener(v -> {
            boolean isFav = dbHelper.toggleFavorite(userId, bookId);
            btnFav.setChecked(isFav);
            String msg = isFav ? "已收藏" : "取消收藏";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // --- 2. 评价功能 ---
        RatingBar ratingBar = findViewById(R.id.rating_bar);
        EditText etComment = findViewById(R.id.et_comment);
        Button btnSubmit = findViewById(R.id.btn_submit_review);
        tvReviews = findViewById(R.id.tv_reviews_list);

        btnSubmit.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = etComment.getText().toString();
            if (comment.isEmpty()) {
                Toast.makeText(this, "写点什么吧", Toast.LENGTH_SHORT).show();
                return;
            }
            dbHelper.addReview(userId, bookId, rating, comment);
            Toast.makeText(this, "评价成功", Toast.LENGTH_SHORT).show();
            etComment.setText(""); // 清空输入框
            loadReviews(); // 刷新评论列表
        });

        loadReviews();
    }

    private void loadReviews() {
        Cursor cursor = dbHelper.getReviews(bookId);
        StringBuilder sb = new StringBuilder();
        while (cursor.moveToNext()) {
            String user = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.UserEntry.COLUMN_USERNAME));
            String comment = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.ReviewEntry.COLUMN_COMMENT));
            float rating = cursor.getFloat(cursor.getColumnIndexOrThrow(BookContract.ReviewEntry.COLUMN_RATING));

            sb.append(user).append(" (").append(rating).append("分):\n");
            sb.append(comment).append("\n\n");
        }
        cursor.close();
        if (sb.length() == 0) sb.append("暂无评论");
        tvReviews.setText(sb.toString());
    }
}