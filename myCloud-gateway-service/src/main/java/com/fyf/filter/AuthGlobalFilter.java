package com.fyf.filter;

import com.fyf.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class AuthGlobalFilter implements GlobalFilter , Ordered {
    private static final List<String> EXCLUDE_PATH = List.of(
            "/auth/login",
            "/auth/register",
            "/auth/captcha"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 放行
        if(EXCLUDE_PATH.stream().anyMatch(path::startsWith)){
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst("token");
        if(token == null || token.isEmpty()){
            return writeResponse(exchange,401,"未登录");
        }
        try{
            Claims claims = JwtUtil.parseToken(token);
            Integer userId = claims.get("userId",Integer.class);

            ServerHttpRequest mutateRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .build();
            return chain.filter(exchange.mutate().request(mutateRequest).build());
        }catch (Exception e){
            return writeResponse(exchange,401,"token 无效或已过期");
        }
    }

    private Mono<Void> writeResponse(ServerWebExchange exchange,int code,String msg){
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type","application/json;charset=UTF-8");
        String body = "{\"code\":"+code+",\"msg\":\""+msg+"\"}";
        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
