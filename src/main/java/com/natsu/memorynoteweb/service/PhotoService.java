package com.natsu.memorynoteweb.service;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.alibaba.druid.util.StringUtils;
import com.natsu.memorynoteweb.entity.DeleteResult;
import com.natsu.memorynoteweb.entity.Photo;
import com.natsu.memorynoteweb.mapper.PhotoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PhotoService {

    @Autowired
    private PhotoMapper photoMapper;

    @Value("${upload.path:/opt/memorynote/upload}")
    private String uploadBasePath;

    private static final Pattern FILENAME_PATTERN = Pattern.compile(
            "Time(\\d{4}-\\d{2}-\\d{2})_(\\d{2}-\\d{2}-\\d{2})_GPS([+-]?\\d+\\.\\d+)_([+-]?\\d+\\.\\d+)"
    );

    public List<Photo> processAndSavePhotos(List<MultipartFile> files, int uid, String username, LocalDateTime uploadTime){
        List<Photo> savedPhotos = new ArrayList<Photo>();

        System.out.println("=== 开始处理照片上传 ===");
        System.out.println("用户ID: " + uid + ", 文件数量" + files.size());

        for(int i = 0; i < files.size(); i++){
            MultipartFile file = files.get(i);
            try{
                System.out.println("\n正在处理文件: " + (i + 1) + "/" + files.size() + ": " + file.getOriginalFilename());

                if(file.isEmpty()){
                    System.out.println("文件为空，跳过处理");
                    continue;
                }

                if(!isValidImageType(file.getContentType())){
                    System.out.println("无效的图片类型: " + file.getContentType() + "，跳过处理");
                    continue;
                }

                //1. 保存文件到服务器
                Photo photo = new Photo();
                extractInfoFromFileName(photo, file.getOriginalFilename());
                LocalDateTime photoTime = photo.getGpsTime() != null ? photo.getGpsTime() : LocalDateTime.now();
                String filePath = saveFileToServer(file, uid, photoTime);
                System.out.println("文件保存路径: " + filePath);

                //2. 设置Photo对象
                photo.setUid(uid);
                photo.setPath(filePath);
                photo.setUploadTime(uploadTime);



                int status = photoMapper.insertPhoto(photo);
                if (status > 0) {
                    // photo对象的id已经被自动回填
                    System.out.println("照片保存成功，ID: " + photo.getId());
                    savedPhotos.add(photo);
                } else {
                    System.out.println("照片保存失败");
                    throw new RuntimeException("照片保存失败，数据库操作异常");
                }

                System.out.println("文件处理完成，已保存到数据库: " + photo);

            } catch (Exception e){
                System.out.println("处理文件时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("=== 照片处理完成 ===");
        System.out.println("总共处理了 " + savedPhotos.size() + " 张照片");
        return savedPhotos;
    }

    private boolean isValidImageType(String contentType) {
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif") ||
                        contentType.equals("image/webp")
        );
    }

    private String saveFileToServer(MultipartFile file, int uid, LocalDateTime photoTime) throws IOException {
        // 生成文件存储路径
        String relativePath = generateFilePath(file, uid, photoTime);

        String fullPath = uploadBasePath + File.separator + relativePath;

        File targetFile = new File(fullPath);
        File parentDir = targetFile.getParentFile();
        if(!parentDir.exists()){
            boolean created = parentDir.mkdirs();
            System.out.println("创建目录: " + parentDir.getAbsolutePath() + " 成功: " + created);
        }

        file.transferTo(new File(targetFile.getAbsolutePath()));
        System.out.println("文件已保存到: " + fullPath);

        return relativePath;
    }

    private String generateFilePath(MultipartFile file, int uid, LocalDateTime photoTime){
        String datePath = photoTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            String extension = getFileExtension(file.getContentType());
            fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        }

        return String.format("users/%d/%s/%s", uid, datePath, fileName);
    }

    private String getFileExtension(String contentType) {
        if (contentType == null) return ".jpg";
        switch (contentType) {
            case "image/jpeg": return ".jpg";
            case "image/png": return ".png";
            case "image/gif": return ".gif";
            case "image/webp": return ".webp";
            default: return ".jpg";
        }
    }

    private void extractInfoFromFileName(Photo photo, String fileName){
        try{
            System.out.println("正在从文件名提取信息: " + fileName);

            Matcher matcher = FILENAME_PATTERN.matcher(fileName);
            if(matcher.find()){
                String datePart = matcher.group(1);
                String timePart = matcher.group(2);
                String latitude = matcher.group(3);
                String longitude = matcher.group(4);

                System.out.println("提取到的日期: " + datePart);
                System.out.println("提取到的时间: " + timePart);
                System.out.println("提取到的纬度: " + latitude);
                System.out.println("提取到的经度: " + longitude);

                //1. 设置GPS时间
                try{
                    String standardTime = timePart.replace("-", ":");
                    String dateTimeString = datePart + " " + standardTime;
                    LocalDateTime gpsTime = LocalDateTime.parse(dateTimeString,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    photo.setGpsTime(gpsTime);
                    System.out.println("提取到的GPS时间: " + gpsTime);
                } catch(Exception e){
                    System.err.println("解析GPS时间失败: " + e.getMessage());
                }

                //2. 设置GPS数据
                try{
                    double lat = Double.parseDouble(latitude);
                    double lng = Double.parseDouble(longitude);

                    String gpsData = String.format(
                            "{\"latitude\":%.6f,\"longitude\":%.6f}",
                            lat, lng
                    );
                    photo.setGpsData(gpsData);
                    System.out.println("提取到的GPS数据: " + gpsData);
                } catch(Exception e){
                    System.err.println("解析GPS数据失败: " + e.getMessage());
                }
            } else{
                System.out.println("文件名格式不匹配，无法提取信息: " + fileName);
            }
        } catch (Exception e){
            System.err.println("提取文件名信息时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public DeleteResult deletePhotosBatch(int uid, List<Integer> photoIds) {
        DeleteResult result = new DeleteResult();

        // 1. 获取用户的所有照片，验证权限
        List<Photo> userPhotos = photoMapper.selectByUserId(uid);
        Map<Integer, Photo> userPhotoMap = userPhotos.stream()
                .collect(Collectors.toMap(Photo::getId, photo -> photo));

        // 2. 验证所有要删除的照片是否属于该用户
        List<Integer> notFoundIds = new ArrayList<>();
        List<Photo> photosToDelete = new ArrayList<>();

        for (Integer photoId : photoIds) {
            Photo photo = userPhotoMap.get(photoId);
            if (photo == null) {
                notFoundIds.add(photoId);
                result.addError(photoId, "照片不存在或不属于该用户");
            } else {
                photosToDelete.add(photo);
            }
        }

        // 3. 如果有不存在的照片，记录但继续处理存在的照片
        if (!notFoundIds.isEmpty()) {
            System.out.println("以下照片ID不存在或不属于用户 " + uid + ": " + notFoundIds);
        }

        // 4. 批量删除存在的照片
        for (Photo photo : photosToDelete) {
            try {
                // 先删除物理文件
                boolean fileDeleted = deletePhysicalFile(photo.getPath());
                if (!fileDeleted) {
                    System.out.println("文件删除失败但继续删除数据库记录:" + photo.getPath());
                }

                // 删除数据库记录
                int dbResult = photoMapper.deleteById(photo.getId());
                if (dbResult > 0) {
                    result.incrementDeletedCount();
                    System.out.println("成功删除照片 - ID:" + photo.getId() + "Path:" + photo.getPath());
                } else {
                    result.addError(photo.getId(), "数据库删除失败");
                    System.out.println("数据库删除失败 - ID:" + photo.getId());
                }

            } catch (Exception e) {
                result.addError(photo.getId(), "删除失败: " + e.getMessage());
                System.out.println("删除照片失败 - ID:" + photo.getId() + e.getMessage());
                // 不抛出异常，继续处理其他照片
            }
        }

        // 5. 生成结果消息
        result.generateMessage(photoIds.size());

        return result;
    }

    private boolean deletePhysicalFile(String relativePath) {
        if (StringUtils.isEmpty(relativePath)) {
            return true; // 没有文件路径，视为成功
        }

        try {
            // 构建完整路径
            Path fullPath = Paths.get(uploadBasePath, relativePath).normalize();

            // 安全检查：确保文件在上传目录内
            Path uploadPath = Paths.get(uploadBasePath).normalize();
            if (!fullPath.startsWith(uploadPath)) {
                System.out.println("安全错误：尝试删除上传目录外的文件:" + fullPath);
                return false;
            }

            // 删除文件
            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                System.out.println("成功删除文件: " + fullPath);

                // 尝试删除空目录
                tryDeleteEmptyDirectories(fullPath.getParent());

                return true;
            } else {
                System.out.println("文件不存在: " + fullPath);
                return true; // 文件不存在也视为成功
            }

        } catch (IOException e) {
            System.out.println("删除文件失败: " + relativePath);
            return false;
        }
    }

    private void tryDeleteEmptyDirectories(Path directory) {
        try {
            // 向上递归删除空目录，直到upload根目录
            Path uploadPath = Paths.get(uploadBasePath).normalize();
            Path current = directory;

            while (current != null && !current.equals(uploadPath) && current.startsWith(uploadPath)) {
                if (isDirectoryEmpty(current)) {
                    Files.delete(current);
                    System.out.println("删除空目录:" + current);
                    current = current.getParent();
                } else {
                    break; // 目录不为空，停止
                }
            }
        } catch (Exception e) {
            System.out.println("清理空目录时出错:" + e.getMessage());
            // 忽略错误，这只是清理操作
        }
    }

    private boolean isDirectoryEmpty(Path directory) throws IOException {
        try (var entries = Files.list(directory)) {
            return !entries.findFirst().isPresent();
        }
    }

}
