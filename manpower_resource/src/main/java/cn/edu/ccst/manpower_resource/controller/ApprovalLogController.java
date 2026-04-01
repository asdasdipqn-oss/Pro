package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.service.IApprovalLogService;
import cn.edu.ccst.manpower_resource.vo.ApprovalLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "审批日志管理", description = "审批日志查询")
@RestController
@RequestMapping("/approval/log")
@RequiredArgsConstructor
public class ApprovalLogController {

    private final IApprovalLogService logService;

    @Operation(summary = "根据业务查询日志")
    @GetMapping("/business")
    public Result<List<ApprovalLogVO>> getByBusiness(
            @Parameter(description = "业务类型") @RequestParam String businessType,
            @Parameter(description = "业务ID") @RequestParam Long businessId) {
        return Result.success(logService.getByBusiness(businessType, businessId));
    }

    @Operation(summary = "分页查询所有日志")
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<ApprovalLogVO>> page(PageQuery query,
                                                  @RequestParam(required = false) String businessType) {
        return Result.success(logService.pageAll(query, businessType));
    }
}
