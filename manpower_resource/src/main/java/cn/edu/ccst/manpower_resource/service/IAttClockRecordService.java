package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.dto.ClockInDTO;
import cn.edu.ccst.manpower_resource.entity.AttClockRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;

public interface IAttClockRecordService extends IService<AttClockRecord> {

    AttClockRecord clockIn(Long employeeId, ClockInDTO dto);

    List<AttClockRecord> getTodayRecords(Long employeeId);

    List<AttClockRecord> getRecordsByDate(Long employeeId, LocalDate date);

    List<AttClockRecord> getRecordsByMonth(Long employeeId, Integer year, Integer month);
}
