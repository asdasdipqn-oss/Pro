package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.AttMonthlyStat;
import cn.edu.ccst.manpower_resource.service.IAttMonthlyStatService;
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

@Tag(name = "月度考勤统计", description = "月度考勤汇总查询")
@RestController
@RequestMapping("/att/monthly")
@RequiredArgsConstructor
public class AttMonthlyStatController {

    private final IAttMonthlyStatService attMonthlyStatService;

    @Operation(summary = "分页查询月度考勤统计")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public Result<Page<AttMonthlyStat>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer statYear,
            @RequestParam(required = false) Integer statMonth) {
        LambdaQueryWrapper<AttMonthlyStat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(employeeId != null, AttMonthlyStat::getEmployeeId, employeeId)
                .eq(statYear != null, AttMonthlyStat::getStatYear, statYear)
                .eq(statMonth != null, AttMonthlyStat::getStatMonth, statMonth)
                .orderByDesc(AttMonthlyStat::getStatYear)
                .orderByDesc(AttMonthlyStat::getStatMonth);
        Page<AttMonthlyStat> page = attMonthlyStatService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @Operation(summary = "查询员工月度考勤统计")
    @GetMapping("/employee/{employeeId}")
    public Result<List<AttMonthlyStat>> getByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(required = false) Integer statYear) {
        LambdaQueryWrapper<AttMonthlyStat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttMonthlyStat::getEmployeeId, employeeId)
                .eq(statYear != null, AttMonthlyStat::getStatYear, statYear)
                .orderByDesc(AttMonthlyStat::getStatYear)
                .orderByDesc(AttMonthlyStat::getStatMonth);
        return Result.success(attMonthlyStatService.list(wrapper));
    }

    @Operation(summary = "获取月度统计详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public Result<AttMonthlyStat> getById(@PathVariable Long id) {
        return Result.success(attMonthlyStatService.getById(id));
    }

    @Operation(summary = "生成月度考勤统计数据")
    @PostMapping("/generate")
    public Result<Integer> generate(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Long employeeId) {
        int count = attMonthlyStatService.generateMonthlyStats(startDate, endDate, employeeId);
        return Result.success(count);
    }
}
