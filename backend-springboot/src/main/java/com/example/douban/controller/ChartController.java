package com.example.douban.controller;

import com.example.douban.mapper.MovieMapper;
import com.example.douban.pojo.Account;
import com.example.douban.pojo.Movie;
import com.example.douban.service.ChartService;
import com.example.douban.service.MovieService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wltea.analyzer.cfg.DefaultConfig;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:8081")
@RequestMapping(value = "/chart")
public class ChartController {
    @Resource
    ChartService chartService;
    @Resource
    MovieService movieService;

    @PostMapping("/chart1")
    public ResponseEntity<ArrayList<Movie>> handleChart1(@RequestBody Map<String, String> data) {
        try {
            ArrayList<String> result = chartService.getMovieYears();
            ArrayList<Movie> movies = new ArrayList<>();
            for (String year : result) {
                movies.add(chartService.getMostPopularMovie(year));
            }
            return ResponseEntity.ok(movies);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/chart2")
    public ResponseEntity<ArrayList<Movie>> handleChart2(@RequestBody Map<String, String> data) {
        try {
            ArrayList<String> result = chartService.getMovieYears();
            ArrayList<Movie> res = new ArrayList<>();
            for (String year : result) {
                Movie movie = chartService.getMovieCountByTag(year, data.get("tag"));
                if (movie.getMovieID().equals("0")) {
                    movie.setYear(year);
                }
                res.add(movie);
            }
            return ResponseEntity.ok(res);
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
            String[] types = new String[]{"动作", "动画", "喜剧", "犯罪"
                    , "科幻", "历史", "音乐", "爱情", "悬疑", "惊悚", "其它"};
            ArrayList<Movie> res = new ArrayList<>();
            for (String type : types) {
                Movie movie = chartService.getMovieCountByTag(data.get("year"), type);
                res.add(movie);
            }
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/figure")
    public ResponseEntity<Map<String, Integer>> handleFigurePage(@RequestBody Map<String, String> userData) {
        try {
            String nickname = userData.get("nickname");
            StringBuilder words = new StringBuilder();
            ArrayList<String> movieIds = movieService.findMovieByNickname(nickname);
            for (String movieId : movieIds) {
                Movie movie = movieService.findMovieById(movieId);
                if (movie == null) continue;
                words.append(movie.getGenre());
            }
            movieIds = movieService.findMovieByNickname2(nickname);
            for (String movieID : movieIds) {
                Movie movie = movieService.findMovieById(movieID);
                if (movie == null) continue;
                words.append(movie.getGenre());
            }
            Map<String, Integer> frequencies = new HashMap<>();
            DefaultConfig conf = new DefaultConfig();
            conf.setUseSmart(true);
            IKSegmenter segmenter = new IKSegmenter(new StringReader(words.toString()), conf);
            Lexeme lexeme;
            while ((lexeme = segmenter.next()) != null) {
                if (lexeme.getLexemeText().length() > 1) {
                    final String term = lexeme.getLexemeText();
                    frequencies.compute(term, (k, v) -> {
                        if (v == null) {
                            v = 1;
                        } else {
                            v += 1;
                        }
                        return v;
                    });
                }
            }
            return ResponseEntity.ok(frequencies);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

}
