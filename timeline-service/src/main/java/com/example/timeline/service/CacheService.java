package com.example.timeline.service;

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

    private final RedisTemplate<String, String> redisTemplate;
    private final Gson gson;

    @Nullable
    @SuppressWarnings("all")
    public List<Long> getTimelineFromCache(String timelineKey) {
        String json = redisTemplate.opsForValue().get(timelineKey);
        List<Long> list = null;

        if (json != null) {
            list = gson.fromJson(json, new TypeToken<List<Long>>(){}.getType());
        }

        return list;
    }

    @SuppressWarnings("all")
    public void cacheTimeline(List<Long> timeline, String timelineKey) {
        String json = gson.toJson(timeline, new TypeToken<List<Long>>(){}.getType());
        redisTemplate.opsForValue().set(timelineKey, json, 14, TimeUnit.DAYS);
    }
}
