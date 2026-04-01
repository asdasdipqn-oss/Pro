package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.dto.EmpEmployeeDTO;
import cn.edu.ccst.manpower_resource.dto.EmpEmployeeQuery;
import cn.edu.ccst.manpower_resource.entity.EmpEmployee;
import cn.edu.ccst.manpower_resource.service.IEmpEmployeeService;
import cn.edu.ccst.manpower_resource.vo.EmpEmployeeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "员工管理", description = "员工信息CRUD操作")
@RestController
@RequestMapping("/emp/employee")
@RequiredArgsConstructor
public class EmpEmployeeController {

    private final IEmpEmployeeService employeeService;

    @Operation(summary = "分页查询员工列表")
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    public Result<PageResult<EmpEmployeeVO>> page(EmpEmployeeQuery query) {
        return Result.success(employeeService.pageList(query));
    }

    @Operation(summary = "获取员工详情")
    @GetMapping("/{id}")
    public Result<EmpEmployee> getDetail(@Parameter(description = "员工ID") @PathVariable Long id) {
        return Result.success(employeeService.getDetail(id));
    }

    @Operation(summary = "新增员工")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> create(@Valid @RequestBody EmpEmployeeDTO dto) {
        employeeService.createEmployee(dto);
        return Result.success();
    }

    @Operation(summary = "修改员工")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> update(@Valid @RequestBody EmpEmployeeDTO dto) {
        employeeService.updateEmployee(dto);
        return Result.success();
    }

    @Operation(summary = "删除员工")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> delete(@Parameter(description = "员工ID") @PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return Result.success();
    }

    @Operation(summary = "修改员工状态(在职/离职/试用期)")
    @PutMapping("/{id}/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> updateStatus(
            @Parameter(description = "员工ID") @PathVariable Long id,
            @Parameter(description = "状态:1-在职 2-离职 3-试用期") @PathVariable Integer status) {
        employeeService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "导出员工列表")
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public void exportEmployees(EmpEmployeeQuery query, HttpServletResponse response) {
        employeeService.exportEmployees(query, response);
    }

    @Operation(summary = "导入员工")
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<String> importEmployees(@RequestParam("file") MultipartFile file) {
        String result = employeeService.importEmployees(file);
        return Result.success(result);
    }

    @Operation(summary = "下载导入模板")
    @GetMapping("/template")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public void downloadTemplate(HttpServletResponse response) {
        employeeService.downloadTemplate(response);
    }

    @Operation(summary = "批量删除员工")
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        employeeService.batchDelete(ids);
        return Result.success();
    }

    @Operation(summary = "批量修改部门")
    @PutMapping("/batch/dept")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> batchUpdateDept(
            @Parameter(description = "员工ID列表") @RequestParam List<Long> ids,
            @Parameter(description = "目标部门ID") @RequestParam Long deptId) {
        employeeService.batchUpdateDept(ids, deptId);
        return Result.success();
    }

    @Operation(summary = "批量修改状态")
    @PutMapping("/batch/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> batchUpdateStatus(
            @Parameter(description = "员工ID列表") @RequestParam List<Long> ids,
            @Parameter(description = "状态") @RequestParam Integer status) {
        employeeService.batchUpdateStatus(ids, status);
        return Result.success();
    }

    @Operation(summary = "获取全部员工列表(不分页)")
    @GetMapping("/list")
    public Result<List<EmpEmployee>> list() {
        return Result.success(employeeService.list());
    }
}
