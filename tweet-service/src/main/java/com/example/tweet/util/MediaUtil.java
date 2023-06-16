package com.example.tweet.util;

import com.example.tweet.client.StorageServiceClient;
import com.example.tweet.entity.Tweet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MediaUtil {

    private final StorageServiceClient storageServiceClient;

    public Tweet addMedia(Tweet entity, MultipartFile[] files) {
        if (files != null) {
            Set<String> urls = new HashSet<>();
            for (MultipartFile file : files) {
                urls.add(storageServiceClient.uploadFile(file));
            }
            entity.setMediaUrls(urls);
        }
        return entity;
    }

    public Tweet updateMedia(Tweet entity, MultipartFile[] files) {
        if (files != null) {
            if (entity.getMediaUrls() != null) {
                for (String mediaUrl : entity.getMediaUrls()) {
                    storageServiceClient.deleteFile(mediaUrl);
                }
            }
            Set<String> urls = new HashSet<>();
            for (MultipartFile file : files) {
                urls.add(storageServiceClient.uploadFile(file));
            }
            entity.setMediaUrls(urls);
        }
        return entity;
    }
}
