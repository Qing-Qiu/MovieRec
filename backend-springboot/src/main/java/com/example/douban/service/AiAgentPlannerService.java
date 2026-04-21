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

        addPlanningStep(plan, "Agent Planner 生成工具计划。");
        if ("chart".equals(mode)) {
            addTool(plan, "retrieveCharts", "读取图表解读所需的年度、类型和画像线索",
                    Map.of("query", query));
            return plan;
        }
        if (!"knowledge".equals(mode) && !"recommend".equals(mode)) {
            addPlanningStep(plan, "当前模式不需要数据库工具，直接进入开放问答。");
            return plan;
        }

        if (plan.isPersonPrimary()) {
            addTool(plan, "retrievePerson", "问题以影人资料或代表作品为主，先锁定影人",
                    Map.of("query", query, "keyword", keyword, "limit", Math.min(3, limit)));
            addTool(plan, "retrievePersonWorks", "影人主查询需要补充代表作品",
                    Map.of("keyword", keyword, "limit", 6));
            return plan;
        }

        addTool(plan, "retrieveMovie", "检索与问题最相关的电影候选",
                Map.of("query", query, "keyword", keyword, "mode", mode, "limit", limit));
        addTool(plan, "retrievePerson", "补充可能相关的影人候选",
                Map.of("query", query, "keyword", keyword, "limit", Math.min(3, limit)));
        addTool(plan, "retrieveMovieCast", "首个电影候选需要补充演职员线索",
                Map.of("source", "firstMovie"));
        if (plan.isCommentIntent()) {
            addTool(plan, "retrieveComments", "识别到评论/口碑意图，补充评论洞察",
                    Map.of("source", "firstMovie", "limit", 20));
        }
        addTool(plan, "retrievePersonWorks", "如命中影人，补充代表作品",
                Map.of("source", "firstPerson", "limit", 6));
        return plan;
    }

    private void addTool(AiAgentPlan plan, String toolName, String reason, Map<String, Object> input) {
        plan.getToolCalls().add(new AiToolCall(toolName, RISK_READ, reason, new LinkedHashMap<>(input)));
        addPlanningStep(plan, "计划工具：" + toolName + "。");
    }

    private void addPlanningStep(AiAgentPlan plan, String step) {
        if (step != null && !step.isBlank()) {
            plan.getPlanningSteps().add(step.trim());
        }
    }
}
