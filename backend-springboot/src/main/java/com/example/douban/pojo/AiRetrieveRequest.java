package com.example.douban.pojo;

import lombok.Data;

@Data
public class AiRetrieveRequest {
    private String query;
    private String mode;
    private String nickname;
    private Integer limit;
}
