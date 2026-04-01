package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.mapper.*;
import cn.edu.ccst.manpower_resource.service.IDashboardService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 仪表盘服务实现
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final EmpEmployeeMapper employeeMapper;
    private final OrgDepartmentMapper departmentMapper;
    private final LeaveApplicationMapper leaveApplicationMapper;
    private final AttClockRecordMapper clockRecordMapper;

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 1. 员工总数（在职）
        Long employeeCount = employeeMapper.selectCount(
            new LambdaQueryWrapper<cn.edu.ccst.manpower_resource.entity.EmpEmployee>()
                .eq(cn.edu.ccst.manpower_resource.entity.EmpEmployee::getDeleted, 0)
                .in(cn.edu.ccst.manpower_resource.entity.EmpEmployee::getEmpStatus, 1, 3) // 在职或试用期
        );
        stats.put("employees", employeeCount);
        
        // 2. 部门数量
        Long departmentCount = departmentMapper.selectCount(
            new LambdaQueryWrapper<cn.edu.ccst.manpower_resource.entity.OrgDepartment>()
                .eq(cn.edu.ccst.manpower_resource.entity.OrgDepartment::getDeleted, 0)
                .eq(cn.edu.ccst.manpower_resource.entity.OrgDepartment::getStatus, 1)
        );
        stats.put("departments", departmentCount);
        
        // 3. 待审批假期
        Long pendingLeaveCount = leaveApplicationMapper.selectCount(
            new LambdaQueryWrapper<cn.edu.ccst.manpower_resource.entity.LeaveApplication>()
                .eq(cn.edu.ccst.manpower_resource.entity.LeaveApplication::getStatus, 0) // 待审批
        );
        stats.put("pendingLeaves", pendingLeaveCount);
        
        // 4. 今日出勤（今日已打卡上班的人数）
        LocalDate today = LocalDate.now();
        Long todayAttendance = clockRecordMapper.selectCount(
            new LambdaQueryWrapper<cn.edu.ccst.manpower_resource.entity.AttClockRecord>()
                .eq(cn.edu.ccst.manpower_resource.entity.AttClockRecord::getClockDate, today)
                .eq(cn.edu.ccst.manpower_resource.entity.AttClockRecord::getClockType, 1) // 上班打卡
        );
        stats.put("todayAttendance", todayAttendance);
        
        return stats;
    }
}
