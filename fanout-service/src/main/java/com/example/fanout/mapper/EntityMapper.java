package com.example.fanout.mapper;

import com.example.fanout.dto.response.TweetResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EntityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "replyTo", ignore = true)
    @Mapping(target = "retweetTo", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "quoteTo", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    void updateEntity(TweetResponse entity, @MappingTarget TweetResponse entityToUpdate);
}
