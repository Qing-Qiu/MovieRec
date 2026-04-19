package com.example.douban.controller;

import com.example.douban.pojo.Movie;
import com.example.douban.service.MovieService;
import com.example.douban.service.RecommendationService;
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

    private final MovieService movieService;
    private final RecommendationService recommendationService;

    @Autowired
    public MovieController(MovieService movieService, RecommendationService recommendationService) {
        this.movieService = movieService;
        this.recommendationService = recommendationService;
    }

    @PostMapping("/recommend")
    public ResponseEntity<ArrayList<Movie>> handleOpenPage(@RequestBody Map<String, String> userData) {
        try {
            String nickname = userData.get("nickname");
            String userId = movieService.findUserIdByNickname(nickname);
            
            ArrayList<Movie> finalMovies = new ArrayList<>();
            ArrayList<Movie> recommendedMovies = new ArrayList<>();
            Set<String> selectedMovieIds = new HashSet<>();

            // 1. Get Recommendations if User Exists
            if (userId != null) {
                try {
                    long uid = Long.parseLong(userId);
                    List<RecommendedItem> items = recommendationService.recommend(uid, 16);
                    
                    if (!items.isEmpty()) {
                        // Extract Item IDs (Sequence IDs)
                        List<String> sequenceIds = items.stream()
                                .map(item -> String.valueOf(item.getItemID()))
                                .collect(Collectors.toList());

                        // Batch convert Sequence IDs to Movie IDs (UUIDs)
                        List<String> movieIds = movieService.findMovieIdsBySequences(sequenceIds);

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

            // 2. Select up to 4 movies from recommendations
            recommendedMovies.stream()
                    .filter(Objects::nonNull)
                    .sorted(this::compareMovieQuality)
                    .forEach(movie -> {
                        if (finalMovies.size() < 4) {
                            addMovieIfNew(finalMovies, selectedMovieIds, movie);
                        }
                    });

            // 3. Fill the rest with weighted popular movies.
            int totalNeeded = 8;
            int totalParamMovies = movieService.countMovieByKeywords("");
            int attempts = 0;
            while (finalMovies.size() < totalNeeded && totalParamMovies > 0 && attempts < totalNeeded * 10) {
                Movie randomMovie = movieService.findRandomMovie(pickWeightedPopularOffset(totalParamMovies));
                addMovieIfNew(finalMovies, selectedMovieIds, randomMovie);
                attempts++;
            }

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

    private int pickWeightedPopularOffset(int totalMovies) {
        int topCount = Math.max(1, (int) Math.ceil(totalMovies * 0.30));
        int middleCount = Math.max(1, (int) Math.ceil(totalMovies * 0.50));
        int tailStart = Math.min(totalMovies - 1, topCount + middleCount);
        int bucket = ThreadLocalRandom.current().nextInt(100);

        if (bucket < 70) {
            return ThreadLocalRandom.current().nextInt(topCount);
        }
        if (bucket < 95 && tailStart > topCount) {
            return topCount + ThreadLocalRandom.current().nextInt(tailStart - topCount);
        }
        return tailStart + ThreadLocalRandom.current().nextInt(Math.max(1, totalMovies - tailStart));
    }

    private int compareMovieQuality(Movie left, Movie right) {
        int scoreCompare = Double.compare(movieQualityScore(right), movieQualityScore(left));
        if (scoreCompare != 0) {
            return scoreCompare;
        }
        return Integer.compare(parseInt(right.getPopular()), parseInt(left.getPopular()));
    }

    private double movieQualityScore(Movie movie) {
        return parseDouble(movie.getRate()) * 1000 + Math.log10(parseInt(movie.getPopular()) + 1);
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
}
