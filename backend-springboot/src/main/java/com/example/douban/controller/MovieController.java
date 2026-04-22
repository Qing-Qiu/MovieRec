package com.example.douban.controller;

import com.example.douban.pojo.Movie;
import com.example.douban.service.MovieService;
import com.example.douban.service.RecommendationService;
import jakarta.annotation.PostConstruct;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = {"http://localhost:8081", "http://localhost:8082"})
@RequestMapping(value = "/movie")
public class MovieController {

    private static final int RECOMMENDATION_SIZE = 8;
    private static final int POPULAR_CANDIDATE_LIMIT = 600;
    private static final long POPULAR_CACHE_MILLIS = 5 * 60 * 1000L;

    private final MovieService movieService;
    private final RecommendationService recommendationService;
    private volatile List<Movie> popularFallbackCache = Collections.emptyList();
    private volatile long popularFallbackCacheExpiresAt = 0L;

    @Autowired
    public MovieController(MovieService movieService, RecommendationService recommendationService) {
        this.movieService = movieService;
        this.recommendationService = recommendationService;
    }

    @PostConstruct
    public void warmPopularFallbackCache() {
        try {
            getPopularFallbackCandidates();
        } catch (Exception e) {
            System.err.println("Failed to warm popular movie cache: " + e.getMessage());
        }
    }

    @PostMapping("/recommend")
    public ResponseEntity<ArrayList<Movie>> handleOpenPage(@RequestBody Map<String, String> userData) {
        try {
            String nickname = userData == null ? "" : Objects.toString(userData.get("nickname"), "");
            String userId = movieService.findUserIdByNickname(nickname);
            ArrayList<Movie> recommendedMovies = new ArrayList<>();
            Map<String, Double> collaborativeScores = new HashMap<>();
            PreferenceProfile profile = buildPreferenceProfile(nickname);

            // 1. Get Recommendations if User Exists
            if (userId != null) {
                try {
                    long uid = Long.parseLong(userId);
                    List<RecommendedItem> items = recommendationService.recommend(uid, 32);
                    
                    if (!items.isEmpty()) {
                        List<String> movieIds = new ArrayList<>();
                        for (RecommendedItem item : items) {
                            String movieId = movieService.findMovieIdById(String.valueOf(item.getItemID()));
                            if (movieId == null || movieId.isBlank()) {
                                continue;
                            }
                            movieIds.add(movieId);
                            collaborativeScores.merge(movieId, normalizeMahoutScore(item.getValue()), Math::max);
                        }

                        // Batch fetch Movies
                        List<Movie> fetchedMovies = movieService.findMoviesByIds(movieIds);
                        
                        // Add found movies to candidates
                        if (fetchedMovies != null) {
                            recommendedMovies.addAll(fetchedMovies);
                        }
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid User ID format: " + userId);
                }
            }

            // 2. Merge collaborative candidates with a cached popular candidate pool, then rerank.
            Map<String, Movie> candidates = new LinkedHashMap<>();
            addCandidates(candidates, recommendedMovies, profile.seenMovieIds);
            List<Movie> popularMovies = getPopularFallbackCandidates();
            addCandidates(candidates, popularMovies, profile.seenMovieIds);

            ArrayList<Movie> finalMovies = selectDiverseRecommendations(candidates, collaborativeScores, profile);

            return ResponseEntity.ok(finalMovies);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @PostMapping("/detail")
    public ResponseEntity<Movie> handleDetailPage(@RequestBody Map<String, String> movieData) {
        try {
            Movie movie = movieService.findMovieById(movieData.get("movie"));
            return ResponseEntity.ok(movie);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(null);
        }
    }

    @PostMapping("/search")
    public ResponseEntity<ArrayList<Movie>> handleSearchPage(@RequestBody Map<String, String> movieData) {
        try {
            int limit = RequestParams.readBoundedInt(movieData, "limit", 8, 1, 50);
            int offset = RequestParams.readBoundedInt(movieData, "offset", 0, 0, 100000);
            ArrayList<Movie> movies = movieService.findMovieByKeyWords(movieData.get("keyword"), limit, offset);
            return ResponseEntity.ok(movies);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(null);
        }
    }

    @PostMapping("/count2")
    public ResponseEntity<Integer> handleCount2Page(@RequestBody Map<String, String> movieData) {
        try {
            Integer cnt = movieService.countMovieByKeywords(movieData.get("keyword"));
            return ResponseEntity.ok(cnt);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(null);
        }
    }

    @PostMapping("/classify")
    public ResponseEntity<ArrayList<Movie>> handleClassifyPage(@RequestBody Map<String, String> movieData) {
        try {
            int limit = RequestParams.readBoundedInt(movieData, "limit", 8, 1, 50);
            int offset = RequestParams.readBoundedInt(movieData, "offset", 0, 0, 100000);
            ArrayList<Movie> movies = movieService.findMovieByTag(
                    movieData.get("tag1"), movieData.get("tag2"), movieData.get("tag3"), limit, offset);
            return ResponseEntity.ok(movies);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(null);
        }
    }

    @PostMapping("/count")
    public ResponseEntity<Integer> handleCountPage(@RequestBody Map<String, String> movieData) {
        try {
            Integer cnt = movieService.countMovieByTag(
                    movieData.get("tag1"), movieData.get("tag2"), movieData.get("tag3"));
            return ResponseEntity.ok(cnt);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(null);
        }
    }

    private boolean addMovieIfNew(ArrayList<Movie> movies, Set<String> selectedMovieIds, Movie movie) {
        if (movie == null || movie.getMovieID() == null || selectedMovieIds.contains(movie.getMovieID())) {
            return false;
        }
        selectedMovieIds.add(movie.getMovieID());
        movies.add(movie);
        return true;
    }

    private void addCandidates(Map<String, Movie> candidates, Collection<Movie> movies, Set<String> excludedMovieIds) {
        if (movies == null) {
            return;
        }
        for (Movie movie : movies) {
            if (movie == null || movie.getMovieID() == null || excludedMovieIds.contains(movie.getMovieID())) {
                continue;
            }
            candidates.putIfAbsent(movie.getMovieID(), copyMovie(movie));
        }
    }

    private ArrayList<Movie> selectDiverseRecommendations(Map<String, Movie> candidates,
                                                          Map<String, Double> collaborativeScores,
                                                          PreferenceProfile profile) {
        Map<String, Double> scores = new HashMap<>();
        for (Movie movie : candidates.values()) {
            scores.put(movie.getMovieID(), recommendationScore(movie, collaborativeScores, profile));
        }
        List<Movie> ranked = candidates.values().stream()
                .sorted((left, right) -> Double.compare(scores.get(right.getMovieID()), scores.get(left.getMovieID())))
                .collect(Collectors.toList());

        ArrayList<Movie> selected = new ArrayList<>();
        Set<String> selectedMovieIds = new HashSet<>();
        Map<String, Integer> genreCounts = new HashMap<>();
        Map<String, Integer> regionCounts = new HashMap<>();
        for (Movie movie : ranked) {
            if (selected.size() >= RECOMMENDATION_SIZE) {
                break;
            }
            if (!passesDiversity(movie, genreCounts, regionCounts)) {
                continue;
            }
            addSelectedMovie(selected, selectedMovieIds, genreCounts, regionCounts, movie, profile, collaborativeScores);
        }

        for (Movie movie : ranked) {
            if (selected.size() >= RECOMMENDATION_SIZE) {
                break;
            }
            addSelectedMovie(selected, selectedMovieIds, genreCounts, regionCounts, movie, profile, collaborativeScores);
        }
        return selected;
    }

    private void addSelectedMovie(ArrayList<Movie> selected,
                                  Set<String> selectedMovieIds,
                                  Map<String, Integer> genreCounts,
                                  Map<String, Integer> regionCounts,
                                  Movie movie,
                                  PreferenceProfile profile,
                                  Map<String, Double> collaborativeScores) {
        if (!addMovieIfNew(selected, selectedMovieIds, movie)) {
            return;
        }
        primaryTerms(movie.getGenre()).stream().findFirst().ifPresent(genre -> genreCounts.merge(genre, 1, Integer::sum));
        primaryTerms(movie.getRegion()).stream().findFirst().ifPresent(region -> regionCounts.merge(region, 1, Integer::sum));
        boolean collaborativeHit = collaborativeScores.containsKey(movie.getMovieID());
        List<String> tags = recommendTags(movie, profile, collaborativeHit);
        movie.setRecommendTags(tags);
        movie.setRecommendSource(tags.isEmpty() ? "新鲜发现" : tags.get(0));
        movie.setRecommendReason(recommendReason(movie, profile, collaborativeHit));
    }

    private boolean passesDiversity(Movie movie, Map<String, Integer> genreCounts, Map<String, Integer> regionCounts) {
        String genre = primaryTerms(movie.getGenre()).stream().findFirst().orElse("");
        String region = primaryTerms(movie.getRegion()).stream().findFirst().orElse("");
        return genreCounts.getOrDefault(genre, 0) < 3 && regionCounts.getOrDefault(region, 0) < 4;
    }

    private double recommendationScore(Movie movie, Map<String, Double> collaborativeScores, PreferenceProfile profile) {
        double cfScore = collaborativeScores.getOrDefault(movie.getMovieID(), 0.0);
        double ratingScore = Math.min(1.0, parseDouble(movie.getRate()) / 10.0);
        double popularityScore = Math.min(1.0, Math.log10(parseInt(movie.getPopular()) + 1) / 6.0);
        double profileScore = profile.match(movie);
        double popularPenalty = profile.hasSignals() && profileScore == 0 ? popularityScore * 0.08 : 0;
        double explorationJitter = profile.hasSignals() || cfScore > 0
                ? ThreadLocalRandom.current().nextDouble(0, 0.035)
                : ThreadLocalRandom.current().nextDouble(0, 0.14);
        return cfScore * 0.38
                + profileScore * 0.30
                + ratingScore * 0.22
                + popularityScore * 0.10
                - popularPenalty
                + explorationJitter;
    }

    private double normalizeMahoutScore(float score) {
        if (Float.isNaN(score) || Float.isInfinite(score)) {
            return 0;
        }
        return Math.max(0, Math.min(1, score / 5.0));
    }

    private String recommendReason(Movie movie, PreferenceProfile profile, boolean collaborativeHit) {
        List<String> matchedGenres = primaryTerms(movie.getGenre()).stream()
                .filter(profile.genreWeights::containsKey)
                .limit(2)
                .collect(Collectors.toList());
        if (collaborativeHit && !matchedGenres.isEmpty()) {
            return "和你常看的 " + String.join("、", matchedGenres) + " 类型接近，也被相似口味的用户喜欢。";
        }
        if (!matchedGenres.isEmpty()) {
            return "和你常看的 " + String.join("、", matchedGenres) + " 类型接近。";
        }
        if (collaborativeHit) {
            return "相似观影口味的用户也喜欢这部。";
        }
        if (parseDouble(movie.getRate()) >= 8.5) {
            return "评分表现稳定，适合放心加入片单。";
        }
        return "给片单加一点新鲜感，避免一直看到同类热门片。";
    }

    private List<String> recommendTags(Movie movie, PreferenceProfile profile, boolean collaborativeHit) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        boolean genreMatched = profile.matchesGenre(movie);
        boolean regionMatched = profile.matchesRegion(movie);
        boolean highScore = parseDouble(movie.getRate()) >= 8.5;

        if (collaborativeHit) {
            tags.add("相似口味");
        }
        if (genreMatched) {
            tags.add("口味相近");
        }
        if (regionMatched) {
            tags.add("常看地区");
        }
        if (highScore) {
            tags.add("高分佳片");
        }
        if (!collaborativeHit && !genreMatched && !regionMatched) {
            tags.add("新鲜发现");
        }
        return tags.stream().limit(3).collect(Collectors.toList());
    }

    private PreferenceProfile buildPreferenceProfile(String nickname) {
        PreferenceProfile profile = new PreferenceProfile();
        if (nickname == null || nickname.isBlank()) {
            return profile;
        }

        Set<String> rawIds = new LinkedHashSet<>();
        ArrayList<String> ratedIds = movieService.findMovieByNickname(nickname);
        ArrayList<String> commentIds = movieService.findMovieByNickname2(nickname);
        if (ratedIds != null) {
            rawIds.addAll(ratedIds);
        }
        if (commentIds != null) {
            rawIds.addAll(commentIds);
        }
        if (rawIds.isEmpty()) {
            return profile;
        }

        List<Movie> historyMovies = findMoviesFromPossibleIds(rawIds);
        for (Movie movie : historyMovies) {
            if (movie == null || movie.getMovieID() == null) {
                continue;
            }
            profile.seenMovieIds.add(movie.getMovieID());
            for (String genre : primaryTerms(movie.getGenre())) {
                profile.genreWeights.merge(genre, 1, Integer::sum);
            }
            for (String region : primaryTerms(movie.getRegion())) {
                profile.regionWeights.merge(region, 1, Integer::sum);
            }
        }
        return profile;
    }

    private List<Movie> findMoviesFromPossibleIds(Collection<String> rawIds) {
        List<String> ids = rawIds.stream().filter(Objects::nonNull).map(String::trim).filter(id -> !id.isEmpty()).limit(80).collect(Collectors.toList());
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Movie> movies = new LinkedHashMap<>();
        List<Movie> directMovies = movieService.findMoviesByIds(ids);
        for (Movie movie : directMovies) {
            if (movie != null && movie.getMovieID() != null) {
                movies.putIfAbsent(movie.getMovieID(), movie);
            }
        }
        List<String> convertedIds = movieService.findMovieIdsBySequences(ids);
        for (Movie movie : movieService.findMoviesByIds(convertedIds)) {
            if (movie != null && movie.getMovieID() != null) {
                movies.putIfAbsent(movie.getMovieID(), movie);
            }
        }
        return new ArrayList<>(movies.values());
    }

    private List<String> primaryTerms(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split("[\\s,，、/]+"))
                .map(String::trim)
                .filter(term -> !term.isEmpty() && !"全部".equals(term) && !"其它".equals(term))
                .distinct()
                .collect(Collectors.toList());
    }

    private Movie copyMovie(Movie movie) {
        Movie copy = new Movie();
        copy.setMovieID(movie.getMovieID());
        copy.setName(movie.getName());
        copy.setDirector(movie.getDirector());
        copy.setActor(movie.getActor());
        copy.setTag(movie.getTag());
        copy.setGenre(movie.getGenre());
        copy.setSummary(movie.getSummary());
        copy.setRate(movie.getRate());
        copy.setPopular(movie.getPopular());
        copy.setYear(movie.getYear());
        copy.setRegion(movie.getRegion());
        copy.setImg(movie.getImg());
        copy.setRecommendReason(movie.getRecommendReason());
        copy.setRecommendSource(movie.getRecommendSource());
        copy.setRecommendTags(movie.getRecommendTags());
        return copy;
    }

    private List<Movie> getPopularFallbackCandidates() {
        long now = System.currentTimeMillis();
        List<Movie> cached = popularFallbackCache;
        if (now < popularFallbackCacheExpiresAt && cached != null && !cached.isEmpty()) {
            return cached;
        }

        synchronized (this) {
            cached = popularFallbackCache;
            if (now < popularFallbackCacheExpiresAt && cached != null && !cached.isEmpty()) {
                return cached;
            }

            ArrayList<Movie> refreshed = movieService.findPopularMovies(POPULAR_CANDIDATE_LIMIT);
            popularFallbackCache = refreshed == null ? Collections.emptyList() : new ArrayList<>(refreshed);
            popularFallbackCacheExpiresAt = System.currentTimeMillis() + POPULAR_CACHE_MILLIS;
            return popularFallbackCache;
        }
    }

    private double parseDouble(String value) {
        try {
            return value == null ? 0 : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int parseInt(String value) {
        try {
            return value == null ? 0 : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static class PreferenceProfile {
        private final Set<String> seenMovieIds = new HashSet<>();
        private final Map<String, Integer> genreWeights = new HashMap<>();
        private final Map<String, Integer> regionWeights = new HashMap<>();

        private boolean hasSignals() {
            return !genreWeights.isEmpty() || !regionWeights.isEmpty();
        }

        private double match(Movie movie) {
            if (!hasSignals()) {
                return 0;
            }
            double genreScore = matchTerms(movie.getGenre(), genreWeights);
            double regionScore = matchTerms(movie.getRegion(), regionWeights) * 0.35;
            return Math.min(1.0, genreScore + regionScore);
        }

        private boolean matchesGenre(Movie movie) {
            return matchTerms(movie.getGenre(), genreWeights) > 0;
        }

        private boolean matchesRegion(Movie movie) {
            return matchTerms(movie.getRegion(), regionWeights) > 0;
        }

        private double matchTerms(String value, Map<String, Integer> weights) {
            if (value == null || weights.isEmpty()) {
                return 0;
            }
            int max = weights.values().stream().max(Integer::compareTo).orElse(1);
            double score = 0;
            for (String term : value.split("[\\s,，、/]+")) {
                Integer weight = weights.get(term.trim());
                if (weight != null) {
                    score = Math.max(score, weight * 1.0 / max);
                }
            }
            return score;
        }
    }
}
