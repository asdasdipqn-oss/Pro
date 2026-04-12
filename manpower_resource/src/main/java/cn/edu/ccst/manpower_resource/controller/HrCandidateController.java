package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.dto.CandidateRegisterRequest;
import cn.edu.ccst.manpower_resource.dto.CandidateProfileDTO;
import cn.edu.ccst.manpower_resource.service.IHrCandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import cn.edu.ccst.manpower_resource.security.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 求职者控制器
 */
@Tag(name = "求职者管理", description = "求职者注册、登录、个人信息等")
@RestController
@RequestMapping("/candidate")
@RequiredArgsConstructor
public class HrCandidateController {

    private final IHrCandidateService candidateService;

    @Operation(summary = "求职者注册")
    @PostMapping("/register")
    public Result<Void> register(
            @Valid @RequestBody CandidateRegisterRequest request) {
        candidateService.register(request);
        return Result.success();
    }

    @Operation(summary = "求职者登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @RequestBody Map<String, String> loginRequest,
            HttpServletRequest httpRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        String ip = getClientIp(httpRequest);

        String token = candidateService.login(username, password, ip);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("tokenType", "Bearer");
        data.put("username", username);
        data.put("userType", "candidate");

        return Result.success(data);
    }

    @Operation(summary = "获取求职者个人信息")
    @GetMapping("/profile")
    public Result<cn.edu.ccst.manpower_resource.vo.CandidateProfileVO> getProfile(
            @AuthenticationPrincipal LoginUser loginUser) {
        cn.edu.ccst.manpower_resource.vo.CandidateProfileVO profile = candidateService.getProfile(loginUser.getUsername());
        return Result.success(profile);
    }

    @Operation(summary = "更新求职者个人信息")
    @PutMapping("/profile")
    public Result<Void> updateProfile(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @RequestBody CandidateProfileDTO request) {
        candidateService.updateProfile(loginUser.getUsername(), request);
        return Result.success();
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
