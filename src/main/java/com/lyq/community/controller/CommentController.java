package com.lyq.community.controller;

import com.lyq.community.dto.CommentCreateDTO;
import com.lyq.community.dto.CommentDTO;
import com.lyq.community.dto.ResultDTO;
import com.lyq.community.enums.CommentTypeEnum;
import com.lyq.community.exception.CustomizeErrorCode;
import com.lyq.community.model.Comment;
import com.lyq.community.model.User;
import com.lyq.community.service.CommentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


@Controller
public class CommentController {

    @Autowired
    private CommentService commentService;

    @ResponseBody
    @RequestMapping(value = "/comment", method = RequestMethod.POST)
    public Object postComment(@RequestBody CommentCreateDTO commentCreateDTO,
                              HttpServletRequest request) {
        //得到当前评论用户
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return ResultDTO.errorOf(CustomizeErrorCode.NO_LOGIN);
        }
        if (commentCreateDTO == null || StringUtils.isBlank(commentCreateDTO.getContent())) {
            return ResultDTO.errorOf(CustomizeErrorCode.CONTENT_IS_EMPTY);
        }
        //将浏览器传过来的comment封装到comment里面并插入数据库
        Comment comment = new Comment();
        //得到评论的父问题或者父评论的ID
        comment.setParentId(commentCreateDTO.getPrentId());
        //设置评论内容
        comment.setContent(commentCreateDTO.getContent());
        //设置评论属性,是评论问题还是评论回复
        comment.setType(commentCreateDTO.getType());
        //设置评论创建时间
        comment.setGmtCreate(System.currentTimeMillis());
        comment.setGmtModified(System.currentTimeMillis());
        //设置likeCount,插入如果没有设置就会将默认值修改为null
        comment.setLikeCount(0L);
        //设置评论人的ID
        comment.setCommentor(user.getId());
        commentService.insert(comment,user);
        return ResultDTO.okOf();
    }

    @ResponseBody
    @RequestMapping(value = "/comment/{id}", method = RequestMethod.GET)
    public ResultDTO<List<CommentDTO>> comments(@PathVariable("id") Long id) {
        List<CommentDTO> commentDTOS = commentService.listByTargetId(id, CommentTypeEnum.COMMENT);
        return ResultDTO.okOf(commentDTOS);
    }
}
