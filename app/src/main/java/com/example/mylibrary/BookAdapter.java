package com.example.mylibrary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> books;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Book book);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public BookAdapter() {
        this.books = new ArrayList<>();
    }

    public void setBooks(List<Book> books) {
        this.books = books;
        notifyDataSetChanged();
    }

    // [新增] 获取当前书籍列表，用于摇一摇重排功能
    public List<Book> getBooks() {
        return books;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book currentBook = books.get(position);
        Context context = holder.itemView.getContext();

        holder.textViewTitle.setText(currentBook.getTitle());
        holder.textViewAuthor.setText(currentBook.getAuthor());
        holder.ratingBar.setRating(currentBook.getRating());

        String statusText;
        switch (currentBook.getStatus()) {
            case 1: statusText = context.getString(R.string.status_reading); break;
            case 2: statusText = context.getString(R.string.status_read); break;
            default: statusText = context.getString(R.string.status_todo); break;
        }
        holder.textViewStatus.setText(statusText);

        if (currentBook.getImageUri() != null && !currentBook.getImageUri().isEmpty()) {
            Glide.with(context)
                    .load(currentBook.getImageUri())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(holder.imageViewCover);
        } else {
            holder.imageViewCover.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentBook);
            }
        });
    }

    @Override
    public int getItemCount() {
        return books != null ? books.size() : 0;
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTitle;
        private final TextView textViewAuthor;
        private final TextView textViewStatus;
        private final ImageView imageViewCover;
        private final RatingBar ratingBar;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.tv_title);
            textViewAuthor = itemView.findViewById(R.id.tv_author);
            textViewStatus = itemView.findViewById(R.id.tv_status_tag);
            imageViewCover = itemView.findViewById(R.id.iv_book_cover);
            ratingBar = itemView.findViewById(R.id.rating_bar_display);
        }
    }
}