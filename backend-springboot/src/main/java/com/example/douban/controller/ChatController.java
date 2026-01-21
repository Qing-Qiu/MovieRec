package com.example.douban.controller;

import com.example.douban.pojo.BaseResponse;
import com.example.douban.service.OpenAiService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:8081")
@RequestMapping(value = "/api")
public class ChatController {
    @Resource
    private OpenAiService openAiService;

    @PostMapping("/chat")
    public BaseResponse<String> chatSingle(@RequestBody Map<String, String> msg) {
        String result = openAiService.chatSingle(msg.get("msg"));
        return BaseResponse.success(result);
    }
}
