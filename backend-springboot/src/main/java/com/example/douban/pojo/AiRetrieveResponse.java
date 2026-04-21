package com.example.douban.pojo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiRetrieveResponse {
    private String query;
    private String keyword;
    private String mode;
    private String contextText;
    private List<AiContextItem> references = new ArrayList<>();
    private List<String> retrievalSteps = new ArrayList<>();
}
