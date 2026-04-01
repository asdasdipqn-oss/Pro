package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.ResultCode;
import cn.edu.ccst.manpower_resource.entity.LeaveType;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.LeaveTypeMapper;
import cn.edu.ccst.manpower_resource.service.ILeaveTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeaveTypeServiceImpl extends ServiceImpl<LeaveTypeMapper, LeaveType> implements ILeaveTypeService {

    @Override
    public List<LeaveType> listEnabled() {
        return baseMapper.selectList(new LambdaQueryWrapper<LeaveType>()
                .eq(LeaveType::getStatus, 1)
                .eq(LeaveType::getDeleted, 0)
                .orderByAsc(LeaveType::getSort));
    }

    @Override
    @Transactional
    public void createType(LeaveType type) {
        type.setDeleted(0);
        type.setCreateTime(LocalDateTime.now());
        type.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(type);
    }

    @Override
    @Transactional
    public void updateType(LeaveType type) {
        LeaveType old = baseMapper.selectById(type.getId());
        if (old == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        type.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(type);
    }

    @Override
    @Transactional
    public void deleteType(Long id) {
        LeaveType type = baseMapper.selectById(id);
        if (type == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        // 使用 MyBatis Plus 的逻辑删除
        removeById(id);
    }
}
