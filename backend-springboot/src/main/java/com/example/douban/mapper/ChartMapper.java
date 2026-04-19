package com.example.douban.mapper;

import com.example.douban.pojo.Movie;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Map;

@Mapper
@Repository
public interface ChartMapper {
    ArrayList<String> getMovieYears();

    ArrayList<Movie> getMostPopularMoviesByYear();

    Movie getMostPopularMovie(@Param("year") String year);

    ArrayList<Movie> getMovieCountsByTagByYear(@Param("tag") String tag);

    Map<String, Object> getMovieTypeCountRow(@Param("year") String year);

    ArrayList<String> getGenreRowsByNickname(@Param("nickname") String nickname);

    Movie getMovieCountByTag(@Param("year") String year, @Param("tag") String tag);
}
