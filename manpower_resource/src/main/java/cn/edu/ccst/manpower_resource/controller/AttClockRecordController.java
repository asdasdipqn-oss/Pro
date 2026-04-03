package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.dto.ClockInDTO;
import cn.edu.ccst.manpower_resource.entity.AttClockRecord;
import cn.edu.ccst.manpower_resource.entity.AttRule;
import cn.edu.ccst.manpower_resource.entity.EmpEmployee;
import cn.edu.ccst.manpower_resource.entity.LeaveApplication;
import cn.edu.ccst.manpower_resource.security.LoginUser;
import cn.edu.ccst.manpower_resource.service.IAttClockRecordService;
import cn.edu.ccst.manpower_resource.service.IAttRuleService;
import cn.edu.ccst.manpower_resource.service.IEmpEmployeeService;
import cn.edu.ccst.manpower_resource.service.ILeaveApplicationService;
import cn.edu.ccst.manpower_resource.vo.AttClockRecordVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "考勤打卡", description = "员工打卡相关操作")
@RestController
@RequestMapping("/att/clock")
@RequiredArgsConstructor
public class AttClockRecordController {

    private final IAttClockRecordService clockRecordService;
    private final IEmpEmployeeService employeeService;
    private final ILeaveApplicationService leaveApplicationService;
    private final IAttRuleService attRuleService;

    @Operation(summary = "员工打卡")
    @PostMapping
    public Result<AttClockRecord> clockIn(@AuthenticationPrincipal LoginUser loginUser,
                                          @Valid @RequestBody ClockInDTO dto) {
        Long employeeId = loginUser.getUser().getEmployeeId();
        AttClockRecord record = clockRecordService.clockIn(employeeId, dto);
        return Result.success(record);
    }

    @Operation(summary = "获取今日打卡记录")
    @GetMapping("/today")
    public Result<List<AttClockRecordVO>> getTodayRecords(@AuthenticationPrincipal LoginUser loginUser) {
        Long employeeId = loginUser.getUser().getEmployeeId();
        List<AttClockRecord> records = clockRecordService.getTodayRecords(employeeId);
        AttRule rule = attRuleService.getDefaultRule();
        List<AttClockRecordVO> voList = records.stream()
                .map(r -> toVO(r, rule))
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    @Operation(summary = "获取指定日期打卡记录")
    @GetMapping("/date")
    public Result<List<AttClockRecordVO>> getRecordsByDate(
            @AuthenticationPrincipal LoginUser loginUser,
            @Parameter(description = "日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        Long employeeId = loginUser.getUser().getEmployeeId();
        List<AttClockRecord> records = clockRecordService.getRecordsByDate(employeeId, date);
        AttRule rule = attRuleService.getDefaultRule();
        List<AttClockRecordVO> voList = records.stream()
                .map(r -> toVO(r, rule))
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    @Operation(summary = "获取月度打卡记录")
    @GetMapping("/month")
    public Result<List<AttClockRecordVO>> getRecordsByMonth(
            @AuthenticationPrincipal LoginUser loginUser,
            @Parameter(description = "年份") @RequestParam Integer year,
            @Parameter(description = "月份") @RequestParam Integer month) {
        Long employeeId = loginUser.getUser().getEmployeeId();
        List<AttClockRecord> records = clockRecordService.getRecordsByMonth(employeeId, year, month);
        AttRule rule = attRuleService.getDefaultRule();
        List<AttClockRecordVO> voList = records.stream()
                .map(r -> toVO(r, rule))
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 将实体转换为VO，并计算迟到和早退时间
     */
    private AttClockRecordVO toVO(AttClockRecord record, AttRule rule) {
        AttClockRecordVO vo = new AttClockRecordVO();
        vo.setId(record.getId());
        vo.setEmployeeId(record.getEmployeeId());
        vo.setClockDate(record.getClockDate());
        vo.setClockType(record.getClockType());
        vo.setClockTime(record.getClockTime());
        vo.setClockLatitude(record.getClockLatitude());
        vo.setClockLongitude(record.getClockLongitude());
        vo.setClockAddress(record.getClockAddress());
        vo.setLocationStatus(record.getLocationStatus());
        vo.setDeviceInfo(record.getDeviceInfo());
        vo.setRemark(record.getRemark());

        // 计算迟到和早退时间
        if (rule != null && rule.getWorkStartTime() != null && rule.getWorkEndTime() != null) {
            LocalTime workStart = rule.getWorkStartTime();
            LocalTime workEnd = rule.getWorkEndTime();
            LocalTime actualTime = record.getClockTime().toLocalTime();

            if (record.getClockType() == 1) {
                // 上班打卡，计算迟到
                if (actualTime.isAfter(workStart)) {
                    int lateMinutes = (int) ChronoUnit.MINUTES.between(workStart, actualTime);
                    vo.setLateMinutes(lateMinutes);
                }
            } else if (record.getClockType() == 2) {
                // 下班打卡，计算早退
                if (actualTime.isBefore(workEnd)) {
                    int earlyMinutes = (int) ChronoUnit.MINUTES.between(actualTime, workEnd);
                    vo.setEarlyMinutes(earlyMinutes);
                }
            }
        }

        return vo;
    }

    @Operation(summary = "获取考勤日历数据", description = "返回所有员工的月度考勤矩阵数据")
    @GetMapping("/calendar")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public Result<Map<String, Object>> getCalendarData(
            @Parameter(description = "年份") @RequestParam Integer year,
            @Parameter(description = "月份") @RequestParam Integer month,
            @AuthenticationPrincipal LoginUser loginUser) {

        // 检查当前用户是否是经理
        boolean isManager = loginUser.getRoles().contains("MANAGER");
        Long managerDeptId = null;

        // 如果是经理，获取其所属部门ID
        if (isManager && loginUser.getUser().getEmployeeId() != null) {
            EmpEmployee managerEmp = employeeService.getById(loginUser.getUser().getEmployeeId());
            if (managerEmp != null) {
                managerDeptId = managerEmp.getDeptId();
            }
        }

        // 获取所有在职员工
        LambdaQueryWrapper<EmpEmployee> empQuery = new LambdaQueryWrapper<EmpEmployee>()
                .eq(EmpEmployee::getEmpStatus, 1)
                .orderByAsc(EmpEmployee::getDeptId)
                .orderByAsc(EmpEmployee::getEmpCode);

        // 如果是经理，只查询本部门员工
        if (isManager && managerDeptId != null) {
            empQuery.eq(EmpEmployee::getDeptId, managerDeptId);
        }

        List<EmpEmployee> employees = employeeService.list(empQuery);
        
        // 获取该月所有打卡记录
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();
        
        List<AttClockRecord> allRecords = clockRecordService.list(
                new LambdaQueryWrapper<AttClockRecord>()
                        .ge(AttClockRecord::getClockDate, startDate)
                        .le(AttClockRecord::getClockDate, endDate));
        
        // 按员工ID和日期分组
        Map<Long, Map<LocalDate, List<AttClockRecord>>> recordMap = allRecords.stream()
                .collect(Collectors.groupingBy(AttClockRecord::getEmployeeId,
                        Collectors.groupingBy(AttClockRecord::getClockDate)));
        
        // 获取该月所有已批准的请假申请
        List<LeaveApplication> approvedLeaves = leaveApplicationService.list(
                new LambdaQueryWrapper<LeaveApplication>()
                        .eq(LeaveApplication::getStatus, 2) // 已通过
                        .le(LeaveApplication::getStartTime, endDate.atTime(23, 59, 59))
                        .ge(LeaveApplication::getEndTime, startDate.atStartOfDay()));
        
        // 构建日历数据
        List<Map<String, Object>> rows = new ArrayList<>();
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        
        for (EmpEmployee emp : employees) {
            Map<String, Object> row = new HashMap<>();
            row.put("employeeId", emp.getId());
            row.put("empCode", emp.getEmpCode());
            row.put("empName", emp.getEmpName());
            
            Map<Integer, Integer> dailyStatus = new HashMap<>();
            Map<LocalDate, List<AttClockRecord>> empRecords = recordMap.getOrDefault(emp.getId(), new HashMap<>());
            
            // 获取该员工的请假记录
            List<LeaveApplication> empLeaves = approvedLeaves.stream()
                    .filter(l -> l.getEmployeeId().equals(emp.getId()))
                    .collect(Collectors.toList());
            
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = LocalDate.of(year, month, day);
                boolean isWeekend = date.getDayOfWeek().getValue() >= 6;
                
                // 跳过未来日期
                if (date.isAfter(LocalDate.now())) {
                    dailyStatus.put(day, -2); // -2表示未来
                    continue;
                }
                
                List<AttClockRecord> dayRecords = empRecords.get(date);
                
                // 检查当天是否有请假
                boolean isOnLeave = empLeaves.stream().anyMatch(l -> {
                    LocalDate leaveStart = l.getStartTime().toLocalDate();
                    LocalDate leaveEnd = l.getEndTime().toLocalDate();
                    return !date.isBefore(leaveStart) && !date.isAfter(leaveEnd);
                });
                
                if (isWeekend) {
                    // 周末：有打卡记录即算全天加班
                    if (dayRecords != null && !dayRecords.isEmpty()) {
                        dailyStatus.put(day, 4); // 4=周末加班
                    } else {
                        dailyStatus.put(day, -1); // -1表示周末休息
                    }
                } else {
                    // 工作日
                    if (dayRecords == null || dayRecords.isEmpty()) {
                        // 无打卡记录：判断是请假还是缺勤
                        if (isOnLeave) {
                            dailyStatus.put(day, 5); // 5=请假
                        } else {
                            dailyStatus.put(day, 0); // 0=缺勤
                        }
                    } else {
                        boolean hasClockIn = dayRecords.stream().anyMatch(r -> r.getClockType() == 1);
                        boolean hasClockOut = dayRecords.stream().anyMatch(r -> r.getClockType() == 2);
                        if (hasClockIn && hasClockOut) {
                            dailyStatus.put(day, 1); // 1=正常
                        } else if (hasClockIn) {
                            dailyStatus.put(day, 2); // 2=只有上班卡
                        } else {
                            dailyStatus.put(day, 3); // 3=只有下班卡
                        }
                    }
                }
            }
            row.put("days", dailyStatus);
            rows.add(row);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("year", year);
        result.put("month", month);
        result.put("daysInMonth", daysInMonth);
        result.put("rows", rows);
        
        return Result.success(result);
    }

    @Operation(summary = "导出考勤记录", description = "导出指定月份所有员工考勤记录Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public void exportAttendance(
            @Parameter(description = "年份") @RequestParam Integer year,
            @Parameter(description = "月份") @RequestParam Integer month,
            @AuthenticationPrincipal LoginUser loginUser,
            HttpServletResponse response) throws IOException {

        // 检查当前用户是否是经理
        boolean isManager = loginUser.getRoles().contains("MANAGER");
        Long managerDeptId = null;

        // 如果是经理，获取其所属部门ID
        if (isManager && loginUser.getUser().getEmployeeId() != null) {
            EmpEmployee managerEmp = employeeService.getById(loginUser.getUser().getEmployeeId());
            if (managerEmp != null) {
                managerDeptId = managerEmp.getDeptId();
            }
        }

        // 获取所有在职员工
        LambdaQueryWrapper<EmpEmployee> empQuery = new LambdaQueryWrapper<EmpEmployee>()
                .eq(EmpEmployee::getEmpStatus, 1)
                .orderByAsc(EmpEmployee::getDeptId)
                .orderByAsc(EmpEmployee::getEmpCode);

        // 如果是经理，只查询本部门员工
        if (isManager && managerDeptId != null) {
            empQuery.eq(EmpEmployee::getDeptId, managerDeptId);
        }

        List<EmpEmployee> employees = employeeService.list(empQuery);
        
        // 获取该月所有打卡记录
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        
        List<AttClockRecord> allRecords = clockRecordService.list(
                new LambdaQueryWrapper<AttClockRecord>()
                        .ge(AttClockRecord::getClockDate, startDate)
                        .le(AttClockRecord::getClockDate, endDate));
        
        Map<Long, Map<LocalDate, List<AttClockRecord>>> recordMap = allRecords.stream()
                .collect(Collectors.groupingBy(AttClockRecord::getEmployeeId,
                        Collectors.groupingBy(AttClockRecord::getClockDate)));
        
        // 创建Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("考勤记录");
        
        // 创建样式
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        
        CellStyle normalStyle = workbook.createCellStyle();
        normalStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        normalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        normalStyle.setAlignment(HorizontalAlignment.CENTER);
        
        CellStyle absentStyle = workbook.createCellStyle();
        absentStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
        absentStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        absentStyle.setAlignment(HorizontalAlignment.CENTER);
        
        CellStyle weekendStyle = workbook.createCellStyle();
        weekendStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        weekendStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        weekendStyle.setAlignment(HorizontalAlignment.CENTER);
        
        // 表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("工号");
        headerRow.createCell(1).setCellValue("姓名");
        for (int day = 1; day <= daysInMonth; day++) {
            Cell cell = headerRow.createCell(day + 1);
            cell.setCellValue(day + "日");
            cell.setCellStyle(headerStyle);
        }
        headerRow.createCell(daysInMonth + 2).setCellValue("出勤天数");
        headerRow.createCell(daysInMonth + 3).setCellValue("缺勤天数");
        
        // 数据行
        int rowNum = 1;
        for (EmpEmployee emp : employees) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(emp.getEmpCode());
            row.createCell(1).setCellValue(emp.getEmpName());
            
            Map<LocalDate, List<AttClockRecord>> empRecords = recordMap.getOrDefault(emp.getId(), new HashMap<>());
            int attendCount = 0;
            int absentCount = 0;
            
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = LocalDate.of(year, month, day);
                Cell cell = row.createCell(day + 1);
                
                if (date.getDayOfWeek().getValue() >= 6) {
                    cell.setCellValue("休");
                    cell.setCellStyle(weekendStyle);
                } else if (date.isAfter(LocalDate.now())) {
                    cell.setCellValue("-");
                } else {
                    List<AttClockRecord> dayRecords = empRecords.get(date);
                    if (dayRecords == null || dayRecords.isEmpty()) {
                        cell.setCellValue("缺");
                        cell.setCellStyle(absentStyle);
                        absentCount++;
                    } else {
                        boolean hasClockIn = dayRecords.stream().anyMatch(r -> r.getClockType() == 1);
                        boolean hasClockOut = dayRecords.stream().anyMatch(r -> r.getClockType() == 2);
                        if (hasClockIn && hasClockOut) {
                            cell.setCellValue("√");
                            cell.setCellStyle(normalStyle);
                            attendCount++;
                        } else {
                            cell.setCellValue("半");
                            attendCount++;
                        }
                    }
                }
            }
            
            row.createCell(daysInMonth + 2).setCellValue(attendCount);
            row.createCell(daysInMonth + 3).setCellValue(absentCount);
        }
        
        // 调整列宽
        sheet.setColumnWidth(0, 3000);
        sheet.setColumnWidth(1, 3000);
        for (int i = 2; i <= daysInMonth + 3; i++) {
            sheet.setColumnWidth(i, 1500);
        }
        
        // 输出
        String fileName = URLEncoder.encode(year + "年" + month + "月考勤记录.xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
