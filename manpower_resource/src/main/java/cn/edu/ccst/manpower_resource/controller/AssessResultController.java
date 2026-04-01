package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.AssessResult;
import cn.edu.ccst.manpower_resource.service.IAssessResultService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "考核结果管理", description = "考核结果CRUD操作")
@RestController
@RequestMapping("/assess/result")
@RequiredArgsConstructor
public class AssessResultController {

    private final IAssessResultService assessResultService;

    @Operation(summary = "分页查询考核结果")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public Result<Page<AssessResult>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long planId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<AssessResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(planId != null, AssessResult::getPlanId, planId)
                .eq(employeeId != null, AssessResult::getEmployeeId, employeeId)
                .eq(status != null, AssessResult::getStatus, status)
                .orderByDesc(AssessResult::getCreateTime);
        Page<AssessResult> page = assessResultService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @Operation(summary = "查询方案下的考核结果")
    @GetMapping("/plan/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public Result<List<AssessResult>> getByPlan(@PathVariable Long planId) {
        List<AssessResult> list = assessResultService.list(new LambdaQueryWrapper<AssessResult>()
                .eq(AssessResult::getPlanId, planId)
                .orderByDesc(AssessResult::getTotalScore));
        return Result.success(list);
    }

    @Operation(summary = "获取考核结果详情")
    @GetMapping("/{id}")
    public Result<AssessResult> getById(@PathVariable Long id) {
        return Result.success(assessResultService.getById(id));
    }

    @Operation(summary = "新增考核结果")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public Result<Void> add(@RequestBody AssessResult assessResult) {
        assessResult.setCreateTime(LocalDateTime.now());
        assessResult.setUpdateTime(LocalDateTime.now());
        assessResultService.save(assessResult);
        return Result.success();
    }

    @Operation(summary = "提交自评")
    @PutMapping("/{id}/self-evaluate")
    public Result<Void> selfEvaluate(@PathVariable Long id, @RequestBody AssessResult dto) {
        AssessResult result = assessResultService.getById(id);
        if (result != null) {
            result.setSelfEvaluation(dto.getSelfEvaluation());
            result.setStatus(1);
            result.setUpdateTime(LocalDateTime.now());
            assessResultService.updateById(result);
        }
        return Result.success();
    }

    @Operation(summary = "考核评分")
    @PutMapping("/{id}/assess")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public Result<Void> assess(@PathVariable Long id, @RequestBody AssessResult dto) {
        AssessResult result = assessResultService.getById(id);
        if (result != null) {
            result.setTotalScore(dto.getTotalScore());
            result.setGrade(dto.getGrade());
            result.setAssessorComment(dto.getAssessorComment());
            result.setAssessorId(dto.getAssessorId());
            result.setAssessTime(LocalDateTime.now());
            result.setStatus(2);
            result.setUpdateTime(LocalDateTime.now());
            assessResultService.updateById(result);
        }
        return Result.success();
    }

    @Operation(summary = "确认考核结果")
    @PutMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        AssessResult result = assessResultService.getById(id);
        if (result != null) {
            result.setStatus(3);
            result.setConfirmTime(LocalDateTime.now());
            result.setUpdateTime(LocalDateTime.now());
            assessResultService.updateById(result);
        }
        return Result.success();
    }

    @Operation(summary = "删除考核结果")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> delete(@PathVariable Long id) {
        assessResultService.removeById(id);
        return Result.success();
    }
}
