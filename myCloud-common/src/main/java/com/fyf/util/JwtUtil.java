package com.fyf.util;

import com.fyf.exception.BaseException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET ="woshidachengxuyuanussicforever999hekurmnhn";
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000;
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    public static String createToken(Long userId){
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims parseToken(String token){
        try{
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token);
            return jws.getBody();
        }catch (ExpiredJwtException e){
            throw new BaseException("token expired");
        }catch (SignatureException e){
            throw new BaseException("token signature error");
        }catch (Exception e){
            throw new BaseException("token error");
        }
    }

    public static Long getUserIdFromToken(String token){
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    public static boolean validateToken(String token){
        try{
            parseToken(token);
            return true;
        }catch (ExpiredJwtException e){
            // token过期
            return false;
        }catch (MalformedJwtException e){
            // token格式错误
            return false;
        }catch (SignatureException e){
            // 签名被篡改
            return false;
        }catch (Exception e){
            return false;
        }
    }
}
