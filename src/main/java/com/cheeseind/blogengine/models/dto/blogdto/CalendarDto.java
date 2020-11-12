package com.cheeseind.blogengine.models.dto.blogdto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class CalendarDto {

    private Integer[] years;
    @JsonProperty("posts")
    private Map<String, Long> postsPerDate;
}
