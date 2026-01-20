package com.example.douban.controller;

import com.example.douban.pojo.Comment;
import com.example.douban.service.CommentService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:8081")
@RequestMapping(value = "/comment")
public class CommentController {
    @Resource
    CommentService commentService;

    @PostMapping("/comment")
    public ResponseEntity<ArrayList<Comment>> handleCommentPage(@RequestBody Map<String, String> commentData) {
        try {
            ArrayList<Comment> comments = commentService.findCommentByMovie(
                    commentData.get("id"), commentData.get("limit"), commentData.get("offset"));
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/count")
    public ResponseEntity<Integer> handleCountPage(@RequestBody Map<String, String> commentData) {
        try {
            Integer cnt = commentService.countCommentByMovie(commentData.get("id"));
            return ResponseEntity.ok(cnt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/append")
    public ResponseEntity<Integer> handleAppendPage(@RequestBody Map<String, String> commentData) {
        try {
            Integer cnt = commentService.appendComment(commentData.get("userID"), commentData.get("nickname"),
                    commentData.get("comment"), commentData.get("movieID"));
            return ResponseEntity.ok(cnt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }
}
