package com.fyf.controller;

import com.fyf.dto.LoginDTO;
import com.fyf.dto.RegisterDTO;
import com.fyf.service.LoginService;
import com.fyf.vo.CaptchaVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.fyf.result.Result;

@RestController
@Slf4j
@RequestMapping("/auth")
public class LoginController {
    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDTO loginDTO) {
        String token = loginService.login(loginDTO);
        return Result.success(token);
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        loginService.register(registerDTO);
        return Result.success();
    }

    @GetMapping("/captcha")
    public Result<CaptchaVO> captcha(){
        CaptchaVO captcha = loginService.captcha();
        return Result.success(captcha);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        loginService.logout();
        return Result.success();
    }
}
