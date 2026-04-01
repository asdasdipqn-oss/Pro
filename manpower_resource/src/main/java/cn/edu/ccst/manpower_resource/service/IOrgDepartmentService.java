package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.dto.OrgDepartmentDTO;
import cn.edu.ccst.manpower_resource.entity.OrgDepartment;
import cn.edu.ccst.manpower_resource.vo.DepartmentTreeVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IOrgDepartmentService extends IService<OrgDepartment> {

    List<DepartmentTreeVO> getDeptTree();

    OrgDepartment getDetail(Long id);

    void createDept(OrgDepartmentDTO dto);

    void updateDept(OrgDepartmentDTO dto);

    void deleteDept(Long id);

    List<OrgDepartment> getChildDepts(Long parentId);
}
