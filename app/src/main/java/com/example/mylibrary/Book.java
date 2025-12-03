package com.example.mylibrary;

public class Book {
    private long id;
    private String title;
    private String author;
    private float rating;
    private String imageUri;
    private int status;      // [修复] 补全字段
    private String filePath; // [修复] 补全字段

    // [修复] 构造函数包含所有7个参数
    public Book(long id, String title, String author, float rating, String imageUri, int status, String filePath) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.rating = rating;
        this.imageUri = imageUri;
        this.status = status;
        this.filePath = filePath;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public float getRating() { return rating; }
    public String getImageUri() { return imageUri; }
    public int getStatus() { return status; }       // [修复] 补全Getter
    public String getFilePath() { return filePath; } // [修复] 补全Getter

    public String getStatusText() {
        switch (status) {
            case 1: return "阅读中";
            case 2: return "已读";
            default: return "想看";
        }
    }
}