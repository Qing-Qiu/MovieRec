package com.example.douban.mapper;

import com.example.douban.pojo.Movie;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Mapper
@Repository
public interface ChartMapper {
    ArrayList<String> getMovieYears();

    Movie getMostPopularMovie(@Param("year") String year);

    Movie getMovieCountByTag(@Param("year") String year, @Param("tag") String tag);
}
