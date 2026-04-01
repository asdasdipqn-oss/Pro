package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.entity.OrgPosition;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IOrgPositionService extends IService<OrgPosition> {

    List<OrgPosition> listByDeptId(Long deptId);

    List<OrgPosition> listAll();

    void createPosition(OrgPosition position);

    void updatePosition(OrgPosition position);

    void deletePosition(Long id);
}
