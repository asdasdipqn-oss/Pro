package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.OrgPosition;
import cn.edu.ccst.manpower_resource.service.IOrgPositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "岗位管理", description = "组织架构-岗位CRUD操作")
@RestController
@RequestMapping("/org/position")
@RequiredArgsConstructor
public class OrgPositionController {

    private final IOrgPositionService positionService;

    @Operation(summary = "获取所有岗位列表")
    @GetMapping("/list")
    public Result<List<OrgPosition>> list() {
        return Result.success(positionService.listAll());
    }

    @Operation(summary = "根据部门获取岗位列表")
    @GetMapping("/list/{deptId}")
    public Result<List<OrgPosition>> listByDept(@Parameter(description = "部门ID") @PathVariable Long deptId) {
        return Result.success(positionService.listByDeptId(deptId));
    }

    @Operation(summary = "获取岗位详情")
    @GetMapping("/{id}")
    public Result<OrgPosition> getDetail(@Parameter(description = "岗位ID") @PathVariable Long id) {
        return Result.success(positionService.getById(id));
    }

    @Operation(summary = "新增岗位")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> create(@Valid @RequestBody OrgPosition position) {
        positionService.createPosition(position);
        return Result.success();
    }

    @Operation(summary = "修改岗位")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> update(@Valid @RequestBody OrgPosition position) {
        positionService.updatePosition(position);
        return Result.success();
    }

    @Operation(summary = "删除岗位")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@Parameter(description = "岗位ID") @PathVariable Long id) {
        positionService.deletePosition(id);
        return Result.success();
    }
}
