package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.AttDailyStat;
import cn.edu.ccst.manpower_resource.service.IAttDailyStatService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "日考勤统计", description = "日考勤统计查询")
@RestController
@RequestMapping("/att/daily")
@RequiredArgsConstructor
public class AttDailyStatController {

    private final IAttDailyStatService attDailyStatService;

    @Operation(summary = "分页查询日考勤统计")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public Result<Page<AttDailyStat>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        LambdaQueryWrapper<AttDailyStat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(employeeId != null, AttDailyStat::getEmployeeId, employeeId)
                .eq(status != null, AttDailyStat::getStatus, status)
                .ge(startDate != null, AttDailyStat::getStatDate, startDate)
                .le(endDate != null, AttDailyStat::getStatDate, endDate)
                .orderByDesc(AttDailyStat::getStatDate);
        Page<AttDailyStat> page = attDailyStatService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @Operation(summary = "查询员工日考勤统计")
    @GetMapping("/employee/{employeeId}")
    public Result<List<AttDailyStat>> getByEmployee(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<AttDailyStat> list = attDailyStatService.list(new LambdaQueryWrapper<AttDailyStat>()
                .eq(AttDailyStat::getEmployeeId, employeeId)
                .ge(AttDailyStat::getStatDate, startDate)
                .le(AttDailyStat::getStatDate, endDate)
                .orderByAsc(AttDailyStat::getStatDate));
        return Result.success(list);
    }

    @Operation(summary = "获取日考勤统计详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public Result<AttDailyStat> getById(@PathVariable Long id) {
        return Result.success(attDailyStatService.getById(id));
    }
}
