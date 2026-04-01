package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.SalaryStandard;
import cn.edu.ccst.manpower_resource.service.ISalaryStandardService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "薪资标准管理", description = "员工薪资标准CRUD操作")
@RestController
@RequestMapping("/salary/standard")
@RequiredArgsConstructor
public class SalaryStandardController {

    private final ISalaryStandardService salaryStandardService;

    @Operation(summary = "分页查询薪资标准")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Page<SalaryStandard>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long itemId) {
        LambdaQueryWrapper<SalaryStandard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(employeeId != null, SalaryStandard::getEmployeeId, employeeId)
                .eq(itemId != null, SalaryStandard::getItemId, itemId)
                .orderByDesc(SalaryStandard::getCreateTime);
        Page<SalaryStandard> page = salaryStandardService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @Operation(summary = "查询员工薪资标准")
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<List<SalaryStandard>> getByEmployee(@PathVariable Long employeeId) {
        List<SalaryStandard> list = salaryStandardService.list(new LambdaQueryWrapper<SalaryStandard>()
                .eq(SalaryStandard::getEmployeeId, employeeId)
                .orderByAsc(SalaryStandard::getItemId));
        return Result.success(list);
    }

    @Operation(summary = "获取薪资标准详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<SalaryStandard> getById(@PathVariable Long id) {
        return Result.success(salaryStandardService.getById(id));
    }

    @Operation(summary = "新增薪资标准")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> add(@RequestBody SalaryStandard salaryStandard) {
        salaryStandard.setCreateTime(LocalDateTime.now());
        salaryStandard.setUpdateTime(LocalDateTime.now());
        salaryStandardService.save(salaryStandard);
        return Result.success();
    }

    @Operation(summary = "批量设置员工薪资标准")
    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> batchSave(@RequestBody List<SalaryStandard> standards) {
        LocalDateTime now = LocalDateTime.now();
        standards.forEach(s -> { s.setCreateTime(now); s.setUpdateTime(now); });
        salaryStandardService.saveBatch(standards);
        return Result.success();
    }

    @Operation(summary = "修改薪资标准")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> update(@RequestBody SalaryStandard salaryStandard) {
        salaryStandard.setUpdateTime(LocalDateTime.now());
        salaryStandardService.updateById(salaryStandard);
        return Result.success();
    }

    @Operation(summary = "删除薪资标准")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> delete(@PathVariable Long id) {
        salaryStandardService.removeById(id);
        return Result.success();
    }
}
