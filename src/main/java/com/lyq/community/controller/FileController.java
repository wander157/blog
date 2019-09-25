package com.lyq.community.controller;

import com.lyq.community.dto.FileDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FileController {
    @RequestMapping("/file/upload")
    public FileDTO upload() {
        FileDTO fileDTO = new FileDTO();
        fileDTO.setMessage("应该是上传成功了");
        fileDTO.setSuccess(1);
        return fileDTO;
    }
}
