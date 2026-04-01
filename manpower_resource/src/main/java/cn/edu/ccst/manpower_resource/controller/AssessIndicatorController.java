package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.AssessIndicator;
import cn.edu.ccst.manpower_resource.service.IAssessIndicatorService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "考核指标管理", description = "考核指标CRUD操作")
@RestController
@RequestMapping("/assess/indicator")
@RequiredArgsConstructor
public class AssessIndicatorController {

    private final IAssessIndicatorService assessIndicatorService;

    @Operation(summary = "查询方案下的考核指标")
    @GetMapping("/plan/{planId}")
    public Result<List<AssessIndicator>> getByPlan(@PathVariable Long planId) {
        List<AssessIndicator> list = assessIndicatorService.list(new LambdaQueryWrapper<AssessIndicator>()
                .eq(AssessIndicator::getPlanId, planId)
                .orderByAsc(AssessIndicator::getSort));
        return Result.success(list);
    }

    @Operation(summary = "获取指标详情")
    @GetMapping("/{id}")
    public Result<AssessIndicator> getById(@PathVariable Long id) {
        return Result.success(assessIndicatorService.getById(id));
    }

    @Operation(summary = "新增考核指标")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> add(@RequestBody AssessIndicator indicator) {
        indicator.setCreateTime(LocalDateTime.now());
        assessIndicatorService.save(indicator);
        return Result.success();
    }

    @Operation(summary = "批量新增考核指标")
    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> batchAdd(@RequestBody List<AssessIndicator> indicators) {
        LocalDateTime now = LocalDateTime.now();
        indicators.forEach(i -> i.setCreateTime(now));
        assessIndicatorService.saveBatch(indicators);
        return Result.success();
    }

    @Operation(summary = "修改考核指标")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> update(@RequestBody AssessIndicator indicator) {
        assessIndicatorService.updateById(indicator);
        return Result.success();
    }

    @Operation(summary = "删除考核指标")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> delete(@PathVariable Long id) {
        assessIndicatorService.removeById(id);
        return Result.success();
    }
}
