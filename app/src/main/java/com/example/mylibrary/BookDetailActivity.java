package com.example.mylibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox; // 修改引入
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BookDetailActivity extends AppCompatActivity {

    private long bookId;
    private long userId;
    private BookDbHelper dbHelper;
    private TextView tvReviews, tvTitle, tvAuthor;
    private ImageView ivCover;
    private RatingBar ratingBar;
    private CheckBox btnFav; // 修改类型

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        dbHelper = new BookDbHelper(this);
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getLong("current_user_id", -1);

        bookId = getIntent().getLongExtra("book_id", -1);

        tvTitle = findViewById(R.id.tv_detail_title);
        tvAuthor = findViewById(R.id.tv_detail_author);
        ivCover = findViewById(R.id.iv_detail_cover);
        ratingBar = findViewById(R.id.rating_bar);
        tvReviews = findViewById(R.id.tv_reviews_list);

        // [修改] 获取 CheckBox
        btnFav = findViewById(R.id.btn_favorite);

        EditText etComment = findViewById(R.id.et_comment);
        Button btnSubmit = findViewById(R.id.btn_submit_review);
        FloatingActionButton fabEdit = findViewById(R.id.fab_edit);

        // --- 收藏逻辑更新 ---
        boolean isFav = dbHelper.isFavorite(userId, bookId);
        updateFavoriteButtonState(isFav); // 初始化状态

        btnFav.setOnClickListener(v -> {
            boolean newStatus = dbHelper.toggleFavorite(userId, bookId);
            updateFavoriteButtonState(newStatus);
            Toast.makeText(this, newStatus ? "已加入收藏" : "已取消收藏", Toast.LENGTH_SHORT).show();
        });

        // ... 评论和编辑逻辑保持不变 ...
        btnSubmit.setOnClickListener(v -> {
            String comment = etComment.getText().toString();
            if (comment.isEmpty()) return;
            dbHelper.addReview(userId, bookId, ratingBar.getRating(), comment);
            Toast.makeText(this, "评价成功", Toast.LENGTH_SHORT).show();
            etComment.setText("");
            loadReviews();
        });

        fabEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddBookActivity.class);
            intent.putExtra("book_id", bookId);
            startActivity(intent);
        });
    }

    // [新增] 辅助方法：更新收藏按钮的文字和状态
    private void updateFavoriteButtonState(boolean isFavorite) {
        btnFav.setChecked(isFavorite);
        btnFav.setText(isFavorite ? "已收藏" : "收藏");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookData();
        loadReviews();
    }

    private void loadBookData() {
        Book book = dbHelper.getBook(bookId);
        if (book != null) {
            tvTitle.setText(book.getTitle());
            tvAuthor.setText("作者: " + book.getAuthor());
            ratingBar.setRating(book.getRating());

            // [修改] 优化 Glide 加载逻辑
            // 无论有没有 URI，都先显示默认图，如果有 URI 再去加载
            // .error() 很重要，如果图片路径坏了，它会退回到默认图
            if (book.getImageUri() != null && !book.getImageUri().isEmpty()) {
                Glide.with(this)
                        .load(book.getImageUri())
                        .centerCrop()
                        .placeholder(R.drawable.ic_default_book_cover) // 加载中显示
                        .error(R.drawable.ic_default_book_cover)       // 加载失败显示
                        .into(ivCover);
            } else {
                // 如果数据库里本来就没存 URI，直接显示默认图
                ivCover.setImageResource(R.drawable.ic_default_book_cover);
                ivCover.setScaleType(ImageView.ScaleType.CENTER_INSIDE); // 默认图不需要裁切，完整显示
            }
        }
    }

    private void loadReviews() {
        Cursor cursor = dbHelper.getReviews(bookId);
        StringBuilder sb = new StringBuilder();
        while (cursor.moveToNext()) {
            String user = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.UserEntry.COLUMN_USERNAME));
            String comment = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.ReviewEntry.COLUMN_COMMENT));
            sb.append(user).append(": ").append(comment).append("\n\n");
        }
        cursor.close();
        tvReviews.setText(sb.length() > 0 ? sb.toString() : "暂无评论，快来抢沙发！");
    }
}