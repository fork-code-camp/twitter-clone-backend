package com.example.fanout.service;

import com.example.fanout.dto.response.TweetResponse;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Gson gson;

    @Nullable
    @SuppressWarnings("all")
    public List<TweetResponse> getTimelineFromCache(String timelineKey) {
        String json = (String) redisTemplate.opsForValue().get(timelineKey);
        List<TweetResponse> list = null;

        if (json != null) {
            list = gson.fromJson(json, new TypeToken<List<TweetResponse>>(){}.getType());
        }

        return list;
    }

    @SuppressWarnings("all")
    public void cacheTimeline(List<TweetResponse> timeline, String timelineKey) {
        String json = gson.toJson(timeline, new TypeToken<List<TweetResponse>>(){}.getType());
        redisTemplate.opsForValue().set(timelineKey, json, 14, TimeUnit.DAYS);
    }
}
