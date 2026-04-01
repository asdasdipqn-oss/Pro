package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.dto.SysRoleDTO;
import cn.edu.ccst.manpower_resource.entity.SysMenu;
import cn.edu.ccst.manpower_resource.entity.SysRole;
import cn.edu.ccst.manpower_resource.service.ISysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理", description = "系统角色CRUD操作")
@RestController
@RequestMapping("/sys/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final ISysRoleService roleService;

    @Operation(summary = "获取所有角色列表")
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<SysRole>> list() {
        return Result.success(roleService.listAll());
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SysRole> getDetail(@Parameter(description = "角色ID") @PathVariable Long id) {
        return Result.success(roleService.getDetail(id));
    }

    @Operation(summary = "获取角色菜单列表")
    @GetMapping("/{id}/menus")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<SysMenu>> getRoleMenus(@Parameter(description = "角色ID") @PathVariable Long id) {
        return Result.success(roleService.getRoleMenus(id));
    }

    @Operation(summary = "新增角色")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> create(@Valid @RequestBody SysRoleDTO dto) {
        roleService.createRole(dto);
        return Result.success();
    }

    @Operation(summary = "修改角色")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@Valid @RequestBody SysRoleDTO dto) {
        roleService.updateRole(dto);
        return Result.success();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@Parameter(description = "角色ID") @PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    @Operation(summary = "分配菜单权限")
    @PutMapping("/{id}/menus")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> assignMenus(
            @Parameter(description = "角色ID") @PathVariable Long id,
            @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return Result.success();
    }
}
