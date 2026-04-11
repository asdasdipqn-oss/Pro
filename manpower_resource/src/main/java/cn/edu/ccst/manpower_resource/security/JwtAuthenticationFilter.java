package cn.edu.ccst.manpower_resource.security;

import cn.edu.ccst.manpower_resource.util.JwtUtil;
import cn.edu.ccst.manpower_resource.security.CandidateAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        log.info("[JwtAuthenticationFilter] Request URI: {}, Context Path: {}, Has token: {}", requestURI, contextPath, StringUtils.hasText(token));

        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsername(token);
            log.info("[JwtAuthenticationFilter] JWT Token valid for user: {}", username);

            // 检查是否是求职者请求
            boolean isCandidate = isCandidateRequest(request);
            log.info("[JwtAuthenticationFilter] isCandidateRequest: {}, Request URI: {}", isCandidate, requestURI);

            if (isCandidate) {
                log.info("[JwtAuthenticationFilter] Candidate request detected, creating candidate authentication for: {}", username);
                // 求职者请求：创建求职者认证对象，包含userId
                Long userId = jwtUtil.getUserId(token);
                CandidateAuthenticationToken authentication =
                        new CandidateAuthenticationToken(username, userId,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CANDIDATE")));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("[JwtAuthenticationFilter] Set CandidateAuthenticationToken for user: {}, userId: {}", username, userId);
            } else {
                // 员工请求：从数据库加载用户信息
                log.info("[JwtAuthenticationFilter] Employee request detected, loading user from database for: {}", username);
                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (Exception e) {
                    // 如果从数据库加载用户失败，可能是求职者用户，创建简单认证
                    log.warn("[JwtAuthenticationFilter] Failed to load user from database for {}, assuming candidate user: {}", username, e.getMessage());
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_CANDIDATE")));
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 判断是否是求职者请求
     * 求职者使用专门的登录接口，token 是通过 /candidate/login 获取的
     * 所以只要判断是求职者相关的接口就可以
     */
    private boolean isCandidateRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        log.info("[isCandidateRequest] Checking URI: {}", uri);
        // 移除 context path（如 /api）
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
            log.info("[isCandidateRequest] After removing context path: {}", uri);
        }

        // 求职者相关的接口路径
        boolean isCandidatePath = uri.startsWith("/candidate/") ||
                                uri.startsWith("/recruit/jobs") || // 岗位列表（公开）
                                uri.startsWith("/recruit/resume/submit") || // 投递简历
                                uri.startsWith("/recruit/applications/my") || // 查询我的投递记录
                                uri.startsWith("/recruit/interview"); // 面试记录

        log.info("[isCandidateRequest] originalUri={}, processedUri={}, result={}",
                request.getRequestURI(), uri, isCandidatePath);
        return isCandidatePath;
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        // 优先从Header获取
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // SSE请求从URL参数获取token
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }

        return null;
    }
}
