package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.dto.TrainRequestDTO;
import cn.edu.ccst.manpower_resource.security.LoginUser;
import cn.edu.ccst.manpower_resource.service.ITrainRequestService;
import cn.edu.ccst.manpower_resource.vo.TrainRequestVO;
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
 * <p>
 * 培训需求表 前端控制器
 * </p>
 *
 * @author
 * @since 2026-04-03
 */
@Tag(name = "培训需求管理", description = "培训需求相关操作")
@RestController
@RequestMapping("/train/request")
@RequiredArgsConstructor
public class TrainRequestController {

    private final ITrainRequestService requestService;

    @Operation(summary = "提交培训需求")
    @PostMapping
    public Result<Void> submit(@AuthenticationPrincipal LoginUser loginUser,
                               @Valid @RequestBody TrainRequestDTO dto) {
        requestService.submit(loginUser.getUser().getEmployeeId(), dto);
        return Result.success();
    }

    @Operation(summary = "撤回培训需求")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@AuthenticationPrincipal LoginUser loginUser,
                               @Parameter(description = "需求ID") @PathVariable Long id) {
        requestService.cancel(id, loginUser.getUser().getEmployeeId());
        return Result.success();
    }

    @Operation(summary = "获取我的培训需求列表")
    @GetMapping("/my")
    public Result<List<TrainRequestVO>> getMyRequests(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(requestService.getMyRequests(loginUser.getUser().getEmployeeId()));
    }

    @Operation(summary = "获取需求详情")
    @GetMapping("/{id}")
    public Result<TrainRequestVO> getDetail(@Parameter(description = "需求ID") @PathVariable Long id) {
        return Result.success(requestService.getDetail(id));
    }

    @Operation(summary = "获取待审批的培训需求列表")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('HR')")
    public Result<List<TrainRequestVO>> getPendingApprovals() {
        return Result.success(requestService.getPendingApprovals());
    }

    @Operation(summary = "审批培训需求")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('HR')")
    public Result<Void> approve(@AuthenticationPrincipal LoginUser loginUser,
                                @Parameter(description = "需求ID") @PathVariable Long id,
                                @Parameter(description = "状态:1-通过 2-拒绝") @RequestParam Integer status,
                                @Parameter(description = "审批意见") @RequestParam(required = false) String comment) {
        requestService.approve(id, loginUser.getUser().getId(), status, comment);
        return Result.success();
    }

    @Operation(summary = "分页查询所有培训需求(管理员)")
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<PageResult<TrainRequestVO>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        return Result.success(requestService.pageAll(pageNum, pageSize, status));
    }
}
