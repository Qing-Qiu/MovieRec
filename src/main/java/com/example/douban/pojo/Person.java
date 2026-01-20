package com.example.douban.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Person {
    String personID;
    String name;
    String img;
    String sex;
    String birthday;
    String birthplace;
    String summary;
    String role;
}
