package com.example.douban.service;

import com.example.douban.pojo.Message;
import com.example.douban.pojo.Movie;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OpenAiService {

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private MovieService movieService;

    @Value("${openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;

    public String chatSingle(String content) {
        // Deprecated, wrapper for multi
        return chatMulti(Collections.singletonList(new Message("user", content)), 1);
    }

    public String chatMulti(List<Message> messages, int mode) {
        String url = baseUrl + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isEmpty()) {
            headers.setBearerAuth(apiKey);
        }

        // RAG Logic
        if (mode == 2 && !messages.isEmpty()) {
            Message lastMsg = messages.get(messages.size() - 1);
            if ("user".equals(lastMsg.getRole())) {
                String potentialMovieName = extractMovieName(lastMsg.getContent());
                if (potentialMovieName != null && !potentialMovieName.isEmpty()) {
                    ArrayList<Movie> movies = movieService.findMovieByKeyWords(potentialMovieName, "1", "0");
                    if (movies != null && !movies.isEmpty()) {
                        Movie m = movies.get(0);
                        String context = String.format("【已知信息】电影名：《%s》，导演：%s，主演：%s，评分：%s(满分5分)，类型：%s。",
                                m.getName(), m.getDirector(), m.getActor(), m.getRate(), m.getGenre());
                        // Insert system message at the beginning
                        messages.add(0, new Message("system", "你是一个电影专家。利用以下已知信息回答用户问题。" + context));
                    }
                }
            }
        }

        ChatRequest request = new ChatRequest(model, messages);
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

    private String extractMovieName(String content) {
        // 1. Try to find 《...》
        Pattern pattern = Pattern.compile("《(.*?)》");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // 2. Fallback: If short enough, assume it's a name? Or just return null to be safe.
        // For better experience, let's just use the query itself if it's short.
        if (content.length() < 10) {
            return content;
        }
        return null;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ChatRequest {
        private String model;
        private List<Message> messages;
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
