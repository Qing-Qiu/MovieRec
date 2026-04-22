package com.example.douban.service;

import com.example.douban.pojo.Message;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Consumer;

@Service
public class OpenAiService {
    private static final int MAX_CONTEXT_CHARS = 4200;
    private static final int MAX_CONTEXT_LINE_CHARS = 360;
    private static final int MAX_HISTORY_MESSAGES = 8;

    @Resource
    private RestTemplate restTemplate;

    @Value("${openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public String chatSingle(String content) {
        return chatMulti(Collections.singletonList(new Message("user", content)), 1, null, null, null);
    }

    public String chatMulti(List<Message> messages, int mode) {
        return chatMulti(messages, mode, null, null, null);
    }

    public String chatMulti(List<Message> messages, int mode, String modeKey, String modeName, String contextText) {
        String url = normalizeBaseUrl() + "/chat/completions";
        HttpHeaders headers = buildHeaders();
        ChatRequest request = new ChatRequest(model, prepareMessages(messages, mode, modeKey, modeName, contextText), false);
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

    public void streamChatMulti(List<Message> messages, int mode, String modeKey, String modeName,
                                String contextText, Consumer<String> chunkConsumer) {
        try {
            ChatRequest request = new ChatRequest(model, prepareMessages(messages, mode, modeKey, modeName, contextText), true);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(normalizeBaseUrl() + "/chat/completions"))
                    .timeout(Duration.ofSeconds(120))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request), StandardCharsets.UTF_8));
            if (apiKey != null && !apiKey.isBlank()) {
                builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
            }

            HttpResponse<java.util.stream.Stream<String>> response =
                    httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofLines());
            if (response.statusCode() >= 400) {
                chunkConsumer.accept("模型服务暂时不可用，请检查 API Key、模型地址或网络配置。");
                return;
            }

            try (java.util.stream.Stream<String> lines = response.body()) {
                lines.forEach(line -> handleStreamLine(line, chunkConsumer));
            }
        } catch (Exception e) {
            e.printStackTrace();
            chunkConsumer.accept("模型服务暂时不可用，请检查 API Key、模型地址或网络配置。");
        }
    }

    private void handleStreamLine(String line, Consumer<String> chunkConsumer) {
        if (line == null || line.isBlank() || !line.startsWith("data:")) {
            return;
        }
        String data = line.substring(5).trim();
        if ("[DONE]".equals(data)) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(data);
            JsonNode content = root.path("choices").path(0).path("delta").path("content");
            if (!content.isMissingNode() && !content.asText().isEmpty()) {
                chunkConsumer.accept(content.asText());
            }
        } catch (Exception ignored) {
        }
    }

    private List<Message> prepareMessages(List<Message> messages, int mode, String modeKey, String modeName, String contextText) {
        List<Message> prepared = new ArrayList<>();
        prepared.add(new Message("system", buildSystemPrompt(resolveModeKey(mode, modeKey), modeName, contextText)));
        if (messages == null) {
            return prepared;
        }
        int start = Math.max(0, messages.size() - MAX_HISTORY_MESSAGES);
        for (Message message : messages.subList(start, messages.size())) {
            if (message == null || message.getContent() == null || message.getContent().isBlank()) {
                continue;
            }
            String role = message.getRole();
            if ("user".equals(role) || "assistant".equals(role)) {
                prepared.add(new Message(role, message.getContent()));
            }
        }
        return prepared;
    }

    private String buildSystemPrompt(String modeKey, String modeName, String contextText) {
        String context = compactContext(contextText);
        String instruction = switch (modeKey) {
            case "knowledge" -> "你正在进行电影知识库问答。优先使用给定的电影、影人和评论数据；如果数据不足，要明确说明。";
            case "recommend" -> "你正在担任电影推荐助手。先总结用户偏好，再基于数据线索给出推荐理由。推荐候选会在页面卡片中展示，文字回答要解释为什么适合。";
            case "chart" -> "你正在解读 MovieRec 的可视化数据。回答应围绕趋势、对比、异常点和可能原因组织，不要编造未给出的数值。";
            default -> "你正在进行开放式电影问答。回答应围绕电影、影人、推荐、评论和观影体验展开。";
        };
        String displayMode = modeName == null || modeName.isBlank() ? modeKey : modeName;
        return String.join("\n",
                "你是 MovieRec 的智能电影助手，只使用中文回答。",
                "当前模式：" + displayMode + "。",
                instruction,
                "回答限制：不要输出思考过程，不要输出 <think> 标签；先给结论，再给依据。",
                "本地小模型优先策略：回答控制在 3-6 条要点内，严格贴合可用数据线索。",
                "项目中的电影评分统一按十分制理解，满分是 10 分。",
                "回答要具体、克制，不要编造数据库里没有给出的片名、人物、评分或评论。",
                "如果需要项目数据而上下文不足，请说明还需要进一步检索。",
                "可用数据线索：",
                context
        );
    }

    private String compactContext(String contextText) {
        if (contextText == null || contextText.isBlank()) {
            return "暂无数据库上下文。";
        }
        String normalized = contextText.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= MAX_CONTEXT_CHARS) {
            return contextText;
        }
        String[] lines = contextText.split("\\R+");
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.replaceAll("\\s+", " ").trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.length() > MAX_CONTEXT_LINE_CHARS) {
                trimmed = trimmed.substring(0, MAX_CONTEXT_LINE_CHARS) + "...";
            }
            if (builder.length() + trimmed.length() + 1 > MAX_CONTEXT_CHARS) {
                break;
            }
            if (!builder.isEmpty()) {
                builder.append('\n');
            }
            builder.append(trimmed);
        }
        if (builder.isEmpty()) {
            return normalized.substring(0, Math.min(MAX_CONTEXT_CHARS, normalized.length())) + "...";
        }
        builder.append("\n[上下文已压缩，仅保留最相关数据线索。]");
        return builder.toString();
    }

    private String resolveModeKey(int mode, String modeKey) {
        if (modeKey != null && !modeKey.isBlank()) {
            return modeKey;
        }
        return mode == 2 ? "knowledge" : "ask";
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isBlank()) {
            headers.setBearerAuth(apiKey);
        }
        return headers;
    }

    private String normalizeBaseUrl() {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "https://api.openai.com/v1";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @Data
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class ChatRequest {
        private String model;
        private List<Message> messages;
        private Boolean stream;
        private Double temperature;
        @JsonProperty("top_p")
        private Double topP;

        ChatRequest(String model, List<Message> messages, Boolean stream) {
            this.model = model;
            this.messages = messages;
            this.stream = stream;
            this.temperature = 0.25;
            this.topP = 0.85;
        }
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
