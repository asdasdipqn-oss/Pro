package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.RecruitApplication;
import cn.edu.ccst.manpower_resource.entity.RecruitInterview;
import cn.edu.ccst.manpower_resource.mapper.RecruitApplicationMapper;
import cn.edu.ccst.manpower_resource.service.IRecruitInterviewService;
import cn.edu.ccst.manpower_resource.security.CandidateAuthenticationToken;
import cn.edu.ccst.manpower_resource.vo.RecruitApplicationVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "面试管理", description = "面试记录CRUD操作")
@RestController
@RequestMapping("/recruit/interview")
@RequiredArgsConstructor
public class RecruitInterviewController {

    private final IRecruitInterviewService recruitInterviewService;
    private final RecruitApplicationMapper recruitApplicationMapper;

    @Operation(summary = "分页查询面试记录")
    @GetMapping("/page")
    public Result<Page<RecruitInterview>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long resumeId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer result) {
        LambdaQueryWrapper<RecruitInterview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(resumeId != null, RecruitInterview::getResumeId, resumeId)
                .eq(status != null, RecruitInterview::getStatus, status)
                .eq(result != null, RecruitInterview::getResult, result)
                .orderByDesc(RecruitInterview::getInterviewTime);
        Page<RecruitInterview> page = recruitInterviewService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @Operation(summary = "查询简历的面试记录")
    @GetMapping("/resume/{resumeId}")
    public Result<List<RecruitInterview>> getByResume(@PathVariable Long resumeId) {
        List<RecruitInterview> list = recruitInterviewService.list(new LambdaQueryWrapper<RecruitInterview>()
                .eq(RecruitInterview::getResumeId, resumeId)
                .orderByAsc(RecruitInterview::getInterviewRound));
        return Result.success(list);
    }

    @Operation(summary = "获取面试记录详情")
    @GetMapping("/{id}")
    public Result<RecruitInterview> getById(@PathVariable Long id) {
        return Result.success(recruitInterviewService.getById(id));
    }

    @Operation(summary = "安排面试")
    @PostMapping
    public Result<Void> add(@RequestBody RecruitInterview interview) {
        interview.setStatus(0);
        interview.setCreateTime(LocalDateTime.now());
        interview.setUpdateTime(LocalDateTime.now());
        recruitInterviewService.save(interview);

        // 同步更新 recruit_application 表的状态为"面试中"
        updateApplicationStatusToInterviewing(interview.getResumeId());
        return Result.success();
    }

    /**
     * 更新投递记录状态为"面试中"
     */
    private void updateApplicationStatusToInterviewing(Long resumeId) {
        if (resumeId == null) {
            System.out.println("[RecruitInterviewController] resumeId is null, cannot update application status");
            return;
        }

        LambdaQueryWrapper<RecruitApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecruitApplication::getResumeId, resumeId);
        List<RecruitApplication> applications = recruitApplicationMapper.selectList(wrapper);

        if (applications != null && !applications.isEmpty()) {
            for (RecruitApplication app : applications) {
                app.setStatus(2); // 2-面试中
                app.setUpdateTime(LocalDateTime.now());
                recruitApplicationMapper.updateById(app);
            }
        }
    }

    @Operation(summary = "更新面试记录（填写评价/评分/结果）")
    @PutMapping
    public Result<Void> update(@RequestBody RecruitInterview interview) {
        interview.setUpdateTime(LocalDateTime.now());
        recruitInterviewService.updateById(interview);
        return Result.success();
    }

    @Operation(summary = "完成面试（填写评价和结果）")
    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id, @RequestBody RecruitInterview dto) {
        RecruitInterview interview = recruitInterviewService.getById(id);
        if (interview != null) {
            interview.setEvaluation(dto.getEvaluation());
            interview.setScore(dto.getScore());
            interview.setResult(dto.getResult());
            interview.setRemark(dto.getRemark());
            interview.setStatus(1);
            interview.setUpdateTime(LocalDateTime.now());
            recruitInterviewService.updateById(interview);

            // 根据面试结果同步更新投递状态
            updateApplicationStatusByInterviewResult(dto.getResult(), interview.getResumeId());
        }
        return Result.success();
    }

    /**
     * 根据面试结果更新投递记录状态
     * 1-通过: 更新为"已录用"(status=4)
     * 2-待定: 不更新
     * 3-不通过: 更新为"未通过"(status=5)
     */
    private void updateApplicationStatusByInterviewResult(Integer interviewResult, Long resumeId) {
        if (resumeId == null) {
            System.out.println("[RecruitInterviewController] resumeId is null, cannot update application status");
            return;
        }

        Integer applicationStatus = null;
        if (interviewResult != null) {
            switch (interviewResult) {
                case 1: // 通过
                    applicationStatus = 4; // 已录用
                    break;
                case 3: // 不通过
                    applicationStatus = 5; // 未通过
                    break;
                default:
                    applicationStatus = null;
                    break;
            }
        }

        if (applicationStatus != null) {
            LambdaQueryWrapper<RecruitApplication> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RecruitApplication::getResumeId, resumeId);
            List<RecruitApplication> applications = recruitApplicationMapper.selectList(wrapper);

            if (applications != null && !applications.isEmpty()) {
                for (RecruitApplication app : applications) {
                    app.setStatus(applicationStatus);
                    app.setUpdateTime(LocalDateTime.now());
                    recruitApplicationMapper.updateById(app);
                }
            }
        }
    }

    @Operation(summary = "取消面试")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        RecruitInterview interview = recruitInterviewService.getById(id);
        if (interview != null) {
            interview.setStatus(2);
            interview.setUpdateTime(LocalDateTime.now());
            recruitInterviewService.updateById(interview);

            // 取消面试时，不更新投递状态（保持"面试中"）
            System.out.println("[RecruitInterviewController] Interview cancelled, application status kept unchanged");
        }
        return Result.success();
    }

    @Operation(summary = "删除面试记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        recruitInterviewService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "求职者获取我的面试记录")
    @GetMapping("/my")
    public Result<List<RecruitInterview>> getMyInterviews(@AuthenticationPrincipal Object authentication) {
        Long candidateId = null;
        if (authentication instanceof CandidateAuthenticationToken) {
            candidateId = ((CandidateAuthenticationToken) authentication).getUserId();
        }

        if (candidateId == null) {
            return Result.fail("无法获取求职者信息");
        }

        // 获取该求职者的所有投递记录
        LambdaQueryWrapper<RecruitApplication> appWrapper = new LambdaQueryWrapper<>();
        appWrapper.eq(RecruitApplication::getJobSeekerId, candidateId);
        List<RecruitApplication> applications = recruitApplicationMapper.selectList(appWrapper);

        if (applications.isEmpty()) {
            return Result.success(List.of());
        }

        // 获取所有简历ID
        List<Long> resumeIds = applications.stream()
                .map(RecruitApplication::getResumeId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (resumeIds.isEmpty()) {
            return Result.success(List.of());
        }

        // 查询所有简历的面试记录
        LambdaQueryWrapper<RecruitInterview> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(RecruitInterview::getResumeId, resumeIds)
                .orderByDesc(RecruitInterview::getInterviewTime);
        List<RecruitInterview> interviews = recruitInterviewService.list(wrapper);

        return Result.success(interviews);
    }

    @Operation(summary = "根据投递ID获取面试记录")
    @GetMapping("/application/{applicationId}")
    public Result<List<RecruitInterview>> getByApplicationId(@PathVariable Long applicationId) {
        System.out.println("[RecruitInterviewController] getByApplicationId called, applicationId: " + applicationId);
        // 根据投递ID查询对应的resumeId
        RecruitApplication application = recruitApplicationMapper.selectById(applicationId);
        System.out.println("[RecruitInterviewController] application: " + application);
        if (application == null || application.getResumeId() == null) {
            System.out.println("[RecruitInterviewController] application or resumeId is null, returning empty list");
            return Result.success(List.of());
        }

        // 查询该简历的面试记录
        LambdaQueryWrapper<RecruitInterview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecruitInterview::getResumeId, application.getResumeId())
                .orderByAsc(RecruitInterview::getInterviewRound);
        List<RecruitInterview> interviews = recruitInterviewService.list(wrapper);
        System.out.println("[RecruitInterviewController] Returning interviews size: " + (interviews != null ? interviews.size() : 0));
        return Result.success(interviews);
    }
}
