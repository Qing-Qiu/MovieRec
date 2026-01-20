package com.example.douban.service;

import com.example.douban.mapper.AccountMapper;
import com.example.douban.pojo.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final AccountMapper accountMapper;

    @Autowired
    public AccountService(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    public Account findAccountByUsername(String username) {
        return accountMapper.findAccountByUsername(username);
    }

    public int updateAccount(Account account) {
        return accountMapper.updateAccount(account);
    }

    public int insertAccount(Account account) {
        return accountMapper.insertAccount(account);
    }

    public String getNewAccountId() {
        return accountMapper.getNewAccountId();
    }
}
