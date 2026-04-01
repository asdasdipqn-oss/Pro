package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.dto.SalaryGenerateDTO;
import cn.edu.ccst.manpower_resource.entity.SalaryRecord;
import cn.edu.ccst.manpower_resource.security.LoginUser;
import cn.edu.ccst.manpower_resource.service.ISalaryRecordService;
import cn.edu.ccst.manpower_resource.vo.SalaryRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "薪资管理", description = "薪资发放相关操作")
@RestController
@RequestMapping("/salary/record")
@RequiredArgsConstructor
public class SalaryRecordController {

    private final ISalaryRecordService salaryRecordService;

    @Operation(summary = "查询我的薪资记录")
    @GetMapping("/my")
    public Result<List<SalaryRecord>> mySalary(@AuthenticationPrincipal LoginUser loginUser,
                                                @Parameter(description = "年份") @RequestParam Integer year) {
        Long employeeId = loginUser.getUser().getEmployeeId();
        return Result.success(salaryRecordService.getByEmployee(employeeId, year));
    }

    @Operation(summary = "查询指定月份薪资条")
    @GetMapping("/my/{year}/{month}")
    public Result<SalaryRecord> mySalaryByMonth(@AuthenticationPrincipal LoginUser loginUser,
                                                 @PathVariable Integer year,
                                                 @PathVariable Integer month) {
        Long employeeId = loginUser.getUser().getEmployeeId();
        return Result.success(salaryRecordService.getByMonth(employeeId, year, month));
    }

    @Operation(summary = "分页查询月度薪资")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<PageResult<SalaryRecordVO>> pageByMonth(
            @Parameter(description = "年份") @RequestParam Integer year,
            @Parameter(description = "月份") @RequestParam Integer month,
            PageQuery query) {
        return Result.success(salaryRecordService.pageByMonth(year, month, query));
    }

    @Operation(summary = "确认薪资")
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Void> confirm(@Parameter(description = "记录ID") @PathVariable Long id) {
        salaryRecordService.confirmSalary(id);
        return Result.success();
    }

    @Operation(summary = "发放薪资")
    @PutMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Void> pay(@Parameter(description = "记录ID") @PathVariable Long id) {
        salaryRecordService.paySalary(id);
        return Result.success();
    }

    @Operation(summary = "生成月度薪资", description = "根据考勤数据生成指定月份的薪资记录。基本工资3000元，全勤奖3000元（请假/缺勤一天扣100元，迟到/早退一次扣50元）")
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Map<String, Object>> generateMonthlySalary(@Validated @RequestBody SalaryGenerateDTO dto) {
        int count = salaryRecordService.generateMonthlySalary(dto);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("message", "成功生成 " + count + " 条薪资记录");
        return Result.success(result);
    }

    @Operation(summary = "一键发放薪资", description = "一键发放指定月份所有已确认的薪资")
    @PutMapping("/batch-pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Map<String, Object>> batchPay(
            @Parameter(description = "年份") @RequestParam Integer year,
            @Parameter(description = "月份") @RequestParam Integer month) {
        int count = salaryRecordService.batchPay(year, month);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("message", "成功发放 " + count + " 条薪资记录");
        return Result.success(result);
    }

    @Operation(summary = "一键确认薪资", description = "一键确认指定月份所有草稿状态的薪资")
    @PutMapping("/batch-confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Map<String, Object>> batchConfirm(
            @Parameter(description = "年份") @RequestParam Integer year,
            @Parameter(description = "月份") @RequestParam Integer month) {
        int count = salaryRecordService.batchConfirm(year, month);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("message", "成功确认 " + count + " 条薪资记录");
        return Result.success(result);
    }

    @Operation(summary = "重新计算薪资", description = "重新计算指定薪资记录（仅草稿状态可重算）")
    @PutMapping("/{id}/recalculate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Void> recalculate(@Parameter(description = "记录ID") @PathVariable Long id) {
        salaryRecordService.recalculate(id);
        return Result.success();
    }

    @Operation(summary = "获取薪资记录详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<SalaryRecord> getById(@Parameter(description = "记录ID") @PathVariable Long id) {
        return Result.success(salaryRecordService.getById(id));
    }

    @Operation(summary = "编辑薪资记录", description = "编辑单条薪资记录（仅草稿或已确认状态可编辑）")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Void> update(@Parameter(description = "记录ID") @PathVariable Long id,
                               @RequestBody SalaryRecord record) {
        // 检查状态，已发放的不能编辑
        SalaryRecord existing = salaryRecordService.getById(id);
        if (existing == null) {
            return Result.fail("薪资记录不存在");
        }
        if (existing.getStatus() == 2) {
            return Result.fail("已发放的薪资记录不能编辑");
        }
        
        // 更新记录
        record.setId(id);
        record.setUpdateTime(java.time.LocalDateTime.now());
        
        // 重新计算应发工资和实发工资
        java.math.BigDecimal gross = java.math.BigDecimal.ZERO;
        if (record.getBaseSalary() != null) gross = gross.add(record.getBaseSalary());
        if (record.getPositionSalary() != null) gross = gross.add(record.getPositionSalary());
        if (record.getPerformanceSalary() != null) gross = gross.add(record.getPerformanceSalary());
        if (record.getOvertimePay() != null) gross = gross.add(record.getOvertimePay());
        if (record.getAllowance() != null) gross = gross.add(record.getAllowance());
        if (record.getBonus() != null) gross = gross.add(record.getBonus());
        if (record.getFullAttendanceBonus() != null) gross = gross.add(record.getFullAttendanceBonus());
        record.setGrossSalary(gross);
        
        java.math.BigDecimal deduction = java.math.BigDecimal.ZERO;
        if (record.getSocialInsurance() != null) deduction = deduction.add(record.getSocialInsurance());
        if (record.getHousingFund() != null) deduction = deduction.add(record.getHousingFund());
        if (record.getPersonalTax() != null) deduction = deduction.add(record.getPersonalTax());
        if (record.getOtherDeduction() != null) deduction = deduction.add(record.getOtherDeduction());
        record.setTotalDeduction(deduction);
        
        record.setNetSalary(gross.subtract(deduction));
        
        salaryRecordService.updateById(record);
        return Result.success();
    }

    @Operation(summary = "获取薪资统计摘要")
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<ISalaryRecordService.SalarySummary> getSummary(
            @Parameter(description = "年份") @RequestParam Integer year,
            @Parameter(description = "月份") @RequestParam Integer month) {
        return Result.success(salaryRecordService.getSummary(year, month));
    }

    @Operation(summary = "导出薪资数据")
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public void exportSalary(
            @Parameter(description = "年份") @RequestParam Integer year,
            @Parameter(description = "月份") @RequestParam Integer month,
            HttpServletResponse response) {
        salaryRecordService.exportSalary(year, month, response);
    }

    @Operation(summary = "推送薪资条通知", description = "向已发放薪资的员工推送消息通知")
    @PostMapping("/push-notification")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Map<String, Object>> pushNotification(
            @Parameter(description = "年份") @RequestParam Integer year,
            @Parameter(description = "月份") @RequestParam Integer month) {
        int count = salaryRecordService.pushSalaryNotification(year, month);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("message", "成功推送 " + count + " 条薪资通知");
        return Result.success(result);
    }

    @Operation(summary = "获取员工未读薪资通知")
    @GetMapping("/unread-notifications")
    public Result<List<SalaryRecord>> getUnreadNotifications(@AuthenticationPrincipal LoginUser loginUser) {
        Long employeeId = loginUser.getUser().getEmployeeId();
        return Result.success(salaryRecordService.getUnreadNotifications(employeeId));
    }

    @Operation(summary = "标记薪资通知为已读")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@AuthenticationPrincipal LoginUser loginUser,
                                   @PathVariable Long id) {
        salaryRecordService.markAsRead(id, loginUser.getUser().getEmployeeId());
        return Result.success();
    }

    @Operation(summary = "删除薪资记录", description = "删除指定的薪资记录（仅草稿状态可删除）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Void> delete(@Parameter(description = "记录ID") @PathVariable Long id) {
        SalaryRecord record = salaryRecordService.getById(id);
        if (record == null) {
            return Result.fail("薪资记录不存在");
        }
        if (record.getStatus() != 0) {
            return Result.fail("只能删除草稿状态的薪资记录");
        }
        salaryRecordService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "批量删除薪资记录", description = "删除指定月份所有草稿状态的薪资记录")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Map<String, Object>> batchDelete(
            @Parameter(description = "年份") @RequestParam Integer year,
            @Parameter(description = "月份") @RequestParam Integer month) {
        int count = salaryRecordService.batchDelete(year, month);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("message", "成功删除 " + count + " 条薪资记录");
        return Result.success(result);
    }
}
