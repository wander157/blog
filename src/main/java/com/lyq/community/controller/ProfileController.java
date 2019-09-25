package com.lyq.community.controller;

import com.lyq.community.dto.NotificationDTO;
import com.lyq.community.dto.PaginationDTO;
import com.lyq.community.exception.CustomizeErrorCode;
import com.lyq.community.exception.CustomizeException;
import com.lyq.community.model.User;
import com.lyq.community.service.NotificationService;
import com.lyq.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ProfileController {

    @Autowired
    private QuestionService questionService;
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/profile/{action}")
    public String profile(@PathVariable(name = "action", value = "") String action,
                          Model model,
                          HttpServletRequest request,
                          @RequestParam(name = "page", defaultValue = "1") Integer page,
                          @RequestParam(name = "size", defaultValue = "2") Integer size) {

        User user= (User) request.getSession().getAttribute("user");

        if (user == null) {
            return "redirect:/";
        }
        if ("questions".equals(action)) {
            model.addAttribute("section", "questions");
            model.addAttribute("sectionName", "我的提问");
            PaginationDTO paginationDTO = questionService.list(user.getId(), page, size);
            model.addAttribute("pagination", paginationDTO);
        } else if ("replies".equals(action)) {
            if (user==null){
                throw new CustomizeException(CustomizeErrorCode.NO_LOGIN);
            }
            PaginationDTO<NotificationDTO> paginationDTO=notificationService.list(user.getId().longValue(),page, size);
            Long unreadCount =notificationService.unReadCount(user.getId());
            model.addAttribute("pagination",paginationDTO);
            model.addAttribute("unreadCount",unreadCount);
            model.addAttribute("section", "replies");
            model.addAttribute("sectionName", "最新回复");
        }
        return "profile";
    }

}
