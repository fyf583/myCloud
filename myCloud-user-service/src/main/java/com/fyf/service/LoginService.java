package com.fyf.service;

import com.fyf.dto.LoginDTO;
import com.fyf.dto.RegisterDTO;
import com.fyf.vo.CaptchaVO;

public interface LoginService {
    String login(LoginDTO loginDTO);

    void register(RegisterDTO registerDTO);

    void logout();

    CaptchaVO captcha();
}
