package com.example.douban.service;

import com.example.douban.pojo.AiContextItem;
import com.example.douban.pojo.AiAgentPlan;
import com.example.douban.pojo.AiRetrieveRequest;
import com.example.douban.pojo.AiRetrieveResponse;
import com.example.douban.pojo.AiToolCall;
import com.example.douban.pojo.Movie;
import com.example.douban.pojo.Person;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiRetrieveService {
    @Resource
    private AiToolService aiToolService;

    @Resource
    private AiAgentPlannerService aiAgentPlannerService;

    public AiRetrieveResponse retrieve(AiRetrieveRequest request) {
        String query = clean(request == null ? "" : request.getQuery());
        String mode = clean(request == null ? "" : request.getMode());
        String nickname = request == null ? "" : request.getNickname();
        int limit = clamp(request == null ? null : request.getLimit(), 5, 1, 8);
        String keyword = aiToolService.extractKeyword(query);

        AiRetrieveResponse response = new AiRetrieveResponse();
        response.setQuery(query);
        response.setMode(mode.isEmpty() ? "ask" : mode);
        response.setKeyword(keyword);
        addStep(response.getRetrievalSteps(), "解析问题，提取关键词：" + (keyword.isEmpty() ? "无明确关键词" : keyword));

        AiAgentPlan plan = aiAgentPlannerService.plan(query, response.getMode(), keyword, limit);
        response.setToolCalls(plan.getToolCalls());
        response.getRetrievalSteps().addAll(plan.getPlanningSteps());
        executeToolPlan(plan, response.getReferences(), nickname, limit, response.getRetrievalSteps());

        response.setContextText(buildContextText(response.getReferences()));
        addStep(response.getRetrievalSteps(), "生成上下文：" + response.getReferences().size() + " 条数据线索。");
        return response;
    }

    private void executeToolPlan(AiAgentPlan plan, List<AiContextItem> references, String nickname, int limit, List<String> steps) {
        List<Movie> movies = new ArrayList<>();
        List<Person> persons = new ArrayList<>();

        for (AiToolCall toolCall : plan.getToolCalls()) {
            switch (toolCall.getToolName()) {
                case "retrieveCharts" -> {
                    aiToolService.retrieveCharts(references, plan.getQuery(), nickname);
                    addStep(steps, "执行 retrieveCharts：加载图表线索。");
                }
                case "retrieveMovie" -> {
                    movies = aiToolService.retrieveMovies(plan.getQuery(), plan.getKeyword(), plan.getMode(), limit);
                    addStep(steps, "执行 retrieveMovie：" + movies.size() + " 条。");
                    for (Movie movie : movies) {
                        addUnique(references, aiToolService.movieItem(movie));
                    }
                }
                case "retrievePerson" -> {
                    persons = aiToolService.retrievePersons(plan.getQuery(), plan.getKeyword(), Math.min(3, limit));
                    addStep(steps, "执行 retrievePerson：" + persons.size() + " 条。");
                    if (plan.isPersonPrimary() && !persons.isEmpty()) {
                        List<Person> narrowedPersons = aiToolService.narrowPrimaryPersons(persons, plan.getKeyword());
                        if (narrowedPersons.size() < persons.size()) {
                            addStep(steps, "聚焦最匹配影人：" + fallback(narrowedPersons.get(0).getName(), plan.getKeyword()));
                        }
                        persons = narrowedPersons;
                    }
                    for (Person person : persons) {
                        addUnique(references, aiToolService.personItem(person));
                    }
                }
                case "retrieveMovieCast" -> {
                    if (!movies.isEmpty()) {
                        AiContextItem castItem = aiToolService.retrieveMovieCast(movies.get(0));
                        addUnique(references, castItem);
                        if (castItem != null) {
                            addStep(steps, "执行 retrieveMovieCast：补充演职员线索。");
                        }
                    }
                }
                case "retrieveComments" -> {
                    if (!movies.isEmpty()) {
                        AiContextItem commentItem = aiToolService.retrieveComments(movies.get(0), 20);
                        addUnique(references, commentItem);
                        if (commentItem != null) {
                            addStep(steps, "执行 retrieveComments：补充评论洞察。");
                        }
                    }
                }
                case "retrievePersonWorks" -> {
                    if (!persons.isEmpty()) {
                        AiContextItem worksItem = aiToolService.retrievePersonWorks(persons.get(0));
                        addUnique(references, worksItem);
                        if (worksItem != null) {
                            addStep(steps, "执行 retrievePersonWorks：补充代表作品线索。");
                        }
                    }
                }
                default -> addStep(steps, "跳过未知工具：" + toolCall.getToolName());
            }
        }
    }

    public AiContextItem commentInsight(AiRetrieveRequest request) {
        return aiToolService.retrieveCommentInsight(clean(request == null ? "" : request.getQuery()));
    }

    private String buildContextText(List<AiContextItem> references) {
        if (references.isEmpty()) {
            return "暂无数据库上下文。";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < references.size(); i++) {
            AiContextItem item = references.get(i);
            builder.append('[').append(i + 1).append("] ")
                    .append(joinParts(item.getTypeLabel(), item.getTitle(), item.getMeta(), item.getDescription()));
            if (i < references.size() - 1) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    private void addUnique(List<AiContextItem> items, AiContextItem item) {
        if (item == null) {
            return;
        }
        boolean exists = items.stream().anyMatch(old ->
                clean(old.getType()).equals(clean(item.getType())) && clean(old.getId()).equals(clean(item.getId())));
        if (!exists) {
            items.add(item);
        }
    }

    private void addStep(List<String> steps, String step) {
        String text = clean(step);
        if (!text.isEmpty()) {
            steps.add(text);
        }
    }

    private int clamp(Integer value, int fallback, int min, int max) {
        int result = value == null ? fallback : value;
        return Math.max(min, Math.min(max, result));
    }

    private String joinParts(String... parts) {
        List<String> values = new ArrayList<>();
        for (String part : parts) {
            String cleaned = clean(part);
            if (!cleaned.isEmpty()) {
                values.add(cleaned);
            }
        }
        return String.join(" · ", values);
    }

    private String fallback(String value, String fallback) {
        return clean(value).isEmpty() ? fallback : clean(value);
    }

    private String clean(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }
}
