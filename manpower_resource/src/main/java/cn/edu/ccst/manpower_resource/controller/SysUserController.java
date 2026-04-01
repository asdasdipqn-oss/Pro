package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.dto.SysUserDTO;
import cn.edu.ccst.manpower_resource.dto.SysUserQuery;
import cn.edu.ccst.manpower_resource.entity.SysRole;
import cn.edu.ccst.manpower_resource.security.LoginUser;
import cn.edu.ccst.manpower_resource.service.ISysUserService;
import cn.edu.ccst.manpower_resource.vo.UserInfoVO;

import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理", description = "系统用户CRUD操作")
@RestController
@RequestMapping("/sys/user")
@RequiredArgsConstructor
public class SysUserController {

    private final ISysUserService userService;

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<PageResult<UserInfoVO>> page(SysUserQuery query) {
        PageResult<UserInfoVO> result = userService.pageList(query);
        return Result.success(result);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<UserInfoVO> getDetail(@Parameter(description = "用户ID") @PathVariable Long id) {
        UserInfoVO vo = userService.getDetail(id);
        return Result.success(vo);
    }

    @Operation(summary = "获取用户角色列表")
    @GetMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<List<SysRole>> getUserRoles(@Parameter(description = "用户ID") @PathVariable Long id) {
        List<SysRole> roles = userService.getUserRoles(id);
        return Result.success(roles);
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> create(@Valid @RequestBody SysUserDTO dto) {
        userService.createUser(dto);
        return Result.success();
    }

    @Operation(summary = "修改用户")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@Valid @RequestBody SysUserDTO dto) {
        userService.updateUser(dto);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @Operation(summary = "修改用户状态(冻结/解冻)")
    @PutMapping("/{id}/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "状态:0-冻结 1-正常") @PathVariable Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "重置密码")
    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> resetPassword(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, String> body) {
        String newPassword = (body != null && body.get("newPassword") != null) 
                ? body.get("newPassword") : "123456";
        userService.resetPassword(id, newPassword);
        return Result.success();
    }

    @Operation(summary = "当前用户修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@AuthenticationPrincipal LoginUser loginUser,
                                       @RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        userService.changePassword(loginUser.getUserId(), oldPassword, newPassword);
        return Result.success();
    }

    @Operation(summary = "更新个人资料")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@AuthenticationPrincipal LoginUser loginUser,
                                      @RequestBody Map<String, String> body) {
        String username = body.get("username");
        userService.updateProfile(loginUser.getUserId(), username);
        return Result.success();
    }
}
