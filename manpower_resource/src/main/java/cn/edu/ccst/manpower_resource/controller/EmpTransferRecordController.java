package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.EmpTransferRecord;
import cn.edu.ccst.manpower_resource.entity.EmpEmployee;
import cn.edu.ccst.manpower_resource.security.LoginUser;
import cn.edu.ccst.manpower_resource.service.IEmpTransferRecordService;
import cn.edu.ccst.manpower_resource.service.IEmpEmployeeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "员工异动管理", description = "员工调岗、晋升、降级等异动记录")
@RestController
@RequestMapping("/emp/transfer")
@RequiredArgsConstructor
public class EmpTransferRecordController {

    private final IEmpTransferRecordService transferRecordService;
    private final IEmpEmployeeService empEmployeeService;

    @Operation(summary = "分页查询异动记录")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public Result<Page<EmpTransferRecord>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer transferType) {
        LambdaQueryWrapper<EmpTransferRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(employeeId != null, EmpTransferRecord::getEmployeeId, employeeId)
                .eq(transferType != null, EmpTransferRecord::getTransferType, transferType)
                .orderByDesc(EmpTransferRecord::getCreateTime);
        Page<EmpTransferRecord> page = transferRecordService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @Operation(summary = "查询员工异动历史")
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public Result<Page<EmpTransferRecord>> getByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<EmpTransferRecord> page = transferRecordService.page(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<EmpTransferRecord>()
                        .eq(EmpTransferRecord::getEmployeeId, employeeId)
                        .orderByDesc(EmpTransferRecord::getCreateTime));
        return Result.success(page);
    }

    @Operation(summary = "获取异动记录详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public Result<EmpTransferRecord> getById(@PathVariable Long id) {
        return Result.success(transferRecordService.getById(id));
    }

    @Operation(summary = "新增异动记录（调岗/晋升/降级等）")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> add(@RequestBody EmpTransferRecord record,
                            @AuthenticationPrincipal LoginUser loginUser) {
        record.setCreateBy(loginUser.getUserId());
        record.setCreateTime(LocalDateTime.now());
        transferRecordService.save(record);
        // 如果有新部门或新岗位，同步更新员工信息
        EmpEmployee emp = empEmployeeService.getById(record.getEmployeeId());
        if (emp != null) {
            boolean needUpdate = false;
            if (record.getNewDeptId() != null) {
                emp.setDeptId(record.getNewDeptId());
                needUpdate = true;
            }
            if (record.getNewPositionId() != null) {
                emp.setPositionId(record.getNewPositionId());
                needUpdate = true;
            }
            if (needUpdate) {
                empEmployeeService.updateById(emp);
            }
        }
        return Result.success();
    }

    @Operation(summary = "删除异动记录")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        transferRecordService.removeById(id);
        return Result.success();
    }
}
