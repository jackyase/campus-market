package org.example.demo4.storage;

import org.example.demo4.common.R;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final FileStorageService storageService;

    public UploadController(FileStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/image")
    public R<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        return R.ok(Map.of("url", storageService.storeImage(file)));
    }

    @PostMapping("/video")
    public R<Map<String, String>> uploadVideo(@RequestParam("file") MultipartFile file) {
        return R.ok(Map.of("url", storageService.storeVideo(file)));
    }
}
