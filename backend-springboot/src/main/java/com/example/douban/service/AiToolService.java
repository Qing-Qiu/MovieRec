package com.example.douban.service;

import com.example.douban.pojo.AiContextItem;
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
public class AiToolService {
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

    public List<Movie> retrieveMovies(String query, String keyword, String mode, int limit) {
        List<Movie> movies = findMovies(query, keyword, mode, limit);
        if (movies.isEmpty()) {
            movies = findMoviesByKeywordCandidates(query, keyword, limit);
        }
        return movies;
    }

    public List<Person> retrievePersons(String query, String keyword, int limit) {
        Map<String, Person> persons = new LinkedHashMap<>();
        for (String candidate : keywordCandidates(query, keyword)) {
            ArrayList<Person> rows = personService.findPersonByKeywords(candidate, limit, 0);
            if (rows == null) {
                continue;
            }
            for (Person person : rows) {
                if (person != null && !clean(person.getPersonID()).isEmpty()) {
                    persons.putIfAbsent(person.getPersonID(), person);
                }
            }
            if (persons.size() >= limit) {
                break;
            }
        }
        return persons.values().stream().limit(limit).collect(Collectors.toList());
    }

    public List<Person> narrowPrimaryPersons(List<Person> persons, String keyword) {
        String target = clean(keyword);
        if (target.isEmpty()) {
            return persons;
        }
        List<Person> exactPersons = persons.stream()
                .filter(person -> {
                    String name = clean(person.getName());
                    return name.equals(target)
                            || name.startsWith(target + " ")
                            || name.startsWith(target + "　")
                            || name.startsWith(target);
                })
                .limit(1)
                .collect(Collectors.toList());
        return exactPersons.isEmpty() ? persons : exactPersons;
    }

    public AiContextItem movieItem(Movie movie) {
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

    public AiContextItem personItem(Person person) {
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

    public AiContextItem retrieveMovieCast(Movie movie) {
        if (movie == null || clean(movie.getMovieID()).isEmpty()) {
            return null;
        }
        ArrayList<Person> persons = personService.findPersonByMovie(movie.getMovieID(), 8, 0);
        if (persons == null || persons.isEmpty()) {
            return null;
        }
        String directors = persons.stream()
                .filter(person -> clean(person.getRole()).contains("导演"))
                .map(Person::getName)
                .map(this::clean)
                .filter(name -> !name.isEmpty())
                .distinct()
                .limit(3)
                .collect(Collectors.joining("、"));
        String actors = persons.stream()
                .filter(person -> clean(person.getRole()).contains("演员"))
                .map(Person::getName)
                .map(this::clean)
                .filter(name -> !name.isEmpty())
                .distinct()
                .limit(6)
                .collect(Collectors.joining("、"));
        String writers = persons.stream()
                .filter(person -> clean(person.getRole()).contains("编剧"))
                .map(Person::getName)
                .map(this::clean)
                .filter(name -> !name.isEmpty())
                .distinct()
                .limit(3)
                .collect(Collectors.joining("、"));
        String description = joinParts(
                directors.isEmpty() ? "" : "导演：" + directors,
                writers.isEmpty() ? "" : "编剧：" + writers,
                actors.isEmpty() ? "" : "演员：" + actors
        );
        if (description.isEmpty()) {
            return null;
        }
        return new AiContextItem(
                "cast",
                "演职员",
                "cast-" + movie.getMovieID(),
                fallback(movie.getName(), "未命名电影") + " 的演职员线索",
                "主创与主演",
                truncate(description, 180),
                null,
                "/movie/" + movie.getMovieID()
        );
    }

    public AiContextItem retrievePersonWorks(Person person) {
        if (person == null || clean(person.getPersonID()).isEmpty()) {
            return null;
        }
        ArrayList<Movie> movies = personService.findMovieByPerson(person.getPersonID(), 6, 0);
        if (movies == null || movies.isEmpty()) {
            return null;
        }
        String works = movies.stream()
                .map(this::compactWork)
                .filter(item -> !item.isEmpty())
                .limit(6)
                .collect(Collectors.joining("；"));
        if (works.isEmpty()) {
            return null;
        }
        return new AiContextItem(
                "work",
                "代表作品",
                "works-" + person.getPersonID(),
                fallback(person.getName(), "未命名影人") + " 的代表作品",
                movies.size() + " 部候选",
                truncate(works, 220),
                null,
                "/person/" + person.getPersonID()
        );
    }

    public AiContextItem retrieveComments(Movie movie, int limit) {
        if (movie == null || clean(movie.getMovieID()).isEmpty()) {
            return null;
        }
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

    public AiContextItem retrieveCommentInsight(String query) {
        String keyword = extractKeyword(clean(query));
        if (keyword.isEmpty()) {
            return null;
        }
        ArrayList<Movie> movies = movieService.findMovieByKeyWords(keyword, 1, 0);
        if (movies == null || movies.isEmpty()) {
            return null;
        }
        return retrieveComments(movies.get(0), 30);
    }

    public void retrieveCharts(List<AiContextItem> references, String query, String nickname) {
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

    public String extractKeyword(String query) {
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
                .replace("是谁", "")
                .replace("是什么", "")
                .replace("有哪些", "")
                .replace("讲了什么", "")
                .replace("高分", "")
                .replace("评分高", "")
                .replace("口碑好", "")
                .replace("几部", "")
                .replace("电影", "")
                .replace("片", "")
                .replace("影人", "")
                .replace("资料", "")
                .replace("简介", "")
                .replace("介绍", "")
                .replace("剧情", "")
                .replace("主演", "")
                .replace("导演", "")
                .replace("代表作品", "")
                .replace("作品", "")
                .replace("演职员", "")
                .replace("评论区", "")
                .replace("主要观点", "");
        return truncate(compact, 16);
    }

    public boolean hasCommentIntent(String query) {
        return query.contains("评论") || query.contains("评价") || query.contains("口碑") || query.contains("观点");
    }

    public boolean hasPersonPrimaryIntent(String query) {
        return query.contains("代表作品")
                || query.contains("作品")
                || query.contains("参演")
                || query.contains("出演")
                || query.contains("演过")
                || query.contains("影人资料")
                || query.contains("演员资料");
    }

    public String clean(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
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

    private List<Movie> findMoviesByKeywordCandidates(String query, String keyword, int limit) {
        Map<String, Movie> movies = new LinkedHashMap<>();
        for (String candidate : keywordCandidates(query, keyword)) {
            ArrayList<Movie> rows = movieService.findMovieByKeyWords(candidate, Math.min(limit, 5), 0);
            if (rows == null) {
                continue;
            }
            for (Movie movie : rows) {
                if (movie != null && !clean(movie.getMovieID()).isEmpty()) {
                    movies.putIfAbsent(movie.getMovieID(), movie);
                }
            }
            if (movies.size() >= limit) {
                break;
            }
        }
        return movies.values().stream().limit(limit).collect(Collectors.toList());
    }

    private List<String> keywordCandidates(String query, String keyword) {
        Map<String, Boolean> candidates = new LinkedHashMap<>();
        addCandidate(candidates, keyword, 24);

        Matcher titleMatcher = TITLE_PATTERN.matcher(query);
        while (titleMatcher.find()) {
            addCandidate(candidates, titleMatcher.group(1), 24);
        }

        String compact = query.replaceAll("[\\s？?。！!，,、：:；;]", "");
        List<String> stopWords = List.of(
                "帮我", "查询", "总结", "推荐", "类似", "相似", "像", "一下",
                "电影", "影片", "片子", "影人", "资料", "简介", "介绍", "剧情",
                "评论区", "评论", "评价", "口碑", "主要观点", "主要", "观点",
                "主演", "导演", "编剧", "演职员", "代表作品", "作品",
                "是谁", "是什么", "有哪些", "讲了什么", "为什么", "如何", "怎么",
                "高分", "评分高", "口碑好", "几部", "一些", "给我"
        );
        for (String stopWord : stopWords) {
            compact = compact.replace(stopWord, "");
        }
        compact = compact.replaceAll("(19|20)\\d{2}", "");
        addCandidate(candidates, compact, 24);

        int possessive = query.indexOf("的");
        if (possessive > 0 && possessive <= 12) {
            addCandidate(candidates, query.substring(0, possessive), 24);
        }
        String simplified = query
                .replace("帮我", "")
                .replace("查询", "")
                .replace("总结", "")
                .replace("介绍", "")
                .replace("一下", "")
                .replaceAll("[\\s？?。！!，,、：:；;]", "");
        int simplifiedPossessive = simplified.indexOf("的");
        if (simplifiedPossessive > 0 && simplifiedPossessive <= 12) {
            addCandidate(candidates, simplified.substring(0, simplifiedPossessive), 24);
        }

        return new ArrayList<>(candidates.keySet());
    }

    private void addCandidate(Map<String, Boolean> candidates, String value, int maxLength) {
        String cleaned = clean(value);
        String candidate = cleaned.length() > maxLength ? cleaned.substring(0, maxLength) : cleaned;
        if (candidate.length() >= 2) {
            candidates.putIfAbsent(candidate, true);
        }
    }

    private String extractYear(String query) {
        Matcher matcher = YEAR_PATTERN.matcher(query);
        return matcher.find() ? matcher.group() : "";
    }

    private String firstContained(String query, List<String> terms) {
        return terms.stream().filter(query::contains).findFirst().orElse("");
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

    private String compactMovie(Movie movie) {
        return joinParts(movie.getGenre(), movie.getRegion(), movie.getRate() == null ? "" : movie.getRate() + " 分");
    }

    private String compactWork(Movie movie) {
        if (movie == null) {
            return "";
        }
        return joinParts(
                fallback(movie.getName(), "未命名电影"),
                movie.getYear(),
                movie.getGenre(),
                movie.getRate() == null ? "" : movie.getRate() + " 分"
        );
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
}
