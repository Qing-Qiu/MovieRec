package com.example.douban.controller;

import com.example.douban.pojo.AiRetrieveRequest;
import com.example.douban.pojo.AiRetrieveResponse;
import com.example.douban.pojo.AiContextItem;
import com.example.douban.pojo.BaseResponse;
import com.example.douban.service.AiRetrieveService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {"http://localhost:8081", "http://localhost:8082"})
@RequestMapping(value = "/api/ai")
public class AiController {
    @Resource
    private AiRetrieveService aiRetrieveService;

    @PostMapping("/retrieve")
    public BaseResponse<AiRetrieveResponse> retrieve(@RequestBody AiRetrieveRequest request) {
        return BaseResponse.success(aiRetrieveService.retrieve(request));
    }

    @PostMapping("/comment-insight")
    public BaseResponse<AiContextItem> commentInsight(@RequestBody AiRetrieveRequest request) {
        AiContextItem result = aiRetrieveService.commentInsight(request);
        return BaseResponse.success(result);
    }
}
