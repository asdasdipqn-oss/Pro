package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.entity.LeaveType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ILeaveTypeService extends IService<LeaveType> {

    List<LeaveType> listEnabled();

    void createType(LeaveType type);

    void updateType(LeaveType type);

    void deleteType(Long id);
}
