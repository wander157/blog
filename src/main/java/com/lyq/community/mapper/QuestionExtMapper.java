package com.lyq.community.mapper;


import com.lyq.community.dto.QuestionQueryDTO;
import com.lyq.community.model.Question;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QuestionExtMapper {
    int incView(Question record);

    int incCommentCount(Question record);

    List<Question> selectRelated(Question question);

    Integer countBySearch(QuestionQueryDTO questionQueryDTO);

    List<Question> selectBySearch(QuestionQueryDTO questionQueryDTO);
}