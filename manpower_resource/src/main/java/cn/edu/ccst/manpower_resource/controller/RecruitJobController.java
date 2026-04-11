package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.entity.RecruitJob;
import cn.edu.ccst.manpower_resource.service.IRecruitJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "招聘岗位管理", description = "招聘岗位发布相关操作")
@RestController
@RequestMapping("/recruit/job")
@RequiredArgsConstructor
public class RecruitJobController {

    private final IRecruitJobService recruitJobService;

    @Operation(summary = "获取已发布岗位列表")
    @GetMapping("/published")
    public Result<List<RecruitJob>> listPublished() {
        return Result.success(recruitJobService.listPublished());
    }

    @Operation(summary = "分页查询招聘岗位")
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<PageResult<RecruitJob>> page(PageQuery query,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        return Result.success(recruitJobService.pageJobs(query, status));
    }

    @Operation(summary = "获取岗位详情")
    @GetMapping("/{id}")
    public Result<RecruitJob> getDetail(@Parameter(description = "岗位ID") @PathVariable Long id) {
        return Result.success(recruitJobService.getById(id));
    }

    @Operation(summary = "发布招聘岗位")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> publish(@Valid @RequestBody RecruitJob job) {
        recruitJobService.publishJob(job);
        return Result.success();
    }

    @Operation(summary = "修改招聘岗位")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> update(@Valid @RequestBody RecruitJob job) {
        recruitJobService.updateJob(job);
        return Result.success();
    }

    @Operation(summary = "关闭招聘岗位")
    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> close(@Parameter(description = "岗位ID") @PathVariable Long id) {
        recruitJobService.closeJob(id);
        return Result.success();
    }
}
