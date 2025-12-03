package com.example.mylibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class BookDetailActivity extends AppCompatActivity {

    private long bookId;
    private long userId;
    private BookDbHelper dbHelper;
    private boolean isPublicMode = false;

    private TextView tvTitle, tvAuthor;
    private ImageView ivCover;
    private RatingBar ratingBar, reviewRatingBar;
    private CheckBox btnFav;
    private Button btnRead, btnShare, btnAddToLib, btnSubmitReview;
    private EditText etComment;
    private FloatingActionButton fabEdit, fabDelete;
    private LinearLayout layoutReviewsContainer, layoutReviewInput;
    private RecyclerView rvPublicReviews;
    private PublicReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        dbHelper = new BookDbHelper(this);
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getLong("current_user_id", -1);

        bookId = getIntent().getLongExtra("book_id", -1);
        isPublicMode = getIntent().getBooleanExtra("is_public_mode", false);

        tvTitle = findViewById(R.id.tv_detail_title);
        tvAuthor = findViewById(R.id.tv_detail_author);
        ivCover = findViewById(R.id.iv_detail_cover);
        ratingBar = findViewById(R.id.rating_bar);

        rvPublicReviews = findViewById(R.id.rv_public_reviews);
        rvPublicReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new PublicReviewAdapter(this, userId);
        rvPublicReviews.setAdapter(reviewAdapter);

        layoutReviewsContainer = findViewById(R.id.layout_reviews_container);
        reviewRatingBar = findViewById(R.id.rating_bar_review);
        etComment = findViewById(R.id.et_comment);
        btnSubmitReview = findViewById(R.id.btn_submit_review);
        layoutReviewInput = findViewById(R.id.layout_public_review_input);

        btnRead = findViewById(R.id.btn_start_read);
        btnShare = findViewById(R.id.btn_share_to_square);
        btnAddToLib = findViewById(R.id.btn_add_to_library);
        btnFav = findViewById(R.id.btn_favorite);

        fabEdit = findViewById(R.id.fab_edit);
        fabDelete = findViewById(R.id.fab_delete);

        setupUIByMode();

        if (!isPublicMode) {
            btnRead.setOnClickListener(v -> startReading());
            btnShare.setOnClickListener(v -> {
                boolean success = dbHelper.shareBookToSquare(bookId, userId);
                Toast.makeText(this, success ? "已分享到广场" : "广场上已存在这本书", Toast.LENGTH_SHORT).show();
            });
            fabEdit.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddBookActivity.class);
                intent.putExtra("book_id", bookId);
                startActivity(intent);
            });
            fabDelete.setOnClickListener(v -> showDeleteConfirmation());
            boolean isFav = dbHelper.isFavorite(userId, bookId);
            updateFavoriteButtonState(isFav);
            btnFav.setOnClickListener(v -> {
                boolean newStatus = dbHelper.toggleFavorite(userId, bookId);
                updateFavoriteButtonState(newStatus);
            });
        } else {
            // [修改] 处理添加结果
            btnAddToLib.setOnClickListener(v -> {
                boolean success = dbHelper.addBookFromSquare(userId, bookId);
                if (success) {
                    Toast.makeText(this, "已加入你的图书馆", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "你的图书馆里已经有这本书了", Toast.LENGTH_SHORT).show();
                }
            });

            btnSubmitReview.setOnClickListener(v -> {
                String comment = etComment.getText().toString();
                if (comment.isEmpty()) return;
                dbHelper.addPublicReview(userId, bookId, reviewRatingBar.getRating(), comment);
                Toast.makeText(this, "评论成功", Toast.LENGTH_SHORT).show();
                etComment.setText("");
                loadPublicReviews();
            });
        }
    }

    private void setupUIByMode() {
        if (isPublicMode) {
            btnRead.setVisibility(View.GONE);
            fabEdit.setVisibility(View.GONE);
            fabDelete.setVisibility(View.GONE);
            btnShare.setVisibility(View.GONE);
            btnFav.setVisibility(View.GONE);
            btnAddToLib.setVisibility(View.VISIBLE);
            layoutReviewsContainer.setVisibility(View.VISIBLE);
            layoutReviewInput.setVisibility(View.VISIBLE);
            loadSquareBookData();
            loadPublicReviews();
        } else {
            btnRead.setVisibility(View.VISIBLE);
            fabEdit.setVisibility(View.VISIBLE);
            fabDelete.setVisibility(View.VISIBLE);
            btnShare.setVisibility(View.VISIBLE);
            btnFav.setVisibility(View.VISIBLE);
            btnAddToLib.setVisibility(View.GONE);
            layoutReviewsContainer.setVisibility(View.GONE);
            loadPrivateBookData();
        }
    }

    private void loadPrivateBookData() {
        Book book = dbHelper.getBook(bookId);
        if (book != null) {
            tvTitle.setText(book.getTitle());
            tvAuthor.setText(book.getAuthor());
            ratingBar.setRating(book.getRating());
            if (book.getImageUri() != null) Glide.with(this).load(book.getImageUri()).into(ivCover);
        }
    }

    private void loadSquareBookData() {
        Cursor c = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM " + BookDbHelper.TABLE_PUBLIC_BOOKS + " WHERE _id=?", new String[]{String.valueOf(bookId)});
        if (c.moveToFirst()) {
            tvTitle.setText(c.getString(c.getColumnIndexOrThrow("title")));
            tvAuthor.setText(c.getString(c.getColumnIndexOrThrow("author")));
            String img = c.getString(c.getColumnIndexOrThrow("image_uri"));
            if (img != null) Glide.with(this).load(img).into(ivCover);
            ratingBar.setRating(0);
        }
        c.close();
    }

    private void loadPublicReviews() {
        List<PublicReview> reviews = new ArrayList<>();
        Cursor cursor = dbHelper.getPublicReviews(bookId);
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
            long uId = cursor.getLong(cursor.getColumnIndexOrThrow("user_id"));
            String user = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            float rating = cursor.getFloat(cursor.getColumnIndexOrThrow("rating"));
            String comment = cursor.getString(cursor.getColumnIndexOrThrow("comment"));
            String time = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));
            reviews.add(new PublicReview(id, uId, user, rating, comment, time));
        }
        cursor.close();
        reviewAdapter.setReviews(reviews);
    }

    private void startReading() {
        Book book = dbHelper.getBook(bookId);
        if (book != null && book.getFilePath() != null && !book.getFilePath().isEmpty()) {
            Intent intent = new Intent(this, ReadActivity.class);
            intent.putExtra("book_path", book.getFilePath());
            intent.putExtra("book_title", book.getTitle());
            startActivity(intent);
        } else {
            Toast.makeText(this, "未关联电子书", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this).setTitle("删除").setMessage("确定删除吗？").setPositiveButton("删除", (d, w) -> { dbHelper.deleteBook(bookId); finish(); }).setNegativeButton("取消", null).show();
    }

    private void updateFavoriteButtonState(boolean isFav) {
        btnFav.setChecked(isFav);
        btnFav.setText(isFav?"已收藏":"收藏");
    }
}