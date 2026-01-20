package com.example.douban.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Movie {
    private String movieID;
    private String name;
    private String director;
    private String actor;
    private String tag;
    private String genre;
    private String summary;
    private String rate;
    private String popular;
    private String year;
    private String region;
    private String img;
}
