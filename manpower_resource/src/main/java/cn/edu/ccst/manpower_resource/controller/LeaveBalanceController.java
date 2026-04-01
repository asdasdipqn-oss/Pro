package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.LeaveBalance;
import cn.edu.ccst.manpower_resource.entity.LeaveType;
import cn.edu.ccst.manpower_resource.entity.EmpEmployee;
import cn.edu.ccst.manpower_resource.service.ILeaveBalanceService;
import cn.edu.ccst.manpower_resource.service.ILeaveTypeService;
import cn.edu.ccst.manpower_resource.service.IEmpEmployeeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "假期额度管理", description = "员工假期额度CRUD操作")
@RestController
@RequestMapping("/leave/balance")
@RequiredArgsConstructor
public class LeaveBalanceController {

    private final ILeaveBalanceService leaveBalanceService;
    private final ILeaveTypeService leaveTypeService;
    private final IEmpEmployeeService empEmployeeService;

    @Operation(summary = "分页查询假期额度")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Page<LeaveBalance>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long leaveTypeId,
            @RequestParam(required = false) Integer year) {
        LambdaQueryWrapper<LeaveBalance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(employeeId != null, LeaveBalance::getEmployeeId, employeeId)
                .eq(leaveTypeId != null, LeaveBalance::getLeaveTypeId, leaveTypeId)
                .eq(year != null, LeaveBalance::getYear, year)
                .orderByDesc(LeaveBalance::getYear);
        Page<LeaveBalance> page = leaveBalanceService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @Operation(summary = "查询员工假期额度")
    @GetMapping("/employee/{employeeId}")
    public Result<List<LeaveBalance>> getByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(required = false) Integer year) {
        if (year == null) {
            year = java.time.LocalDate.now().getYear();
        }
        List<LeaveBalance> list = leaveBalanceService.list(new LambdaQueryWrapper<LeaveBalance>()
                .eq(LeaveBalance::getEmployeeId, employeeId)
                .eq(LeaveBalance::getYear, year));
        return Result.success(list);
    }

    @Operation(summary = "新增假期额度")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> add(@RequestBody LeaveBalance leaveBalance) {
        leaveBalance.setCreateTime(LocalDateTime.now());
        leaveBalance.setUpdateTime(LocalDateTime.now());
        if (leaveBalance.getUsedDays() == null) {
            leaveBalance.setUsedDays(BigDecimal.ZERO);
        }
        if (leaveBalance.getRemainingDays() == null) {
            leaveBalance.setRemainingDays(leaveBalance.getTotalDays());
        }
        leaveBalanceService.save(leaveBalance);
        return Result.success();
    }

    @Operation(summary = "批量初始化年度假期额度")
    @PostMapping("/init/{year}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> initYear(@PathVariable Integer year) {
        List<EmpEmployee> employees = empEmployeeService.list(new LambdaQueryWrapper<EmpEmployee>()
                .eq(EmpEmployee::getEmpStatus, 1));
        List<LeaveType> leaveTypes = leaveTypeService.list(new LambdaQueryWrapper<LeaveType>()
                .eq(LeaveType::getStatus, 1));
        List<LeaveBalance> balances = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (EmpEmployee emp : employees) {
            for (LeaveType lt : leaveTypes) {
                long exists = leaveBalanceService.count(new LambdaQueryWrapper<LeaveBalance>()
                        .eq(LeaveBalance::getEmployeeId, emp.getId())
                        .eq(LeaveBalance::getLeaveTypeId, lt.getId())
                        .eq(LeaveBalance::getYear, year));
                if (exists == 0) {
                    LeaveBalance b = new LeaveBalance();
                    b.setEmployeeId(emp.getId());
                    b.setLeaveTypeId(lt.getId());
                    b.setYear(year);
                    b.setTotalDays(lt.getMaxDays() != null ? new BigDecimal(lt.getMaxDays()) : BigDecimal.ZERO);
                    b.setUsedDays(BigDecimal.ZERO);
                    b.setRemainingDays(b.getTotalDays());
                    b.setCreateTime(now);
                    b.setUpdateTime(now);
                    balances.add(b);
                }
            }
        }
        if (!balances.isEmpty()) {
            leaveBalanceService.saveBatch(balances);
        }
        return Result.success();
    }

    @Operation(summary = "修改假期额度")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> update(@RequestBody LeaveBalance leaveBalance) {
        leaveBalance.setUpdateTime(LocalDateTime.now());
        leaveBalanceService.updateById(leaveBalance);
        return Result.success();
    }

    @Operation(summary = "删除假期额度")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> delete(@PathVariable Long id) {
        leaveBalanceService.removeById(id);
        return Result.success();
    }
}
