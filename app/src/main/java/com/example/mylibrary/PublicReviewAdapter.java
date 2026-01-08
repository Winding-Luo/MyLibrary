package com.example.mylibrary;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class PublicReviewAdapter extends RecyclerView.Adapter<PublicReviewAdapter.ReviewViewHolder> {

    private List<PublicReview> reviews = new ArrayList<>();
    private Context context;
    private BookDbHelper dbHelper;
    private long currentUserId;

    public PublicReviewAdapter(Context context, long currentUserId) {
        this.context = context;
        this.dbHelper = new BookDbHelper(context);
        this.currentUserId = currentUserId;
    }

    public void setReviews(List<PublicReview> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_public_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        PublicReview review = reviews.get(position);
        holder.tvUser.setText(review.getUsername());
        holder.tvContent.setText(review.getComment());
        holder.tvTime.setText(review.getTimestamp());

        // 使用 TimeUtil 格式化并显示
        if (review.getUserReadingDuration() != null && !review.getUserReadingDuration().isEmpty()) {
            holder.tvUserDuration.setVisibility(View.VISIBLE);
            try {
                long millis = Long.parseLong(review.getUserReadingDuration());
                holder.tvUserDuration.setText("已读 " + TimeUtil.formatDuration(millis));
            } catch (NumberFormatException e) {
                holder.tvUserDuration.setVisibility(View.GONE);
            }
        } else {
            holder.tvUserDuration.setVisibility(View.GONE);
        }

        int likeCount = dbHelper.getReviewLikeCount(review.getId());
        boolean isLiked = dbHelper.isReviewLikedByMe(currentUserId, review.getId());
        holder.tvLikeCount.setText(likeCount > 0 ? String.valueOf(likeCount) : "赞");
        int color = isLiked ? 0xFFFFD700 : 0xFF757575;
        holder.ivLike.setColorFilter(color);
        holder.tvLikeCount.setTextColor(color);

        holder.layoutLike.setOnClickListener(v -> {
            dbHelper.toggleReviewLike(currentUserId, review.getId());
            notifyItemChanged(position);
        });

        holder.layoutReplies.removeAllViews();
        Cursor cursor = dbHelper.getReviewReplies(review.getId());
        while (cursor.moveToNext()) {
            String rUser = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            String rContent = cursor.getString(cursor.getColumnIndexOrThrow("content"));
            TextView tvReply = new TextView(context);
            tvReply.setText(rUser + ": " + rContent);
            tvReply.setTextSize(13);
            tvReply.setTextColor(0xFF555555);
            tvReply.setPadding(0, 4, 0, 4);
            holder.layoutReplies.addView(tvReply);
        }
        cursor.close();
        holder.layoutReplies.setVisibility(holder.layoutReplies.getChildCount() > 0 ? View.VISIBLE : View.GONE);
        holder.layoutReply.setOnClickListener(v -> showReplyDialog(review));
    }

    private void showReplyDialog(PublicReview review) {
        EditText etInput = new EditText(context);
        etInput.setHint("回复 " + review.getUsername() + "...");
        int padding = 32;
        etInput.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(context)
                .setTitle("回复")
                .setView(etInput)
                .setPositiveButton("发送", (dialog, which) -> {
                    String text = etInput.getText().toString();
                    if (!text.isEmpty()) {
                        dbHelper.addReviewReply(currentUserId, review.getId(), text);
                        notifyDataSetChanged();
                        Toast.makeText(context, "回复成功", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public int getItemCount() { return reviews.size(); }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvContent, tvTime, tvLikeCount, tvUserDuration;
        ImageView ivLike;
        LinearLayout layoutReplies, layoutLike, layoutReply;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tv_review_user);
            tvUserDuration = itemView.findViewById(R.id.tv_review_user_duration);
            tvContent = itemView.findViewById(R.id.tv_review_content);
            tvTime = itemView.findViewById(R.id.tv_review_time);
            layoutLike = itemView.findViewById(R.id.layout_btn_like);
            ivLike = itemView.findViewById(R.id.iv_review_like);
            tvLikeCount = itemView.findViewById(R.id.tv_review_like_count);
            layoutReply = itemView.findViewById(R.id.layout_btn_reply);
            layoutReplies = itemView.findViewById(R.id.layout_replies_container);
        }
    }
}