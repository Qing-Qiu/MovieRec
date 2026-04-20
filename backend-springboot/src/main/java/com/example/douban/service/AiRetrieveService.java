package com.example.douban.service;

import com.example.douban.pojo.AiContextItem;
import com.example.douban.pojo.AiRetrieveRequest;
import com.example.douban.pojo.AiRetrieveResponse;
import com.example.douban.pojo.Comment;
import com.example.douban.pojo.Movie;
import com.example.douban.pojo.Person;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AiRetrieveService {
    private static final List<String> GENRES = List.of(
            "动作", "动画", "喜剧", "犯罪", "科幻", "历史", "音乐", "爱情", "悬疑", "惊悚", "剧情", "纪录片", "战争", "奇幻"
    );
    private static final List<String> REGIONS = List.of(
            "大陆", "香港", "台湾", "美国", "韩国", "日本", "俄罗斯", "印度", "泰国", "英国", "法国", "德国", "意大利", "西班牙"
    );
    private static final Pattern TITLE_PATTERN = Pattern.compile("《([^》]+)》");
    private static final Pattern YEAR_PATTERN = Pattern.compile("(19|20)\\d{2}");

    @Resource
    private MovieService movieService;

    @Resource
    private PersonService personService;

    @Resource
    private CommentService commentService;

    @Resource
    private ChartService chartService;

    public AiRetrieveResponse retrieve(AiRetrieveRequest request) {
        String query = clean(request.getQuery());
        String mode = clean(request.getMode());
        int limit = clamp(request.getLimit(), 5, 1, 8);
        String keyword = extractKeyword(query);

        AiRetrieveResponse response = new AiRetrieveResponse();
        response.setQuery(query);
        response.setMode(mode.isEmpty() ? "ask" : mode);
        response.setKeyword(keyword);

        if ("chart".equals(response.getMode())) {
            addChartItems(response.getReferences(), query, request.getNickname());
        } else if ("knowledge".equals(response.getMode()) || "recommend".equals(response.getMode())) {
            addMovieAndPersonItems(response.getReferences(), query, keyword, response.getMode(), limit);
        }

        response.setContextText(buildContextText(response.getReferences()));
        return response;
    }

    private void addMovieAndPersonItems(List<AiContextItem> references, String query, String keyword, String mode, int limit) {
        List<Movie> movies = findMovies(query, keyword, mode, limit);
        List<Person> persons = safePersonSearch(keyword, Math.min(3, limit));

        for (Movie movie : movies) {
            addUnique(references, movieItem(movie));
        }
        for (Person person : persons) {
            addUnique(references, personItem(person));
        }

        if (!movies.isEmpty() && hasCommentIntent(query)) {
            AiContextItem commentItem = commentInsightItem(movies.get(0), 20);
            if (commentItem != null) {
                addUnique(references, commentItem);
            }
        }
    }

    private List<Movie> findMovies(String query, String keyword, String mode, int limit) {
        if ("recommend".equals(mode)) {
            String genre = firstContained(query, GENRES);
            String region = firstContained(query, REGIONS);
            String year = extractYear(query);
            if (!genre.isEmpty() || !region.isEmpty() || !year.isEmpty()) {
                String entity = extractEntityKeyword(query);
                int candidateLimit = (!entity.isEmpty() || wantsHighScore(query)) ? Math.max(limit * 20, 60) : limit;
                ArrayList<Movie> movies = movieService.findMovieByTag(
                        genre.isEmpty() ? "全部" : genre,
                        year.isEmpty() ? "全部" : year,
                        region.isEmpty() ? "全部" : region,
                        candidateLimit,
                        0
                );
                if (movies == null) {
                    return new ArrayList<>();
                }
                if (wantsHighScore(query)) {
                    movies.sort(Comparator.comparingDouble(this::rateFromMovie).reversed());
                }
                if (!entity.isEmpty()) {
                    List<Movie> filtered = movies.stream()
                            .filter(movie -> movieContains(movie, entity))
                            .limit(limit)
                            .collect(Collectors.toList());
                    if (!filtered.isEmpty()) {
                        return filtered;
                    }
                }
                return movies.stream().limit(limit).collect(Collectors.toList());
            }
        }

        if (keyword.isEmpty()) {
            return new ArrayList<>();
        }
        ArrayList<Movie> movies = movieService.findMovieByKeyWords(keyword, limit, 0);
        return movies == null ? new ArrayList<>() : movies;
    }

    private List<Person> safePersonSearch(String keyword, int limit) {
        if (keyword.isEmpty()) {
            return new ArrayList<>();
        }
        ArrayList<Person> persons = personService.findPersonByKeywords(keyword, limit, 0);
        return persons == null ? new ArrayList<>() : persons;
    }

    private void addChartItems(List<AiContextItem> references, String query, String nickname) {
        addUnique(references, new AiContextItem(
                "chart", "图表", "popular-by-year", "历年最受欢迎电影", "年度趋势",
                "用于观察每一年最受欢迎影片与热度变化。", null, "/chart"
        ));
        addUnique(references, new AiContextItem(
                "chart", "图表", "genre-counts", "类型数量分布", "类型对比",
                "用于比较不同年份、不同类型电影的数量结构。", null, "/chart"
        ));
        addUnique(references, new AiContextItem(
                "profile", "画像", "user-profile", "我的画像", "用户偏好",
                "用于把用户高频类型转成可解释的推荐理由。", null, "/chart"
        ));

        String year = extractYear(query);
        if (!year.isEmpty()) {
            Movie popular = chartService.getMostPopularMovie(year);
            if (popular != null) {
                addUnique(references, new AiContextItem(
                        "chart", "年度热片", popular.getMovieID(), year + " 年最受欢迎电影",
                        popular.getName(), compactMovie(popular), popular.getImg(),
                        popular.getMovieID() == null ? "/chart" : "/movie/" + popular.getMovieID()
                ));
            }
            addUnique(references, new AiContextItem(
                    "chart", "年度类型", "genre-" + year, year + " 年类型数量",
                    "类型统计", topGenreSummary(chartService.getMovieTypeCounts(year)), null, "/chart"
            ));
        }

        String profileSummary = profileSummary(nickname);
        if (!profileSummary.isEmpty()) {
            addUnique(references, new AiContextItem(
                    "profile", "画像", "profile-" + nickname, nickname + " 的偏好画像",
                    "高频类型", profileSummary, null, "/chart"
            ));
        }
    }

    private AiContextItem movieItem(Movie movie) {
        return new AiContextItem(
                "movie",
                "电影",
                movie.getMovieID(),
                fallback(movie.getName(), "未命名电影"),
                joinParts(movie.getYear(), movie.getRegion(), movie.getGenre(), movie.getRate() == null ? "" : movie.getRate() + " 分"),
                truncate(firstNonBlank(movie.getSummary(), movie.getActor(), movie.getDirector()), 140),
                movie.getImg(),
                movie.getMovieID() == null ? "" : "/movie/" + movie.getMovieID()
        );
    }

    private AiContextItem personItem(Person person) {
        return new AiContextItem(
                "person",
                "影人",
                person.getPersonID(),
                fallback(person.getName(), "未命名影人"),
                joinParts(person.getRole(), person.getBirthday(), person.getBirthplace()),
                truncate(person.getSummary(), 140),
                person.getImg(),
                person.getPersonID() == null ? "" : "/person/" + person.getPersonID()
        );
    }

    public AiContextItem commentInsight(AiRetrieveRequest request) {
        String keyword = extractKeyword(clean(request.getQuery()));
        if (keyword.isEmpty()) {
            return null;
        }
        ArrayList<Movie> movies = movieService.findMovieByKeyWords(keyword, 1, 0);
        if (movies == null || movies.isEmpty()) {
            return null;
        }
        return commentInsightItem(movies.get(0), 30);
    }

    private AiContextItem commentInsightItem(Movie movie, int limit) {
        ArrayList<Comment> comments = commentService.findCommentByMovie(movie.getMovieID(), limit, 0);
        if (comments == null || comments.isEmpty()) {
            return null;
        }
        String samples = comments.stream()
                .map(Comment::getComment)
                .filter(item -> !clean(item).isEmpty())
                .limit(5)
                .collect(Collectors.joining(" / "));
        String description = "样本 " + comments.size() + " 条，" + sentimentSummary(comments) + "。代表评论：" + samples;
        return new AiContextItem(
                "comment",
                "评论洞察",
                movie.getMovieID(),
                movie.getName() + " 的评论洞察",
                comments.size() + " 条评论样本",
                truncate(description, 180),
                null,
                "/movie/" + movie.getMovieID()
        );
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

    private String extractKeyword(String query) {
        Matcher titleMatcher = TITLE_PATTERN.matcher(query);
        if (titleMatcher.find()) {
            return clean(titleMatcher.group(1));
        }
        int possessive = query.indexOf("的");
        if (possessive > 0 && possessive <= 8) {
            return clean(query.substring(0, possessive));
        }
        String compact = query
                .replaceAll("[\\s？?。！!，,、：:；;]", "")
                .replace("帮我", "")
                .replace("查询", "")
                .replace("总结", "")
                .replace("推荐", "")
                .replace("类似", "")
                .replace("相似", "")
                .replace("像", "")
                .replace("高分", "")
                .replace("评分高", "")
                .replace("口碑好", "")
                .replace("几部", "")
                .replace("电影", "")
                .replace("片", "")
                .replace("影人", "")
                .replace("资料", "")
                .replace("评论区", "")
                .replace("主要观点", "");
        return truncate(compact, 16);
    }

    private String extractYear(String query) {
        Matcher matcher = YEAR_PATTERN.matcher(query);
        return matcher.find() ? matcher.group() : "";
    }

    private String firstContained(String query, List<String> terms) {
        return terms.stream().filter(query::contains).findFirst().orElse("");
    }

    private boolean hasCommentIntent(String query) {
        return query.contains("评论") || query.contains("评价") || query.contains("口碑") || query.contains("观点");
    }

    private boolean wantsHighScore(String query) {
        return query.contains("高分") || query.contains("评分高") || query.contains("口碑好");
    }

    private boolean movieContains(Movie movie, String keyword) {
        String target = joinParts(movie.getName(), movie.getDirector(), movie.getActor(), movie.getGenre(), movie.getRegion());
        return target.contains(keyword);
    }

    private String extractEntityKeyword(String query) {
        String compact = query.replaceAll("[\\s？?。！!，,、：:；;]", "");
        for (String genre : GENRES) {
            compact = compact.replace(genre, "");
        }
        for (String region : REGIONS) {
            compact = compact.replace(region, "");
        }
        compact = compact
                .replace("推荐", "")
                .replace("几部", "")
                .replace("高分", "")
                .replace("评分高", "")
                .replace("口碑好", "")
                .replace("电影", "")
                .replace("片", "")
                .replace("节奏紧凑", "")
                .replace("最好", "");
        compact = compact.replaceAll("(19|20)\\d{2}", "");
        return truncate(compact, 12);
    }

    private String sentimentSummary(List<Comment> comments) {
        List<String> positiveWords = List.of("喜欢", "好看", "经典", "感动", "精彩", "推荐", "优秀", "震撼", "温暖", "神作");
        List<String> negativeWords = List.of("无聊", "失望", "烂", "尴尬", "拖沓", "不好", "一般", "差", "看不懂");
        int positive = 0;
        int negative = 0;
        for (Comment comment : comments) {
            String text = clean(comment.getComment());
            for (String word : positiveWords) {
                if (text.contains(word)) {
                    positive++;
                }
            }
            for (String word : negativeWords) {
                if (text.contains(word)) {
                    negative++;
                }
            }
        }
        if (positive == 0 && negative == 0) {
            return "评论倾向较分散";
        }
        return "正向关键词 " + positive + " 次，负向关键词 " + negative + " 次";
    }

    private String topGenreSummary(ArrayList<Movie> rows) {
        if (rows == null || rows.isEmpty()) {
            return "";
        }
        return rows.stream()
                .sorted(Comparator.comparingInt(this::countFromMovie).reversed())
                .limit(5)
                .map(row -> row.getGenre() + "：" + countFromMovie(row))
                .collect(Collectors.joining("，"));
    }

    private String profileSummary(String nickname) {
        if (clean(nickname).isEmpty()) {
            return "";
        }
        ArrayList<String> genres = chartService.getGenreRowsByNickname(nickname);
        if (genres == null || genres.isEmpty()) {
            return "";
        }
        Map<String, Integer> frequencies = new LinkedHashMap<>();
        for (String genre : genres) {
            String[] terms = clean(genre).split("[\\s,，、/]+");
            for (String term : terms) {
                if (!term.isBlank()) {
                    frequencies.merge(term, 1, Integer::sum);
                }
            }
        }
        return frequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(6)
                .map(entry -> entry.getKey() + "：" + entry.getValue())
                .collect(Collectors.joining("，"));
    }

    private int countFromMovie(Movie movie) {
        try {
            return Integer.parseInt(clean(movie.getMovieID()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double rateFromMovie(Movie movie) {
        try {
            return Double.parseDouble(clean(movie.getRate()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int clamp(Integer value, int fallback, int min, int max) {
        int result = value == null ? fallback : value;
        return Math.max(min, Math.min(max, result));
    }

    private String compactMovie(Movie movie) {
        return joinParts(movie.getGenre(), movie.getRegion(), movie.getRate() == null ? "" : movie.getRate() + " 分");
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

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!clean(value).isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private String fallback(String value, String fallback) {
        return clean(value).isEmpty() ? fallback : clean(value);
    }

    private String truncate(String value, int size) {
        String text = clean(value);
        return text.length() > size ? text.substring(0, size) + "..." : text;
    }

    private String clean(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }
}
