package com.example.douban.mapper;

import com.example.douban.pojo.Movie;
import com.example.douban.pojo.Person;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Mapper
@Repository
public interface PersonMapper {
    Person findPersonById(@Param("id") String id);

    ArrayList<Person> findPersonByMovie(@Param("id") String id, @Param("limit") Integer limit, @Param("offset") Integer offset);

    Integer countPersonByMovie(@Param("id") String id);

    ArrayList<Movie> findMovieByPerson(@Param("id") String id, @Param("limit") Integer limit, @Param("offset") Integer offset);

    Integer countMovieByPerson(@Param("id") String id);

    ArrayList<Person> findPersonByKeywords(@Param("keyword") String keyword, @Param("limit") Integer limit, @Param("offset") Integer offset);

    Integer countPersonByKeywords(@Param("keyword") String keyword);
}
