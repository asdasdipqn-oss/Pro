package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.SysMenu;
import cn.edu.ccst.manpower_resource.service.ISysMenuService;
import cn.edu.ccst.manpower_resource.vo.MenuTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单管理", description = "系统菜单CRUD操作")
@RestController
@RequestMapping("/sys/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final ISysMenuService menuService;

    @Operation(summary = "获取完整菜单树")
    @GetMapping("/tree")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<MenuTreeVO>> tree() {
        return Result.success(menuService.getMenuTree());
    }

    @Operation(summary = "获取当前用户菜单树")
    @GetMapping("/user/tree")
    public Result<List<MenuTreeVO>> userTree(@Parameter(description = "用户ID") @RequestParam Long userId) {
        return Result.success(menuService.getUserMenuTree(userId));
    }

    @Operation(summary = "获取菜单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SysMenu> getDetail(@Parameter(description = "菜单ID") @PathVariable Long id) {
        return Result.success(menuService.getDetail(id));
    }

    @Operation(summary = "新增菜单")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> create(@Valid @RequestBody SysMenu menu) {
        menuService.createMenu(menu);
        return Result.success();
    }

    @Operation(summary = "修改菜单")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@Valid @RequestBody SysMenu menu) {
        menuService.updateMenu(menu);
        return Result.success();
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@Parameter(description = "菜单ID") @PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.success();
    }
}
