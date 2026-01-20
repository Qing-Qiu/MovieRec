package com.example.douban.mapper;

import com.example.douban.pojo.Movie;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Mapper
@Repository
public interface MovieMapper {
    Movie findMovieById(@Param("id") String id);

    Movie findRandomMovie(@Param("offset") String offset);

    ArrayList<Movie> findMovieByTag(@Param("tag1") String tag1, @Param("tag2") String tag2, @Param("tag3") String tag3, @Param("limit") String limit, @Param("offset") String offset);

    Integer countMovieByTag(@Param("tag1") String tag1, @Param("tag2") String tag2, @Param("tag3") String tag3);

    ArrayList<Movie> findMovieByKeywords(@Param("keyword") String keyword, @Param("limit") String limit, @Param("offset") String offset);

    Integer countMovieByKeywords(@Param("keyword") String keyword);

    String findUserIdByNickname(@Param("nickname") String nickname);

    String findMovieIdById(@Param("id") String id);

    ArrayList<String> findMovieByNickname(@Param("nickname") String nickname);

    ArrayList<String> findMovieByNickname2(@Param("nickname") String nickname);
}
