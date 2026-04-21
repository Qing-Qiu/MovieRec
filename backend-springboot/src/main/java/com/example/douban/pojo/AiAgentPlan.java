package com.example.douban.pojo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiAgentPlan {
    private String query;
    private String mode;
    private String keyword;
    private boolean personPrimary;
    private boolean commentIntent;
    private List<AiToolCall> toolCalls = new ArrayList<>();
    private List<String> planningSteps = new ArrayList<>();

    public boolean hasTool(String toolName) {
        return toolCalls.stream().anyMatch(tool -> toolName.equals(tool.getToolName()));
    }
}
