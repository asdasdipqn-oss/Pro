package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.dto.LeaveApplicationDTO;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.entity.LeaveApplication;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ILeaveApplicationService extends IService<LeaveApplication> {

    LeaveApplication apply(Long employeeId, LeaveApplicationDTO dto);

    void cancel(Long id, Long employeeId);

    void approve(Long id, Long approverId, Integer status, String comment);

    PageResult<LeaveApplication> pageByEmployee(Long employeeId, PageQuery query);

    List<LeaveApplication> getPendingApprovals(Long approverId);
}
