package com.fyf.util;

import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.UUID;

public class CaptchaUtil {

    public static String createCaptcha(){
        String chars="abcdefghijklmnopqrstuvwxyz123456789";
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for(int i=0;i<6;i++){
            int index= random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    public static String createCaptchaKey(){
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return uuid;
    }

}
