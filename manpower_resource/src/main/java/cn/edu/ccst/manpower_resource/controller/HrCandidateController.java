package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.dto.CandidateRegisterRequest;
import cn.edu.ccst.manpower_resource.dto.CandidateProfileDTO;
import cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory;
import cn.edu.ccst.manpower_resource.entity.HrCandidate;
import cn.edu.ccst.manpower_resource.mapper.CandidateProfileHistoryMapper;
import cn.edu.ccst.manpower_resource.mapper.HrCandidateMapper;
import cn.edu.ccst.manpower_resource.service.IHrCandidateService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
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
@Slf4j
public class HrCandidateController {

    private final IHrCandidateService candidateService;
    private final HrCandidateMapper candidateMapper;
    private final CandidateProfileHistoryMapper candidateProfileHistoryMapper;

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
    public Result<cn.edu.ccst.manpower_resource.vo.CandidateProfileVO> getProfile() {
        Object authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = extractUsername(authentication);
        cn.edu.ccst.manpower_resource.vo.CandidateProfileVO profile = candidateService.getProfile(username);
        return Result.success(profile);
    }

    @Operation(summary = "更新求职者个人信息")
    @PutMapping("/profile")
    public Result<Void> updateProfile(
            @Valid @RequestBody CandidateProfileDTO request) {
        Object authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = extractUsername(authentication);
        candidateService.updateProfile(username, request);
        return Result.success();
    }

    @Operation(summary = "获取求职者个人信息修改历史")
    @GetMapping("/profile/history")
    public Result<java.util.List<cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory>> getProfileHistory() {
        Object authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = extractUsername(authentication);
        log.info("获取求职者历史记录，username: {}", username);
        java.util.List<cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory> historyList = candidateService.getProfileHistory(username);
        log.info("返回历史记录，数量: {}", historyList != null ? historyList.size() : 0);
        return Result.success(historyList);
    }

    @Operation(summary = "测试：获取当前求职者信息")
    @GetMapping("/test/current")
    public Result<cn.edu.ccst.manpower_resource.vo.CandidateProfileVO> getCurrentCandidateInfo() {
        Object authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = extractUsername(authentication);
        log.info("测试：获取当前求职者信息，username: {}", username);

        // 查询求职者
        HrCandidate candidate = candidateMapper.selectOne(
                new LambdaQueryWrapper<HrCandidate>()
                        .eq(HrCandidate::getUsername, username)
        );

        if (candidate == null) {
            log.warn("测试：未找到求职者");
            return Result.fail(404, "求职者不存在");
        }

        log.info("测试：找到求职者，candidateId: {}, deleted: {}", candidate.getId(), candidate.getDeleted());

        cn.edu.ccst.manpower_resource.vo.CandidateProfileVO vo = new cn.edu.ccst.manpower_resource.vo.CandidateProfileVO();
        vo.setUsername(candidate.getUsername());
        vo.setRealName(candidate.getRealName());
        vo.setPhone(candidate.getPhone());
        vo.setEmail(candidate.getEmail());
        vo.setGender(candidate.getGender());
        vo.setIdCard(candidate.getIdCard());
        vo.setEducation(candidate.getEducation());
        vo.setGraduateSchool(candidate.getGraduateSchool());
        vo.setMajor(candidate.getMajor());
        vo.setWorkExperience(candidate.getWorkExperience());
        vo.setExpectedSalary(candidate.getExpectedSalary());
        vo.setExpectedPosition(candidate.getExpectedPosition());
        vo.setResumeUrl(candidate.getResumeUrl());

        return Result.success(vo);
    }

    @Operation(summary = "测试：获取求职者的历史记录")
    @GetMapping("/test/history")
    public Result<java.util.List<cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory>> getCandidateHistoryTest() {
        Object authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = extractUsername(authentication);
        log.info("测试：获取求职者历史记录，username: {}", username);

        // 先根据用户名获取求职者ID
        HrCandidate candidate = candidateMapper.selectOne(
                new LambdaQueryWrapper<HrCandidate>()
                        .eq(HrCandidate::getUsername, username)
        );

        if (candidate == null) {
            log.warn("测试：未找到求职者，username: {}", username);
            return Result.fail(404, "求职者不存在");
        }

        // 查询求职者的历史记录
        java.util.List<cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory> historyList = candidateProfileHistoryMapper.selectList(
                new LambdaQueryWrapper<cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory>()
                        .eq(cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory::getCandidateId, candidate.getId())
                        .eq(cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory::getDeleted, 0)
                        .orderByDesc(cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory::getSubmitTime)
        );

        log.info("测试：获取求职者历史记录，数量: {}", historyList != null ? historyList.size() : 0);

        return Result.success(historyList);
    }

    @Operation(summary = "测试：检查当前认证信息")
    @GetMapping("/test/auth")
    public Result<java.util.Map<String, Object>> checkAuth() {
        Object authentication = SecurityContextHolder.getContext().getAuthentication();
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("authenticationType", authentication != null ? authentication.getClass().getName() : "null");
        result.put("authentication", authentication);
        result.put("username", extractUsername(authentication));
        return Result.success(result);
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

    private String extractUsername(Object authentication) {
        log.info("[HrCandidateController] extractUsername called, authentication type: {}",
                authentication != null ? authentication.getClass().getName() : "null");
        if (authentication instanceof String) {
            log.info("[HrCandidateController] Authentication is String: {}", authentication);
            return (String) authentication;
        } else if (authentication instanceof cn.edu.ccst.manpower_resource.security.CandidateAuthenticationToken) {
            cn.edu.ccst.manpower_resource.security.CandidateAuthenticationToken token =
                    (cn.edu.ccst.manpower_resource.security.CandidateAuthenticationToken) authentication;
            String username = token.getName();
            log.info("[HrCandidateController] Authentication is CandidateAuthenticationToken, name: {}, userId: {}",
                    username, token.getUserId());
            return username;
        } else if (authentication instanceof cn.edu.ccst.manpower_resource.security.LoginUser) {
            cn.edu.ccst.manpower_resource.security.LoginUser loginUser =
                    (cn.edu.ccst.manpower_resource.security.LoginUser) authentication;
            String username = loginUser.getUsername();
            log.info("[HrCandidateController] Authentication is LoginUser, username: {}", username);
            return username;
        }
        log.error("[HrCandidateController] 无法从认证信息中获取用户名，认证类型: {}",
                (authentication != null ? authentication.getClass().getName() : "null"));
        throw new RuntimeException("无法从认证信息中获取用户名，认证类型: " +
                (authentication != null ? authentication.getClass().getName() : "null"));
    }
}
