package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.LeaveType;
import cn.edu.ccst.manpower_resource.service.ILeaveTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "假期类型管理", description = "假期类型配置")
@RestController
@RequestMapping("/leave/type")
@RequiredArgsConstructor
public class LeaveTypeController {

    private final ILeaveTypeService leaveTypeService;

    @Operation(summary = "获取启用的假期类型列表")
    @GetMapping("/list")
    public Result<List<LeaveType>> list() {
        return Result.success(leaveTypeService.listEnabled());
    }

    @Operation(summary = "获取所有假期类型")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<List<LeaveType>> listAll() {
        return Result.success(leaveTypeService.list());
    }

    @Operation(summary = "新增假期类型")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> create(@Valid @RequestBody LeaveType type) {
        leaveTypeService.createType(type);
        return Result.success();
    }

    @Operation(summary = "修改假期类型")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@Valid @RequestBody LeaveType type) {
        leaveTypeService.updateType(type);
        return Result.success();
    }

    @Operation(summary = "删除假期类型")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@Parameter(description = "类型ID") @PathVariable Long id) {
        leaveTypeService.deleteType(id);
        return Result.success();
    }
}
