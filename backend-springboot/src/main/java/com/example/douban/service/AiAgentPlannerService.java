package com.example.douban.service;

import com.example.douban.pojo.AiAgentPlan;
import com.example.douban.pojo.AiToolCall;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AiAgentPlannerService {
    private static final String RISK_READ = "read";

    @Resource
    private AiToolService aiToolService;

    public AiAgentPlan plan(String query, String mode, String keyword, int limit) {
        AiAgentPlan plan = new AiAgentPlan();
        plan.setQuery(query);
        plan.setMode(mode);
        plan.setKeyword(keyword);
        plan.setCommentIntent(aiToolService.hasCommentIntent(query));
        plan.setPersonPrimary("knowledge".equals(mode) && aiToolService.hasPersonPrimaryIntent(query));

        addPlanningStep(plan, "先判断需要查哪些资料。");
        if ("chart".equals(mode)) {
            addTool(plan, "retrieveCharts", "查看图表相关数据，方便解释趋势",
                    Map.of("query", query));
            return plan;
        }
        if (!"knowledge".equals(mode) && !"recommend".equals(mode)) {
            addPlanningStep(plan, "这个问题更适合直接交流，不强制查资料库。");
            return plan;
        }

        if (plan.isPersonPrimary()) {
            addTool(plan, "retrievePerson", "先确认相关影人资料",
                    Map.of("query", query, "keyword", keyword, "limit", Math.min(3, limit)));
            addTool(plan, "retrievePersonWorks", "再看看这位影人的代表作品",
                    Map.of("keyword", keyword, "limit", 6));
            return plan;
        }

        addTool(plan, "retrieveMovie", "先找和问题最相关的电影",
                Map.of("query", query, "keyword", keyword, "mode", mode, "limit", limit));
        addTool(plan, "retrievePerson", "再补充可能相关的影人",
                Map.of("query", query, "keyword", keyword, "limit", Math.min(3, limit)));
        addTool(plan, "retrieveMovieCast", "查看首部相关电影的主创阵容",
                Map.of("source", "firstMovie"));
        if (plan.isCommentIntent()) {
            addTool(plan, "retrieveComments", "这个问题提到评价或口碑，补充观众评论",
                    Map.of("source", "firstMovie", "limit", 20));
        }
        addTool(plan, "retrievePersonWorks", "如果找到影人，再看看代表作品",
                Map.of("source", "firstPerson", "limit", 6));
        return plan;
    }

    private void addTool(AiAgentPlan plan, String toolName, String reason, Map<String, Object> input) {
        plan.getToolCalls().add(new AiToolCall(toolName, RISK_READ, reason, new LinkedHashMap<>(input)));
        addPlanningStep(plan, "准备：" + toolLabel(toolName) + "。");
    }

    private void addPlanningStep(AiAgentPlan plan, String step) {
        if (step != null && !step.isBlank()) {
            plan.getPlanningSteps().add(step.trim());
        }
    }

    private String toolLabel(String toolName) {
        return switch (toolName) {
            case "retrieveMovie" -> "找相关电影";
            case "retrievePerson" -> "找相关影人";
            case "retrieveMovieCast" -> "查看主创阵容";
            case "retrievePersonWorks" -> "查看代表作品";
            case "retrieveComments" -> "查看观众评价";
            case "retrieveCharts" -> "查看图表线索";
            default -> "补充资料";
        };
    }
}
