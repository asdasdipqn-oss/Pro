package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.dto.ApproveDTO;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.security.LoginUser;
import cn.edu.ccst.manpower_resource.service.IApprovalRecordService;
import cn.edu.ccst.manpower_resource.vo.ApprovalRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审批记录表 前端控制器
 */
@Tag(name = "审批记录管理", description = "审批记录相关操作")
@RestController
@RequestMapping("/approval/record")
@RequiredArgsConstructor
public class ApprovalRecordController {

    private final IApprovalRecordService approvalRecordService;

    @Operation(summary = "执行审批")
    @PostMapping("/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public Result<Void> approve(
            @AuthenticationPrincipal LoginUser loginUser,
            @Validated @RequestBody ApproveDTO dto) {
        approvalRecordService.approve(loginUser.getUser().getId(), dto);
        return Result.success();
    }

    @Operation(summary = "查询待我审批的列表")
    @GetMapping("/pending")
    public Result<PageResult<ApprovalRecordVO>> getPendingList(
            @AuthenticationPrincipal LoginUser loginUser,
            @Parameter(description = "业务类型：1-请假 2-考勤异常") @RequestParam(required = false) Integer businessType,
            PageQuery query) {
        return Result.success(approvalRecordService.getPendingList(loginUser.getUser().getId(), businessType, query));
    }

    @Operation(summary = "查询我已审批的列表")
    @GetMapping("/approved")
    public Result<PageResult<ApprovalRecordVO>> getApprovedList(
            @AuthenticationPrincipal LoginUser loginUser,
            @Parameter(description = "业务类型") @RequestParam(required = false) Integer businessType,
            PageQuery query) {
        return Result.success(approvalRecordService.getApprovedList(loginUser.getUser().getId(), businessType, query));
    }

    @Operation(summary = "根据业务查询审批记录")
    @GetMapping("/business")
    public Result<List<ApprovalRecordVO>> getByBusiness(
            @Parameter(description = "业务类型") @RequestParam Integer businessType,
            @Parameter(description = "业务ID") @RequestParam Long businessId) {
        return Result.success(approvalRecordService.getByBusiness(businessType, businessId));
    }

    @Operation(summary = "分页查询所有审批记录（管理员）")
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<ApprovalRecordVO>> pageAll(
            @Parameter(description = "业务类型") @RequestParam(required = false) Integer businessType,
            @Parameter(description = "状态：0-待审批 1-已通过 2-已驳回") @RequestParam(required = false) Integer status,
            PageQuery query) {
        return Result.success(approvalRecordService.pageAll(businessType, status, query));
    }
}
