package com.fyf.filter;

import com.fyf.util.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 解析网关透传的 X-User-Id 请求头，写入 UserContext(ThreadLocal)。
 * 请求结束后统一清理，避免线程池复用导致不同请求之间串号。
 */
@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = "jakarta.servlet.http.HttpServletRequest")
public class UserContextFilter extends OncePerRequestFilter implements Ordered {
    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String userIdHeader = request.getHeader(USER_ID_HEADER);
        try {
            if (userIdHeader != null && !userIdHeader.isBlank() && !"null".equalsIgnoreCase(userIdHeader)) {
                try {
                    UserContext.setCurrentUserId(Long.valueOf(userIdHeader));
                } catch (NumberFormatException ignored) {
                    // 非法请求头不填充，由具体业务方法自行校验登录状态
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            UserContext.remove();
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
