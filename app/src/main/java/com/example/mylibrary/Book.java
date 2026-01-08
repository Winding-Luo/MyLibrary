package com.example.mylibrary;

public class Book {
    private long id;
    private String title;
    private String author;
    private float rating;
    private String imageUri;
    private int status;
    private String filePath;
    private String readingDuration; // [新增]

    // [修改] 构造函数包含所有参数
    public Book(long id, String title, String author, float rating, String imageUri, int status, String filePath, String readingDuration) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.rating = rating;
        this.imageUri = imageUri;
        this.status = status;
        this.filePath = filePath;
        this.readingDuration = readingDuration;
    }

    // 兼容旧代码的构造函数（为了严谨，如果你有测试代码依赖这个，建议更新测试代码，这里作为备选）
    // 但根据你的需求，我们优先使用全参数构造
    public Book(long id, String title, String author, float rating, String imageUri, int status, String filePath) {
        this(id, title, author, rating, imageUri, status, filePath, "");
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public float getRating() { return rating; }
    public String getImageUri() { return imageUri; }
    public int getStatus() { return status; }
    public String getFilePath() { return filePath; }
    public String getReadingDuration() { return readingDuration; } // [新增]

    public String getStatusText() {
        switch (status) {
            case 1: return "阅读中";
            case 2: return "已读";
            default: return "想看";
        }
    }
}