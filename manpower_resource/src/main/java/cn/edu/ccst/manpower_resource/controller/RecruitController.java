package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.dto.JobApplicationRequest;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.service.IRecruitCandidateService;
import cn.edu.ccst.manpower_resource.vo.RecruitJobVO;
import cn.edu.ccst.manpower_resource.vo.RecruitApplicationVO;
import cn.edu.ccst.manpower_resource.security.LoginUser;
import cn.edu.ccst.manpower_resource.security.CandidateAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 招聘控制器（求职者相关）
 */
@Tag(name = "招聘管理", description = "岗位列表、投递简历等")
@RestController
@RequestMapping("/recruit")
@RequiredArgsConstructor
public class RecruitController {

    private final IRecruitCandidateService recruitService;

    @Operation(summary = "获取岗位列表")
    @GetMapping("/jobs")
    public Result<List<RecruitJobVO>> getJobs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        List<RecruitJobVO> result = recruitService.getJobList();
        return Result.success(result);
    }

    @Operation(summary = "获取岗位详情")
    @GetMapping("/jobs/{id}")
    public Result<RecruitJobVO> getJobDetail(
            @Parameter(description = "岗位ID") @PathVariable("id") Long id) {
        RecruitJobVO job = recruitService.getJobById(id);
        return Result.success(job);
    }

    @Operation(summary = "投递简历")
    @PostMapping("/applications")
    public Result<Void> applyJob(
            @Valid @RequestBody JobApplicationRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Object authentication) {
        System.out.println("[RecruitController] applyJob called, authentication type: " +
            (authentication != null ? authentication.getClass().getName() : "null"));

        Long jobSeekerId = null;

        if (authentication instanceof CandidateAuthenticationToken) {
            jobSeekerId = ((CandidateAuthenticationToken) authentication).getUserId();
            System.out.println("[RecruitController] Candidate auth detected, userId: " + jobSeekerId);
        } else if (authentication instanceof LoginUser) {
            jobSeekerId = ((LoginUser) authentication).getUserId();
            System.out.println("[RecruitController] Employee auth detected, userId: " + jobSeekerId);
        } else {
            System.out.println("[RecruitController] Unknown auth type: " +
                (authentication != null ? authentication.getClass().getName() : "null"));
            throw new RuntimeException("Unable to get user ID from authentication");
        }

        recruitService.applyJob(request, jobSeekerId);
        return Result.success();
    }

    @Operation(summary = "获取我的投递记录")
    @GetMapping("/applications/my")
    public Result<java.util.List<RecruitApplicationVO>> getMyApplications(
            @org.springframework.security.core.annotation.AuthenticationPrincipal Object authentication) {
        System.out.println("[RecruitController] getMyApplications called, authentication type: " +
            (authentication != null ? authentication.getClass().getName() : "null"));

        Long jobSeekerId = null;

        if (authentication instanceof CandidateAuthenticationToken) {
            jobSeekerId = ((CandidateAuthenticationToken) authentication).getUserId();
            System.out.println("[RecruitController] Candidate auth detected, userId: " + jobSeekerId);
        } else if (authentication instanceof LoginUser) {
            jobSeekerId = ((LoginUser) authentication).getUserId();
            System.out.println("[RecruitController] Employee auth detected, userId: " + jobSeekerId);
        } else {
            System.out.println("[RecruitController] Unknown auth type: " +
                (authentication != null ? authentication.getClass().getName() : "null"));
            throw new RuntimeException("Unable to get user ID from authentication");
        }

        java.util.List<RecruitApplicationVO> list = recruitService.getMyApplications(jobSeekerId);
        System.out.println("[RecruitController] Returning list size: " + (list != null ? list.size() : 0));
        return Result.success(list);
    }

    @Operation(summary = "获取投递申请详情")
    @GetMapping("/applications/{id}")
    public Result<RecruitApplicationVO> getApplicationDetail(
            @Parameter(description = "申请ID") @PathVariable("id") Long id) {
        RecruitApplicationVO application = recruitService.getApplicationDetail(id);
        return Result.success(application);
    }
}
