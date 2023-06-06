package com.example.storage.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.example.storage.exception.DownloadException;
import com.example.storage.exception.FileFormatException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final AmazonS3 s3Client;

    @Value("${application.bucket.name}")
    private String bucketName;

    public String uploadFile(MultipartFile multipartFile) {
        File file = convertMultiPartFileToFile(multipartFile);
        String fileName = System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename();

        try {
            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file);
            request.setCannedAcl(CannedAccessControlList.PublicRead);
            s3Client.putObject(request);
            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);
        } finally {
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public byte[] downloadFile(String url) {
        try {
            S3Object s3Object = s3Client.getObject(bucketName, extractFileName(url));
            S3ObjectInputStream is = s3Object.getObjectContent();
            return IOUtils.toByteArray(is);
        } catch (AmazonS3Exception | IOException e) {
            throw new DownloadException("The specified image does not exist");
        }
    }

    public Boolean deleteFile(String url) {
        String fileName = extractFileName(url);
        if (s3Client.doesObjectExist(bucketName, fileName)) {
            s3Client.deleteObject(bucketName, fileName);
            return true;
        }
        return false;
    }

    private String extractFileName(String url) {
        System.out.println(url.substring(url.lastIndexOf("/") + 1));
        return url.substring(url.lastIndexOf("/") + 1);
    }

    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            throw new FileFormatException(e.getMessage());
        }
        return convertedFile;
    }
}
