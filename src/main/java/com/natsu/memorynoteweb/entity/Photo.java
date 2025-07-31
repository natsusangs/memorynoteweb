package com.natsu.memorynoteweb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@TableName("photo")
public class Photo {
    @TableId(type = IdType.AUTO)
    private int id;
    private int uid;
    private String path;
    private String gpsData;
    private LocalDateTime gpsTime;
    private LocalDateTime uploadTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getGpsData() {
        return gpsData;
    }
    public void setGpsData(String gpsData) {
        this.gpsData = gpsData;
    }

    public LocalDateTime getGpsTime() {
        return gpsTime;
    }

    public void setGpsTime(LocalDateTime gpsTime) {
        this.gpsTime = gpsTime;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    @Override
    public String toString() {
        return "Photo{" +
                "id=" + id +
                ", uid=" + uid +
                ", path='" + path + '\'' +
                ", gpsData='" + gpsData + '\'' +
                ", gpsTime=" + gpsTime +
                ", uploadTime='" + uploadTime + '\'' +
                '}';
    }

}
