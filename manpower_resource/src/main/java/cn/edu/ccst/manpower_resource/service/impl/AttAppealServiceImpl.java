package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.ResultCode;
import cn.edu.ccst.manpower_resource.dto.AppealDTO;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.entity.AttAppeal;
import cn.edu.ccst.manpower_resource.entity.AttClockRecord;
import cn.edu.ccst.manpower_resource.entity.EmpEmployee;
import cn.edu.ccst.manpower_resource.entity.OrgDepartment;
import cn.edu.ccst.manpower_resource.entity.SysRole;
import cn.edu.ccst.manpower_resource.entity.SysUser;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.AttAppealMapper;
import cn.edu.ccst.manpower_resource.mapper.AttClockRecordMapper;
import cn.edu.ccst.manpower_resource.mapper.EmpEmployeeMapper;
import cn.edu.ccst.manpower_resource.mapper.OrgDepartmentMapper;
import cn.edu.ccst.manpower_resource.mapper.SysRoleMapper;
import cn.edu.ccst.manpower_resource.mapper.SysUserMapper;
import cn.edu.ccst.manpower_resource.service.IAttAppealService;
import cn.edu.ccst.manpower_resource.service.IApprovalLogService;
import cn.edu.ccst.manpower_resource.vo.AttAppealVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 考勤异常申诉表 服务实现类
 */
@Service
@RequiredArgsConstructor
public class AttAppealServiceImpl extends ServiceImpl<AttAppealMapper, AttAppeal> implements IAttAppealService {

    private static final Logger log = LoggerFactory.getLogger(AttAppealServiceImpl.class);

    private final EmpEmployeeMapper employeeMapper;
    private final OrgDepartmentMapper departmentMapper;
    private final AttClockRecordMapper clockRecordMapper;
    private final IApprovalLogService approvalLogService;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;

    @Override
    @Transactional
    public void submit(Long employeeId, AppealDTO dto) {
        // 校验申诉日期不能是未来日期
        if (dto.getAppealDate().isAfter(LocalDate.now())) {
            throw new BusinessException("申诉日期不能晚于今天");
        }

        // 如果是漏打卡申诉，需要检查打卡类型和现有打卡记录
        if (dto.getAppealType() == 1) {
            if (dto.getClockType() == null) {
                throw new BusinessException("漏打卡申诉必须选择打卡类型");
            }
            // 检查当天是否已有对应类型的打卡记录
            Long existCount = clockRecordMapper.selectCount(new LambdaQueryWrapper<AttClockRecord>()
                    .eq(AttClockRecord::getEmployeeId, employeeId)
                    .eq(AttClockRecord::getClockDate, dto.getAppealDate())
                    .eq(AttClockRecord::getClockType, dto.getClockType()));
            if (existCount > 0) {
                String clockTypeName = dto.getClockType() == 1 ? "上班" : "下班";
                throw new BusinessException("该日期已有" + clockTypeName + "打卡记录，无法申诉");
            }
        }

        AttAppeal appeal = new AttAppeal();
        appeal.setAppealCode("AP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        appeal.setEmployeeId(employeeId);
        appeal.setAppealDate(dto.getAppealDate());
        appeal.setAppealType(dto.getAppealType());
        appeal.setClockType(dto.getClockType());
        appeal.setAppealReason(dto.getAppealReason());
        appeal.setProofUrl(dto.getProofUrl());
        appeal.setStatus(0); // 待审批
        appeal.setCreateTime(LocalDateTime.now());
        appeal.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(appeal);
    }

    @Override
    @Transactional
    public void cancel(Long id, Long employeeId) {
        AttAppeal appeal = baseMapper.selectById(id);
        if (appeal == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (!appeal.getEmployeeId().equals(employeeId)) {
            throw new BusinessException("无权操作此申诉");
        }
        if (appeal.getStatus() != 0 && appeal.getStatus() != 1) {
            throw new BusinessException("当前状态不可撤回");
        }
        appeal.setStatus(4); // 已撤回
        appeal.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(appeal);
    }

    @Override
    public PageResult<AttAppealVO> pageByEmployee(Long employeeId, PageQuery query) {
        Page<AttAppeal> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<AttAppeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttAppeal::getEmployeeId, employeeId)
                .orderByDesc(AttAppeal::getCreateTime);
        Page<AttAppeal> result = baseMapper.selectPage(page, wrapper);
        List<AttAppealVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), voList);
    }

    @Override
    public AttAppealVO getDetail(Long id) {
        AttAppeal appeal = baseMapper.selectById(id);
        if (appeal == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return toVO(appeal);
    }

    @Override
    public List<AttAppealVO> getPendingApprovals(Long approverId) {
        // 获取审批人信息
        SysUser approverUser = userMapper.selectById(approverId);
        if (approverUser == null) {
            return List.of();
        }

        // 获取审批人角色
        List<SysRole> roles = roleMapper.selectRolesByUserId(approverId);
        boolean isManager = roles.stream()
                .anyMatch(r -> "MANAGER".equals(r.getRoleCode()));

        // 如果是经理，获取其部门ID
        Long finalManagerDeptId;
        if (isManager && approverUser.getEmployeeId() != null) {
            EmpEmployee managerEmp = employeeMapper.selectById(approverUser.getEmployeeId());
            finalManagerDeptId = managerEmp != null ? managerEmp.getDeptId() : null;
        } else {
            finalManagerDeptId = null;
        }

        List<AttAppeal> list = baseMapper.selectList(new LambdaQueryWrapper<AttAppeal>()
                .in(AttAppeal::getStatus, 0, 1)
                .orderByAsc(AttAppeal::getCreateTime));

        // 如果是经理，只显示本部门的申诉申请
        if (isManager && finalManagerDeptId != null) {
            list = list.stream()
                    .filter(appeal -> {
                        if (appeal.getEmployeeId() == null) {
                            return false;
                        }
                        EmpEmployee emp = employeeMapper.selectById(appeal.getEmployeeId());
                        return emp != null && emp.getDeptId().equals(finalManagerDeptId);
                    })
                    .toList();
        }

        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approve(Long id, Long approverId, Integer status, String comment) {
        AttAppeal appeal = baseMapper.selectById(id);
        if (appeal == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        if (appeal.getStatus() != 0 && appeal.getStatus() != 1) {
            throw new BusinessException("当前状态不可审批");
        }
        appeal.setStatus(status);
        appeal.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(appeal);

        // 如果审批通过，处理打卡记录
        if (status == 2) {
            handleClockRecordOnApproval(appeal);
        }

        // 记录审批日志
        approvalLogService.log("考勤申诉", id, approverId, status == 2 ? "通过" : "驳回", comment);
    }

    /**
     * 审批通过后处理打卡记录
     */
    private void handleClockRecordOnApproval(AttAppeal appeal) {
        log.info("审批通过，处理打卡记录。申诉ID: {}, 类型: {}, 日期: {}",
                appeal.getId(), appeal.getAppealType(), appeal.getAppealDate());

        // 查询当天是否已有打卡记录
        List<AttClockRecord> existingRecords = clockRecordMapper.selectList(
                new LambdaQueryWrapper<AttClockRecord>()
                        .eq(AttClockRecord::getEmployeeId, appeal.getEmployeeId())
                        .eq(AttClockRecord::getClockDate, appeal.getAppealDate()));

        if (appeal.getAppealType() == 1) {
            // 漏打卡：创建指定类型的打卡记录
            if (appeal.getClockType() != null) {
                createClockRecord(appeal.getEmployeeId(), appeal.getAppealDate(),
                        appeal.getClockType(), appeal.getAppealCode(), appeal.getAppealReason());
            } else {
                // 旧数据没有clockType，创建上下班两条记录
                createBothClockRecords(appeal, existingRecords);
            }
        } else if (appeal.getAppealType() == 2) {
            // 定位异常：把当天所有打卡记录的定位状态改为正常
            if (!existingRecords.isEmpty()) {
                for (AttClockRecord record : existingRecords) {
                    record.setLocationStatus(1);
                    record.setRemark((record.getRemark() != null ? record.getRemark() + "; " : "")
                            + "申诉通过，编号：" + appeal.getAppealCode());
                    clockRecordMapper.updateById(record);
                }
                log.info("定位异常申诉通过，已更新{}条记录的定位状态", existingRecords.size());
            } else {
                // 没有打卡记录，创建上下班两条
                createBothClockRecords(appeal, existingRecords);
            }
        } else {
            // 其他类型：如果没有打卡记录，创建上下班两条
            if (existingRecords.isEmpty()) {
                createBothClockRecords(appeal, existingRecords);
            } else {
                // 已有记录，确保定位状态正常
                for (AttClockRecord record : existingRecords) {
                    if (record.getLocationStatus() != 1) {
                        record.setLocationStatus(1);
                        clockRecordMapper.updateById(record);
                    }
                }
            }
        }
    }

    /**
     * 创建上班和下班两条打卡记录（跳过已存在的）
     */
    private void createBothClockRecords(AttAppeal appeal, List<AttClockRecord> existingRecords) {
        boolean hasClockIn = existingRecords.stream().anyMatch(r -> r.getClockType() == 1);
        boolean hasClockOut = existingRecords.stream().anyMatch(r -> r.getClockType() == 2);

        if (!hasClockIn) {
            createClockRecord(appeal.getEmployeeId(), appeal.getAppealDate(), 1,
                        appeal.getAppealCode(), appeal.getAppealReason());
        }
        if (!hasClockOut) {
            createClockRecord(appeal.getEmployeeId(), appeal.getAppealDate(), 2,
                        appeal.getAppealCode(), appeal.getAppealReason());
        }
    }

    /**
     * 创建单条打卡记录
     */
    private void createClockRecord(Long employeeId, LocalDate clockDate, Integer clockType,
            String appealCode, String appealReason) {
        log.info("开始创建补卡记录，员工ID: {}, 日期: {}, 打卡类型: {}",
                employeeId, clockDate, clockType);

        AttClockRecord record = new AttClockRecord();
        record.setEmployeeId(employeeId);
        record.setClockDate(clockDate);
        record.setClockType(clockType);

        // 根据打卡类型设置默认时间（上班卡9点，下班卡18点）
        LocalDateTime clockTime;
        if (clockType == 1) {
            clockTime = clockDate.atTime(9, 0, 0);
        } else {
            clockTime = clockDate.atTime(18, 0, 0);
        }
        record.setClockTime(clockTime);

        // 设置默认地址信息
        record.setClockLatitude(new BigDecimal("39.908823"));
        record.setClockLongitude(new BigDecimal("116.397470"));
        record.setClockAddress("申诉补卡 - " + appealReason);
        record.setLocationStatus(1); // 正常
        record.setRemark("申诉补卡，申诉编号：" + appealCode);
        record.setCreateTime(LocalDateTime.now());

        int rows = clockRecordMapper.insert(record);
        log.info("补卡记录创建完成，插入行数: {}, 记录ID: {}", rows, record.getId());
    }

    @Override
    public PageResult<AttAppealVO> pageAll(PageQuery query, Integer status) {
        Page<AttAppeal> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<AttAppeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, AttAppeal::getStatus, status)
                .orderByDesc(AttAppeal::getCreateTime);
        Page<AttAppeal> result = baseMapper.selectPage(page, wrapper);
        List<AttAppealVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), voList);
    }

    @Override
    public List<AttAppealVO> getApprovedAppeals(Integer year, Integer month) {
        LambdaQueryWrapper<AttAppeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttAppeal::getStatus, 2) // 已通过
                .and(w -> {
                    w.apply("YEAR(appeal_date) = {0}", year)
                     .apply("MONTH(appeal_date) = {0}", month);
                });
        List<AttAppeal> list = baseMapper.selectList(wrapper);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    private AttAppealVO toVO(AttAppeal appeal) {
        AttAppealVO vo = new AttAppealVO();
        BeanUtils.copyProperties(appeal, vo);

        // 填充申诉人信息
        if (appeal.getEmployeeId() != null) {
            EmpEmployee emp = employeeMapper.selectById(appeal.getEmployeeId());
            if (emp != null) {
                vo.setEmployeeName(emp.getEmpName());
                if (emp.getDeptId() != null) {
                    OrgDepartment dept = departmentMapper.selectById(emp.getDeptId());
                    if (dept != null) {
                        vo.setDeptName(dept.getDeptName());
                    }
                }
            }
        }

        // 填充类型名称
        vo.setAppealTypeName(getAppealTypeName(appeal.getAppealType()));
        vo.setStatusName(getStatusName(appeal.getStatus()));

        return vo;
    }

    private String getAppealTypeName(Integer type) {
        if (type == null) return "";
        switch (type) {
            case 1: return "漏打卡";
            case 2: return "定位异常";
            case 3: return "其他";
            default: return "";
        }
    }

    private String getStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待审批";
            case 1: return "审批中";
            case 2: return "已通过";
            case 3: return "已驳回";
            case 4: return "已撤回";
            default: return "";
        }
    }
}
