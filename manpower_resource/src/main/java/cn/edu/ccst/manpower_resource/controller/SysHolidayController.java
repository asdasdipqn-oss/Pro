package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.SysHoliday;
import cn.edu.ccst.manpower_resource.service.ISysHolidayService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "节假日管理", description = "节假日配置CRUD操作")
@RestController
@RequestMapping("/sys/holiday")
@RequiredArgsConstructor
public class SysHolidayController {

    private final ISysHolidayService sysHolidayService;

    @Operation(summary = "分页查询节假日")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Page<SysHoliday>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer holidayType) {
        LambdaQueryWrapper<SysHoliday> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(year != null, SysHoliday::getYear, year)
                .eq(holidayType != null, SysHoliday::getHolidayType, holidayType)
                .orderByAsc(SysHoliday::getHolidayDate);
        Page<SysHoliday> page = sysHolidayService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @Operation(summary = "查询年度节假日列表")
    @GetMapping("/year/{year}")
    public Result<List<SysHoliday>> getByYear(@PathVariable Integer year) {
        List<SysHoliday> list = sysHolidayService.list(new LambdaQueryWrapper<SysHoliday>()
                .eq(SysHoliday::getYear, year)
                .orderByAsc(SysHoliday::getHolidayDate));
        return Result.success(list);
    }

    @Operation(summary = "获取节假日详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<SysHoliday> getById(@PathVariable Long id) {
        return Result.success(sysHolidayService.getById(id));
    }

    @Operation(summary = "新增节假日")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> add(@RequestBody SysHoliday sysHoliday) {
        sysHoliday.setCreateTime(LocalDateTime.now());
        if (sysHoliday.getYear() == null && sysHoliday.getHolidayDate() != null) {
            sysHoliday.setYear(sysHoliday.getHolidayDate().getYear());
        }
        sysHolidayService.save(sysHoliday);
        return Result.success();
    }

    @Operation(summary = "批量新增节假日")
    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> batchAdd(@RequestBody List<SysHoliday> holidays) {
        LocalDateTime now = LocalDateTime.now();
        holidays.forEach(h -> {
            h.setCreateTime(now);
            if (h.getYear() == null && h.getHolidayDate() != null) {
                h.setYear(h.getHolidayDate().getYear());
            }
        });
        sysHolidayService.saveBatch(holidays);
        return Result.success();
    }

    @Operation(summary = "修改节假日")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> update(@RequestBody SysHoliday sysHoliday) {
        sysHolidayService.updateById(sysHoliday);
        return Result.success();
    }

    @Operation(summary = "删除节假日")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> delete(@PathVariable Long id) {
        sysHolidayService.removeById(id);
        return Result.success();
    }
}
