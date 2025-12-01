package com.example.mylibrary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // 引入 TextView
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    // 1. 数据源列表
    private List<Book> books;

    // 2. 点击事件监听器
    private OnItemClickListener listener;

    /**
     * 点击事件的接口定义
     */
    public interface OnItemClickListener {
        void onItemClick(Book book);
    }

    /**
     * 为外部设置监听器的公共方法
     *
     * @param listener 在Activity中实现的监听器实例
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 无参数的构造函数
     * 在 Activity 中使用 "new BookAdapter()" 时调用
     */
    public BookAdapter() {
        this.books = new ArrayList<>();
    }

    /**
     * 用于更新数据并刷新 RecyclerView 的方法
     *
     * @param books 新的书籍数据列表
     */
    public void setBooks(List<Book> books) {
        this.books = books; // 直接替换列表
        notifyDataSetChanged(); // 通知适配器数据已更改，刷新整个列表
    }

    /**
     * 创建 ViewHolder 实例
     * 这个方法会为每个列表项创建一个新的视图
     */
    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载列表项的布局文件 (你需要创建 item_book.xml)
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(itemView);
    }

    /**
     * 将数据绑定到 ViewHolder 的视图上
     *
     * @param holder   当前列表项的 ViewHolder
     * @param position 当前列表项的位置
     */
    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        // 获取当前位置的书籍对象
        Book currentBook = books.get(position);

        // 将书籍数据设置到视图上 (你需要确保ViewHolder中有这些视图)
        holder.textViewTitle.setText(currentBook.getTitle());
        holder.textViewAuthor.setText(currentBook.getAuthor());

        // 为整个列表项设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentBook);
            }
        });
    }

    /**
     * 返回数据项的总数
     */
    @Override
    public int getItemCount() {
        return books != null ? books.size() : 0;
    }

    /**
     * ViewHolder 内部类，用于持有每个列表项的视图引用
     */
    class BookViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTitle;
        private final TextView textViewAuthor;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            // 修改这里：使用 item_book.xml 中定义的正确 ID (tv_title 和 tv_author)
            textViewTitle = itemView.findViewById(R.id.tv_title);
            textViewAuthor = itemView.findViewById(R.id.tv_author);
        }
    }
}
