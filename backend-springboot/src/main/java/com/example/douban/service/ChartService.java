package com.example.douban.service;

import com.example.douban.mapper.ChartMapper;
import com.example.douban.pojo.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ChartService {
    private final ChartMapper chartMapper;
    private static final String[][] TYPE_COLUMNS = {
            {"动作", "action_count"},
            {"动画", "animation_count"},
            {"喜剧", "comedy_count"},
            {"犯罪", "crime_count"},
            {"科幻", "science_count"},
            {"历史", "history_count"},
            {"音乐", "music_count"},
            {"爱情", "romance_count"},
            {"悬疑", "mystery_count"},
            {"惊悚", "thriller_count"},
            {"其它", "other_count"}
    };

    @Autowired
    public ChartService(ChartMapper chartMapper) {
        this.chartMapper = chartMapper;
    }

    public ArrayList<String> getMovieYears() {
        return chartMapper.getMovieYears();
    }

    public ArrayList<Movie> getMostPopularMoviesByYear() {
        ArrayList<Movie> movies = chartMapper.getMostPopularMoviesByYear();
        Map<String, Movie> moviesByYear = new LinkedHashMap<>();
        for (Movie movie : movies) {
            moviesByYear.putIfAbsent(movie.getYear(), movie);
        }
        return new ArrayList<>(moviesByYear.values());
    }

    public Movie getMostPopularMovie(String year) {
        return chartMapper.getMostPopularMovie(year);
    }

    public ArrayList<Movie> getMovieCountsByTagByYear(String tag) {
        return chartMapper.getMovieCountsByTagByYear(tag);
    }

    public ArrayList<Movie> getMovieTypeCounts(String year) {
        Map<String, Object> row = chartMapper.getMovieTypeCountRow(year);
        ArrayList<Movie> movies = new ArrayList<>();
        for (String[] typeColumn : TYPE_COLUMNS) {
            Movie movie = new Movie();
            movie.setGenre(typeColumn[0]);
            movie.setMovieID(String.valueOf(readCount(row, typeColumn[1])));
            movie.setYear(year);
            movies.add(movie);
        }
        return movies;
    }

    public ArrayList<String> getGenreRowsByNickname(String nickname) {
        return chartMapper.getGenreRowsByNickname(nickname);
    }

    public Movie getMovieCountByTag(String year, String tag) {
        return chartMapper.getMovieCountByTag(year, tag);
    }

    private int readCount(Map<String, Object> row, String key) {
        if (row == null) {
            return 0;
        }
        Object value = row.get(key);
        if (value == null) {
            value = row.get(key.toUpperCase());
        }
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }
}
