package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.dto.LoginRequest;
import cn.edu.ccst.manpower_resource.dto.RegisterRequest;
import cn.edu.ccst.manpower_resource.security.LoginUser;
import cn.edu.ccst.manpower_resource.service.AuthService;
import cn.edu.ccst.manpower_resource.vo.LoginVO;
import cn.edu.ccst.manpower_resource.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "登录、注册、获取用户信息等")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        LoginVO vo = authService.login(request, ip);
        return Result.success(vo);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserInfoVO> getCurrentUser() {
        UserInfoVO vo = authService.getCurrentUser();
        return Result.success(vo);
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    @Operation(summary = "调试：检查用户角色和权限")
    @GetMapping("/debug/roles")
    public Result<java.util.Map<String, Object>> debugRoles(
        @org.springframework.security.core.annotation.AuthenticationPrincipal LoginUser loginUser) {
        java.util.Map<String, Object> debugInfo = new java.util.HashMap<>();
        try {
            debugInfo.put("username", loginUser.getUsername());
            debugInfo.put("roles", loginUser.getRoles());
            debugInfo.put("permissions", loginUser.getPermissions());
            debugInfo.put("authorities", loginUser.getAuthorities());
            debugInfo.put("userId", loginUser.getUserId());

            // 检查是否有 ADMIN 或 HR 角色
            java.util.List<String> roles = loginUser.getRoles();
            boolean hasAdmin = roles.stream().anyMatch(r -> r != null && r.toUpperCase().equals("ADMIN"));
            boolean hasHR = roles.stream().anyMatch(r -> r != null && r.toUpperCase().equals("HR"));
            debugInfo.put("hasADMIN", hasAdmin);
            debugInfo.put("hasHR", hasHR);
        } catch (Exception e) {
            debugInfo.put("error", e.getMessage());
            debugInfo.put("stackTrace", e.toString());
        }
        return Result.success(debugInfo);
    }

    @Operation(summary = "调试：测试招聘管理接口权限")
    @GetMapping("/debug/recruit-permission")
    public Result<String> testRecruitPermission() {
        return Result.success("你有权限访问招聘管理接口");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
