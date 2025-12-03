package com.example.mylibrary;

public class PublicReview {
    private long id;
    private long userId;
    private String username;
    private float rating;
    private String comment;
    private String timestamp;

    public PublicReview(long id, long userId, String username, float rating, String comment, String timestamp) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public long getUserId() { return userId; }
    public String getUsername() { return username; }
    public float getRating() { return rating; }
    public String getComment() { return comment; }
    public String getTimestamp() { return timestamp; }
}