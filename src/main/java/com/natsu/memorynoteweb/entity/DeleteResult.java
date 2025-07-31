package com.natsu.memorynoteweb.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeleteResult {
    private int deletedCount = 0;
    private List<Integer> failedIds = new ArrayList<>();
    private Map<Integer, String> errors = new HashMap<>();
    private String message;

    public int getDeletedCount() {
        return deletedCount;
    }

    public List<Integer> getFailedIds() {
        return failedIds;
    }

    public Map<Integer, String> getErrors() {
        return errors;
    }

    public String getMessage() {
        return message;
    }

    public void setDeletedCount(int deletedCount) {
        this.deletedCount = deletedCount;
    }

    public void setFailedIds(List<Integer> failedIds) {
        this.failedIds = failedIds;
    }

    public void setErrors(Map<Integer, String> errors) {
        this.errors = errors;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void incrementDeletedCount() {
        this.deletedCount++;
    }

    public void addError(Integer photoId, String error) {
        this.failedIds.add(photoId);
        this.errors.put(photoId, error);
    }

    public boolean isSuccess() {
        return failedIds.isEmpty();
    }

    public boolean hasPartialSuccess() {
        return deletedCount > 0 && !failedIds.isEmpty();
    }

    public void generateMessage(int totalRequested) {
        if (isSuccess()) {
            this.message = String.format("成功删除 %d 张照片", deletedCount);
        } else if (hasPartialSuccess()) {
            this.message = String.format("部分成功：删除 %d/%d 张照片，%d 张失败",
                    deletedCount, totalRequested, failedIds.size());
        } else {
            this.message = String.format("删除失败：无法删除任何照片");
        }
    }
}
