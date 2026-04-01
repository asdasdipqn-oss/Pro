package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.ResultCode;
import cn.edu.ccst.manpower_resource.dto.LeaveApplicationDTO;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.entity.EmpEmployee;
import cn.edu.ccst.manpower_resource.entity.LeaveApplication;
import cn.edu.ccst.manpower_resource.entity.OrgDepartment;
import cn.edu.ccst.manpower_resource.entity.SysRole;
import cn.edu.ccst.manpower_resource.entity.SysUser;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.EmpEmployeeMapper;
import cn.edu.ccst.manpower_resource.mapper.LeaveApplicationMapper;
import cn.edu.ccst.manpower_resource.mapper.OrgDepartmentMapper;
import cn.edu.ccst.manpower_resource.mapper.SysRoleMapper;
import cn.edu.ccst.manpower_resource.mapper.SysUserMapper;
import cn.edu.ccst.manpower_resource.service.IApprovalRecordService;
import cn.edu.ccst.manpower_resource.service.ILeaveApplicationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class LeaveApplicationServiceImpl extends ServiceImpl<LeaveApplicationMapper, LeaveApplication> implements ILeaveApplicationService {

    private final IApprovalRecordService approvalRecordService;
    private final EmpEmployeeMapper employeeMapper;
    private final OrgDepartmentMapper departmentMapper;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;

    public LeaveApplicationServiceImpl(@Lazy IApprovalRecordService approvalRecordService,
                                       EmpEmployeeMapper employeeMapper,
                                       OrgDepartmentMapper departmentMapper,
                                       SysUserMapper userMapper,
                                       SysRoleMapper roleMapper) {
        this.approvalRecordService = approvalRecordService;
        this.employeeMapper = employeeMapper;
        this.departmentMapper = departmentMapper;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    @Transactional
    public LeaveApplication apply(Long employeeId, LeaveApplicationDTO dto) {
        LeaveApplication app = new LeaveApplication();
        app.setApplyCode("LA" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) 
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        app.setEmployeeId(employeeId);
        app.setLeaveTypeId(dto.getLeaveTypeId());
        app.setStartTime(dto.getStartTime());
        app.setEndTime(dto.getEndTime());
        app.setDuration(dto.getDuration());
        app.setReason(dto.getReason());
        app.setProofUrl(dto.getProofUrl());
        app.setStatus(0); // 待审批
        app.setCreateTime(LocalDateTime.now());
        app.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(app);

        // 创建审批记录 - 找到审批人（部门负责人或HR）
        Long approverId = findApprover(employeeId);
        if (approverId != null) {
            approvalRecordService.createRecord(1, app.getId(), approverId); // 1-请假
        }

        return app;
    }

    /**
     * 查找审批人：直属领导 → 部门负责人 → HR角色用户
     * 排除自己给自己审批的情况
     */
    private Long findApprover(Long employeeId) {
        EmpEmployee emp = employeeMapper.selectById(employeeId);
        if (emp == null) {
            return findHRUser(employeeId);
        }

        // 1. 优先找直属领导
        if (emp.getDirectLeaderId() != null) {
            SysUser leaderUser = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmployeeId, emp.getDirectLeaderId())
                    .eq(SysUser::getDeleted, 0)
                    .eq(SysUser::getStatus, 1)
                    .last("LIMIT 1"));
            if (leaderUser != null && !leaderUser.getEmployeeId().equals(employeeId)) {
                return leaderUser.getId();
            }
        }

        // 2. 其次找部门负责人
        if (emp.getDeptId() != null) {
            OrgDepartment dept = departmentMapper.selectById(emp.getDeptId());
            if (dept != null && dept.getLeaderId() != null && !dept.getLeaderId().equals(employeeId)) {
                SysUser deptLeaderUser = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getEmployeeId, dept.getLeaderId())
                        .eq(SysUser::getDeleted, 0)
                        .eq(SysUser::getStatus, 1)
                        .last("LIMIT 1"));
                if (deptLeaderUser != null) {
                    return deptLeaderUser.getId();
                }
            }
        }

        // 3. 最后找HR角色用户
        return findHRUser(employeeId);
    }

    /**
     * 查找HR角色用户（排除申请人自己）
     */
    private Long findHRUser(Long excludeEmployeeId) {
        // 查找拥有HR角色的用户，排除申请人自己
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .eq(SysUser::getStatus, 1)
                .ne(excludeEmployeeId != null, SysUser::getEmployeeId, excludeEmployeeId));
        
        // 优先找有HR或ADMIN角色的用户
        for (SysUser user : users) {
            List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
            boolean isHROrAdmin = roles.stream()
                    .anyMatch(r -> "HR".equals(r.getRoleCode()) || "ADMIN".equals(r.getRoleCode()));
            if (isHROrAdmin) {
                return user.getId();
            }
        }
        
        // 没有找到HR/ADMIN，返回任意一个非employee的用户
        return users.isEmpty() ? null : users.get(0).getId();
    }

    @Override
    @Transactional
    public void cancel(Long id, Long employeeId) {
        LeaveApplication app = baseMapper.selectById(id);
        if (app == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (!app.getEmployeeId().equals(employeeId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        if (app.getStatus() != 0 && app.getStatus() != 1) {
            throw new BusinessException("只有待审批的申请可以撤回");
        }
        app.setStatus(4); // 已撤回
        app.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(app);
    }

    @Override
    @Transactional
    public void approve(Long id, Long approverId, Integer status, String comment) {
        LeaveApplication app = baseMapper.selectById(id);
        if (app == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        app.setStatus(status); // 2-通过 3-驳回
        app.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(app);
    }

    @Override
    public PageResult<LeaveApplication> pageByEmployee(Long employeeId, PageQuery query) {
        Page<LeaveApplication> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<LeaveApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LeaveApplication::getEmployeeId, employeeId)
                .orderByDesc(LeaveApplication::getCreateTime);
        Page<LeaveApplication> result = baseMapper.selectPage(page, wrapper);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public List<LeaveApplication> getPendingApprovals(Long approverId) {
        List<LeaveApplication> list = baseMapper.selectList(new LambdaQueryWrapper<LeaveApplication>()
                .in(LeaveApplication::getStatus, 0, 1)
                .orderByAsc(LeaveApplication::getCreateTime));
        
        // 填充申请人姓名
        for (LeaveApplication app : list) {
            if (app.getEmployeeId() != null) {
                EmpEmployee emp = employeeMapper.selectById(app.getEmployeeId());
                if (emp != null) {
                    app.setEmployeeName(emp.getEmpName());
                }
            }
        }
        return list;
    }
}
