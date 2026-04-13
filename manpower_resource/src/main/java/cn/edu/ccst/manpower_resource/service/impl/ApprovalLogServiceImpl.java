package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.entity.*;
import cn.edu.ccst.manpower_resource.mapper.*;
import cn.edu.ccst.manpower_resource.service.IApprovalLogService;
import cn.edu.ccst.manpower_resource.vo.ApprovalLogVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalLogServiceImpl implements IApprovalLogService {

    private final ApprovalLogMapper logMapper;
    private final SysUserMapper userMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final LeaveApplicationMapper leaveApplicationMapper;
    private final AttAppealMapper attAppealMapper;
    private final EmpEmployeeMapper employeeMapper;

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
        // 合并 approval_record 和 approval_log 的数据
        List<ApprovalLogVO> allRecords = new ArrayList<>();

        // 1. 从 approval_record 获取所有审批记录
        LambdaQueryWrapper<ApprovalRecord> recordWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(businessType)) {
            recordWrapper.eq(ApprovalRecord::getBusinessType, businessTypeToInt(businessType));
        }
        recordWrapper.orderByDesc(ApprovalRecord::getCreateTime);
        List<ApprovalRecord> records = approvalRecordMapper.selectList(recordWrapper);
        for (ApprovalRecord record : records) {
            ApprovalLogVO vo = new ApprovalLogVO();
            vo.setId(record.getId());
            vo.setBusinessType(businessTypeToString(record.getBusinessType()));
            vo.setBusinessId(record.getBusinessId());
            vo.setApproverId(record.getApproverId());
            vo.setStatus(record.getStatus());
            vo.setCreateTime(record.getCreateTime());

            // 审批人姓名
            fillApproverName(vo, record.getApproverId());
            // 申请人姓名
            fillApplicantName(vo, record.getBusinessType(), record.getBusinessId());

            // 操作动作
            switch (record.getStatus()) {
                case 0: vo.setAction("待审批"); break;
                case 1: vo.setAction("通过"); break;
                case 2: vo.setAction("驳回"); break;
                default: vo.setAction("未知"); break;
            }
            vo.setComment(record.getOpinion());
            allRecords.add(vo);
        }

        // 2. 从 approval_log 获取额外日志（补充 record 未覆盖的）
        LambdaQueryWrapper<ApprovalLog> logWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(businessType)) {
            logWrapper.eq(ApprovalLog::getBusinessType, businessType);
        }
        logWrapper.orderByDesc(ApprovalLog::getCreateTime);
        List<ApprovalLog> logs = logMapper.selectList(logWrapper);

        // 构建 record 的 key 集合，避免重复
        Set<String> recordKeys = records.stream()
                .map(r -> r.getBusinessType() + ":" + r.getBusinessId() + ":" + r.getStatus())
                .collect(Collectors.toSet());

        for (ApprovalLog log : logs) {
            ApprovalLogVO vo = toVO(log);
            vo.setStatus(vo.getAction().contains("通过") ? 1 : (vo.getAction().contains("驳回") ? 2 : null));
            fillApplicantName(vo, businessTypeToInt(log.getBusinessType()), log.getBusinessId());

            // 如果 record 已覆盖，跳过
            String key = businessTypeToInt(log.getBusinessType()) + ":" + log.getBusinessId() + ":" + vo.getStatus();
            if (recordKeys.contains(key)) continue;

            allRecords.add(vo);
        }

        // 按时间倒序
        allRecords.sort((a, b) -> {
            if (a.getCreateTime() == null && b.getCreateTime() == null) return 0;
            if (a.getCreateTime() == null) return 1;
            if (b.getCreateTime() == null) return -1;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });

        // 手动分页
        int total = allRecords.size();
        int from = Math.min((query.getPageNum() - 1) * query.getPageSize(), total);
        int to = Math.min(from + query.getPageSize(), total);
        List<ApprovalLogVO> pageList = allRecords.subList(from, to);

        return PageResult.of((long) query.getPageNum(), (long) query.getPageSize(), (long) total, pageList);
    }

    private void fillApproverName(ApprovalLogVO vo, Long approverId) {
        if (approverId == null) return;
        vo.setApproverName(getEmployeeNameByUserId(approverId));
    }

    private String getEmployeeNameByUserId(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) return "未知用户";
        if (user.getEmployeeId() != null) {
            EmpEmployee emp = employeeMapper.selectById(user.getEmployeeId());
            if (emp != null) return emp.getEmpName();
        }
        return user.getUsername();
    }

    private void fillApplicantName(ApprovalLogVO vo, Integer businessType, Long businessId) {
        if (businessType == null || businessId == null) return;
        Long empId = null;
        switch (businessType) {
            case 1:
                LeaveApplication leave = leaveApplicationMapper.selectById(businessId);
                empId = leave != null ? leave.getEmployeeId() : null;
                break;
            case 2:
                AttAppeal appeal = attAppealMapper.selectById(businessId);
                empId = appeal != null ? appeal.getEmployeeId() : null;
                break;
        }
        if (empId != null) {
            EmpEmployee emp = employeeMapper.selectById(empId);
            vo.setApplicantName(emp != null ? emp.getEmpName() : null);
        }
    }

    private ApprovalLogVO toVO(ApprovalLog log) {
        ApprovalLogVO vo = new ApprovalLogVO();
        BeanUtils.copyProperties(log, vo);
        if (log.getApproverId() != null) {
            vo.setApproverName(getEmployeeNameByUserId(log.getApproverId()));
        }
        return vo;
    }

    private String businessTypeToString(Integer type) {
        if (type == null) return "";
        switch (type) {
            case 1: return "请假";
            case 2: return "考勤申诉";
            case 3: return "调岗";
            case 4: return "离职";
            default: return "其他";
        }
    }

    private Integer businessTypeToInt(String type) {
        if (!StringUtils.hasText(type)) return null;
        switch (type) {
            case "LEAVE": return 1;
            case "APPEAL": return 2;
            case "TRANSFER": return 3;
            case "RESIGNATION": return 4;
            case "请假": return 1;
            case "考勤申诉": return 2;
            case "调岗": return 3;
            case "离职": return 4;
            default: return null;
        }
    }
}
