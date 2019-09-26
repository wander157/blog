package com.lyq.community.service;

import com.lyq.community.dto.CommentDTO;
import com.lyq.community.enums.CommentTypeEnum;
import com.lyq.community.enums.NotificationStatusEnum;
import com.lyq.community.enums.NotificationTypeEnum;
import com.lyq.community.exception.CustomizeErrorCode;
import com.lyq.community.exception.CustomizeException;
import com.lyq.community.mapper.CommentMapper;
import com.lyq.community.mapper.NotificationMapper;
import com.lyq.community.mapper.QuestionMapper;
import com.lyq.community.mapper.UserMapper;
import com.lyq.community.model.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Transactional
    public void insert(Comment comment, User commentor) {
        //查看父评论或者ID是否存在
        if (comment.getParentId() == null || comment.getParentId() == 0) {
            throw new CustomizeException(CustomizeErrorCode.TARGET_PARAM_NOT_FOUND);
        }
        //看看评论类型是否传过来了
        if (comment.getType() == null || !CommentTypeEnum.isExist(comment.getType())) {
            throw new CustomizeException(CustomizeErrorCode.TYPE_PARAM_WRONG);
        }
        //根据评论类型插入类型
        if (comment.getType() == CommentTypeEnum.COMMENT.getType()) {
            // 回复评论,查出父评论,看看是否为空,然后给通知设置问题的id
            //根据父ID查出评论
            Comment dbComment = commentMapper.selectByPrimaryKey(comment.getParentId());
            if (dbComment == null) {
                throw new CustomizeException(CustomizeErrorCode.COMMENT_NOT_FOUND);
            }
            //查出问题,因为只有二级评论,所以父评论的id一定就是问题,为通知做准备
            Question question = questionMapper.selectByPrimaryKey(dbComment.getParentId());
            if (question == null) {
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }
            //然后插入评论
            commentMapper.insert(comment);
            //给父评论增加评论数,这里需要优化
            Comment parentComment = commentMapper.selectByPrimaryKey(comment.getParentId());
            parentComment.setCommentCount(parentComment.getCommentCount() + 1);
            commentMapper.updateByPrimaryKey(parentComment);
            //这里想插入通知得得到问题的id,而不是评论的id
            createNotify(comment, dbComment.getCommentor(), commentor.getName(), question.getTitle(), NotificationTypeEnum.REPLY_COMMENT, question.getId());
        } else {
            //回复问题
            Question question = questionMapper.selectByPrimaryKey(comment.getParentId());
            if (question == null) {
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }
            comment.setCommentCount(0);
            commentMapper.insert(comment);


            //给问题增加评论数因为已经查出了父问题
            question.setCommentCount(question.getCommentCount() + 1);
            questionMapper.updateByPrimaryKey(question);
            //设置回复问题
            createNotify(comment, question.getCreator(), commentor.getName(), question.getTitle(), NotificationTypeEnum.REPLY_QUESTION, question.getId());
        }
    }

    private void createNotify(Comment comment, Integer receiver, String notifierName, String outerTitle, NotificationTypeEnum notificationType, Long outerId) {
        //如果接受者和评论者是同一个人就不评论
        if (receiver.intValue() == comment.getCommentor()) {
            return;
        }
        Notification notification = new Notification();
        notification.setGmtCreate(System.currentTimeMillis());
        notification.setType(notificationType.getType());
        notification.setOuterId(outerId);
        notification.setNotifier(comment.getCommentor().longValue());
        notification.setStatus(NotificationStatusEnum.UNREAD.getStatus());
        notification.setReceiver(receiver.longValue());
        notification.setNotifierName(notifierName);
        notification.setOuterTitle(outerTitle);
        notificationMapper.insert(notification);
    }

    public List<CommentDTO> listByTargetId(Long id, CommentTypeEnum type) {
        CommentExample commentExample = new CommentExample();
        commentExample.createCriteria().andParentIdEqualTo(id).andTypeEqualTo(type.getType());
        commentExample.setOrderByClause("gmt_create desc");
        List<Comment> comments = commentMapper.selectByExample(commentExample);
        if (comments.size() == 0) {
            return new ArrayList<>();
        }
        //利用set去重
        Set<Integer> commentors = comments.stream().map(comment -> comment.getCommentor()).collect(Collectors.toSet());
        UserExample userExample = new UserExample();
        List<Integer> userIds = new ArrayList<>();
        //获取评论人并转化为map
        userIds.addAll(commentors);
        userExample.createCriteria().andIdIn(userIds);
        List<User> users = userMapper.selectByExample(userExample);
        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(user -> user.getId(), user -> user));

        List<CommentDTO> commentDTOS = comments.stream().map(comment -> {
            CommentDTO commentDTO = new CommentDTO();
            BeanUtils.copyProperties(comment, commentDTO);
            commentDTO.setUser(userMap.get(comment.getCommentor()));
            return commentDTO;
        }).collect(Collectors.toList());
        return commentDTOS;
    }


}
