package com.example.storypocket;

import android.view.View;

public class Story {

    private String author, cover, desc, text, title;

    private long date_upload;
    public Story(){};

    public Story(String author, String cover, long date_upload, String desc, String text, String title) {
        this.author = author;
        this.cover = cover;
        this.date_upload = date_upload;
        this.desc = desc;
        this.text = text;
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public long getDate_upload() {
        return date_upload;
    }

    public void setDate_upload(long date_upload) {
        this.date_upload = date_upload;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
