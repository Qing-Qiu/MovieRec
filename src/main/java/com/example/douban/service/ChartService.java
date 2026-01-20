package com.example.douban.service;

import com.example.douban.mapper.ChartMapper;
import com.example.douban.pojo.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ChartService {
    private final ChartMapper chartMapper;

    @Autowired
    public ChartService(ChartMapper chartMapper) {
        this.chartMapper = chartMapper;
    }

    public ArrayList<String> getMovieYears() {
        return chartMapper.getMovieYears();
    }

    public Movie getMostPopularMovie(String year) {
        return chartMapper.getMostPopularMovie(year);
    }

    public Movie getMovieCountByTag(String year, String tag) {
        return chartMapper.getMovieCountByTag(year, tag);
    }
}
