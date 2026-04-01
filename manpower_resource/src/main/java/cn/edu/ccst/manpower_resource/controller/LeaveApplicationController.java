package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.dto.LeaveApplicationDTO;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.entity.LeaveApplication;
import cn.edu.ccst.manpower_resource.security.LoginUser;
import cn.edu.ccst.manpower_resource.service.ILeaveApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "请假管理", description = "假期申请相关操作")
@RestController
@RequestMapping("/leave/application")
@RequiredArgsConstructor
public class LeaveApplicationController {

    private final ILeaveApplicationService leaveApplicationService;

    @Operation(summary = "提交请假申请")
    @PostMapping
    public Result<LeaveApplication> apply(@AuthenticationPrincipal LoginUser loginUser,
                                          @Valid @RequestBody LeaveApplicationDTO dto) {
        Long employeeId = loginUser.getUser().getEmployeeId();
        return Result.success(leaveApplicationService.apply(employeeId, dto));
    }

    @Operation(summary = "撤回请假申请")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@AuthenticationPrincipal LoginUser loginUser,
                               @Parameter(description = "申请ID") @PathVariable Long id) {
        Long employeeId = loginUser.getUser().getEmployeeId();
        leaveApplicationService.cancel(id, employeeId);
        return Result.success();
    }

    @Operation(summary = "查询我的请假记录")
    @GetMapping("/my")
    public Result<PageResult<LeaveApplication>> myApplications(@AuthenticationPrincipal LoginUser loginUser,
                                                                PageQuery query) {
        Long employeeId = loginUser.getUser().getEmployeeId();
        return Result.success(leaveApplicationService.pageByEmployee(employeeId, query));
    }

    @Operation(summary = "获取申请详情")
    @GetMapping("/{id}")
    public Result<LeaveApplication> getDetail(@Parameter(description = "申请ID") @PathVariable Long id) {
        return Result.success(leaveApplicationService.getById(id));
    }

    @Operation(summary = "获取待审批列表")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('HR')")
    public Result<List<LeaveApplication>> getPendingList(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(leaveApplicationService.getPendingApprovals(loginUser.getUserId()));
    }

    @Operation(summary = "审批请假申请")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('HR')")
    public Result<Void> approve(@AuthenticationPrincipal LoginUser loginUser,
                                @Parameter(description = "申请ID") @PathVariable Long id,
                                @Parameter(description = "状态:2-通过 3-驳回") @RequestParam Integer status,
                                @Parameter(description = "审批意见") @RequestParam(required = false) String comment) {
        leaveApplicationService.approve(id, loginUser.getUserId(), status, comment);
        return Result.success();
    }
}
