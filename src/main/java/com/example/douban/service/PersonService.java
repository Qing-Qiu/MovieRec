package com.example.douban.service;

import com.example.douban.mapper.PersonMapper;
import com.example.douban.pojo.Movie;
import com.example.douban.pojo.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class PersonService {
    private final PersonMapper personMapper;

    @Autowired
    public PersonService(PersonMapper personMapper) {
        this.personMapper = personMapper;
    }

    public Person findPersonById(String id) {
        return personMapper.findPersonById(id);
    }

    public ArrayList<Person> findPersonByMovie(String id, String limit, String offset) {
        return personMapper.findPersonByMovie(id, limit, offset);
    }

    public Integer countPersonByMovie(String id) {
        return personMapper.countPersonByMovie(id);
    }

    public ArrayList<Movie> findMovieByPerson(String id, String limit, String offset) {
        return personMapper.findMovieByPerson(id, limit, offset);
    }

    public Integer countMovieByPerson(String id) {
        return personMapper.countMovieByPerson(id);
    }

    public ArrayList<Person> findPersonByKeywords(String keyword, String limit, String offset) {
        return personMapper.findPersonByKeywords(keyword, limit, offset);
    }

    public Integer countPersonByKeywords(String keyword) {
        return personMapper.countPersonByKeywords(keyword);
    }
}
