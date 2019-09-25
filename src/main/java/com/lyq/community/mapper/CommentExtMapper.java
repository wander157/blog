package com.lyq.community.mapper;

import com.lyq.community.model.Comment;

public interface CommentExtMapper {
    int incCommentCount(Comment comment);
}