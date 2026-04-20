package com.example.douban.controller;

import com.example.douban.pojo.BaseResponse;
import com.example.douban.pojo.Message;
import com.example.douban.service.OpenAiService;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@CrossOrigin(origins = {"http://localhost:8081", "http://localhost:8082"})
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

        String result = openAiService.chatMulti(payload.getMessages(), payload.getMode(),
                payload.getModeKey(), payload.getModeName(), payload.getContextText());
        return BaseResponse.success(result);
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody ChatPayload payload) {
        SseEmitter emitter = new SseEmitter(120000L);
        CompletableFuture.runAsync(() -> {
            try {
                openAiService.streamChatMulti(payload.getMessages(), payload.getMode(),
                        payload.getModeKey(), payload.getModeName(), payload.getContextText(),
                        chunk -> sendEvent(emitter, "chunk", Map.<String, Object>of("content", chunk)));
                sendEvent(emitter, "done", Map.<String, Object>of("done", true));
                emitter.complete();
            } catch (Exception e) {
                sendEvent(emitter, "error", Map.<String, Object>of("message", "模型服务暂时不可用，请检查 API Key、模型地址或网络配置。"));
                emitter.complete();
            }
        });
        return emitter;
    }

    private void sendEvent(SseEmitter emitter, String name, Map<String, Object> data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data));
        } catch (Exception ignored) {
        }
    }
    
    @Data
    public static class ChatPayload {
        private String msg; // For backward compatibility if needed
        private int mode;
        private String modeKey;
        private String modeName;
        private String contextText;
        private List<Message> messages;
    }
}
