package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.entity.AttClockRecord;
import cn.edu.ccst.manpower_resource.entity.AttDailyStat;
import cn.edu.ccst.manpower_resource.entity.AttRule;
import cn.edu.ccst.manpower_resource.entity.LeaveApplication;
import cn.edu.ccst.manpower_resource.mapper.AttClockRecordMapper;
import cn.edu.ccst.manpower_resource.mapper.AttDailyStatMapper;
import cn.edu.ccst.manpower_resource.mapper.AttRuleMapper;
import cn.edu.ccst.manpower_resource.mapper.LeaveApplicationMapper;
import cn.edu.ccst.manpower_resource.service.IAttDailyStatService;
import cn.edu.ccst.manpower_resource.mapper.EmpEmployeeMapper;
import cn.edu.ccst.manpower_resource.entity.EmpEmployee;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 考勤日统计表 服务实现类
 * </p>
 *
 * @author
 * @since 2025-12-17
 */
@Service
@RequiredArgsConstructor
public class AttDailyStatServiceImpl extends ServiceImpl<AttDailyStatMapper, AttDailyStat> implements IAttDailyStatService {

    private final AttClockRecordMapper clockRecordMapper;
    private final AttRuleMapper ruleMapper;
    private final LeaveApplicationMapper leaveMapper;
    private final EmpEmployeeMapper employeeMapper;

    @Override
    @Transactional
    public int generateDailyStats(LocalDate startDate, LocalDate endDate, Long employeeId) {
        // 获取默认考勤规则
        AttRule rule = ruleMapper.selectOne(new LambdaQueryWrapper<AttRule>()
                .eq(AttRule::getStatus, 1)
                .orderByDesc(AttRule::getIsDefault)
                .last("LIMIT 1"));

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
        LocalDate currentDate = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        while (!currentDate.isAfter(end)) {
            for (Long empId : employeeIds) {
                if (generateDayStat(currentDate, empId, rule)) {
                    count++;
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        return count;
    }

    private boolean generateDayStat(LocalDate statDate, Long employeeId, AttRule rule) {
        // 检查是否已存在
        Long existCount = baseMapper.selectCount(new LambdaQueryWrapper<AttDailyStat>()
                .eq(AttDailyStat::getEmployeeId, employeeId)
                .eq(AttDailyStat::getStatDate, statDate));
        if (existCount > 0) {
            return false;
        }

        // 获取当日打卡记录
        List<AttClockRecord> clockRecords = clockRecordMapper.selectList(
                new LambdaQueryWrapper<AttClockRecord>()
                        .eq(AttClockRecord::getEmployeeId, employeeId)
                        .eq(AttClockRecord::getClockDate, statDate)
                        .orderByAsc(AttClockRecord::getClockTime));

        // 检查是否请假
        LeaveApplication leave = leaveMapper.selectOne(new LambdaQueryWrapper<LeaveApplication>()
                .eq(LeaveApplication::getEmployeeId, employeeId)
                .le(LeaveApplication::getStartTime, statDate.atTime(23, 59, 59))
                .ge(LeaveApplication::getEndTime, statDate.atStartOfDay())
                .eq(LeaveApplication::getStatus, 2) // 已通过
                .last("LIMIT 1"));

        if (leave != null) {
            // 请假状态
            AttDailyStat stat = new AttDailyStat();
            stat.setEmployeeId(employeeId);
            stat.setStatDate(statDate);
            stat.setStatus(6); // 请假
            stat.setIsWorkday(1);
            stat.setRemark(leave.getReason());
            stat.setCreateTime(LocalDateTime.now());
            stat.setUpdateTime(LocalDateTime.now());
            baseMapper.insert(stat);
            return true;
        }

        // 没有打卡记录
        if (clockRecords.isEmpty()) {
            // 检查是否是工作日（这里简化处理，周一到周五为工作日）
            int dayOfWeek = statDate.getDayOfWeek().getValue();
            int isWorkday = (dayOfWeek >= 1 && dayOfWeek <= 5) ? 1 : 0;

            AttDailyStat stat = new AttDailyStat();
            stat.setEmployeeId(employeeId);
            stat.setStatDate(statDate);
            stat.setStatus(isWorkday == 1 ? 5 : 0); // 工作日缺勤或非工作日未打卡
            stat.setIsWorkday(isWorkday);
            stat.setCreateTime(LocalDateTime.now());
            stat.setUpdateTime(LocalDateTime.now());
            baseMapper.insert(stat);
            return true;
        }

        // 有打卡记录，分析考勤状态
        LocalDateTime clockInTime = null;
        LocalDateTime clockOutTime = null;
        int lateMinutes = 0;
        int earlyMinutes = 0;
        int status = 1; // 默认正常

        for (AttClockRecord record : clockRecords) {
            if (record.getClockType() == 1) {
                clockInTime = record.getClockTime();
                // 计算迟到
                if (rule != null && rule.getWorkStartTime() != null) {
                    LocalTime workStart = rule.getWorkStartTime();
                    LocalTime actualStart = record.getClockTime().toLocalTime();
                    if (actualStart.isAfter(workStart)) {
                        lateMinutes = (int) ChronoUnit.MINUTES.between(workStart, actualStart);
                    }
                }
            } else if (record.getClockType() == 2) {
                clockOutTime = record.getClockTime();
                // 计算早退
                if (rule != null && rule.getWorkEndTime() != null) {
                    LocalTime workEnd = rule.getWorkEndTime();
                    LocalTime actualEnd = record.getClockTime().toLocalTime();
                    if (actualEnd.isBefore(workEnd)) {
                        earlyMinutes = (int) ChronoUnit.MINUTES.between(actualEnd, workEnd);
                    }
                }
            }
        }

        // 判断考勤状态
        if (lateMinutes > 0 && earlyMinutes > 0) {
            status = 4; // 迟到+早退
        } else if (lateMinutes > 0) {
            status = 2; // 迟到
        } else if (earlyMinutes > 0) {
            status = 3; // 早退
        }

        // 计算工作时长
        int workDuration = 0;
        if (clockInTime != null && clockOutTime != null) {
            workDuration = (int) ChronoUnit.MINUTES.between(clockInTime, clockOutTime);
        }

        int dayOfWeek = statDate.getDayOfWeek().getValue();
        int isWorkday = (dayOfWeek >= 1 && dayOfWeek <= 5) ? 1 : 0;

        AttDailyStat stat = new AttDailyStat();
        stat.setEmployeeId(employeeId);
        stat.setStatDate(statDate);
        stat.setClockInTime(clockInTime);
        stat.setClockOutTime(clockOutTime);
        stat.setWorkDuration(workDuration);
        stat.setStatus(status);
        stat.setLateMinutes(lateMinutes > 0 ? lateMinutes : null);
        stat.setEarlyMinutes(earlyMinutes > 0 ? earlyMinutes : null);
        stat.setIsWorkday(isWorkday);
        stat.setCreateTime(LocalDateTime.now());
        stat.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(stat);
        return true;
    }
}
