package com.natsu.memorynoteweb.entity;

import java.util.List;

public class PhotoRequest {
    private int uid;
    private String username;
    private String uploadTime;
    private List<Photo> photos;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    @Override
    public String toString() {
        return "PhotoRequest{" +
                "uid=" + uid +
                ", username=" + username +
                ", uploadTime=" + uploadTime +
                ", photos=" + photos +
                '}';
    }
}
