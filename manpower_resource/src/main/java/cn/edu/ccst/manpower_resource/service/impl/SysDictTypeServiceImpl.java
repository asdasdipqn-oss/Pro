package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.ResultCode;
import cn.edu.ccst.manpower_resource.entity.SysDictData;
import cn.edu.ccst.manpower_resource.entity.SysDictType;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.SysDictDataMapper;
import cn.edu.ccst.manpower_resource.mapper.SysDictTypeMapper;
import cn.edu.ccst.manpower_resource.service.ISysDictTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements ISysDictTypeService {

    private final SysDictDataMapper dictDataMapper;

    @Override
    public List<SysDictType> listAll() {
        return baseMapper.selectList(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDeleted, 0)
                .orderByAsc(SysDictType::getId));
    }

    @Override
    public List<SysDictData> getDataByType(String typeCode) {
        return dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, typeCode)
                .eq(SysDictData::getStatus, 1)
                .eq(SysDictData::getDeleted, 0)
                .orderByAsc(SysDictData::getSort));
    }

    @Override
    @Transactional
    public void createType(SysDictType type) {
        type.setDeleted(0);
        type.setCreateTime(LocalDateTime.now());
        type.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(type);
    }

    @Override
    @Transactional
    public void updateType(SysDictType type) {
        SysDictType old = baseMapper.selectById(type.getId());
        if (old == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        type.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(type);
    }

    @Override
    @Transactional
    public void deleteType(Long id) {
        SysDictType type = baseMapper.selectById(id);
        if (type == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        // 使用 MyBatis Plus 的逻辑删除
        removeById(id);
    }
}
