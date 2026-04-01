package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.dto.ClockInDTO;
import cn.edu.ccst.manpower_resource.entity.AttClockLocation;
import cn.edu.ccst.manpower_resource.entity.AttClockRecord;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.AttClockRecordMapper;
import cn.edu.ccst.manpower_resource.service.IAttClockLocationService;
import cn.edu.ccst.manpower_resource.service.IAttClockRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttClockRecordServiceImpl extends ServiceImpl<AttClockRecordMapper, AttClockRecord> implements IAttClockRecordService {

    private final IAttClockLocationService locationService;
    
    // 打卡时间限制
    private static final LocalTime CLOCK_MIN_TIME = LocalTime.of(6, 0);   // 最早打卡时间 6:00
    private static final LocalTime CLOCK_MAX_TIME = LocalTime.of(23, 0);  // 最晚打卡时间 23:00

    @Override
    @Transactional
    public AttClockRecord clockIn(Long employeeId, ClockInDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        LocalTime currentTime = now.toLocalTime();
        
        // 1. 校验打卡时间是否在合理范围内
        if (currentTime.isBefore(CLOCK_MIN_TIME) || currentTime.isAfter(CLOCK_MAX_TIME)) {
            throw new BusinessException("打卡时间必须在 " + CLOCK_MIN_TIME + " 至 " + CLOCK_MAX_TIME + " 之间");
        }
        
        // 2. 校验同一天同一类型是否已打卡
        Long existingCount = baseMapper.selectCount(new LambdaQueryWrapper<AttClockRecord>()
                .eq(AttClockRecord::getEmployeeId, employeeId)
                .eq(AttClockRecord::getClockDate, today)
                .eq(AttClockRecord::getClockType, dto.getClockType()));
        if (existingCount > 0) {
            String clockTypeName = dto.getClockType() == 1 ? "上班" : "下班";
            throw new BusinessException("今天已经打过" + clockTypeName + "卡，不能重复打卡");
        }
        
        // 3. 验证打卡位置
        AttClockLocation matchedLocation = null;
        int locationStatus = 0; // 默认异常
        
        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            matchedLocation = locationService.validateLocation(dto.getLatitude(), dto.getLongitude());
            if (matchedLocation != null) {
                locationStatus = 1; // 在范围内，正常
            }
        }
        
        // 4. 创建打卡记录
        AttClockRecord record = new AttClockRecord();
        record.setEmployeeId(employeeId);
        record.setClockDate(today);
        record.setClockType(dto.getClockType());
        record.setClockTime(now);
        record.setClockLatitude(dto.getLatitude());
        record.setClockLongitude(dto.getLongitude());
        record.setClockAddress(dto.getAddress());
        record.setLocationStatus(locationStatus);
        record.setDeviceInfo(dto.getDeviceInfo());
        record.setRemark(dto.getRemark());
        record.setCreateTime(now);
        baseMapper.insert(record);
        return record;
    }

    @Override
    public List<AttClockRecord> getTodayRecords(Long employeeId) {
        return getRecordsByDate(employeeId, LocalDate.now());
    }

    @Override
    public List<AttClockRecord> getRecordsByDate(Long employeeId, LocalDate date) {
        return baseMapper.selectList(new LambdaQueryWrapper<AttClockRecord>()
                .eq(AttClockRecord::getEmployeeId, employeeId)
                .eq(AttClockRecord::getClockDate, date)
                .orderByAsc(AttClockRecord::getClockTime));
    }

    @Override
    public List<AttClockRecord> getRecordsByMonth(Long employeeId, Integer year, Integer month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return baseMapper.selectList(new LambdaQueryWrapper<AttClockRecord>()
                .eq(AttClockRecord::getEmployeeId, employeeId)
                .between(AttClockRecord::getClockDate, startDate, endDate)
                .orderByAsc(AttClockRecord::getClockDate)
                .orderByAsc(AttClockRecord::getClockTime));
    }
}
