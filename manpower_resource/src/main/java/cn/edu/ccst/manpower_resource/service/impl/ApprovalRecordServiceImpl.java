package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.ResultCode;
import cn.edu.ccst.manpower_resource.dto.ApproveDTO;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.entity.*;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.*;
import cn.edu.ccst.manpower_resource.service.IApprovalRecordService;
import cn.edu.ccst.manpower_resource.vo.ApprovalRecordVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审批记录表 服务实现类
 */
@Service
@RequiredArgsConstructor
public class ApprovalRecordServiceImpl extends ServiceImpl<ApprovalRecordMapper, ApprovalRecord> implements IApprovalRecordService {

    private final LeaveApplicationMapper leaveApplicationMapper;
    private final AttAppealMapper attAppealMapper;
    private final EmpEmployeeMapper employeeMapper;
    private final OrgDepartmentMapper departmentMapper;
    private final SysUserMapper userMapper;

    @Override
    @Transactional
    public ApprovalRecord createRecord(Integer businessType, Long businessId, Long approverId) {
        ApprovalRecord record = new ApprovalRecord();
        record.setBusinessType(businessType);
        record.setBusinessId(businessId);
        record.setApproverId(approverId);
        record.setStatus(0); // 待审批
        record.setCreateTime(LocalDateTime.now());
        baseMapper.insert(record);
        return record;
    }

    @Override
    @Transactional
    public void approve(Long approverId, ApproveDTO dto) {
        // 查找待审批记录
        ApprovalRecord record = baseMapper.selectOne(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getBusinessType, dto.getBusinessType())
                .eq(ApprovalRecord::getBusinessId, dto.getBusinessId())
                .eq(ApprovalRecord::getApproverId, approverId)
                .eq(ApprovalRecord::getStatus, 0));

        if (record == null) {
            throw new BusinessException("未找到待审批记录或您无权审批");
        }

        // 更新审批记录
        record.setStatus(dto.getStatus());
        record.setOpinion(dto.getOpinion());
        record.setApproveTime(LocalDateTime.now());
        baseMapper.updateById(record);

        // 更新业务状态
        updateBusinessStatus(dto.getBusinessType(), dto.getBusinessId(), dto.getStatus());
    }

    /**
     * 更新业务状态
     */
    private void updateBusinessStatus(Integer businessType, Long businessId, Integer approveStatus) {
        // approveStatus: 1-通过 2-驳回
        // 业务状态: 2-已通过 3-已驳回
        int newStatus = approveStatus == 1 ? 2 : 3;

        switch (businessType) {
            case 1: // 请假
                LeaveApplication leave = leaveApplicationMapper.selectById(businessId);
                if (leave != null) {
                    leave.setStatus(newStatus);
                    leave.setUpdateTime(LocalDateTime.now());
                    leaveApplicationMapper.updateById(leave);
                }
                break;
            case 2: // 考勤异常
                AttAppeal appeal = attAppealMapper.selectById(businessId);
                if (appeal != null) {
                    appeal.setStatus(newStatus);
                    appeal.setUpdateTime(LocalDateTime.now());
                    attAppealMapper.updateById(appeal);
                }
                break;
            // 其他业务类型...
        }
    }

    @Override
    public PageResult<ApprovalRecordVO> getPendingList(Long approverId, Integer businessType, PageQuery query) {
        Page<ApprovalRecord> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ApprovalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalRecord::getApproverId, approverId)
                .eq(ApprovalRecord::getStatus, 0);
        if (businessType != null) {
            wrapper.eq(ApprovalRecord::getBusinessType, businessType);
        }
        wrapper.orderByDesc(ApprovalRecord::getCreateTime);

        Page<ApprovalRecord> result = baseMapper.selectPage(page, wrapper);
        List<ApprovalRecordVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), voList);
    }

    @Override
    public PageResult<ApprovalRecordVO> getApprovedList(Long approverId, Integer businessType, PageQuery query) {
        Page<ApprovalRecord> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ApprovalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalRecord::getApproverId, approverId)
                .ne(ApprovalRecord::getStatus, 0);
        if (businessType != null) {
            wrapper.eq(ApprovalRecord::getBusinessType, businessType);
        }
        wrapper.orderByDesc(ApprovalRecord::getApproveTime);

        Page<ApprovalRecord> result = baseMapper.selectPage(page, wrapper);
        List<ApprovalRecordVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), voList);
    }

    @Override
    public List<ApprovalRecordVO> getByBusiness(Integer businessType, Long businessId) {
        List<ApprovalRecord> records = baseMapper.selectList(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getBusinessType, businessType)
                .eq(ApprovalRecord::getBusinessId, businessId)
                .orderByAsc(ApprovalRecord::getCreateTime));

        return records.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<ApprovalRecordVO> pageAll(Integer businessType, Integer status, PageQuery query) {
        Page<ApprovalRecord> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ApprovalRecord> wrapper = new LambdaQueryWrapper<>();
        if (businessType != null) {
            wrapper.eq(ApprovalRecord::getBusinessType, businessType);
        }
        if (status != null) {
            wrapper.eq(ApprovalRecord::getStatus, status);
        }
        wrapper.orderByDesc(ApprovalRecord::getCreateTime);

        Page<ApprovalRecord> result = baseMapper.selectPage(page, wrapper);
        List<ApprovalRecordVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), voList);
    }

    /**
     * 转换为VO
     */
    private ApprovalRecordVO convertToVO(ApprovalRecord record) {
        ApprovalRecordVO vo = new ApprovalRecordVO();
        BeanUtils.copyProperties(record, vo);

        // 获取审批人姓名
        SysUser approver = userMapper.selectById(record.getApproverId());
        if (approver != null) {
            vo.setApproverName(approver.getUsername());
        }

        // 根据业务类型获取申请人信息
        Long employeeId = getEmployeeIdByBusiness(record.getBusinessType(), record.getBusinessId());
        if (employeeId != null) {
            EmpEmployee employee = employeeMapper.selectById(employeeId);
            if (employee != null) {
                vo.setApplicantName(employee.getEmpName());
                if (employee.getDeptId() != null) {
                    OrgDepartment dept = departmentMapper.selectById(employee.getDeptId());
                    if (dept != null) {
                        vo.setDeptName(dept.getDeptName());
                    }
                }
            }
        }

        return vo;
    }

    /**
     * 根据业务获取员工ID
     */
    private Long getEmployeeIdByBusiness(Integer businessType, Long businessId) {
        switch (businessType) {
            case 1: // 请假
                LeaveApplication leave = leaveApplicationMapper.selectById(businessId);
                return leave != null ? leave.getEmployeeId() : null;
            case 2: // 考勤异常
                AttAppeal appeal = attAppealMapper.selectById(businessId);
                return appeal != null ? appeal.getEmployeeId() : null;
            default:
                return null;
        }
    }
}
