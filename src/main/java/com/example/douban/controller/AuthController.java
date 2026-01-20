package com.example.douban.controller;

import com.example.douban.pojo.Account;
import com.example.douban.service.AccountService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@CrossOrigin(origins = "http://localhost:8081")
@RequestMapping(value = "/auth")
public class AuthController {
    @Resource
    AccountService accountService;

    @PostMapping("/login")
    public ResponseEntity<String[]> handleLoginFormSubmission(@RequestBody Map<String, String> formData) {
        try {
            String username = formData.get("username");
            String password = formData.get("password");
            Account account = accountService.findAccountByUsername(username);
            if (account == null) return ResponseEntity.ok(new String[]{"failed", "failed"});
            if (Objects.equals(account.getPassword(), password)) {
                return ResponseEntity.ok(new String[]{"success", account.getNickname()});
            } else return ResponseEntity.ok(new String[]{"failed", "failed"});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(new String[]{"failed", "failed"});
    }

    @PostMapping("/register")
    public ResponseEntity<String[]> handleRegisterFormSubmission(@RequestBody Map<String, String> formData) {
        try {
            String nickname = formData.get("nickname");
            String password = formData.get("password");
            String new_id = accountService.getNewAccountId();
            Account account1 = new Account(new_id, nickname, password);
            int cnt = accountService.insertAccount(account1);
            return ResponseEntity.ok(new String[]{"success", new_id});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(new String[]{"failed", "failed"});
    }
}
