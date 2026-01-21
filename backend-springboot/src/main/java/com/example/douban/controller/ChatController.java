package com.example.douban.controller;

import com.example.douban.pojo.BaseResponse;
import com.example.douban.pojo.Message;
import com.example.douban.service.OpenAiService;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:8081")
@RequestMapping(value = "/api")
public class ChatController {
    @Resource
    private OpenAiService openAiService;

    @PostMapping("/chat")
    public BaseResponse<String> chatMulti(@RequestBody ChatPayload payload) {
        // Fallback for old clients if needed, but assuming full control
        if (payload.getMessages() == null && payload.getMsg() != null) {
             return BaseResponse.success(openAiService.chatSingle(payload.getMsg()));
        }
        
        String result = openAiService.chatMulti(payload.getMessages(), payload.getMode());
        return BaseResponse.success(result);
    }
    
    @Data
    public static class ChatPayload {
        private String msg; // For backward compatibility if needed
        private int mode;
        private List<Message> messages;
    }
}
