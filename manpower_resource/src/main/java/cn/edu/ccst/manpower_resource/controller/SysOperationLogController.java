package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.SysOperationLog;
import cn.edu.ccst.manpower_resource.service.ISysOperationLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 系统操作日志表 前端控制器
 */
@Tag(name = "操作日志", description = "系统操作日志查询")
@RestController
@RequestMapping("/sys/operation-log")
@RequiredArgsConstructor
public class SysOperationLogController {

    private final ISysOperationLogService operationLogService;

    @Operation(summary = "分页查询操作日志")
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<SysOperationLog>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "操作描述") @RequestParam(required = false) String operation,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        
        Page<SysOperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(username), SysOperationLog::getUsername, username)
                .like(StringUtils.hasText(operation), SysOperationLog::getOperation, operation)
                .eq(status != null, SysOperationLog::getStatus, status)
                .orderByDesc(SysOperationLog::getCreateTime);
        
        Page<SysOperationLog> result = operationLogService.page(page, wrapper);
        return Result.success(PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "获取日志详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SysOperationLog> getDetail(@Parameter(description = "日志ID") @PathVariable Long id) {
        return Result.success(operationLogService.getById(id));
    }
}
