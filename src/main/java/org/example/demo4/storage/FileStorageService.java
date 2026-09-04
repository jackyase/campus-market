package org.example.demo4.storage;

import org.example.demo4.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root;
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final Set<String> VIDEO_TYPES = Set.of("video/mp4", "video/webm", "video/quicktime");

    public FileStorageService(@Value("${app.upload.dir}") String dir) {
        this.root = Paths.get(dir);
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建上传目录: " + dir, e);
        }
    }

    public String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !IMAGE_TYPES.contains(contentType)) {
            throw new BusinessException("仅支持图片格式（jpg/png/gif/webp）");
        }
        String ext = extension(contentType);
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        String sub = LocalDate.now().toString().replace("-", "/");
        Path target = root.resolve(sub).resolve(filename);
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw new BusinessException("文件保存失败");
        }
        return "/uploads/" + sub + "/" + filename;
    }

    public String storeVideo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !VIDEO_TYPES.contains(contentType)) {
            throw new BusinessException("仅支持视频格式（mp4/webm/mov）");
        }
        return save(file, videoExtension(contentType));
    }

    private String save(MultipartFile file, String ext) {
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        String sub = LocalDate.now().toString().replace("-", "/");
        Path target = root.resolve(sub).resolve(filename);
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw new BusinessException("文件保存失败");
        }
        return "/uploads/" + sub + "/" + filename;
    }

    private String extension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".bin";
        };
    }

    private String videoExtension(String contentType) {
        return switch (contentType) {
            case "video/mp4" -> ".mp4";
            case "video/webm" -> ".webm";
            case "video/quicktime" -> ".mov";
            default -> ".bin";
        };
    }
}
