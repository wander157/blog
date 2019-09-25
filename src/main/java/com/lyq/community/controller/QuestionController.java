package com.lyq.community.controller;


import com.lyq.community.dto.CommentDTO;
import com.lyq.community.dto.QuestionDTO;
import com.lyq.community.enums.CommentTypeEnum;
import com.lyq.community.service.CommentService;
import com.lyq.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class QuestionController {
    @Autowired
    QuestionService questionService;
    @Autowired
    private CommentService commentService;

    @GetMapping("/question/{id}")
    public String question(@PathVariable(name = "id") Long id,
                           Model model) {
        QuestionDTO questionDTO = questionService.getById(id);
        List<QuestionDTO> questionDTOS=questionService.selectRelated(questionDTO);
        List<CommentDTO> comments = commentService.listByTargetId(id, CommentTypeEnum.QUESTION);
        //累加阅读数
        questionService.incView(id);
        model.addAttribute("question", questionDTO);
        model.addAttribute("comment",comments);
        model.addAttribute("relatedQuestions",questionDTOS);

        return "question";
    }
}
