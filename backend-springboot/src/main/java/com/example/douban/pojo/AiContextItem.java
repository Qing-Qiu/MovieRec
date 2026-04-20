package com.example.douban.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiContextItem {
    private String type;
    private String typeLabel;
    private String id;
    private String title;
    private String meta;
    private String description;
    private String image;
    private String route;
}
