package com.example.douban.mapper;

import com.example.douban.pojo.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface AccountMapper {
//    @Select("SELECT * FROM account WHERE username= #{username}")
    Account findAccountByUsername(@Param("id") String id);

//    @Update("UPDATE account SET password= #{password} WHERE username= #{username}")
    int updateAccount(Account account);
    int insertAccount(Account account);
    String getNewAccountId();
}
