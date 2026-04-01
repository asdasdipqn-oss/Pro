package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.AssessPlan;
import cn.edu.ccst.manpower_resource.service.IAssessPlanService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 考核方案表 前端控制器
 */
@Tag(name = "考核管理", description = "考核方案相关操作")
@RestController
@RequestMapping("/assess/plan")
@RequiredArgsConstructor
public class AssessPlanController {

    private final IAssessPlanService assessPlanService;

    @Operation(summary = "分页查询考核方案")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<PageResult<AssessPlan>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "方案名称") @RequestParam(required = false) String planName,
            @Parameter(description = "考核类型") @RequestParam(required = false) Integer assessType,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        
        Page<AssessPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AssessPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(planName), AssessPlan::getPlanName, planName)
                .eq(assessType != null, AssessPlan::getAssessType, assessType)
                .eq(status != null, AssessPlan::getStatus, status)
                .eq(AssessPlan::getDeleted, 0)
                .orderByDesc(AssessPlan::getCreateTime);
        
        Page<AssessPlan> result = assessPlanService.page(page, wrapper);
        return Result.success(PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "获取考核方案详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<AssessPlan> getById(@Parameter(description = "方案ID") @PathVariable Long id) {
        return Result.success(assessPlanService.getById(id));
    }

    @Operation(summary = "创建考核方案")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Void> create(@RequestBody AssessPlan plan) {
        // 生成方案编号
        String planCode = "ASS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        plan.setPlanCode(planCode);
        plan.setStatus(0);
        plan.setDeleted(0);
        plan.setCreateTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        assessPlanService.save(plan);
        return Result.success();
    }

    @Operation(summary = "更新考核方案")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Void> update(@RequestBody AssessPlan plan) {
        plan.setUpdateTime(LocalDateTime.now());
        assessPlanService.updateById(plan);
        return Result.success();
    }

    @Operation(summary = "删除考核方案")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Void> delete(@Parameter(description = "方案ID") @PathVariable Long id) {
        AssessPlan plan = new AssessPlan();
        plan.setId(id);
        plan.setDeleted(1);
        plan.setUpdateTime(LocalDateTime.now());
        assessPlanService.updateById(plan);
        return Result.success();
    }

    @Operation(summary = "发布考核方案")
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Void> publish(@Parameter(description = "方案ID") @PathVariable Long id) {
        AssessPlan plan = new AssessPlan();
        plan.setId(id);
        plan.setStatus(1);
        plan.setUpdateTime(LocalDateTime.now());
        assessPlanService.updateById(plan);
        return Result.success();
    }

    @Operation(summary = "结束考核方案")
    @PutMapping("/{id}/finish")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Void> finish(@Parameter(description = "方案ID") @PathVariable Long id) {
        AssessPlan plan = new AssessPlan();
        plan.setId(id);
        plan.setStatus(3);
        plan.setUpdateTime(LocalDateTime.now());
        assessPlanService.updateById(plan);
        return Result.success();
    }
}
