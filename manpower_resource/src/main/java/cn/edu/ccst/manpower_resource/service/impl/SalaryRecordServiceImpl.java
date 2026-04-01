package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.ResultCode;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.dto.SalaryGenerateDTO;
import cn.edu.ccst.manpower_resource.entity.*;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.*;
import cn.edu.ccst.manpower_resource.service.ISalaryRecordService;
import cn.edu.ccst.manpower_resource.vo.SalaryRecordVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalaryRecordServiceImpl extends ServiceImpl<SalaryRecordMapper, SalaryRecord> implements ISalaryRecordService {

    // 薪资常量 - 根据岗位级别设置不同基本工资
    private static final Map<Integer, BigDecimal> POSITION_LEVEL_SALARY = Map.of(
            1, new BigDecimal("3000"),   // 初级
            2, new BigDecimal("5000"),   // 中级
            3, new BigDecimal("8000"),   // 高级
            4, new BigDecimal("12000")   // 专家
    );
    private static final BigDecimal DEFAULT_BASE_SALARY = new BigDecimal("3000");     // 默认基本工资
    private static final BigDecimal FULL_ATTENDANCE_BONUS = new BigDecimal("500");    // 全勤奖
    private static final BigDecimal UNPAID_LEAVE_DEDUCTION_PER_DAY = new BigDecimal("100");  // 无薪假每天扛100
    private static final BigDecimal ABSENT_DEDUCTION_PER_DAY = new BigDecimal("200"); // 缺勤每天扣200(比请假严重)
    private static final BigDecimal LATE_EARLY_DEDUCTION = new BigDecimal("50");      // 迟到/早退每次扛50
    
    // 社保公积金比例（个人部分）
    private static final BigDecimal SOCIAL_INSURANCE_RATE = new BigDecimal("0.08");   // 社保个人8%
    private static final BigDecimal HOUSING_FUND_RATE = new BigDecimal("0.12");       // 公积金个人12%
    private static final BigDecimal TAX_THRESHOLD = new BigDecimal("5000");           // 个税起征点
    private static final BigDecimal TAX_RATE_LEVEL1 = new BigDecimal("0.03");         // 第一档税率3%

    private final EmpEmployeeMapper employeeMapper;
    private final OrgDepartmentMapper departmentMapper;
    private final OrgPositionMapper positionMapper;
    private final AttDailyStatMapper attDailyStatMapper;
    private final AttClockRecordMapper attClockRecordMapper;
    private final AttRuleMapper attRuleMapper;
    private final LeaveApplicationMapper leaveApplicationMapper;
    private final LeaveTypeMapper leaveTypeMapper;

    @Override
    public List<SalaryRecord> getByEmployee(Long employeeId, Integer year) {
        // 普通用户只能看到已发放的薪资（status=2）
        return baseMapper.selectList(new LambdaQueryWrapper<SalaryRecord>()
                .eq(SalaryRecord::getEmployeeId, employeeId)
                .eq(SalaryRecord::getSalaryYear, year)
                .eq(SalaryRecord::getStatus, 2)  // 只查询已发放的
                .orderByDesc(SalaryRecord::getSalaryMonth));
    }

    @Override
    public SalaryRecord getByMonth(Long employeeId, Integer year, Integer month) {
        // 普通用户只能看到已发放的薪资（status=2）
        return baseMapper.selectOne(new LambdaQueryWrapper<SalaryRecord>()
                .eq(SalaryRecord::getEmployeeId, employeeId)
                .eq(SalaryRecord::getSalaryYear, year)
                .eq(SalaryRecord::getSalaryMonth, month)
                .eq(SalaryRecord::getStatus, 2));  // 只查询已发放的
    }

    @Override
    public PageResult<SalaryRecordVO> pageByMonth(Integer year, Integer month, PageQuery query) {
        Page<SalaryRecord> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SalaryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalaryRecord::getSalaryYear, year)
                .eq(SalaryRecord::getSalaryMonth, month)
                .orderByDesc(SalaryRecord::getCreateTime);
        Page<SalaryRecord> result = baseMapper.selectPage(page, wrapper);

        List<SalaryRecordVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), voList);
    }

    @Override
    @Transactional
    public void confirmSalary(Long id) {
        SalaryRecord record = baseMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (record.getStatus() != 0) {
            throw new BusinessException("只能确认草稿状态的薪资记录");
        }
        record.setStatus(1);
        record.setConfirmTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(record);
    }

    @Override
    @Transactional
    public void paySalary(Long id) {
        SalaryRecord record = baseMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (record.getStatus() != 1) {
            throw new BusinessException("请先确认薪资后再发放");
        }
        record.setStatus(2);
        record.setPayTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(record);
    }

    @Override
    @Transactional
    public int generateMonthlySalary(SalaryGenerateDTO dto) {
        // 获取需要生成薪资的员工列表
        List<EmpEmployee> employees;
        if (dto.getEmployeeIds() != null && !dto.getEmployeeIds().isEmpty()) {
            employees = employeeMapper.selectList(new LambdaQueryWrapper<EmpEmployee>()
                    .in(EmpEmployee::getId, dto.getEmployeeIds())
                    .eq(EmpEmployee::getDeleted, 0)
                    .ne(EmpEmployee::getEmpStatus, 2)); // 排除离职员工(状态2=离职)
        } else {
            // 获取所有在职员工(包括试用期)
            employees = employeeMapper.selectList(new LambdaQueryWrapper<EmpEmployee>()
                    .eq(EmpEmployee::getDeleted, 0)
                    .ne(EmpEmployee::getEmpStatus, 2)); // 排除离职员工(状态2=离职)
        }

        int count = 0;
        for (EmpEmployee emp : employees) {
            // 检查是否已存在该月薪资记录
            SalaryRecord existing = baseMapper.selectOne(new LambdaQueryWrapper<SalaryRecord>()
                    .eq(SalaryRecord::getEmployeeId, emp.getId())
                    .eq(SalaryRecord::getSalaryYear, dto.getYear())
                    .eq(SalaryRecord::getSalaryMonth, dto.getMonth()));

            if (existing != null) {
                // 已存在则跳过（如果是草稿状态可以重新计算）
                if (existing.getStatus() == 0) {
                    calculateAndUpdateSalary(existing, emp, dto.getYear(), dto.getMonth());
                    count++;
                }
                continue;
            }

            // 创建新的薪资记录
            SalaryRecord record = new SalaryRecord();
            record.setEmployeeId(emp.getId());
            record.setSalaryYear(dto.getYear());
            record.setSalaryMonth(dto.getMonth());
            record.setStatus(0); // 草稿
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());

            // 计算薪资
            calculateSalary(record, emp, dto.getYear(), dto.getMonth());

            baseMapper.insert(record);
            count++;
        }

        return count;
    }

    /**
     * 计算薪资
     */
    private void calculateSalary(SalaryRecord record, EmpEmployee emp, Integer year, Integer month) {
        // 1. 根据岗位级别设置基本工资
        BigDecimal baseSalary = DEFAULT_BASE_SALARY;
        if (emp.getPositionId() != null) {
            OrgPosition position = positionMapper.selectById(emp.getPositionId());
            if (position != null && position.getPositionLevel() != null) {
                baseSalary = POSITION_LEVEL_SALARY.getOrDefault(position.getPositionLevel(), DEFAULT_BASE_SALARY);
            }
        }
        record.setBaseSalary(baseSalary);

        // 2. 计算考勤数据
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        // 如果是当月，截止到今天
        LocalDate today = LocalDate.now();
        if (endDate.isAfter(today)) {
            endDate = today;
        }

        // 获取考勤规则（上班时间、下班时间）
        AttRule attRule = attRuleMapper.selectOne(new LambdaQueryWrapper<AttRule>()
                .eq(AttRule::getIsDefault, 1)
                .eq(AttRule::getStatus, 1)
                .last("LIMIT 1"));
        LocalTime workStartTime = attRule != null ? attRule.getWorkStartTime() : LocalTime.of(9, 0);
        LocalTime workEndTime = attRule != null ? attRule.getWorkEndTime() : LocalTime.of(18, 0);

        // 计算工作日列表（排除周末）
        List<LocalDate> workDayList = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                workDayList.add(current);
            }
            current = current.plusDays(1);
        }
        int workDays = workDayList.size();
        record.setWorkDays(workDays);

        // 查询该月所有打卡记录
        List<AttClockRecord> clockRecords = attClockRecordMapper.selectList(new LambdaQueryWrapper<AttClockRecord>()
                .eq(AttClockRecord::getEmployeeId, emp.getId())
                .ge(AttClockRecord::getClockDate, startDate)
                .le(AttClockRecord::getClockDate, endDate));

        // 按日期分组
        Map<LocalDate, List<AttClockRecord>> clockByDate = clockRecords.stream()
                .collect(Collectors.groupingBy(AttClockRecord::getClockDate));

        // 3. 计算请假天数（已通过的请假）- 区分带薪假和无薪假
        LeaveStats leaveStats = calculateLeaveStats(emp.getId(), year, month);
        record.setLeaveDays(leaveStats.totalLeaveDays);

        // 获取请假日期列表
        Set<LocalDate> leaveDates = getLeaveDates(emp.getId(), year, month);

        // 统计考勤情况
        int actualDays = 0;      // 实际出勤天数
        int lateOnlyCount = 0;   // 只迟到次数
        int earlyOnlyCount = 0;  // 只早退次数
        int lateAndEarlyCount = 0; // 迟到+早退次数
        int absentCount = 0;     // 缺勤天数

        for (LocalDate workDay : workDayList) {
            // 如果是请假日，跳过
            if (leaveDates.contains(workDay)) {
                continue;
            }

            List<AttClockRecord> dayRecords = clockByDate.get(workDay);
            if (dayRecords == null || dayRecords.isEmpty()) {
                // 没有任何打卡记录 = 缺勤
                absentCount++;
                continue;
            }

            // 检查上班打卡
            AttClockRecord clockIn = dayRecords.stream()
                    .filter(r -> r.getClockType() == 1)
                    .findFirst().orElse(null);
            // 检查下班打卡
            AttClockRecord clockOut = dayRecords.stream()
                    .filter(r -> r.getClockType() == 2)
                    .findFirst().orElse(null);

            if (clockIn == null && clockOut == null) {
                // 有记录但没有上下班卡 = 缺勤
                absentCount++;
                continue;
            }

            // 有打卡记录，算出勤
            actualDays++;

            // 检查迟到（上班打卡时间晚于规定时间）
            boolean isLate = false;
            if (clockIn != null && clockIn.getClockTime() != null) {
                LocalTime clockInTime = clockIn.getClockTime().toLocalTime();
                isLate = clockInTime.isAfter(workStartTime);
            } else {
                // 没有上班卡，算迟到
                isLate = true;
            }

            // 检查早退（下班打卡时间早于规定时间）
            boolean isEarly = false;
            if (clockOut != null && clockOut.getClockTime() != null) {
                LocalTime clockOutTime = clockOut.getClockTime().toLocalTime();
                isEarly = clockOutTime.isBefore(workEndTime);
            } else {
                // 没有下班卡，算早退
                isEarly = true;
            }

            // 统计迟到早退
            if (isLate && isEarly) {
                lateAndEarlyCount++;
            } else if (isLate) {
                lateOnlyCount++;
            } else if (isEarly) {
                earlyOnlyCount++;
            }
        }

        record.setActualDays(actualDays);
        record.setLateCount(lateOnlyCount + earlyOnlyCount + lateAndEarlyCount);

        // 4. 计算周末加班费（双倍工资）
        BigDecimal dailyWage = baseSalary.divide(BigDecimal.valueOf(22), 2, RoundingMode.HALF_UP); // 日薪 = 基本工资/22
        BigDecimal overtimePay = calculateOvertimePay(emp.getId(), year, month, dailyWage);
        record.setOvertimePay(overtimePay);

        // 5. 计算考勤扣款
        // 扣款规则：
        // - 无薪假一天扣100，带薪假不扣款
        // - 缺勤一天扣200  
        // - 只迟到扣%50
        // - 只早退扣%50
        // - 迟到+早退扣100（各%50）
        BigDecimal unpaidLeaveDeduction = leaveStats.unpaidLeaveDays.multiply(UNPAID_LEAVE_DEDUCTION_PER_DAY);
        BigDecimal absentDeduction = BigDecimal.valueOf(absentCount).multiply(ABSENT_DEDUCTION_PER_DAY);
        BigDecimal lateOnlyDeduction = BigDecimal.valueOf(lateOnlyCount).multiply(LATE_EARLY_DEDUCTION);
        BigDecimal earlyOnlyDeduction = BigDecimal.valueOf(earlyOnlyCount).multiply(LATE_EARLY_DEDUCTION);
        BigDecimal lateAndEarlyDeduction = BigDecimal.valueOf(lateAndEarlyCount).multiply(LATE_EARLY_DEDUCTION.multiply(BigDecimal.valueOf(2)));
        
        BigDecimal totalAttendanceDeduction = unpaidLeaveDeduction
                .add(absentDeduction)
                .add(lateOnlyDeduction)
                .add(earlyOnlyDeduction)
                .add(lateAndEarlyDeduction);
        
        // 全勤奖：无缺勤、无迟到早退才有
        BigDecimal actualFullAttendanceBonus = BigDecimal.ZERO;
        if (absentCount == 0 && lateOnlyCount == 0 && earlyOnlyCount == 0 && lateAndEarlyCount == 0) {
            actualFullAttendanceBonus = FULL_ATTENDANCE_BONUS;
        }
        record.setFullAttendanceBonus(actualFullAttendanceBonus);

        // 6. 其他薪资项目（可扩展）
        record.setPositionSalary(BigDecimal.ZERO);
        record.setPerformanceSalary(BigDecimal.ZERO);
        // overtimePay 已在上面计算
        record.setAllowance(BigDecimal.ZERO);
        record.setBonus(BigDecimal.ZERO);

        // 7. 计算应发工资（基本工资 + 全勤奖 + 加班费 + 其他）
        BigDecimal grossSalary = baseSalary
                .add(actualFullAttendanceBonus)
                .add(record.getPositionSalary())
                .add(record.getPerformanceSalary())
                .add(record.getOvertimePay())
                .add(record.getAllowance())
                .add(record.getBonus());
        record.setGrossSalary(grossSalary);

        // 8. 扣款项目 - 社保公积金个税
        // 社保基数以基本工资为准
        BigDecimal socialInsurance = baseSalary.multiply(SOCIAL_INSURANCE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal housingFund = baseSalary.multiply(HOUSING_FUND_RATE).setScale(2, RoundingMode.HALF_UP);
        
        // 个税计算：应纳税所得额 = 应发工资 - 社保 - 公积金 - 5000
        BigDecimal taxableIncome = grossSalary.subtract(socialInsurance).subtract(housingFund).subtract(TAX_THRESHOLD);
        BigDecimal personalTax = BigDecimal.ZERO;
        if (taxableIncome.compareTo(BigDecimal.ZERO) > 0) {
            // 简化税率：超过5000的部分按%3%计算
            personalTax = taxableIncome.multiply(TAX_RATE_LEVEL1).setScale(2, RoundingMode.HALF_UP);
        }
        
        record.setSocialInsurance(socialInsurance);
        record.setHousingFund(housingFund);
        record.setPersonalTax(personalTax);
        // 考勤扣款记录到其他扣款字段
        record.setOtherDeduction(totalAttendanceDeduction);

        BigDecimal totalDeduction = socialInsurance
                .add(housingFund)
                .add(personalTax)
                .add(totalAttendanceDeduction);
        record.setTotalDeduction(totalDeduction);

        // 9. 实发工资 = 应发 - 扣款
        BigDecimal netSalary = grossSalary.subtract(totalDeduction);
        record.setNetSalary(netSalary);

        // 备注
        StringBuilder remark = new StringBuilder();
        remark.append("基本工资:").append(baseSalary).append("元");
        if (actualFullAttendanceBonus.compareTo(BigDecimal.ZERO) > 0) {
            remark.append(",全勤奖:").append(actualFullAttendanceBonus).append("元");
        }
        remark.append(",社保:").append(socialInsurance).append("元");
        remark.append(",公积金:").append(housingFund).append("元");
        if (personalTax.compareTo(BigDecimal.ZERO) > 0) {
            remark.append(",个税:").append(personalTax).append("元");
        }
        if (overtimePay.compareTo(BigDecimal.ZERO) > 0) {
            remark.append(",周末加班费:").append(overtimePay).append("元");
        }
        if (leaveStats.paidLeaveDays.compareTo(BigDecimal.ZERO) > 0) {
            remark.append(",带薪假").append(leaveStats.paidLeaveDays).append("天(不扣款)");
        }
        if (leaveStats.unpaidLeaveDays.compareTo(BigDecimal.ZERO) > 0) {
            remark.append(",无薪假").append(leaveStats.unpaidLeaveDays).append("天扣").append(unpaidLeaveDeduction).append("元");
        }
        if (absentCount > 0) {
            remark.append(",缺勤").append(absentCount).append("天扣").append(absentDeduction).append("元");
        }
        if (lateOnlyCount > 0) {
            remark.append(",迟到").append(lateOnlyCount).append("次扣").append(lateOnlyDeduction).append("元");
        }
        if (earlyOnlyCount > 0) {
            remark.append(",早退").append(earlyOnlyCount).append("次扣").append(earlyOnlyDeduction).append("元");
        }
        if (lateAndEarlyCount > 0) {
            remark.append(",迟到+早退").append(lateAndEarlyCount).append("次扣").append(lateAndEarlyDeduction).append("元");
        }
        record.setRemark(remark.toString());
    }
    
    /**
     * 请假统计结果
     */
    private static class LeaveStats {
        BigDecimal totalLeaveDays = BigDecimal.ZERO;  // 总请假天数
        BigDecimal paidLeaveDays = BigDecimal.ZERO;   // 带薪假天数
        BigDecimal unpaidLeaveDays = BigDecimal.ZERO; // 无薪假天数
    }
    
    /**
     * 计算周末加班费（双倍工资）
     * @param employeeId 员工ID
     * @param year 年份
     * @param month 月份
     * @param dailyWage 日薪
     * @return 加班费
     */
    private BigDecimal calculateOvertimePay(Long employeeId, Integer year, Integer month, BigDecimal dailyWage) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        LocalDate today = LocalDate.now();
        if (endDate.isAfter(today)) {
            endDate = today;
        }
        
        // 查询该月所有打卡记录
        List<AttClockRecord> clockRecords = attClockRecordMapper.selectList(new LambdaQueryWrapper<AttClockRecord>()
                .eq(AttClockRecord::getEmployeeId, employeeId)
                .ge(AttClockRecord::getClockDate, startDate)
                .le(AttClockRecord::getClockDate, endDate));
        
        // 按日期分组
        Map<LocalDate, List<AttClockRecord>> clockByDate = clockRecords.stream()
                .collect(Collectors.groupingBy(AttClockRecord::getClockDate));
        
        // 统计周末加班（打卡即算全天）
        BigDecimal overtimeDays = BigDecimal.ZERO;
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            // 只统计周末
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                List<AttClockRecord> dayRecords = clockByDate.get(current);
                if (dayRecords != null && !dayRecords.isEmpty()) {
                    // 有打卡记录即算全天加班
                    overtimeDays = overtimeDays.add(BigDecimal.ONE);
                }
            }
            current = current.plusDays(1);
        }
        
        // 加班费 = 日薪 * 2 * 加班天数
        BigDecimal overtimePay = dailyWage.multiply(BigDecimal.valueOf(2)).multiply(overtimeDays);
        
        return overtimePay.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 重新计算并更新薪资
     */
    private void calculateAndUpdateSalary(SalaryRecord record, EmpEmployee emp, Integer year, Integer month) {
        calculateSalary(record, emp, year, month);
        record.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(record);
    }

    /**
     * 计算指定月份的请假统计(区分带薪假和无薪假)
     */
    private LeaveStats calculateLeaveStats(Long employeeId, Integer year, Integer month) {
        LeaveStats stats = new LeaveStats();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // 查询该月已通过的请假申请
        List<LeaveApplication> leaves = leaveApplicationMapper.selectList(new LambdaQueryWrapper<LeaveApplication>()
                .eq(LeaveApplication::getEmployeeId, employeeId)
                .eq(LeaveApplication::getStatus, 2) // 已通过
                .and(w -> w.le(LeaveApplication::getStartTime, endOfMonth)
                        .ge(LeaveApplication::getEndTime, startOfMonth)));

        for (LeaveApplication leave : leaves) {
            if (leave.getDuration() == null) continue;
            
            stats.totalLeaveDays = stats.totalLeaveDays.add(leave.getDuration());
            
            // 查询请假类型判断是否带薪
            boolean isPaid = false;
            if (leave.getLeaveTypeId() != null) {
                LeaveType leaveType = leaveTypeMapper.selectById(leave.getLeaveTypeId());
                if (leaveType != null && leaveType.getIsPaid() != null && leaveType.getIsPaid() == 1) {
                    isPaid = true;
                }
            }
            
            if (isPaid) {
                stats.paidLeaveDays = stats.paidLeaveDays.add(leave.getDuration());
            } else {
                stats.unpaidLeaveDays = stats.unpaidLeaveDays.add(leave.getDuration());
            }
        }
        return stats;
    }

    /**
     * 获取请假日期集合
     */
    private Set<LocalDate> getLeaveDates(Long employeeId, Integer year, Integer month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // 查询该月已通过的请假申请
        List<LeaveApplication> leaves = leaveApplicationMapper.selectList(new LambdaQueryWrapper<LeaveApplication>()
                .eq(LeaveApplication::getEmployeeId, employeeId)
                .eq(LeaveApplication::getStatus, 2) // 已通过
                .and(w -> w.le(LeaveApplication::getStartTime, endOfMonth)
                        .ge(LeaveApplication::getEndTime, startOfMonth)));

        Set<LocalDate> leaveDates = new HashSet<>();
        for (LeaveApplication leave : leaves) {
            LocalDate start = leave.getStartTime().toLocalDate();
            LocalDate end = leave.getEndTime().toLocalDate();
            // 限制在当月范围内
            if (start.isBefore(yearMonth.atDay(1))) {
                start = yearMonth.atDay(1);
            }
            if (end.isAfter(yearMonth.atEndOfMonth())) {
                end = yearMonth.atEndOfMonth();
            }
            // 添加请假日期
            LocalDate d = start;
            while (!d.isAfter(end)) {
                leaveDates.add(d);
                d = d.plusDays(1);
            }
        }
        return leaveDates;
    }

    @Override
    @Transactional
    public int batchPay(Integer year, Integer month) {
        // 查询所有已确认的薪资记录
        List<SalaryRecord> records = baseMapper.selectList(new LambdaQueryWrapper<SalaryRecord>()
                .eq(SalaryRecord::getSalaryYear, year)
                .eq(SalaryRecord::getSalaryMonth, month)
                .eq(SalaryRecord::getStatus, 1)); // 已确认

        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        for (SalaryRecord record : records) {
            record.setStatus(2); // 已发放
            record.setPayTime(now);
            record.setUpdateTime(now);
            baseMapper.updateById(record);
            count++;
        }
        return count;
    }

    @Override
    @Transactional
    public int batchConfirm(Integer year, Integer month) {
        // 查询所有草稿状态的薪资记录
        List<SalaryRecord> records = baseMapper.selectList(new LambdaQueryWrapper<SalaryRecord>()
                .eq(SalaryRecord::getSalaryYear, year)
                .eq(SalaryRecord::getSalaryMonth, month)
                .eq(SalaryRecord::getStatus, 0)); // 草稿

        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        for (SalaryRecord record : records) {
            record.setStatus(1); // 已确认
            record.setConfirmTime(now);
            record.setUpdateTime(now);
            baseMapper.updateById(record);
            count++;
        }
        return count;
    }

    @Override
    @Transactional
    public int batchDelete(Integer year, Integer month) {
        // 只删除草稿状态的薪资记录
        int count = baseMapper.delete(new LambdaQueryWrapper<SalaryRecord>()
                .eq(SalaryRecord::getSalaryYear, year)
                .eq(SalaryRecord::getSalaryMonth, month)
                .eq(SalaryRecord::getStatus, 0)); // 草稿
        return count;
    }

    @Override
    @Transactional
    public void recalculate(Long id) {
        SalaryRecord record = baseMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (record.getStatus() != 0) {
            throw new BusinessException("只能重新计算草稿状态的薪资记录");
        }

        EmpEmployee emp = employeeMapper.selectById(record.getEmployeeId());
        if (emp == null) {
            throw new BusinessException("员工不存在");
        }

        calculateAndUpdateSalary(record, emp, record.getSalaryYear(), record.getSalaryMonth());
    }

    @Override
    public SalarySummary getSummary(Integer year, Integer month) {
        SalarySummary summary = new SalarySummary();

        List<SalaryRecord> records = baseMapper.selectList(new LambdaQueryWrapper<SalaryRecord>()
                .eq(SalaryRecord::getSalaryYear, year)
                .eq(SalaryRecord::getSalaryMonth, month));

        summary.totalCount = records.size();
        summary.draftCount = (int) records.stream().filter(r -> r.getStatus() == 0).count();
        summary.confirmedCount = (int) records.stream().filter(r -> r.getStatus() == 1).count();
        summary.paidCount = (int) records.stream().filter(r -> r.getStatus() == 2).count();
        summary.totalAmount = records.stream()
                .map(SalaryRecord::getNetSalary)
                .filter(n -> n != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return summary;
    }

    /**
     * 转换为VO
     */
    private SalaryRecordVO convertToVO(SalaryRecord record) {
        SalaryRecordVO vo = new SalaryRecordVO();
        BeanUtils.copyProperties(record, vo);

        // 查询员工信息
        EmpEmployee emp = employeeMapper.selectById(record.getEmployeeId());
        if (emp != null) {
            vo.setEmpCode(emp.getEmpCode());
            vo.setEmpName(emp.getEmpName());

            // 查询部门名称
            if (emp.getDeptId() != null) {
                OrgDepartment dept = departmentMapper.selectById(emp.getDeptId());
                if (dept != null) {
                    vo.setDeptName(dept.getDeptName());
                }
            }

            // 查询岗位名称
            if (emp.getPositionId() != null) {
                OrgPosition position = positionMapper.selectById(emp.getPositionId());
                if (position != null) {
                    vo.setPositionName(position.getPositionName());
                }
            }
        }

        return vo;
    }

    @Override
    public void exportSalary(Integer year, Integer month, HttpServletResponse response) {
        List<SalaryRecord> records = baseMapper.selectList(new LambdaQueryWrapper<SalaryRecord>()
                .eq(SalaryRecord::getSalaryYear, year)
                .eq(SalaryRecord::getSalaryMonth, month));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("薪资明细");
            
            String[] headers = {"工号", "姓名", "部门", "基本工资", "全勤奖", "加班费", "补贴", "社保", "公积金", "个税", "实发工资", "状态"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (SalaryRecord record : records) {
                EmpEmployee emp = employeeMapper.selectById(record.getEmployeeId());
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(emp != null ? emp.getEmpCode() : "");
                row.createCell(1).setCellValue(emp != null ? emp.getEmpName() : "");
                row.createCell(2).setCellValue(getDeptName(emp));
                row.createCell(3).setCellValue(record.getBaseSalary() != null ? record.getBaseSalary().doubleValue() : 0);
                row.createCell(4).setCellValue(record.getFullAttendanceBonus() != null ? record.getFullAttendanceBonus().doubleValue() : 0);
                row.createCell(5).setCellValue(record.getOvertimePay() != null ? record.getOvertimePay().doubleValue() : 0);
                row.createCell(6).setCellValue(record.getAllowance() != null ? record.getAllowance().doubleValue() : 0);
                row.createCell(7).setCellValue(record.getSocialInsurance() != null ? record.getSocialInsurance().doubleValue() : 0);
                row.createCell(8).setCellValue(record.getHousingFund() != null ? record.getHousingFund().doubleValue() : 0);
                row.createCell(9).setCellValue(record.getIncomeTax() != null ? record.getIncomeTax().doubleValue() : 0);
                row.createCell(10).setCellValue(record.getNetSalary() != null ? record.getNetSalary().doubleValue() : 0);
                row.createCell(11).setCellValue(getStatusName(record.getStatus()));
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + 
                    URLEncoder.encode(year + "年" + month + "月薪资明细.xlsx", StandardCharsets.UTF_8));
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            throw new BusinessException("导出失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public int pushSalaryNotification(Integer year, Integer month) {
        List<SalaryRecord> records = baseMapper.selectList(new LambdaQueryWrapper<SalaryRecord>()
                .eq(SalaryRecord::getSalaryYear, year)
                .eq(SalaryRecord::getSalaryMonth, month)
                .eq(SalaryRecord::getStatus, 2) // 已发放
                .eq(SalaryRecord::getNotified, 0)); // 未推送

        for (SalaryRecord record : records) {
            record.setNotified(1);
            record.setNotifyTime(LocalDateTime.now());
            baseMapper.updateById(record);
        }
        return records.size();
    }

    @Override
    public List<SalaryRecord> getUnreadNotifications(Long employeeId) {
        return baseMapper.selectList(new LambdaQueryWrapper<SalaryRecord>()
                .eq(SalaryRecord::getEmployeeId, employeeId)
                .eq(SalaryRecord::getNotified, 1)
                .eq(SalaryRecord::getReadStatus, 0)
                .orderByDesc(SalaryRecord::getNotifyTime));
    }

    @Override
    @Transactional
    public void markAsRead(Long id, Long employeeId) {
        SalaryRecord record = baseMapper.selectById(id);
        if (record != null && record.getEmployeeId().equals(employeeId)) {
            record.setReadStatus(1);
            baseMapper.updateById(record);
        }
    }

    private String getDeptName(EmpEmployee emp) {
        if (emp == null || emp.getDeptId() == null) return "";
        OrgDepartment dept = departmentMapper.selectById(emp.getDeptId());
        return dept != null ? dept.getDeptName() : "";
    }

    private String getStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "草稿";
            case 1: return "已确认";
            case 2: return "已发放";
            default: return "";
        }
    }
}
