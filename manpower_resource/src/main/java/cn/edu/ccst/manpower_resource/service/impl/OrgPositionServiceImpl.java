package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.ResultCode;
import cn.edu.ccst.manpower_resource.entity.EmpEmployee;
import cn.edu.ccst.manpower_resource.entity.OrgPosition;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.EmpEmployeeMapper;
import cn.edu.ccst.manpower_resource.mapper.OrgPositionMapper;
import cn.edu.ccst.manpower_resource.service.IOrgPositionService;
import cn.edu.ccst.manpower_resource.vo.PositionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrgPositionServiceImpl extends ServiceImpl<OrgPositionMapper, OrgPosition> implements IOrgPositionService {

    private final EmpEmployeeMapper employeeMapper;

    @Override
    public List<PositionVO> listByDeptId(Long deptId) {
        return baseMapper.selectPositionWithDept(deptId);
    }

    @Override
    public List<PositionVO> listAll() {
        return baseMapper.selectPositionWithDept(null);
    }

    @Override
    @Transactional
    public void createPosition(OrgPosition position) {
        position.setDeleted(0);
        position.setCreateTime(LocalDateTime.now());
        position.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(position);
    }

    @Override
    @Transactional
    public void updatePosition(OrgPosition position) {
        OrgPosition old = baseMapper.selectById(position.getId());
        if (old == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        position.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(position);
    }

    @Override
    @Transactional
    public void deletePosition(Long id) {
        OrgPosition position = baseMapper.selectById(id);
        if (position == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        
        // 检查是否有员工使用该岗位
        Long count = employeeMapper.selectCount(new LambdaQueryWrapper<EmpEmployee>()
                .eq(EmpEmployee::getPositionId, id)
                .eq(EmpEmployee::getDeleted, 0));
        if (count > 0) {
            throw new BusinessException("该岗位下有" + count + "名员工，不能删除");
        }
        
        // 使用 MyBatis Plus 的逻辑删除
        removeById(id);
    }
}
