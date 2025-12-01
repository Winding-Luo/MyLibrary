package com.example.mylibrary;

public class Book {
    private long id;
    private String title;
    private String author;
    private float rating;

    public Book(long id, String title, String author, float rating) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.rating = rating;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public float getRating() { return rating; }
}