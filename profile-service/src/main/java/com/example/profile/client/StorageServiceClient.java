package com.example.profile.client;

import com.example.profile.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "${services.storage.name}", configuration = FeignConfig.class)
public interface StorageServiceClient {

    @PostMapping(value = "/api/v1/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadFile(@RequestPart(value = "file") MultipartFile file);

    @GetMapping(value = "/api/v1/files")
    ByteArrayResource downloadFile(@RequestParam String url);

    @DeleteMapping(value = "/api/v1/files")
    Boolean deleteFile(@RequestParam String url);
}
