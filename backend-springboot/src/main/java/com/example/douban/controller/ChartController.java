package com.example.douban.controller;

import com.example.douban.pojo.Movie;
import com.example.douban.service.ChartService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:8081", "http://localhost:8082"})
@RequestMapping(value = "/chart")
public class ChartController {
    @Resource
    ChartService chartService;

    @PostMapping("/chart1")
    public ResponseEntity<ArrayList<Movie>> handleChart1(@RequestBody Map<String, String> data) {
        try {
            return ResponseEntity.ok(chartService.getMostPopularMoviesByYear());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/chart2")
    public ResponseEntity<ArrayList<Movie>> handleChart2(@RequestBody Map<String, String> data) {
        try {
            return ResponseEntity.ok(chartService.getMovieCountsByTagByYear(data.get("tag")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/year")
    public ResponseEntity<ArrayList<String>> handleYear(@RequestBody Map<String, String> data) {
        try {
            ArrayList<String> result = chartService.getMovieYears();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/chart3")
    public ResponseEntity<ArrayList<Movie>> handleChart3(@RequestBody Map<String, String> data) {
        try {
            return ResponseEntity.ok(chartService.getMovieTypeCounts(data.get("year")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/figure")
    public ResponseEntity<Map<String, Integer>> handleFigurePage(@RequestBody Map<String, String> userData) {
        try {
            String nickname = userData.get("nickname");
            Map<String, Integer> frequencies = new HashMap<>();
            ArrayList<String> genres = chartService.getGenreRowsByNickname(nickname);
            if (genres != null) {
                for (String genre : genres) {
                    addGenreTerms(frequencies, genre);
                }
            }

            return ResponseEntity.ok(frequencies);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

    private void addGenreTerms(Map<String, Integer> frequencies, String genre) {
        if (genre == null || genre.isBlank()) {
            return;
        }
        String[] terms = genre.split("[\\s,，、/]+");
        for (String term : terms) {
            if (!term.isBlank()) {
                frequencies.merge(term.trim(), 1, Integer::sum);
            }
        }
    }

}
