package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.SalaryItem;
import cn.edu.ccst.manpower_resource.service.ISalaryItemService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "薪资项目管理", description = "薪资项目CRUD操作")
@RestController
@RequestMapping("/salary/item")
@RequiredArgsConstructor
public class SalaryItemController {

    private final ISalaryItemService salaryItemService;

    @Operation(summary = "分页查询薪资项目")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Page<SalaryItem>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) Integer itemType,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SalaryItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(itemName), SalaryItem::getItemName, itemName)
                .eq(itemType != null, SalaryItem::getItemType, itemType)
                .eq(status != null, SalaryItem::getStatus, status)
                .orderByAsc(SalaryItem::getSort);
        Page<SalaryItem> page = salaryItemService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @Operation(summary = "获取所有启用的薪资项目")
    @GetMapping("/list")
    public Result<List<SalaryItem>> list() {
        List<SalaryItem> list = salaryItemService.list(new LambdaQueryWrapper<SalaryItem>()
                .eq(SalaryItem::getStatus, 1)
                .orderByAsc(SalaryItem::getSort));
        return Result.success(list);
    }

    @Operation(summary = "获取薪资项目详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<SalaryItem> getById(@PathVariable Long id) {
        return Result.success(salaryItemService.getById(id));
    }

    @Operation(summary = "新增薪资项目")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> add(@RequestBody SalaryItem salaryItem) {
        salaryItem.setCreateTime(LocalDateTime.now());
        salaryItem.setUpdateTime(LocalDateTime.now());
        salaryItemService.save(salaryItem);
        return Result.success();
    }

    @Operation(summary = "修改薪资项目")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> update(@RequestBody SalaryItem salaryItem) {
        salaryItem.setUpdateTime(LocalDateTime.now());
        salaryItemService.updateById(salaryItem);
        return Result.success();
    }

    @Operation(summary = "删除薪资项目")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> delete(@PathVariable Long id) {
        salaryItemService.removeById(id);
        return Result.success();
    }
}
