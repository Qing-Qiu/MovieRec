package com.example.douban.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiToolCall {
    private String toolName;
    private String riskLevel;
    private String reason;
    private Map<String, Object> input = new LinkedHashMap<>();
}
