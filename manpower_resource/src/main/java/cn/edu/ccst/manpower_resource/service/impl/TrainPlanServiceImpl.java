package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.ResultCode;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.entity.EmpEmployee;
import cn.edu.ccst.manpower_resource.entity.OrgDepartment;
import cn.edu.ccst.manpower_resource.entity.TrainParticipant;
import cn.edu.ccst.manpower_resource.entity.TrainPlan;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.EmpEmployeeMapper;
import cn.edu.ccst.manpower_resource.mapper.OrgDepartmentMapper;
import cn.edu.ccst.manpower_resource.mapper.TrainParticipantMapper;
import cn.edu.ccst.manpower_resource.mapper.TrainPlanMapper;
import cn.edu.ccst.manpower_resource.service.ITrainPlanService;
import cn.edu.ccst.manpower_resource.vo.TrainParticipantVO;
import org.springframework.beans.BeanUtils;
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

@Service
@RequiredArgsConstructor
public class TrainPlanServiceImpl extends ServiceImpl<TrainPlanMapper, TrainPlan> implements ITrainPlanService {

    private final TrainParticipantMapper participantMapper;
    private final EmpEmployeeMapper employeeMapper;
    private final OrgDepartmentMapper departmentMapper;

    @Override
    public PageResult<TrainPlan> pageList(PageQuery query, Integer status) {
        Page<TrainPlan> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<TrainPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, TrainPlan::getStatus, status)
                .eq(TrainPlan::getDeleted, 0)
                .orderByDesc(TrainPlan::getCreateTime);
        Page<TrainPlan> result = baseMapper.selectPage(page, wrapper);
        // 自动更新状态
        result.getRecords().forEach(this::updateStatusByTime);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public List<TrainPlan> listByEmployee(Long employeeId) {
        List<TrainParticipant> participants = participantMapper.selectList(
                new LambdaQueryWrapper<TrainParticipant>().eq(TrainParticipant::getEmployeeId, employeeId));
        List<Long> planIds = participants.stream().map(TrainParticipant::getPlanId).collect(Collectors.toList());
        if (planIds.isEmpty()) return List.of();
        List<TrainPlan> plans = baseMapper.selectList(new LambdaQueryWrapper<TrainPlan>()
                .in(TrainPlan::getId, planIds)
                .eq(TrainPlan::getDeleted, 0));
        // 自动更新状态
        plans.forEach(this::updateStatusByTime);
        return plans;
    }
    
    /**
     * 根据时间自动更新培训状态
     * 状态：0-未开始 1-进行中 2-已结束 3-已取消
     */
    private void updateStatusByTime(TrainPlan plan) {
        // 如果已取消，不更新
        if (plan.getStatus() != null && plan.getStatus() == 3) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = plan.getStartTime();
        LocalDateTime endTime = plan.getEndTime();
        
        Integer newStatus = plan.getStatus();
        
        if (endTime != null && now.isAfter(endTime)) {
            // 已过结束时间 -> 已结束
            newStatus = 2;
        } else if (startTime != null && now.isAfter(startTime)) {
            // 已过开始时间但未结束 -> 进行中
            newStatus = 1;
        } else {
            // 未到开始时间 -> 未开始
            newStatus = 0;
        }
        
        // 如果状态变化，更新数据库
        if (!newStatus.equals(plan.getStatus())) {
            plan.setStatus(newStatus);
            plan.setUpdateTime(LocalDateTime.now());
            baseMapper.updateById(plan);
        }
    }

    @Override
    @Transactional
    public void createPlan(TrainPlan plan) {
        // 生成计划编号：TP + 时间戳 + 随机4位
        plan.setPlanCode("TP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        plan.setDeleted(0);
        plan.setStatus(plan.getStatus() != null ? plan.getStatus() : 0); // 默认草稿状态
        plan.setCreateTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(plan);
    }

    @Override
    @Transactional
    public void updatePlan(TrainPlan plan) {
        TrainPlan old = baseMapper.selectById(plan.getId());
        if (old == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        plan.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(plan);
    }

    @Override
    @Transactional
    public void deletePlan(Long id) {
        TrainPlan plan = baseMapper.selectById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        // 使用 MyBatis Plus 的逻辑删除
        removeById(id);
    }

    @Override
    @Transactional
    public void assignParticipants(Long planId, List<Long> employeeIds) {
        TrainPlan plan = baseMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        
        for (Long employeeId : employeeIds) {
            // 检查是否已存在
            Long count = participantMapper.selectCount(new LambdaQueryWrapper<TrainParticipant>()
                    .eq(TrainParticipant::getPlanId, planId)
                    .eq(TrainParticipant::getEmployeeId, employeeId));
            if (count > 0) continue;
            
            TrainParticipant participant = new TrainParticipant();
            participant.setPlanId(planId);
            participant.setEmployeeId(employeeId);
            participant.setAttendanceStatus(0); // 未签到
            participant.setCreateTime(LocalDateTime.now());
            participant.setUpdateTime(LocalDateTime.now());
            participantMapper.insert(participant);
        }
    }

    @Override
    public List<TrainParticipantVO> getParticipants(Long planId) {
        List<TrainParticipant> participants = participantMapper.selectList(
                new LambdaQueryWrapper<TrainParticipant>().eq(TrainParticipant::getPlanId, planId));
        return participants.stream().map(this::toParticipantVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeParticipant(Long planId, Long employeeId) {
        participantMapper.delete(new LambdaQueryWrapper<TrainParticipant>()
                .eq(TrainParticipant::getPlanId, planId)
                .eq(TrainParticipant::getEmployeeId, employeeId));
    }

    @Override
    @Transactional
    public void signIn(Long planId, Long employeeId) {
        TrainParticipant participant = participantMapper.selectOne(new LambdaQueryWrapper<TrainParticipant>()
                .eq(TrainParticipant::getPlanId, planId)
                .eq(TrainParticipant::getEmployeeId, employeeId));
        if (participant == null) {
            throw new BusinessException("您不在此培训的参训名单中");
        }
        if (participant.getAttendanceStatus() == 1) {
            throw new BusinessException("您已签到");
        }
        participant.setSignInTime(LocalDateTime.now());
        participant.setAttendanceStatus(1);
        participant.setUpdateTime(LocalDateTime.now());
        participantMapper.updateById(participant);
    }

    @Override
    @Transactional
    public void recordScore(Long participantId, Integer score, String evaluation) {
        TrainParticipant participant = participantMapper.selectById(participantId);
        if (participant == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        participant.setScore(score);
        participant.setEvaluation(evaluation);
        participant.setUpdateTime(LocalDateTime.now());
        participantMapper.updateById(participant);
    }

    private TrainParticipantVO toParticipantVO(TrainParticipant participant) {
        TrainParticipantVO vo = new TrainParticipantVO();
        BeanUtils.copyProperties(participant, vo);
        
        if (participant.getEmployeeId() != null) {
            EmpEmployee emp = employeeMapper.selectById(participant.getEmployeeId());
            if (emp != null) {
                vo.setEmployeeName(emp.getEmpName());
                vo.setEmpCode(emp.getEmpCode());
                if (emp.getDeptId() != null) {
                    OrgDepartment dept = departmentMapper.selectById(emp.getDeptId());
                    if (dept != null) {
                        vo.setDeptName(dept.getDeptName());
                    }
                }
            }
        }
        
        vo.setAttendanceStatusName(getAttendanceStatusName(participant.getAttendanceStatus()));
        return vo;
    }

    private String getAttendanceStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "未签到";
            case 1: return "已签到";
            case 2: return "缺席";
            default: return "";
        }
    }
}
