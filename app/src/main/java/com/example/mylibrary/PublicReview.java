package com.example.mylibrary;

public class PublicReview {
    private long id;
    private long userId;
    private String username;
    private float rating;
    private String comment;
    private String timestamp;
    private String userReadingDuration; // [新增] 评论者的阅读时长

    // [修改] 构造函数
    public PublicReview(long id, long userId, String username, float rating, String comment, String timestamp, String userReadingDuration) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
        this.userReadingDuration = userReadingDuration;
    }

    public long getId() { return id; }
    public long getUserId() { return userId; }
    public String getUsername() { return username; }
    public float getRating() { return rating; }
    public String getComment() { return comment; }
    public String getTimestamp() { return timestamp; }
    public String getUserReadingDuration() { return userReadingDuration; } // [新增]
}