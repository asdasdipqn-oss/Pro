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

        List<AttDailyStat> batchList = new java.util.ArrayList<>();
        int batchSize = 100;

        while (!currentDate.isAfter(end)) {
            // 批量查询当天已存在的记录
            List<AttDailyStat> existingStats = baseMapper.selectList(
                    new LambdaQueryWrapper<AttDailyStat>()
                            .eq(AttDailyStat::getStatDate, currentDate)
                            .in(AttDailyStat::getEmployeeId, employeeIds));
            Map<Long, AttDailyStat> existingMap = existingStats.stream()
                    .collect(Collectors.toMap(AttDailyStat::getEmployeeId, s -> s, (a, b) -> a));

            // 批量查询当天所有打卡记录
            List<AttClockRecord> allClockRecords = clockRecordMapper.selectList(
                    new LambdaQueryWrapper<AttClockRecord>()
                            .eq(AttClockRecord::getClockDate, currentDate)
                            .in(AttClockRecord::getEmployeeId, employeeIds)
                            .orderByAsc(AttClockRecord::getClockTime));
            Map<Long, List<AttClockRecord>> clockMap = allClockRecords.stream()
                    .collect(Collectors.groupingBy(AttClockRecord::getEmployeeId));

            // 批量查询当天所有已通过的请假记录
            List<LeaveApplication> allLeaves = leaveMapper.selectList(
                    new LambdaQueryWrapper<LeaveApplication>()
                            .in(LeaveApplication::getEmployeeId, employeeIds)
                            .le(LeaveApplication::getStartTime, currentDate.atTime(23, 59, 59))
                            .ge(LeaveApplication::getEndTime, currentDate.atStartOfDay())
                            .eq(LeaveApplication::getStatus, 2));
            Map<Long, LeaveApplication> leaveMap = allLeaves.stream()
                    .collect(Collectors.toMap(LeaveApplication::getEmployeeId, l -> l, (a, b) -> a));

            for (Long empId : employeeIds) {
                if (existingMap.containsKey(empId)) continue;
                AttDailyStat stat = buildDayStat(currentDate, empId, rule,
                        clockMap.getOrDefault(empId, List.of()),
                        leaveMap.get(empId));
                if (stat != null) {
                    batchList.add(stat);
                    count++;
                    if (batchList.size() >= batchSize) {
                        saveBatch(batchList);
                        batchList.clear();
                    }
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        // 保存剩余
        if (!batchList.isEmpty()) {
            saveBatch(batchList);
        }

        return count;
    }

    private AttDailyStat buildDayStat(LocalDate statDate, Long employeeId, AttRule rule,
                                       List<AttClockRecord> clockRecords, LeaveApplication leave) {
        int dayOfWeek = statDate.getDayOfWeek().getValue();
        int isWorkday = (dayOfWeek >= 1 && dayOfWeek <= 5) ? 1 : 0;

        if (leave != null) {
            AttDailyStat stat = new AttDailyStat();
            stat.setEmployeeId(employeeId);
            stat.setStatDate(statDate);
            stat.setStatus(6);
            stat.setIsWorkday(1);
            stat.setRemark(leave.getReason());
            stat.setCreateTime(LocalDateTime.now());
            stat.setUpdateTime(LocalDateTime.now());
            return stat;
        }

        if (clockRecords.isEmpty()) {
            AttDailyStat stat = new AttDailyStat();
            stat.setEmployeeId(employeeId);
            stat.setStatDate(statDate);
            stat.setStatus(isWorkday == 1 ? 5 : 0);
            stat.setIsWorkday(isWorkday);
            stat.setCreateTime(LocalDateTime.now());
            stat.setUpdateTime(LocalDateTime.now());
            return stat;
        }

        LocalDateTime clockInTime = null;
        LocalDateTime clockOutTime = null;
        int lateMinutes = 0;
        int earlyMinutes = 0;
        int status = 1;

        for (AttClockRecord record : clockRecords) {
            if (record.getClockType() == 1) {
                clockInTime = record.getClockTime();
                if (rule != null && rule.getWorkStartTime() != null) {
                    LocalTime actualStart = record.getClockTime().toLocalTime();
                    if (actualStart.isAfter(rule.getWorkStartTime())) {
                        lateMinutes = (int) ChronoUnit.MINUTES.between(rule.getWorkStartTime(), actualStart);
                    }
                }
            } else if (record.getClockType() == 2) {
                clockOutTime = record.getClockTime();
                if (rule != null && rule.getWorkEndTime() != null) {
                    LocalTime actualEnd = record.getClockTime().toLocalTime();
                    if (actualEnd.isBefore(rule.getWorkEndTime())) {
                        earlyMinutes = (int) ChronoUnit.MINUTES.between(actualEnd, rule.getWorkEndTime());
                    }
                }
            }
        }

        if (lateMinutes > 0 && earlyMinutes > 0) status = 4;
        else if (lateMinutes > 0) status = 2;
        else if (earlyMinutes > 0) status = 3;

        int workDuration = 0;
        if (clockInTime != null && clockOutTime != null) {
            workDuration = (int) ChronoUnit.MINUTES.between(clockInTime, clockOutTime);
        }

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
        return stat;
    }
}
