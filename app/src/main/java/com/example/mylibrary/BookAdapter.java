package com.example.mylibrary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // 确保引入了 Glide

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> books;
    private OnItemClickListener listener;
    // [修改点 1] 改个名字，防止和系统接口冲突
    private OnBookLongClickListener longListener;

    // --- 接口定义 ---

    public interface OnItemClickListener {
        void onItemClick(Book book);
    }

    // [修改点 2] 自定义长按接口，改名为 OnBookLongClickListener
    public interface OnBookLongClickListener {
        void onBookLongClick(Book book);
    }

    // 设置点击监听
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // [修改点 3] 设置长按监听的方法名也改一下
    public void setOnBookLongClickListener(OnBookLongClickListener listener) {
        this.longListener = listener;
    }

    public BookAdapter() {
        this.books = new ArrayList<>();
    }

    public void setBooks(List<Book> books) {
        this.books = books;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载布局
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book currentBook = books.get(position);

        // 设置文本
        holder.textViewTitle.setText(currentBook.getTitle());
        holder.textViewAuthor.setText(currentBook.getAuthor());
        holder.ratingBar.setRating(currentBook.getRating());

        // 加载图片 (Glide)
        if (currentBook.getImageUri() != null && !currentBook.getImageUri().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(currentBook.getImageUri())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(holder.imageViewCover);
        } else {
            holder.imageViewCover.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // 点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentBook);
            }
        });

        // [修改点 4] 长按事件
        holder.itemView.setOnLongClickListener(v -> {
            if (longListener != null) {
                longListener.onBookLongClick(currentBook); // 调用我们改名后的接口方法
                return true; // 返回 true 表示消费了事件，不会再触发点击
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return books != null ? books.size() : 0;
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTitle;
        private final TextView textViewAuthor;
        private final ImageView imageViewCover; // 封面图
        private final RatingBar ratingBar;      // 评分条

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            // 绑定控件
            textViewTitle = itemView.findViewById(R.id.tv_title);
            textViewAuthor = itemView.findViewById(R.id.tv_author);
            imageViewCover = itemView.findViewById(R.id.iv_book_cover);
            ratingBar = itemView.findViewById(R.id.rating_bar_display);
        }
    }
}