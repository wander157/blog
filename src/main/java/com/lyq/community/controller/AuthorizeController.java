package com.lyq.community.controller;

import com.lyq.community.dto.AccessTokenDTO;
import com.lyq.community.dto.GithubUser;
import com.lyq.community.model.User;
import com.lyq.community.provider.GithubProvider;
import com.lyq.community.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
@Slf4j
public class AuthorizeController {

    @Autowired
    private GithubProvider githubProvider;

    @Value("${github.client.id}")
    private String id;
    @Value("${github.client.secret}")
    private String secret;
    @Value("${github.redirect.uri}")
    private String uri;


    @Autowired
    private UserService userService;

    @RequestMapping("/callback")
    public String callback(@RequestParam(name = "code") String code,
                           @RequestParam(name = "state") String state,
                           HttpServletResponse response) {

        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
        accessTokenDTO.setCode(code);
        accessTokenDTO.setClient_id(id);
        accessTokenDTO.setClient_secret(secret);
        accessTokenDTO.setState(state);
        accessTokenDTO.setRedirect_uri(uri);
        //得到git传回来的数据
        String accessToken = githubProvider.getAccessToken(accessTokenDTO);
        //通过解析封装成一个对象!
        GithubUser githubUser = githubProvider.getUser(accessToken);
        if (githubUser != null) {
            if (githubUser != null) {
                //写入数据库
                //使用github登陆成功后获取用户信息会生成一个token 把token放入user并存入数据库,再放入cookie里面去
                User user = new User();
                String token = UUID.randomUUID().toString();
                user.setToken(token);
                user.setName(githubUser.getName());
                user.setAccountId(String.valueOf(githubUser.getId()));
                user.setAvatarUrl(githubUser.getAvatarUrl());
                userService.createOrUpdate(user);
                response.addCookie(new Cookie("token", token));
            }
            //login success
            //  request.getSession().setAttribute("user",githubUser);
            return "redirect:/";
        } else {
            log.error("登陆有毛病,在Authrize");
            return "redirect:/";
        }
    }
    @GetMapping("/logout")
    public String logout(HttpServletRequest request,HttpServletResponse response){
        request.getSession().removeAttribute("user");
        Cookie cookie = new Cookie("token",null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/";
    }
}
