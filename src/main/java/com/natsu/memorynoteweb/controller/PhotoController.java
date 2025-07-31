package com.natsu.memorynoteweb.controller;

import com.natsu.memorynoteweb.entity.DeleteResult;
import com.natsu.memorynoteweb.entity.Photo;
import com.natsu.memorynoteweb.entity.PhotoRequest;
import com.natsu.memorynoteweb.mapper.PhotoMapper;
import com.natsu.memorynoteweb.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class PhotoController {

    // This controller handles photo-related operations
    @Autowired
    private PhotoMapper photoMapper;

    @Autowired
    private PhotoService photoService;

    @PostMapping(value = "/uploadPhotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPhotos(
            @RequestParam("uid") int uid,
            @RequestParam("username") String username,
            @RequestParam("uploadTime")
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") LocalDateTime uploadTime,
            @RequestParam("files") List<MultipartFile> files
    ){
        try{
            System.out.println("=== 接收到照片上传请求 ===");
            System.out.println("用户ID: " + uid);
            System.out.println("用户名: " + username);
            System.out.println("上传时间: " + uploadTime);
            System.out.println("文件数量: " + files.size());

            if(files.isEmpty()){
                return ResponseEntity.badRequest().body(createErrorResponse("没有上传任何文件"));
            }

            List<Photo> savedPhotos = photoService.processAndSavePhotos(files, uid, username, uploadTime);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "照片上传成功");
            response.put("count", savedPhotos.size());

            System.out.println("=== 照片上传处理完成 ===");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("图片上传失败" + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(createErrorResponse("照片上传失败: " + e.getMessage()));
        }
    }

    @GetMapping("/photos/{uid}")
    private ResponseEntity<?> getPhotosByUserId(@PathVariable int uid){
        List<Photo> photos = photoMapper.selectByUserId(uid);

        if(photos == null || photos.isEmpty()){
            return ResponseEntity.ok(createErrorResponse("User photos not found"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("photos", photos);
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> createErrorResponse(String message){
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }

    @DeleteMapping("/photos")
    public ResponseEntity<?> deletePhotosByUserId(@RequestParam("uid") int uid,
                                                  @RequestParam("photoIds") List<Integer> photoIds) {
        try {
            // 参数验证
            if (photoIds == null || photoIds.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Do not provide photo IDs to delete"));
            }

            // 调用Service层处理
            DeleteResult result = photoService.deletePhotosBatch(uid, photoIds);

            // 根据结果返回响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("deletedCount", result.getDeletedCount());
            response.put("failedIds", result.getFailedIds());
            response.put("errors", result.getErrors());

            if (result.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(result.hasPartialSuccess() ? 206 : 500).body(response);
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("删除操作失败: " + e.getMessage()));
        }
    }
}
