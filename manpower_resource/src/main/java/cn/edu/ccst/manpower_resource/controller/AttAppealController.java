package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.dto.AppealDTO;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.entity.AttAppeal;
import cn.edu.ccst.manpower_resource.security.LoginUser;
import cn.edu.ccst.manpower_resource.service.IAttAppealService;
import cn.edu.ccst.manpower_resource.vo.AttAppealVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考勤异常申诉表 前端控制器
 */
@Tag(name = "考勤申诉管理", description = "考勤异常申诉相关操作")
@RestController
@RequestMapping("/att/appeal")
@RequiredArgsConstructor
public class AttAppealController {

    private final IAttAppealService appealService;

    @Operation(summary = "提交考勤申诉")
    @PostMapping
    public Result<Void> submit(@AuthenticationPrincipal LoginUser loginUser, 
                               @Valid @RequestBody AppealDTO dto) {
        appealService.submit(loginUser.getUser().getEmployeeId(), dto);
        return Result.success();
    }

    @Operation(summary = "撤回申诉")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@AuthenticationPrincipal LoginUser loginUser,
                               @Parameter(description = "申诉ID") @PathVariable Long id) {
        appealService.cancel(id, loginUser.getUser().getEmployeeId());
        return Result.success();
    }

    @Operation(summary = "查询我的申诉记录")
    @GetMapping("/my")
    public Result<PageResult<AttAppealVO>> myAppeals(@AuthenticationPrincipal LoginUser loginUser,
                                                     PageQuery query) {
        return Result.success(appealService.pageByEmployee(loginUser.getUser().getEmployeeId(), query));
    }

    @Operation(summary = "获取申诉详情")
    @GetMapping("/{id}")
    public Result<AttAppealVO> getDetail(@Parameter(description = "申诉ID") @PathVariable Long id) {
        return Result.success(appealService.getDetail(id));
    }

    @Operation(summary = "获取待审批申诉列表")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('HR')")
    public Result<List<AttAppealVO>> getPendingList(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(appealService.getPendingApprovals(loginUser.getUserId()));
    }

    @Operation(summary = "审批申诉")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('HR')")
    public Result<Void> approve(@AuthenticationPrincipal LoginUser loginUser,
                                @Parameter(description = "申诉ID") @PathVariable Long id,
                                @Parameter(description = "状态:2-通过 3-驳回") @RequestParam Integer status,
                                @Parameter(description = "审批意见") @RequestParam(required = false) String comment) {
        appealService.approve(id, loginUser.getUser().getId(), status, comment);
        return Result.success();
    }

    @Operation(summary = "分页查询所有申诉(管理员)")
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<PageResult<AttAppealVO>> page(PageQuery query,
                                                @RequestParam(required = false) Integer status) {
        return Result.success(appealService.pageAll(query, status));
    }

    @Operation(summary = "获取已通过的申诉列表（用于考勤日历显示）")
    @GetMapping("/approved")
    public Result<List<AttAppealVO>> getApprovedAppeals(
            @Parameter(description = "年份") @RequestParam Integer year,
            @Parameter(description = "月份") @RequestParam Integer month) {
        return Result.success(appealService.getApprovedAppeals(year, month));
    }
}
