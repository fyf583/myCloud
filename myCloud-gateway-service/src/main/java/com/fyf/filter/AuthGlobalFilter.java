package com.fyf.filter;

import com.fyf.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
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
            "/auth/captcha",
            // 相册图片静态访问：浏览器 <img> 标签无法携带 token 请求头，
            // 因此该前缀放行（文件名是 UUID，不可枚举）
            "/album/file/",
            // 文件下载：浏览器 <a download> 无法携带 token 请求头，
            // 同样用 UUID 文件名保证不可枚举
            "/file/download/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 放行浏览器的跨域预检请求（OPTIONS），预检请求不会携带 token
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return chain.filter(exchange);
        }

        // 放行
        if(EXCLUDE_PATH.stream().anyMatch(path::startsWith)){
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst("token");
        if(token == null || token.isEmpty()){
            return writeResponse(exchange,401,"未登录");
        }
        try{
            Long userId = JwtUtil.getUserIdFromToken(token);

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
