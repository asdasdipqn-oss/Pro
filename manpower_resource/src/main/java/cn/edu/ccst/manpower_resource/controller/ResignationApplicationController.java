package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.EmpEmployee;
import cn.edu.ccst.manpower_resource.entity.ResignationApplication;
import cn.edu.ccst.manpower_resource.security.LoginUser;
import cn.edu.ccst.manpower_resource.service.IEmpEmployeeService;
import cn.edu.ccst.manpower_resource.service.IResignationApplicationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 离职申请 前端控制器
 */
@Tag(name = "离职管理", description = "离职申请相关操作")
@RestController
@RequestMapping("/resignation")
@RequiredArgsConstructor
public class ResignationApplicationController {

    private final IResignationApplicationService resignationService;
    private final IEmpEmployeeService employeeService;

    @Operation(summary = "提交离职申请")
    @PostMapping("/apply")
    public Result<Void> apply(@AuthenticationPrincipal LoginUser loginUser,
                              @RequestBody ResignationApplication application) {
        Long employeeId = loginUser.getUser().getEmployeeId();
        if (employeeId == null) {
            return Result.fail("当前用户未关联员工信息");
        }
        
        // 检查是否有待处理的申请
        Long count = resignationService.count(new LambdaQueryWrapper<ResignationApplication>()
                .eq(ResignationApplication::getEmployeeId, employeeId)
                .eq(ResignationApplication::getStatus, 0));
        if (count > 0) {
            return Result.fail("您已有待处理的离职申请");
        }
        
        application.setEmployeeId(employeeId);
        application.setStatus(0);
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        resignationService.save(application);
        return Result.success();
    }

    @Operation(summary = "查询我的离职申请")
    @GetMapping("/my")
    public Result<List<ResignationApplication>> myApplications(@AuthenticationPrincipal LoginUser loginUser) {
        Long employeeId = loginUser.getUser().getEmployeeId();
        List<ResignationApplication> list = resignationService.list(
                new LambdaQueryWrapper<ResignationApplication>()
                        .eq(ResignationApplication::getEmployeeId, employeeId)
                        .orderByDesc(ResignationApplication::getCreateTime));
        return Result.success(list);
    }

    @Operation(summary = "撤销离职申请")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@AuthenticationPrincipal LoginUser loginUser,
                               @Parameter(description = "申请ID") @PathVariable Long id) {
        ResignationApplication application = resignationService.getById(id);
        if (application == null) {
            return Result.fail("申请不存在");
        }
        Long employeeId = loginUser.getUser().getEmployeeId();
        if (!application.getEmployeeId().equals(employeeId)) {
            return Result.fail("无权操作此申请");
        }
        if (application.getStatus() != 0) {
            return Result.fail("只能撤销待审批的申请");
        }
        
        application.setStatus(3);
        application.setUpdateTime(LocalDateTime.now());
        resignationService.updateById(application);
        return Result.success();
    }

    @Operation(summary = "分页查询离职申请列表(管理)")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public Result<PageResult<Map<String, Object>>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        
        Page<ResignationApplication> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ResignationApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, ResignationApplication::getStatus, status)
                .orderByDesc(ResignationApplication::getCreateTime);
        
        Page<ResignationApplication> result = resignationService.page(page, wrapper);
        
        // 封装员工信息
        List<Map<String, Object>> records = result.getRecords().stream().map(app -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", app.getId());
            map.put("employeeId", app.getEmployeeId());
            map.put("resignationType", app.getResignationType());
            map.put("expectedDate", app.getExpectedDate());
            map.put("reason", app.getReason());
            map.put("handoverTo", app.getHandoverTo());
            map.put("status", app.getStatus());
            map.put("approverId", app.getApproverId());
            map.put("approveTime", app.getApproveTime());
            map.put("approveRemark", app.getApproveRemark());
            map.put("actualLeaveDate", app.getActualLeaveDate());
            map.put("createTime", app.getCreateTime());
            
            // 员工信息
            EmpEmployee emp = employeeService.getById(app.getEmployeeId());
            if (emp != null) {
                map.put("empName", emp.getEmpName());
                map.put("empCode", emp.getEmpCode());
            }
            
            // 交接人信息
            if (app.getHandoverTo() != null) {
                EmpEmployee handover = employeeService.getById(app.getHandoverTo());
                if (handover != null) {
                    map.put("handoverName", handover.getEmpName());
                }
            }
            
            return map;
        }).toList();
        
        return Result.success(PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), records));
    }

    @Operation(summary = "审批离职申请")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public Result<Void> approve(@AuthenticationPrincipal LoginUser loginUser,
                                @Parameter(description = "申请ID") @PathVariable Long id,
                                @Parameter(description = "是否批准") @RequestParam Boolean approved,
                                @Parameter(description = "审批备注") @RequestParam(required = false) String remark) {
        ResignationApplication application = resignationService.getById(id);
        if (application == null) {
            return Result.fail("申请不存在");
        }
        if (application.getStatus() != 0) {
            return Result.fail("该申请已处理");
        }
        
        application.setStatus(approved ? 1 : 2);
        application.setApproverId(loginUser.getUser().getId());
        application.setApproveTime(LocalDateTime.now());
        application.setApproveRemark(remark);
        application.setUpdateTime(LocalDateTime.now());
        
        // 如果批准，设置实际离职日期为期望日期
        if (approved) {
            application.setActualLeaveDate(application.getExpectedDate());
            
            // 更新员工状态为离职
            EmpEmployee employee = employeeService.getById(application.getEmployeeId());
            if (employee != null) {
                employee.setEmpStatus(3); // 离职
                employee.setLeaveDate(application.getExpectedDate());
                employee.setUpdateTime(LocalDateTime.now());
                employeeService.updateById(employee);
            }
        }
        
        resignationService.updateById(application);
        return Result.success();
    }

    @Operation(summary = "获取待审批数量")
    @GetMapping("/pending-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public Result<Long> getPendingCount() {
        Long count = resignationService.count(
                new LambdaQueryWrapper<ResignationApplication>().eq(ResignationApplication::getStatus, 0));
        return Result.success(count);
    }
}
