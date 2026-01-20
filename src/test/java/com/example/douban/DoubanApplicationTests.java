package com.example.douban;

import com.example.douban.mapper.AccountMapper;
import org.apache.mahout.cf.taste.eval.*;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.eval.GenericRecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


@SpringBootTest
class DoubanApplicationTests {
    @Autowired
    DataSource dataSource;

    @Test
    void contextLoads() throws SQLException {
        System.out.println(dataSource.getClass());
        Connection connection = dataSource.getConnection();
        System.out.println(connection);
        connection.close();
    }

    @Autowired
    AccountMapper accountMapper;

    @Test
    public void toTest() throws Exception {
        File modelFile = new File("src/main/resources/static/ratInfo2.txt");
        DataModel model = new FileDataModel(modelFile);
        RecommenderBuilder recommenderBuilder = dataModel -> {
            ItemSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);
            return new GenericItemBasedRecommender(dataModel, similarity);
        };
        List<RecommendedItem> recommendedItemList = recommenderBuilder.buildRecommender(model).recommend(5, 16);
        RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
        double score = evaluator.evaluate(recommenderBuilder, null, model, 0.7, 1.0);
        System.out.println(score);
        RecommenderIRStatsEvaluator statsEvaluator = new GenericRecommenderIRStatsEvaluator();
        IRStatistics statistics = statsEvaluator.evaluate(recommenderBuilder,
                null, model, null, 16,
                GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD,
                1.0);
        System.out.println(statistics.getPrecision());
        System.out.println(statistics.getRecall());
        System.out.println(recommendedItemList.size());
        for (RecommendedItem recommendedItem : recommendedItemList) {
            System.out.println(recommendedItem);
        }
    }
}
