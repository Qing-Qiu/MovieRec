package com.example.douban.controller;

import com.example.douban.pojo.BaseResponse;
import com.gearwenxin.client.ernie.ErnieBotClient;
import com.gearwenxin.entity.response.ChatResponse;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:8081")
@RequestMapping(value = "/api")
public class ChatController {
    @Resource
    private ErnieBotClient ernieBotClient;

    @PostMapping("/chat")
    public BaseResponse<String> chatSingle(@RequestBody Map<String, String> msg) {
        ChatResponse response = ernieBotClient.chatSingle(msg.get("msg")).block();
        return BaseResponse.success(response.getResult());
    }
}
