package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.*;
import cn.edu.ccst.manpower_resource.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表统计 前端控制器
 */
@Tag(name = "报表统计", description = "各类统计报表")
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final EmpEmployeeMapper employeeMapper;
    private final OrgDepartmentMapper departmentMapper;
    private final AttDailyStatMapper attDailyStatMapper;
    private final AttClockRecordMapper clockRecordMapper;
    private final LeaveApplicationMapper leaveApplicationMapper;
    private final SalaryRecordMapper salaryRecordMapper;
    private final ApprovalRecordMapper approvalRecordMapper;

    @Operation(summary = "员工统计概览")
    @GetMapping("/employee/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Map<String, Object>> employeeOverview() {
        Map<String, Object> data = new HashMap<>();

        // 在职员工（status = 1）
        long activeCount = employeeMapper.selectCount(new LambdaQueryWrapper<EmpEmployee>()
                .eq(EmpEmployee::getDeleted, 0)
                .eq(EmpEmployee::getEmpStatus, 1));
        data.put("activeCount", activeCount);

        // 试用期员工（status = 2）
        long probationCount = employeeMapper.selectCount(new LambdaQueryWrapper<EmpEmployee>()
                .eq(EmpEmployee::getDeleted, 0)
                .eq(EmpEmployee::getEmpStatus, 2));
        data.put("probationCount", probationCount);

        // 离职员工（status = 3）
        long leftCount = employeeMapper.selectCount(new LambdaQueryWrapper<EmpEmployee>()
                .eq(EmpEmployee::getDeleted, 0)
                .eq(EmpEmployee::getEmpStatus, 3));
        data.put("leftCount", leftCount);
        
        // 本月入职
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        long newHireCount = employeeMapper.selectCount(new LambdaQueryWrapper<EmpEmployee>()
                .eq(EmpEmployee::getDeleted, 0)
                .ge(EmpEmployee::getHireDate, firstDayOfMonth));
        data.put("newHireCount", newHireCount);
        
        return Result.success(data);
    }

    @Operation(summary = "部门员工分布统计")
    @GetMapping("/employee/dept-distribution")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<List<Map<String, Object>>> deptDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        List<OrgDepartment> depts = departmentMapper.selectList(new LambdaQueryWrapper<OrgDepartment>()
                .eq(OrgDepartment::getDeleted, 0)
                .eq(OrgDepartment::getStatus, 1));
        
        for (OrgDepartment dept : depts) {
            Map<String, Object> item = new HashMap<>();
            item.put("deptId", dept.getId());
            item.put("deptName", dept.getDeptName());
            
            long count = employeeMapper.selectCount(new LambdaQueryWrapper<EmpEmployee>()
                    .eq(EmpEmployee::getDeleted, 0)
                    .eq(EmpEmployee::getDeptId, dept.getId())
                    .ne(EmpEmployee::getEmpStatus, 3));
            item.put("count", count);
            
            result.add(item);
        }
        
        return Result.success(result);
    }

    @Operation(summary = "考勤月度统计")
    @GetMapping("/attendance/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Map<String, Object>> attendanceMonthly(
            @Parameter(description = "年份") @RequestParam Integer year,
            @Parameter(description = "月份") @RequestParam Integer month) {
        
        Map<String, Object> data = new HashMap<>();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        // 从打卡记录表获取数据
        List<AttClockRecord> clockRecords = clockRecordMapper.selectList(new LambdaQueryWrapper<AttClockRecord>()
                .ge(AttClockRecord::getClockDate, startDate)
                .le(AttClockRecord::getClockDate, endDate));
        
        // 标准上班时间 9:00，下班时间 18:00
        int standardClockInHour = 9;
        int standardClockInMinute = 0;
        int standardClockOutHour = 18;
        int standardClockOutMinute = 0;
        
        // 按日期+员工分组
        Map<String, List<AttClockRecord>> recordsByDateAndEmployee = clockRecords.stream()
                .collect(Collectors.groupingBy(r -> r.getClockDate() + "_" + r.getEmployeeId()));
        
        long normalCount = 0;
        long lateCount = 0;
        long earlyCount = 0;
        
        for (Map.Entry<String, List<AttClockRecord>> entry : recordsByDateAndEmployee.entrySet()) {
            List<AttClockRecord> dayRecords = entry.getValue();
            
            boolean isLate = false;
            boolean isEarly = false;
            boolean hasClockIn = false;
            boolean hasClockOut = false;
            
            // 检查上班卡
            AttClockRecord clockIn = dayRecords.stream()
                    .filter(r -> r.getClockType() != null && r.getClockType() == 1)
                    .findFirst().orElse(null);
            if (clockIn != null && clockIn.getClockTime() != null) {
                hasClockIn = true;
                int clockInHour = clockIn.getClockTime().getHour();
                int clockInMinute = clockIn.getClockTime().getMinute();
                if (clockInHour > standardClockInHour || 
                    (clockInHour == standardClockInHour && clockInMinute > standardClockInMinute)) {
                    isLate = true;
                }
            }
            
            // 检查下班卡
            AttClockRecord clockOut = dayRecords.stream()
                    .filter(r -> r.getClockType() != null && r.getClockType() == 2)
                    .findFirst().orElse(null);
            if (clockOut != null && clockOut.getClockTime() != null) {
                hasClockOut = true;
                int clockOutHour = clockOut.getClockTime().getHour();
                int clockOutMinute = clockOut.getClockTime().getMinute();
                if (clockOutHour < standardClockOutHour || 
                    (clockOutHour == standardClockOutHour && clockOutMinute < standardClockOutMinute)) {
                    isEarly = true;
                }
            }
            
            // 统计
            if (isLate) lateCount++;
            if (isEarly) earlyCount++;
            if (hasClockIn && hasClockOut && !isLate && !isEarly) {
                normalCount++;
            }
        }
        
        data.put("normalCount", normalCount);
        data.put("lateCount", lateCount);
        data.put("earlyCount", earlyCount);
        
        // 获取所有在职员工
        List<EmpEmployee> activeEmployees = employeeMapper.selectList(new LambdaQueryWrapper<EmpEmployee>()
                .eq(EmpEmployee::getDeleted, 0)
                .ne(EmpEmployee::getEmpStatus, 3));
        long activeEmployeeCount = activeEmployees.size();
        
        // 获取该月所有已批准的请假申请
        List<LeaveApplication> approvedLeaves = leaveApplicationMapper.selectList(
                new LambdaQueryWrapper<LeaveApplication>()
                        .eq(LeaveApplication::getStatus, 2) // 已通过
                        .le(LeaveApplication::getStartTime, endDate.atTime(23, 59, 59))
                        .ge(LeaveApplication::getEndTime, startDate.atStartOfDay()));
        
        // 计算工作日列表
        List<LocalDate> workDayList = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate) && !current.isAfter(LocalDate.now())) {
            int dayOfWeek = current.getDayOfWeek().getValue();
            if (dayOfWeek < 6) { // 周一到周五
                workDayList.add(current);
            }
            current = current.plusDays(1);
        }
        int workDays = workDayList.size();
        
        // 统计请假人次和缺勤人次
        long leavePersonDays = 0; // 请假人次
        long absentCount = 0; // 缺勤人次
        
        for (EmpEmployee emp : activeEmployees) {
            // 获取该员工的请假记录
            List<LeaveApplication> empLeaves = approvedLeaves.stream()
                    .filter(l -> l.getEmployeeId().equals(emp.getId()))
                    .collect(Collectors.toList());
            
            for (LocalDate workDay : workDayList) {
                String key = workDay + "_" + emp.getId();
                boolean hasClock = recordsByDateAndEmployee.containsKey(key);
                
                if (!hasClock) {
                    // 无打卡记录，检查是否请假
                    boolean isOnLeave = empLeaves.stream().anyMatch(l -> {
                        LocalDate leaveStart = l.getStartTime().toLocalDate();
                        LocalDate leaveEnd = l.getEndTime().toLocalDate();
                        return !workDay.isBefore(leaveStart) && !workDay.isAfter(leaveEnd);
                    });
                    
                    if (isOnLeave) {
                        leavePersonDays++;
                    } else {
                        absentCount++;
                    }
                }
            }
        }
        
        data.put("absentCount", absentCount); // 缺勤人次
        data.put("leaveCount", leavePersonDays); // 请假人次
        
        return Result.success(data);
    }

    @Operation(summary = "薪资月度统计")
    @GetMapping("/salary/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Map<String, Object>> salaryMonthly(
            @Parameter(description = "年份") @RequestParam Integer year,
            @Parameter(description = "月份") @RequestParam Integer month) {
        
        Map<String, Object> data = new HashMap<>();
        
        List<SalaryRecord> records = salaryRecordMapper.selectList(new LambdaQueryWrapper<SalaryRecord>()
                .eq(SalaryRecord::getSalaryYear, year)
                .eq(SalaryRecord::getSalaryMonth, month));
        
        // 总人数
        data.put("totalCount", records.size());
        
        // 已发放人数
        long paidCount = records.stream().filter(r -> r.getStatus() == 2).count();
        data.put("paidCount", paidCount);
        
        // 总发放金额
        BigDecimal totalAmount = records.stream()
                .filter(r -> r.getStatus() == 2)
                .map(SalaryRecord::getNetSalary)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("totalAmount", totalAmount);
        
        // 平均工资
        BigDecimal avgSalary = records.isEmpty() ? BigDecimal.ZERO :
                records.stream()
                        .map(SalaryRecord::getNetSalary)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(records.size()), 2, BigDecimal.ROUND_HALF_UP);
        data.put("avgSalary", avgSalary);
        
        return Result.success(data);
    }

    @Operation(summary = "薪资年度趋势")
    @GetMapping("/salary/trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<List<Map<String, Object>>> salaryTrend(
            @Parameter(description = "年份") @RequestParam Integer year) {
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (int month = 1; month <= 12; month++) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", month);
            
            List<SalaryRecord> records = salaryRecordMapper.selectList(new LambdaQueryWrapper<SalaryRecord>()
                    .eq(SalaryRecord::getSalaryYear, year)
                    .eq(SalaryRecord::getSalaryMonth, month)
                    .eq(SalaryRecord::getStatus, 2));
            
            BigDecimal totalAmount = records.stream()
                    .map(SalaryRecord::getNetSalary)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            item.put("totalAmount", totalAmount);
            item.put("count", records.size());
            
            result.add(item);
        }
        
        return Result.success(result);
    }

    @Operation(summary = "请假统计")
    @GetMapping("/leave/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Map<String, Object>> leaveStatistics(
            @Parameter(description = "年份") @RequestParam Integer year,
            @Parameter(description = "月份") @RequestParam(required = false) Integer month) {
        
        Map<String, Object> data = new HashMap<>();
        
        LambdaQueryWrapper<LeaveApplication> wrapper = new LambdaQueryWrapper<>();
        if (month != null) {
            YearMonth yearMonth = YearMonth.of(year, month);
            wrapper.ge(LeaveApplication::getCreateTime, yearMonth.atDay(1).atStartOfDay())
                    .le(LeaveApplication::getCreateTime, yearMonth.atEndOfMonth().atTime(23, 59, 59));
        } else {
            wrapper.ge(LeaveApplication::getCreateTime, LocalDate.of(year, 1, 1).atStartOfDay())
                    .le(LeaveApplication::getCreateTime, LocalDate.of(year, 12, 31).atTime(23, 59, 59));
        }
        
        List<LeaveApplication> leaves = leaveApplicationMapper.selectList(wrapper);
        
        // 申请总数
        data.put("totalCount", leaves.size());
        
        // 待审批
        long pendingCount = leaves.stream().filter(l -> l.getStatus() == 0 || l.getStatus() == 1).count();
        data.put("pendingCount", pendingCount);
        
        // 已通过
        long approvedCount = leaves.stream().filter(l -> l.getStatus() == 2).count();
        data.put("approvedCount", approvedCount);
        
        // 已驳回
        long rejectedCount = leaves.stream().filter(l -> l.getStatus() == 3).count();
        data.put("rejectedCount", rejectedCount);
        
        // 总请假天数
        BigDecimal totalDays = leaves.stream()
                .filter(l -> l.getStatus() == 2)
                .map(LeaveApplication::getDuration)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("totalDays", totalDays);
        
        return Result.success(data);
    }

    @Operation(summary = "审批统计")
    @GetMapping("/approval/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Map<String, Object>> approvalStatistics() {
        Map<String, Object> data = new HashMap<>();
        
        // 待审批数量
        long pendingCount = approvalRecordMapper.selectCount(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getStatus, 0));
        data.put("pendingCount", pendingCount);
        
        // 已通过数量
        long approvedCount = approvalRecordMapper.selectCount(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getStatus, 1));
        data.put("approvedCount", approvedCount);
        
        // 已驳回数量
        long rejectedCount = approvalRecordMapper.selectCount(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getStatus, 2));
        data.put("rejectedCount", rejectedCount);
        
        return Result.success(data);
    }
}
