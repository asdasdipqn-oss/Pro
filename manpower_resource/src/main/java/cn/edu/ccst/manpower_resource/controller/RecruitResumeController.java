package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.dto.RecruitResumeDTO;
import cn.edu.ccst.manpower_resource.service.IRecruitResumeService;
import cn.edu.ccst.manpower_resource.vo.RecruitResumeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 简历/候选人管理 前端控制器
 */
@Tag(name = "简历管理", description = "简历接收与筛选")
@RestController
@RequestMapping("/recruit/resume")
@RequiredArgsConstructor
public class RecruitResumeController {

    private final IRecruitResumeService resumeService;

    @Operation(summary = "投递简历(公开接口)")
    @PostMapping("/submit")
    public Result<Void> submit(@Valid @RequestBody RecruitResumeDTO dto) {
        resumeService.submitResume(dto);
        return Result.success();
    }

    @Operation(summary = "分页查询简历")
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<PageResult<RecruitResumeVO>> page(PageQuery query,
                                                    @RequestParam(required = false) Long jobId,
                                                    @RequestParam(required = false) Integer status) {
        return Result.success(resumeService.pageList(query, jobId, status));
    }

    @Operation(summary = "获取简历详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<RecruitResumeVO> getDetail(@PathVariable Long id) {
        return Result.success(resumeService.getDetail(id));
    }

    @Operation(summary = "筛选简历(通过/不通过)")
    @PutMapping("/{id}/screen")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> screen(@Parameter(description = "简历ID") @PathVariable Long id,
                               @Parameter(description = "状态:2-通过 5-不通过") @RequestParam Integer status) {
        resumeService.screenResume(id, status);
        return Result.success();
    }

    @Operation(summary = "更新简历状态(流程推进)")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @Parameter(description = "状态:3-面试中 4-已录用 5-未通过 6-已放弃") @RequestParam Integer status) {
        resumeService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "招聘统计报表")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Map<String, Object>> getStatistics() {
        return Result.success(resumeService.getStatistics());
    }

    @Operation(summary = "删除简历")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> delete(@PathVariable Long id) {
        resumeService.removeById(id);
        return Result.success();
    }
}
