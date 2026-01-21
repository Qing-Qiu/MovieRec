package com.example.douban.service;

import jakarta.annotation.Resource;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class OpenAiService {

    @Resource
    private RestTemplate restTemplate;

    @Value("${openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;

    public String chatSingle(String content) {
        String url = baseUrl + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isEmpty()) {
            headers.setBearerAuth(apiKey);
        }

        ChatRequest request = new ChatRequest(model, Collections.singletonList(new Message("user", content)));
        HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<ChatResponse> response = restTemplate.postForEntity(url, entity, ChatResponse.class);
            if (response.getBody() != null && response.getBody().getChoices() != null && !response.getBody().getChoices().isEmpty()) {
                return response.getBody().getChoices().get(0).getMessage().getContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling AI service: " + e.getMessage();
        }
        return "No response from AI service.";
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ChatRequest {
        private String model;
        private List<Message> messages;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Message {
        private String role;
        private String content;
    }

    @Data
    @NoArgsConstructor
    private static class ChatResponse {
        private List<Choice> choices;
    }

    @Data
    @NoArgsConstructor
    private static class Choice {
        private Message message;
    }
}
