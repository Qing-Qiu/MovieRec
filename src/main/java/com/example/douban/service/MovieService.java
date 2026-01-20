package com.example.douban.service;

import com.example.douban.mapper.MovieMapper;
import com.example.douban.pojo.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class MovieService {
    private final MovieMapper movieMapper;

    @Autowired
    public MovieService(MovieMapper movieMapper) {
        this.movieMapper = movieMapper;
    }

    public Movie findMovieById(String id) {
        return movieMapper.findMovieById(id);
    }

    public Movie findRandomMovie(String offset) {
        return movieMapper.findRandomMovie(offset);
    }

    public ArrayList<Movie> findMovieByTag(String tag1, String tag2, String tag3, String limit, String offset) {
        return movieMapper.findMovieByTag(tag1, tag2, tag3, limit, offset);
    }

    public Integer countMovieByTag(String tag1, String tag2, String tag3) {
        return movieMapper.countMovieByTag(tag1, tag2, tag3);
    }

    public ArrayList<Movie> findMovieByKeyWords(String keyword, String limit, String offset) {
        return movieMapper.findMovieByKeywords(keyword, limit, offset);
    }

    public Integer countMovieByKeywords(String keyword) {
        return movieMapper.countMovieByKeywords(keyword);
    }

    public String findUserIdByNickname(String nickname) {
        return movieMapper.findUserIdByNickname(nickname);
    }

    public String findMovieIdById(String id) {
        return movieMapper.findMovieIdById(id);
    }

    public ArrayList<String> findMovieByNickname(String nickname) {
        return movieMapper.findMovieByNickname(nickname);
    }

    public ArrayList<String> findMovieByNickname2(String nickname) {
        return movieMapper.findMovieByNickname2(nickname);
    }
}
