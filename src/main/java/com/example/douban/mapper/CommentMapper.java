package com.example.douban.mapper;

import com.example.douban.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Mapper
@Repository
public interface CommentMapper {
    ArrayList<Comment> findCommentByMovie(@Param("id") String id, @Param("limit") String limit, @Param("offset") String offset);

    Integer countCommentByMovie(@Param("id") String id);

    Integer appendComment(@Param("userID") String userID, @Param("nickname") String nickname,
                          @Param("comment") String comment, @Param("movieID") String movieID);
}
