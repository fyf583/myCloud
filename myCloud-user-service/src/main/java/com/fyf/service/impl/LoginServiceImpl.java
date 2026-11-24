package com.fyf.service.impl;

import com.fyf.dto.LoginDTO;
import com.fyf.dto.RegisterDTO;
import com.fyf.entity.User;
import com.fyf.mapper.LoginMapper;
import com.fyf.service.LoginService;
import com.fyf.enums.UserStatusEnum;
import com.fyf.exception.LoginException;
import com.fyf.util.CaptchaUtil;
import com.fyf.vo.CaptchaVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.fyf.util.JwtUtil;
import com.fyf.util.UserContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private LoginMapper loginMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private DefaultRedisScript<Long> checkCaptchaScript;

    public static final String LOGIN_KEY_PREFIX = "login:token:";
    public static final String LOGIN_KEY_CAPTCHA = "login:captcha:";

    /**
     * 登录方法
     * 每次登录都会生成新 token，覆盖 Redis 中该用户的旧 token（实现单点登录，旧设备被踢下线）
     * @param loginDTO
     * @return
     */

    public String login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        if(username==null || username.equals("")){
            throw new LoginException("用户名不能为空");
        }
        String password = loginDTO.getPassword();
        if(password==null || password.equals("")){
            throw new LoginException("密码不能为空");
        }

        User user = loginMapper.selectUserByUsername(username);
        if(user==null){
            throw new LoginException("用户名或密码错误");
        }
        String realPassword = user.getPassword();
        // 校验密码
        if(!passwordEncoder.matches(password,realPassword)){
            throw new LoginException("用户名或密码错误");
        }

        // 生成token，存入Redis
        Integer userId = user.getId();
        String token = JwtUtil.createToken(userId);
        stringRedisTemplate.opsForValue().set(LOGIN_KEY_PREFIX+userId,token,7, TimeUnit.DAYS);

        return token;
    }

    /**
     * 注册方法
     * 注册完前端跳转login界面
     * @param registerDTO
     * @return
     */
    public void register(RegisterDTO  registerDTO) {
        String userName = registerDTO.getUsername();
        String password = registerDTO.getPassword();
        String password2 = registerDTO.getPassword2();
        if(!password.equals(password2)){
            throw new LoginException("两次密码不同");
        }
        String captcha = registerDTO.getCaptcha();
        String captchaKey = registerDTO.getCaptchaKey();
        Long result = stringRedisTemplate.execute(checkCaptchaScript,
                List.of(LOGIN_KEY_CAPTCHA + captchaKey),
                captcha);
        if (result == null || result != 1L) {
            throw new LoginException("验证码错误");
        }
        User user = User.builder()
                .username(userName)
                .password(passwordEncoder.encode(password))
                .phone(registerDTO.getPhone())
                .status(UserStatusEnum.NORMAL.getCode())
                .build();
        loginMapper.insertUser(user);
    }

    /**
     * 退出登录方法
     * 退出登录后删除token(Redis的也需要删除)
     */
    public void logout() {
        Long userId = UserContext.getCurrentUserId();
        if(userId==null){
            throw new LoginException("请先登录");
        }
        stringRedisTemplate.delete(LOGIN_KEY_PREFIX+userId);
        UserContext.remove();
    }

    /**
     * 获取验证码
     * 验证后记得删除
     * @return
     */
    public CaptchaVO captcha(){
        String captcha= CaptchaUtil.createCaptcha();
        String captchaKey= CaptchaUtil.createCaptchaKey();

        stringRedisTemplate.opsForValue().set(LOGIN_KEY_CAPTCHA+captchaKey,captcha,5, TimeUnit.MINUTES);
        CaptchaVO captchaVO=new CaptchaVO();
        captchaVO.setCaptcha(captcha);
        captchaVO.setCaptchaKey(captchaKey);
        return captchaVO;
    }
}
