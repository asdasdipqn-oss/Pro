package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.entity.ApprovalLog;
import cn.edu.ccst.manpower_resource.entity.SysUser;
import cn.edu.ccst.manpower_resource.mapper.ApprovalLogMapper;
import cn.edu.ccst.manpower_resource.mapper.SysUserMapper;
import cn.edu.ccst.manpower_resource.service.IApprovalLogService;
import cn.edu.ccst.manpower_resource.vo.ApprovalLogVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalLogServiceImpl implements IApprovalLogService {

    private final ApprovalLogMapper logMapper;
    private final SysUserMapper userMapper;

    @Override
    public void log(String businessType, Long businessId, Long approverId, String action, String comment) {
        ApprovalLog log = new ApprovalLog();
        log.setBusinessType(businessType);
        log.setBusinessId(businessId);
        log.setApproverId(approverId);
        log.setAction(action);
        log.setComment(comment);
        log.setCreateTime(LocalDateTime.now());
        logMapper.insert(log);
    }

    @Override
    public List<ApprovalLogVO> getByBusiness(String businessType, Long businessId) {
        List<ApprovalLog> logs = logMapper.selectList(new LambdaQueryWrapper<ApprovalLog>()
                .eq(ApprovalLog::getBusinessType, businessType)
                .eq(ApprovalLog::getBusinessId, businessId)
                .orderByDesc(ApprovalLog::getCreateTime));
        return logs.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public PageResult<ApprovalLogVO> pageAll(PageQuery query, String businessType) {
        Page<ApprovalLog> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ApprovalLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(businessType), ApprovalLog::getBusinessType, businessType)
                .orderByDesc(ApprovalLog::getCreateTime);
        Page<ApprovalLog> result = logMapper.selectPage(page, wrapper);
        List<ApprovalLogVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), voList);
    }

    private ApprovalLogVO toVO(ApprovalLog log) {
        ApprovalLogVO vo = new ApprovalLogVO();
        BeanUtils.copyProperties(log, vo);
        if (log.getApproverId() != null) {
            SysUser user = userMapper.selectById(log.getApproverId());
            if (user != null) {
                vo.setApproverName(user.getUsername());
            }
        }
        return vo;
    }
}
