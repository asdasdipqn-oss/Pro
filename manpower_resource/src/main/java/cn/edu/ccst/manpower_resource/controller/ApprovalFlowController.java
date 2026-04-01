package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.dto.ApprovalFlowDTO;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.service.IApprovalFlowService;
import cn.edu.ccst.manpower_resource.vo.ApprovalFlowVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审批流程配置表 前端控制器
 */
@Tag(name = "审批流程管理", description = "审批流程配置相关操作")
@RestController
@RequestMapping("/approval/flow")
@RequiredArgsConstructor
public class ApprovalFlowController {

    private final IApprovalFlowService approvalFlowService;

    @Operation(summary = "创建审批流程")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ApprovalFlowVO> create(@Validated @RequestBody ApprovalFlowDTO dto) {
        return Result.success(approvalFlowService.create(dto));
    }

    @Operation(summary = "更新审批流程")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ApprovalFlowVO> update(@Validated @RequestBody ApprovalFlowDTO dto) {
        return Result.success(approvalFlowService.update(dto));
    }

    @Operation(summary = "删除审批流程")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@Parameter(description = "流程ID") @PathVariable Long id) {
        approvalFlowService.delete(id);
        return Result.success();
    }

    @Operation(summary = "获取审批流程详情")
    @GetMapping("/{id}")
    public Result<ApprovalFlowVO> getDetail(@Parameter(description = "流程ID") @PathVariable Long id) {
        return Result.success(approvalFlowService.getDetail(id));
    }

    @Operation(summary = "分页查询审批流程")
    @GetMapping("/page")
    public Result<PageResult<ApprovalFlowVO>> page(
            @Parameter(description = "流程类型：1-请假 2-考勤异常 3-调岗 4-离职") @RequestParam(required = false) Integer flowType,
            @Parameter(description = "状态：0-禁用 1-启用") @RequestParam(required = false) Integer status,
            PageQuery query) {
        return Result.success(approvalFlowService.page(flowType, status, query));
    }

    @Operation(summary = "获取启用的流程列表")
    @GetMapping("/list")
    public Result<List<ApprovalFlowVO>> listEnabled(
            @Parameter(description = "流程类型") @RequestParam(required = false) Integer flowType) {
        return Result.success(approvalFlowService.listEnabled(flowType));
    }
}
