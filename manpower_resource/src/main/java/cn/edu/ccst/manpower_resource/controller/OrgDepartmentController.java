package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.dto.OrgDepartmentDTO;
import cn.edu.ccst.manpower_resource.entity.OrgDepartment;
import cn.edu.ccst.manpower_resource.service.IOrgDepartmentService;
import cn.edu.ccst.manpower_resource.vo.DepartmentTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "部门管理", description = "组织架构-部门CRUD操作")
@RestController
@RequestMapping("/org/department")
@RequiredArgsConstructor
public class OrgDepartmentController {

    private final IOrgDepartmentService departmentService;

    @Operation(summary = "获取部门树")
    @GetMapping("/tree")
    public Result<List<DepartmentTreeVO>> tree() {
        return Result.success(departmentService.getDeptTree());
    }

    @Operation(summary = "获取部门详情")
    @GetMapping("/{id}")
    public Result<OrgDepartment> getDetail(@Parameter(description = "部门ID") @PathVariable Long id) {
        return Result.success(departmentService.getDetail(id));
    }

    @Operation(summary = "新增部门")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> create(@Valid @RequestBody OrgDepartmentDTO dto) {
        departmentService.createDept(dto);
        return Result.success();
    }

    @Operation(summary = "修改部门")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> update(@Valid @RequestBody OrgDepartmentDTO dto) {
        departmentService.updateDept(dto);
        return Result.success();
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@Parameter(description = "部门ID") @PathVariable Long id) {
        departmentService.deleteDept(id);
        return Result.success();
    }
}
