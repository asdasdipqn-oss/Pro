package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.entity.OrgPosition;
import cn.edu.ccst.manpower_resource.vo.PositionVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IOrgPositionService extends IService<OrgPosition> {

    /**
     * 根据部门查询岗位列表（含部门名称）
     */
    List<PositionVO> listByDeptId(Long deptId);

    /**
     * 查询所有岗位列表（含部门名称）
     */
    List<PositionVO> listAll();

    void createPosition(OrgPosition position);

    void updatePosition(OrgPosition position);

    void deletePosition(Long id);
}
