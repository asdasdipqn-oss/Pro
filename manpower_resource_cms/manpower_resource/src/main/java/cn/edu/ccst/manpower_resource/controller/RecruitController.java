package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.dto.JobApplicationRequest;
import cn.edu.ccst.manpower_resource.service.IRecruitCandidateService;
import cn.edu.ccst.manpower_resource.vo.RecruitJobVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    public Result<List<RecruitJobVO>> getJobs() {
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
            @AuthenticationPrincipal String username) {
        Long jobSeekerId = recruitService.getCandidateIdByUsername(username);
        recruitService.applyJob(request, jobSeekerId);
        return Result.success();
    }

    @Operation(summary = "获取我的投递记录")
    @GetMapping("/applications/my")
    public Result<List<RecruitApplicationVO>> getMyApplications(
            @AuthenticationPrincipal String username) {
        Long jobSeekerId = recruitService.getCandidateIdByUsername(username);
        List<RecruitApplicationVO> list = recruitService.getMyApplications(jobSeekerId);
        return Result.success(list);
    }

    @Operation(summary = "获取投递申请详情")
    @GetMapping("/applications/{id}")
    public Result<cn.edu.ccst.manpower_resource.entity.RecruitApplication> getApplicationDetail(
            @Parameter(description = "申请ID") @PathVariable("id") Long id) {
        cn.edu.ccst.manpower_resource.entity.RecruitApplication application = recruitService.getApplicationDetail(id);
        return Result.success(application);
    }
}
