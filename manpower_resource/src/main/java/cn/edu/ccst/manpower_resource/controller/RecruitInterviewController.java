package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.RecruitInterview;
import cn.edu.ccst.manpower_resource.service.IRecruitInterviewService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "面试管理", description = "面试记录CRUD操作")
@RestController
@RequestMapping("/recruit/interview")
@RequiredArgsConstructor
public class RecruitInterviewController {

    private final IRecruitInterviewService recruitInterviewService;

    @Operation(summary = "分页查询面试记录")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
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
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<List<RecruitInterview>> getByResume(@PathVariable Long resumeId) {
        List<RecruitInterview> list = recruitInterviewService.list(new LambdaQueryWrapper<RecruitInterview>()
                .eq(RecruitInterview::getResumeId, resumeId)
                .orderByAsc(RecruitInterview::getInterviewRound));
        return Result.success(list);
    }

    @Operation(summary = "获取面试记录详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<RecruitInterview> getById(@PathVariable Long id) {
        return Result.success(recruitInterviewService.getById(id));
    }

    @Operation(summary = "安排面试")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> add(@RequestBody RecruitInterview interview) {
        interview.setStatus(0);
        interview.setCreateTime(LocalDateTime.now());
        interview.setUpdateTime(LocalDateTime.now());
        recruitInterviewService.save(interview);
        return Result.success();
    }

    @Operation(summary = "更新面试记录（填写评价/评分/结果）")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> update(@RequestBody RecruitInterview interview) {
        interview.setUpdateTime(LocalDateTime.now());
        recruitInterviewService.updateById(interview);
        return Result.success();
    }

    @Operation(summary = "完成面试（填写评价和结果）")
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
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
        }
        return Result.success();
    }

    @Operation(summary = "取消面试")
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> cancel(@PathVariable Long id) {
        RecruitInterview interview = recruitInterviewService.getById(id);
        if (interview != null) {
            interview.setStatus(2);
            interview.setUpdateTime(LocalDateTime.now());
            recruitInterviewService.updateById(interview);
        }
        return Result.success();
    }

    @Operation(summary = "删除面试记录")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        recruitInterviewService.removeById(id);
        return Result.success();
    }
}
