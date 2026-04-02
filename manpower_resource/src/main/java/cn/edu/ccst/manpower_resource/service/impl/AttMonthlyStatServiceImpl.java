package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.entity.AttDailyStat;
import cn.edu.ccst.manpower_resource.entity.AttMonthlyStat;
import cn.edu.ccst.manpower_resource.mapper.AttDailyStatMapper;
import cn.edu.ccst.manpower_resource.mapper.AttMonthlyStatMapper;
import cn.edu.ccst.manpower_resource.mapper.LeaveApplicationMapper;
import cn.edu.ccst.manpower_resource.mapper.EmpEmployeeMapper;
import cn.edu.ccst.manpower_resource.entity.LeaveApplication;
import cn.edu.ccst.manpower_resource.entity.EmpEmployee;
import cn.edu.ccst.manpower_resource.service.IAttMonthlyStatService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 考勤月度汇总表 服务实现类
 * </p>
 *
 * @author
 * @since 2025-12-17
 */
@Service
@RequiredArgsConstructor
public class AttMonthlyStatServiceImpl extends ServiceImpl<AttMonthlyStatMapper, AttMonthlyStat> implements IAttMonthlyStatService {

    private final AttDailyStatMapper dailyStatMapper;
    private final LeaveApplicationMapper leaveMapper;
    private final EmpEmployeeMapper employeeMapper;

    @Override
    @Transactional
    public int generateMonthlyStats(LocalDate startDate, LocalDate endDate, Long employeeId) {
        // 获取需要生成统计的员工列表
        List<Long> employeeIds;
        if (employeeId != null) {
            employeeIds = List.of(employeeId);
        } else {
            employeeIds = employeeMapper.selectObjs(new LambdaQueryWrapper<EmpEmployee>()
                    .select(EmpEmployee::getId)
                    .eq(EmpEmployee::getDeleted, 0));
        }

        int count = 0;
        LocalDate currentStart = startDate != null ? startDate : LocalDate.now().minusMonths(6);
        LocalDate currentEnd = endDate != null ? endDate : LocalDate.now();

        // 按月遍历
        LocalDate monthDate = currentStart.withDayOfMonth(1);
        while (!monthDate.isAfter(currentEnd)) {
            LocalDate monthEnd = monthDate.withDayOfMonth(monthDate.lengthOfMonth());
            for (Long empId : employeeIds) {
                if (generateMonthStat(empId, monthDate.getYear(), monthDate.getMonthValue())) {
                    count++;
                }
            }
            monthDate = monthDate.plusMonths(1);
        }

        return count;
    }

    private boolean generateMonthStat(Long employeeId, int year, int month) {
        // 检查是否已存在
        Long existCount = baseMapper.selectCount(new LambdaQueryWrapper<AttMonthlyStat>()
                .eq(AttMonthlyStat::getEmployeeId, employeeId)
                .eq(AttMonthlyStat::getStatYear, year)
                .eq(AttMonthlyStat::getStatMonth, month));
        if (existCount > 0) {
            return false;
        }

        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        // 获取该月的日度统计数据
        List<AttDailyStat> dailyStats = dailyStatMapper.selectList(
                new LambdaQueryWrapper<AttDailyStat>()
                        .eq(AttDailyStat::getEmployeeId, employeeId)
                        .ge(AttDailyStat::getStatDate, monthStart)
                        .le(AttDailyStat::getStatDate, monthEnd));

        // 获取该月的请假总天数
        List<LeaveApplication> leaves = leaveMapper.selectList(
                new LambdaQueryWrapper<LeaveApplication>()
                        .eq(LeaveApplication::getEmployeeId, employeeId)
                        .ge(LeaveApplication::getStartTime, monthStart.atStartOfDay())
                        .le(LeaveApplication::getEndTime, monthEnd.atTime(23, 59, 59))
                        .eq(LeaveApplication::getStatus, 2) // 已通过
        );
        double leaveDays = leaves.stream()
                .mapToDouble(l -> l.getDuration() != null ? l.getDuration().doubleValue() / 60 / 8 : 0.0) // 小时转工作日
                .sum();

        // 计算统计数据
        int workDays = dailyStats.stream()
                .mapToInt(s -> s.getIsWorkday() != null && s.getIsWorkday() == 1 ? 1 : 0)
                .sum();

        int actualDays = dailyStats.stream()
                .filter(s -> s.getStatus() != null && s.getStatus() != 5)
                .mapToInt(s -> s.getIsWorkday() != null && s.getIsWorkday() == 1 ? 1 : 0)
                .sum();

        int lateCount = dailyStats.stream()
                .mapToInt(s -> (s.getStatus() != null && (s.getStatus() == 2 || s.getStatus() == 4)) ? 1 : 0)
                .sum();

        int earlyCount = dailyStats.stream()
                .mapToInt(s -> (s.getStatus() != null && (s.getStatus() == 3 || s.getStatus() == 4)) ? 1 : 0)
                .sum();

        int absentCount = dailyStats.stream()
                .mapToInt(s -> s.getStatus() != null && s.getStatus() == 5 ? 1 : 0)
                .sum();

        // 加班小时数（上下班打卡时间差超过8小时的部分）
        int overtimeHours = dailyStats.stream()
                .filter(s -> s.getWorkDuration() != null && s.getWorkDuration() > 480) // 8小时=480分钟
                .mapToInt(s -> (s.getWorkDuration() - 480) / 60)
                .sum();

        AttMonthlyStat stat = new AttMonthlyStat();
        stat.setEmployeeId(employeeId);
        stat.setStatYear(year);
        stat.setStatMonth(month);
        stat.setWorkDays(workDays);
        stat.setActualDays(actualDays);
        stat.setLateCount(lateCount);
        stat.setEarlyCount(earlyCount);
        stat.setAbsentCount(absentCount);
        stat.setLeaveDays(BigDecimal.valueOf(Math.round(leaveDays * 100) / 100.0));
        stat.setOvertimeHours(BigDecimal.valueOf(overtimeHours));
        stat.setTravelDays(0); // 暂不支持出差统计
        stat.setCreateTime(LocalDateTime.now());
        stat.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(stat);
        return true;
    }
}
