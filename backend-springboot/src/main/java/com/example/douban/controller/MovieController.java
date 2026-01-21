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
@CrossOrigin(origins = "http://localhost:8081")
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
            if (recommendedMovies.size() <= 4) {
                finalMovies.addAll(recommendedMovies);
            } else {
                Collections.shuffle(recommendedMovies);
                finalMovies.addAll(recommendedMovies.subList(0, 4));
            }

            // 3. Fill the rest with random movies (Logic preserved/cleaned)
            int currentSize = finalMovies.size();
            int totalNeeded = 8;
            int needed = totalNeeded - currentSize;
            
            if (needed > 0) {
                int totalParamMovies = movieService.countMovieByKeywords("");
                int top60PercentCount = (int) (totalParamMovies * 0.6);
                
                for (int i = 0; i < needed; i++) {
                    int percent = ThreadLocalRandom.current().nextInt(100) + 1;
                    int offset;
                    
                    // 5% chance -> Top 60% popular (Low offset)
                    // 95% chance -> Bottom 40% popular (High offset)
                    if (percent > 95) {
                         offset = ThreadLocalRandom.current().nextInt(top60PercentCount);
                    } else {
                         offset = top60PercentCount + ThreadLocalRandom.current().nextInt(totalParamMovies - top60PercentCount);
                    }
                    
                    Movie randomMovie = movieService.findRandomMovie(String.valueOf(offset));
                    if (randomMovie != null) {
                        finalMovies.add(randomMovie);
                    }
                }
            }

            // 4. Shuffle final result
            Collections.shuffle(finalMovies);

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
            ArrayList<Movie> movies = movieService.findMovieByKeyWords(movieData.get("keyword"), movieData.get("limit"), movieData.get("offset"));
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
            ArrayList<Movie> movies = movieService.findMovieByTag(
                    movieData.get("tag1"), movieData.get("tag2"), movieData.get("tag3"), movieData.get("limit"), movieData.get("offset"));
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
}
