package com.example.mylibrary;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mylibrary.BookContract.ReviewEntry;

public class MyReviewAdapter extends RecyclerView.Adapter<MyReviewAdapter.ViewHolder> {

    private Cursor cursor;

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) cursor.close();
        cursor = newCursor;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (cursor == null || !cursor.moveToPosition(position)) return;

        String bookTitle = cursor.getString(cursor.getColumnIndexOrThrow("book_title"));
        String comment = cursor.getString(cursor.getColumnIndexOrThrow(ReviewEntry.COLUMN_COMMENT));
        String time = cursor.getString(cursor.getColumnIndexOrThrow(ReviewEntry.COLUMN_TIMESTAMP));
        float rating = cursor.getFloat(cursor.getColumnIndexOrThrow(ReviewEntry.COLUMN_RATING));

        holder.tvBookTitle.setText("《" + (bookTitle != null ? bookTitle : "未知书籍") + "》");
        holder.tvComment.setText(comment);
        holder.tvTime.setText(time);
        holder.tvRating.setText(rating + "分");
    }

    @Override
    public int getItemCount() { return cursor == null ? 0 : cursor.getCount(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookTitle, tvComment, tvTime, tvRating;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tv_my_review_book);
            tvComment = itemView.findViewById(R.id.tv_my_review_content);
            tvTime = itemView.findViewById(R.id.tv_my_review_time);
            tvRating = itemView.findViewById(R.id.tv_my_review_rating);
        }
    }
}