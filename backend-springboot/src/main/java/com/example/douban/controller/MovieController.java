package com.example.douban.controller;

import com.example.douban.pojo.Movie;
import com.example.douban.service.MovieService;
import jakarta.annotation.Resource;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:8081")
@RequestMapping(value = "/movie")
public class MovieController {
    @Resource
    MovieService movieService;

    @PostMapping("/recommend")
    public ResponseEntity<ArrayList<Movie>> handleOpenPage(@RequestBody Map<String, String> userData) {
        try {
            String nickname = userData.get("nickname");
            String userID = movieService.findUserIdByNickname(nickname);
            ArrayList<Movie> movies = new ArrayList<>();
            ArrayList<Movie> movies1 = new ArrayList<>();
            if (userID != null) {
                File modelFile = new File("src/main/resources/static/ratInfo2.txt");
                DataModel model = new FileDataModel(modelFile);
                ItemSimilarity similarity = new PearsonCorrelationSimilarity(model);
                Recommender recommender = new GenericItemBasedRecommender(model, similarity);
                List<RecommendedItem> recommendedItemList = recommender.recommend(Long.parseLong(userID),
                        16);
                for (RecommendedItem recommendedItem : recommendedItemList) {
                    String itemID = String.valueOf(recommendedItem.getItemID());
                    String movieID = movieService.findMovieIdById(itemID);
                    if (movieID == null) continue;
                    Movie movie = movieService.findMovieById(movieID);
                    if (movie == null) continue;
                    movies.add(movie);
                }
                if (movies.size() <= 4) {
                    movies1 = movies;
                } else {
                    ArrayList<Integer> list = new ArrayList<>();
                    for (int i = 0; i < movies.size(); i++)
                        list.add(i);
                    Random random = new Random();
                    for (int i = 1; i <= 4; i++) {
                        int index = random.nextInt(list.size());
                        movies1.add(movies.get(list.get(index)));
                        list.remove(index);
                    }
                }
            }
            movies = movies1;
            int len = movies.size();
            int cnt = movieService.countMovieByKeywords("");
            for (int i = 1; i <= 8 - len; i++) {
                Random random = new Random();
                int percent = random.nextInt(100) + 1;
                int offset;
                if (percent > 95) {
                    offset = random.nextInt((int) (cnt * 0.6));
                } else {
                    offset = (int) (cnt * 0.6) + random.nextInt(cnt - (int) (cnt * 0.6));
                }
                Movie movie = movieService.findRandomMovie(String.valueOf(offset));
                movies.add(movie);
            }
            Random random = new Random();
            for (int i = 7; i >= 0; i--) {
                int pos = random.nextInt(8);
                Movie tmp = movies.get(i);
                movies.set(i, movies.get(pos));
                movies.set(pos, tmp);
            }
            return ResponseEntity.ok(movies);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/detail")
    public ResponseEntity<Movie> handleDetailPage(@RequestBody Map<String, String> movieData) {
        try {
            Movie movie = movieService.findMovieById(movieData.get("movie"));
            return ResponseEntity.ok(movie);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/search")
    public ResponseEntity<ArrayList<Movie>> handleSearchPage(@RequestBody Map<String, String> movieData) {
        try {
            ArrayList<Movie> movies = movieService.findMovieByKeyWords(movieData.get("keyword"), movieData.get("limit"), movieData.get("offset"));
            return ResponseEntity.ok(movies);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/count2")
    public ResponseEntity<Integer> handleCount2Page(@RequestBody Map<String, String> movieData) {
        try {
            Integer cnt = movieService.countMovieByKeywords(movieData.get("keyword"));
            return ResponseEntity.ok(cnt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/classify")
    public ResponseEntity<ArrayList<Movie>> handleClassifyPage(@RequestBody Map<String, String> movieData) {
        try {
            ArrayList<Movie> movies = movieService.findMovieByTag(
                    movieData.get("tag1"), movieData.get("tag2"), movieData.get("tag3"), movieData.get("limit"), movieData.get("offset"));
            return ResponseEntity.ok(movies);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/count")
    public ResponseEntity<Integer> handleCountPage(@RequestBody Map<String, String> movieData) {
        try {
            Integer cnt = movieService.countMovieByTag(
                    movieData.get("tag1"), movieData.get("tag2"), movieData.get("tag3"));
            return ResponseEntity.ok(cnt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }
}
