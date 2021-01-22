package com.cheeseind.blogengine.services;

import com.cheeseind.blogengine.models.dto.blogdto.tagdto.SingleTagDto;
import com.cheeseind.blogengine.repositories.PostRepository;
import com.cheeseind.blogengine.models.dto.blogdto.tagdto.TagsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Precision;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {

    private final PostRepository postRepository;

    @Value("${tag.min_weight}")
    private float tagMinWeight;

    public TagsResponse findTagsByName() {
        List<List<String>> tagsPerPost = postRepository.findAllTags();

        Map<String, Long> tagsWithCounts = tagsPerPost
                .stream()
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));

        if (tagsWithCounts.isEmpty()) {
            return new TagsResponse(null);
        }

        Long maxWeight = Collections.max(tagsWithCounts.values());

        List<SingleTagDto> tagDtoList = tagsWithCounts.entrySet().stream()
                .map(entry -> {
                    String tagName = entry.getKey();
                    float tagWeight = Precision.round((float) entry.getValue() / maxWeight, 3);
                    if (tagWeight < tagMinWeight)
                        tagWeight = tagMinWeight;
                    return new SingleTagDto(tagName, tagWeight);
                })
                .sorted(Comparator.comparing(SingleTagDto::getWeight).reversed())
                .collect(Collectors.toList());

        SingleTagDto[] tags = new SingleTagDto[tagDtoList.size()];
        tags = tagDtoList.toArray(tags);
        return new TagsResponse(tags);
    }
}
