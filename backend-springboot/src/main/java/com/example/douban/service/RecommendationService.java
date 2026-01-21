package com.example.douban.service;

import jakarta.annotation.PostConstruct;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RecommendationService {

    private Recommender recommender;

    @PostConstruct
    public void init() {
        try {
            // Use ClassPathResource to load the file from resources
            Resource resource = new ClassPathResource("static/ratInfo2.txt");
            
            // Mahout's FileDataModel requires a real File object.
            // When running from a JAR, we need to copy the resource to a temporary file.
            File modelFile;
            try {
                modelFile = resource.getFile();
            } catch (IOException e) {
                // If getFile() fails (e.g. inside JAR), copy to temp file
                modelFile = File.createTempFile("ratInfo2", ".txt");
                modelFile.deleteOnExit();
                try (InputStream inputStream = resource.getInputStream();
                     FileOutputStream outputStream = new FileOutputStream(modelFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            }

            DataModel model = new FileDataModel(modelFile);
            ItemSimilarity similarity = new PearsonCorrelationSimilarity(model);
            this.recommender = new GenericItemBasedRecommender(model, similarity);
            System.out.println("Recommendation model initialized successfully.");

        } catch (Exception e) {
            System.err.println("Failed to initialize recommendation model: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<RecommendedItem> recommend(Long userId, int count) {
        if (recommender == null) {
            return Collections.emptyList();
        }
        try {
            return recommender.recommend(userId, count);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
