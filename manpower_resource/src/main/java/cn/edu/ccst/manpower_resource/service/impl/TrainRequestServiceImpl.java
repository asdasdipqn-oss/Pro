package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.ResultCode;
import cn.edu.ccst.manpower_resource.dto.TrainRequestDTO;
import cn.edu.ccst.manpower_resource.entity.EmpEmployee;
import cn.edu.ccst.manpower_resource.entity.OrgDepartment;
import cn.edu.ccst.manpower_resource.entity.SysRole;
import cn.edu.ccst.manpower_resource.entity.SysUser;
import cn.edu.ccst.manpower_resource.entity.TrainParticipant;
import cn.edu.ccst.manpower_resource.entity.TrainPlan;
import cn.edu.ccst.manpower_resource.entity.TrainRequest;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.EmpEmployeeMapper;
import cn.edu.ccst.manpower_resource.mapper.OrgDepartmentMapper;
import cn.edu.ccst.manpower_resource.mapper.SysRoleMapper;
import cn.edu.ccst.manpower_resource.mapper.SysUserMapper;
import cn.edu.ccst.manpower_resource.mapper.TrainParticipantMapper;
import cn.edu.ccst.manpower_resource.mapper.TrainPlanMapper;
import cn.edu.ccst.manpower_resource.mapper.TrainRequestMapper;
import cn.edu.ccst.manpower_resource.service.IApprovalLogService;
import cn.edu.ccst.manpower_resource.service.ITrainRequestService;
import cn.edu.ccst.manpower_resource.vo.TrainRequestVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <p>
 * 培训需求表 服务实现类
 * </p>
 *
 * @author
 * @since 2026-04-03
 */
@Service
@RequiredArgsConstructor
public class TrainRequestServiceImpl extends ServiceImpl<TrainRequestMapper, TrainRequest> implements ITrainRequestService {

    private final EmpEmployeeMapper employeeMapper;
    private final OrgDepartmentMapper departmentMapper;
    private final IApprovalLogService approvalLogService;
    private final TrainPlanMapper trainPlanMapper;
    private final TrainParticipantMapper trainParticipantMapper;
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;

    @Override
    @Transactional
    public void submit(Long applicantId, TrainRequestDTO dto) {
        TrainRequest request = new TrainRequest();
        request.setRequestCode("TR" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        request.setTrainTitle(dto.getTrainTitle());
        request.setTrainType(dto.getTrainType());
        request.setExpectedDate(dto.getExpectedDate());
        request.setExpectedHours(dto.getExpectedHours());
        request.setRequestReason(dto.getRequestReason());
        request.setParticipantCount(dto.getParticipantCount());
        request.setStatus(0); // 待审批
        request.setApplicantId(applicantId);
        request.setCreateTime(LocalDateTime.now());
        request.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(request);
    }

    @Override
    @Transactional
    public void cancel(Long id, Long applicantId) {
        TrainRequest request = baseMapper.selectById(id);
        if (request == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (!request.getApplicantId().equals(applicantId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        if (request.getStatus() != 0) {
            throw new BusinessException("当前状态不可撤回");
        }
        request.setStatus(3); // 已取消
        request.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(request);
    }

    @Override
    public List<TrainRequestVO> getMyRequests(Long userId) {
        List<TrainRequest> list = baseMapper.selectByUserId(userId);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<TrainRequestVO> getPendingApprovals() {
        List<TrainRequest> list = baseMapper.selectPendingApprovals();
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approve(Long id, Long approverId, Integer status, String comment) {
        TrainRequest request = baseMapper.selectById(id);
        if (request == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (request.getStatus() != 0) {
            throw new BusinessException("当前状态不可审批");
        }
        request.setStatus(status); // 1-通过 2-拒绝
        request.setApprovalComment(comment);
        request.setApproverId(approverId);
        request.setApprovalTime(LocalDateTime.now());
        request.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(request);

        // 记录审批日志
        approvalLogService.log("培训需求", id, approverId, status == 1 ? "通过" : "拒绝", comment);

        // 审批通过时自动创建培训计划
        if (status == 1) {
            createTrainPlanFromRequest(request, approverId);
        }
    }

    /**
     * 从培训需求自动创建培训计划
     */
    private void createTrainPlanFromRequest(TrainRequest request, Long createBy) {
        TrainPlan plan = new TrainPlan();
        plan.setPlanCode("TP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        plan.setPlanName(request.getTrainTitle());
        plan.setTrainType(request.getTrainType());
        plan.setTrainer("待定");
        plan.setTrainLocation("待定");
        // 默认设置为期望日期，如果期望日期为空则设置为明天
        if (request.getExpectedDate() != null) {
            plan.setStartTime(request.getExpectedDate());
            plan.setEndTime(request.getExpectedDate().plusHours(request.getExpectedHours() != null ? request.getExpectedHours() : 2));
        } else {
            plan.setStartTime(LocalDateTime.now().plusDays(1));
            plan.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        }
        plan.setMaxParticipants(request.getParticipantCount());
        plan.setDescription(request.getRequestReason());
        plan.setStatus(0); // 草稿状态，管理员可以后续修改后发布
        plan.setCreateBy(createBy);
        plan.setCreateTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        trainPlanMapper.insert(plan);

        // 自动添加参训人员
        autoAssignParticipantsForRequest(plan, request, createBy);
    }

    /**
     * 根据培训需求自动添加参训人员
     * 如果审批人是经理角色，则添加审批人所在部门的所有员工
     * 否则，添加申请人所在部门的所有员工
     */
    private void autoAssignParticipantsForRequest(TrainPlan plan, TrainRequest request, Long approverUserId) {
        // 获取申请人的部门
        EmpEmployee applicant = employeeMapper.selectById(request.getApplicantId());
        if (applicant == null) {
            return;
        }
        Long deptId = applicant.getDeptId();

        // 判断审批人是否是经理角色
        List<SysRole> approverRoles = sysRoleMapper.selectRolesByUserId(approverUserId);
        boolean isManager = approverRoles.stream().anyMatch(role -> "MANAGER".equals(role.getRoleCode()));

        if (isManager) {
            // 如果审批人是经理，则使用审批人所在部门
            SysUser approverUser = sysUserMapper.selectById(approverUserId);
            if (approverUser != null && approverUser.getEmployeeId() != null) {
                EmpEmployee approver = employeeMapper.selectById(approverUser.getEmployeeId());
                if (approver != null && approver.getDeptId() != null) {
                    deptId = approver.getDeptId();
                }
            }
        }

        // 获取该部门及其所有子部门的员工
        List<Long> deptIds = new java.util.ArrayList<>();
        deptIds.add(deptId);
        getChildDeptIds(deptId, deptIds);

        // 查询这些部门下的所有员工
        List<EmpEmployee> employees = employeeMapper.selectList(
                new LambdaQueryWrapper<EmpEmployee>()
                        .in(EmpEmployee::getDeptId, deptIds)
                        .eq(EmpEmployee::getDeleted, 0));

        // 添加参训人员
        for (EmpEmployee emp : employees) {
            // 检查是否已存在
            Long count = trainParticipantMapper.selectCount(new LambdaQueryWrapper<TrainParticipant>()
                    .eq(TrainParticipant::getPlanId, plan.getId())
                    .eq(TrainParticipant::getEmployeeId, emp.getId()));
            if (count > 0) continue;

            TrainParticipant participant = new TrainParticipant();
            participant.setPlanId(plan.getId());
            participant.setEmployeeId(emp.getId());
            participant.setAttendanceStatus(0); // 未签到
            participant.setCreateTime(LocalDateTime.now());
            participant.setUpdateTime(LocalDateTime.now());
            trainParticipantMapper.insert(participant);
        }
    }

    /**
     * 递归获取子部门ID
     */
    private void getChildDeptIds(Long parentId, List<Long> deptIds) {
        List<OrgDepartment> children = departmentMapper.selectList(
                new LambdaQueryWrapper<OrgDepartment>()
                        .eq(OrgDepartment::getParentId, parentId)
                        .eq(OrgDepartment::getDeleted, 0));
        for (OrgDepartment dept : children) {
            deptIds.add(dept.getId());
            getChildDeptIds(dept.getId(), deptIds);
        }
    }

    @Override
    public PageResult<TrainRequestVO> pageAll(Integer pageNum, Integer pageSize, Integer status) {
        Page<TrainRequest> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TrainRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, TrainRequest::getStatus, status)
                .orderByDesc(TrainRequest::getCreateTime);
        Page<TrainRequest> result = baseMapper.selectPage(page, wrapper);
        List<TrainRequestVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), voList);
    }

    @Override
    public TrainRequestVO getDetail(Long id) {
        TrainRequest request = baseMapper.selectById(id);
        if (request == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return toVO(request);
    }

    private TrainRequestVO toVO(TrainRequest request) {
        TrainRequestVO vo = new TrainRequestVO();
        vo.setId(request.getId());
        vo.setRequestCode(request.getRequestCode());
        vo.setTrainTitle(request.getTrainTitle());
        vo.setTrainType(request.getTrainType());
        vo.setTrainTypeName(getTrainTypeName(request.getTrainType()));
        vo.setExpectedDate(request.getExpectedDate());
        vo.setExpectedHours(request.getExpectedHours());
        vo.setRequestReason(request.getRequestReason());
        vo.setParticipantCount(request.getParticipantCount());
        vo.setStatus(request.getStatus());
        vo.setStatusName(getStatusName(request.getStatus()));
        vo.setApprovalComment(request.getApprovalComment());
        vo.setApplicantId(request.getApplicantId());
        vo.setApproverId(request.getApproverId());
        vo.setApprovalTime(request.getApprovalTime());
        vo.setCreateTime(request.getCreateTime());

        // 填充申请人信息
        if (request.getApplicantId() != null) {
            EmpEmployee emp = employeeMapper.selectById(request.getApplicantId());
            if (emp != null) {
                vo.setApplicantName(emp.getEmpName());
                if (emp.getDeptId() != null) {
                    OrgDepartment dept = departmentMapper.selectById(emp.getDeptId());
                    if (dept != null) {
                        vo.setDeptName(dept.getDeptName());
                    }
                }
            }
        }

        return vo;
    }

    private String getTrainTypeName(Integer type) {
        if (type == null) return "";
        switch (type) {
            case 1: return "入职培训";
            case 2: return "技能培训";
            case 3: return "管理培训";
            case 4: return "安全培训";
            case 5: return "其他";
            default: return "";
        }
    }

    private String getStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待审批";
            case 1: return "已通过";
            case 2: return "已拒绝";
            case 3: return "已取消";
            default: return "";
        }
    }
}
