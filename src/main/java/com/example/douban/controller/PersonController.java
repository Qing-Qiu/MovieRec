package com.example.douban.controller;

import com.example.douban.pojo.Movie;
import com.example.douban.pojo.Person;
import com.example.douban.service.PersonService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:8081")
@RequestMapping(value = "/person")
public class PersonController {
    @Resource
    PersonService personService;

    @PostMapping("/detail")
    public ResponseEntity<Person> handleDetailPage(@RequestBody Map<String, String> personData) {
        try {
            Person person = personService.findPersonById(personData.get("id"));
            return ResponseEntity.ok(person);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/relevant")
    public ResponseEntity<ArrayList<Person>> handleRelevantPage(@RequestBody Map<String, String> personData) {
        try {
            ArrayList<Person> persons = personService.findPersonByMovie(
                    personData.get("id"), personData.get("limit"), personData.get("offset"));
            return ResponseEntity.ok(persons);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/count")
    public ResponseEntity<Integer> handleCountPage(@RequestBody Map<String, String> personData) {
        try {
            Integer cnt = personService.countPersonByMovie(personData.get("id"));
            return ResponseEntity.ok(cnt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }
    @PostMapping("/relevant2")
    public ResponseEntity<ArrayList<Movie>> handleRelevant2Page(@RequestBody Map<String, String> personData) {
        try {
            ArrayList<Movie> movies = personService.findMovieByPerson(
                    personData.get("id"), personData.get("limit"), personData.get("offset"));
            return ResponseEntity.ok(movies);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/count2")
    public ResponseEntity<Integer> handleCount2Page(@RequestBody Map<String, String> personData) {
        try {
            Integer cnt = personService.countMovieByPerson(personData.get("id"));
            return ResponseEntity.ok(cnt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }
    @PostMapping("/search")
    public ResponseEntity<ArrayList<Person>> handleSearchPage(@RequestBody Map<String, String> personData) {
        try {
            ArrayList<Person> persons = personService.findPersonByKeywords(
                    personData.get("keyword"), personData.get("limit"), personData.get("offset"));
            return ResponseEntity.ok(persons);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }
    @PostMapping("/count3")
    public ResponseEntity<Integer> handleCount3Page(@RequestBody Map<String, String> personData) {
        try {
            Integer cnt = personService.countPersonByKeywords(
                    personData.get("keyword"));
            return ResponseEntity.ok(cnt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }
}
