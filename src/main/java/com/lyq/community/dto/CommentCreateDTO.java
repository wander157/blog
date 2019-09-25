package com.lyq.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CommentCreateDTO {
    @JsonProperty(value = "parentId")
    private Long prentId;
    private String content;
    private Integer type;
}
