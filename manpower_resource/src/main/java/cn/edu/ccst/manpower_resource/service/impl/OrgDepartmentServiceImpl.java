package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.ResultCode;
import cn.edu.ccst.manpower_resource.dto.OrgDepartmentDTO;
import cn.edu.ccst.manpower_resource.entity.EmpEmployee;
import cn.edu.ccst.manpower_resource.entity.OrgDepartment;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.EmpEmployeeMapper;
import cn.edu.ccst.manpower_resource.mapper.OrgDepartmentMapper;
import cn.edu.ccst.manpower_resource.service.IOrgDepartmentService;
import cn.edu.ccst.manpower_resource.vo.DepartmentTreeVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrgDepartmentServiceImpl extends ServiceImpl<OrgDepartmentMapper, OrgDepartment> implements IOrgDepartmentService {

    private final EmpEmployeeMapper empEmployeeMapper;

    @Override
    public List<DepartmentTreeVO> getDeptTree() {
        List<OrgDepartment> all = baseMapper.selectList(new LambdaQueryWrapper<OrgDepartment>()
                .eq(OrgDepartment::getDeleted, 0)
                .orderByAsc(OrgDepartment::getSort));
        return buildTree(all, 0L);
    }

    @Override
    public OrgDepartment getDetail(Long id) {
        OrgDepartment dept = baseMapper.selectById(id);
        if (dept == null || dept.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return dept;
    }

    @Override
    @Transactional
    public void createDept(OrgDepartmentDTO dto) {
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<OrgDepartment>().eq(OrgDepartment::getDeptCode, dto.getDeptCode()));
        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "Dept code exists");
        }

        OrgDepartment dept = new OrgDepartment();
        dept.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        dept.setDeptCode(dto.getDeptCode());
        dept.setDeptName(dto.getDeptName());
        dept.setLeaderId(dto.getLeaderId());
        dept.setPhone(dto.getPhone());
        dept.setEmail(dto.getEmail());
        dept.setSort(dto.getSort() != null ? dto.getSort() : 0);
        dept.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        dept.setDeleted(0);
        dept.setCreateTime(LocalDateTime.now());
        dept.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(dept);
    }

    @Override
    @Transactional
    public void updateDept(OrgDepartmentDTO dto) {
        OrgDepartment dept = baseMapper.selectById(dto.getId());
        if (dept == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        if (dto.getParentId() != null) dept.setParentId(dto.getParentId());
        dept.setDeptName(dto.getDeptName());
        dept.setLeaderId(dto.getLeaderId());
        dept.setPhone(dto.getPhone());
        dept.setEmail(dto.getEmail());
        if (dto.getSort() != null) dept.setSort(dto.getSort());
        if (dto.getStatus() != null) dept.setStatus(dto.getStatus());
        dept.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(dept);
    }

    @Override
    @Transactional
    public void deleteDept(Long id) {
        log.info("====== deleteDept 被调用, id={} ======", id);
        
        // 检查是否存在子部门
        Long childCount = baseMapper.selectCount(new LambdaQueryWrapper<OrgDepartment>()
                .eq(OrgDepartment::getParentId, id)
                .eq(OrgDepartment::getDeleted, 0));
        log.info("子部门数量: {}", childCount);
        if (childCount > 0) {
            throw new BusinessException("该部门存在子部门，无法删除");
        }
        
        // 检查是否有员工关联
        Long empCount = empEmployeeMapper.selectCount(new LambdaQueryWrapper<EmpEmployee>()
                .eq(EmpEmployee::getDeptId, id)
                .eq(EmpEmployee::getDeleted, 0));
        log.info("员工数量: {}", empCount);
        if (empCount > 0) {
            throw new BusinessException("该部门下还有员工，无法删除");
        }

        // 使用 MyBatis-Plus 的逻辑删除
        boolean success = removeById(id);
        log.info("删除部门完成, 结果: {}", success);
    }

    @Override
    public List<OrgDepartment> getChildDepts(Long parentId) {
        return baseMapper.selectList(new LambdaQueryWrapper<OrgDepartment>()
                .eq(OrgDepartment::getParentId, parentId)
                .eq(OrgDepartment::getDeleted, 0)
                .orderByAsc(OrgDepartment::getSort));
    }

    private List<DepartmentTreeVO> buildTree(List<OrgDepartment> all, Long parentId) {
        return all.stream()
                .filter(d -> parentId.equals(d.getParentId()))
                .map(d -> {
                    DepartmentTreeVO vo = new DepartmentTreeVO();
                    vo.setId(d.getId());
                    vo.setParentId(d.getParentId());
                    vo.setDeptCode(d.getDeptCode());
                    vo.setDeptName(d.getDeptName());
                    vo.setLeaderId(d.getLeaderId());
                    vo.setStatus(d.getStatus());
                    vo.setSort(d.getSort());
                    vo.setChildren(buildTree(all, d.getId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
