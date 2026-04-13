package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.entity.*;
import cn.edu.ccst.manpower_resource.mapper.*;
import cn.edu.ccst.manpower_resource.entity.*;import cn.edu.ccst.manpower_resource.security.LoginUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息通知 前端控制器
 */
@Api(tags = "消息通知")
@Tag(name = "消息通知", description = "消息通知相关操作")
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private static final String READ_CONFIG_KEY_PREFIX = "notification.read.user.";
    private static final int MAX_READ_MARK_SIZE = 500;

    private final ApprovalRecordMapper approvalRecordMapper;
    private final LeaveApplicationMapper leaveApplicationMapper;
    private final TrainPlanMapper trainPlanMapper;
    private final TrainParticipantMapper trainParticipantMapper;
    private final SalaryRecordMapper salaryRecordMapper;
    private final AssessSubmissionMapper assessSubmissionMapper;
    private final AssessTaskMapper assessTaskMapper;
    private final SysConfigMapper sysConfigMapper;
    private final SysRoleMapper sysRoleMapper;

    @Data
    public static class NotificationVO {
        private Long id;
        private String type;      // approval, leave, train, salary, assess
        private String title;
        private String content;
        private Integer status;   // 0-未读 1-已读
        private LocalDateTime createTime;
        private Map<String, Object> extra;
    }

    @Data
    @ApiModel(value = "NotificationReadRequest", description = "消息已读请求")
    public static class NotificationReadRequest {
        @ApiModelProperty(value = "消息类型：approval/leave/train/salary/assess", required = true)
        private String type;

        @ApiModelProperty(value = "消息业务ID", required = true)
        private Long id;
    }

    @Operation(summary = "获取消息列表")
    @ApiOperation(value = "获取消息列表", notes = "获取当前用户的消息通知列表（待审批、请假结果、培训通知、薪资发放、考核结果）")
    @GetMapping("/list")
    public Result<List<NotificationVO>> getNotifications(
            @ApiParam(value = "当前登录用户", hidden = true)
            @AuthenticationPrincipal LoginUser loginUser) {
        if ("CANDIDATE".equals(loginUser.getUserType())) {
            return Result.success(Collections.emptyList());
        }
        Long userId = loginUser.getUser().getId();
        Long employeeId = loginUser.getUser().getEmployeeId();
        return Result.success(buildNotifications(userId, employeeId));
    }

    @Operation(summary = "获取未读消息数量")
    @ApiOperation(value = "获取未读消息数量", notes = "获取当前用户未读消息数量（支持真正已读状态）")
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> getUnreadCount(
            @ApiParam(value = "当前登录用户", hidden = true)
            @AuthenticationPrincipal LoginUser loginUser) {
        if ("CANDIDATE".equals(loginUser.getUserType())) {
            Map<String, Object> data = new HashMap<>();
            data.put("approvalCount", 0L);
            data.put("leaveCount", 0L);
            data.put("trainCount", 0L);
            data.put("salaryCount", 0L);
            data.put("assessCount", 0L);
            data.put("totalCount", 0L);
            return Result.success(data);
        }
        Long userId = loginUser.getUser().getId();
        Long employeeId = loginUser.getUser().getEmployeeId();
        List<NotificationVO> notifications = buildNotifications(userId, employeeId);

        Map<String, Object> data = new HashMap<>();

        long approvalCount = countUnreadByType(notifications, "approval");
        long leaveCount = countUnreadByType(notifications, "leave");
        long trainCount = countUnreadByType(notifications, "train");
        long salaryCount = countUnreadByType(notifications, "salary");
        long assessCount = countUnreadByType(notifications, "assess");

        data.put("approvalCount", approvalCount);
        data.put("leaveCount", leaveCount);
        data.put("trainCount", trainCount);
        data.put("salaryCount", salaryCount);
        data.put("assessCount", assessCount);
        data.put("totalCount", approvalCount + leaveCount + trainCount + salaryCount + assessCount);

        return Result.success(data);
    }

    @Operation(summary = "标记单条消息已读")
    @ApiOperation(value = "标记单条消息已读", notes = "按消息类型+业务ID标记已读")
    @PutMapping("/read")
    public Result<Void> markRead(
            @RequestBody NotificationReadRequest request,
            @ApiParam(value = "当前登录用户", hidden = true)
            @AuthenticationPrincipal LoginUser loginUser) {
        if (request == null || !StringUtils.hasText(request.getType()) || request.getId() == null) {
            throw new BusinessException("参数错误");
        }
        if ("CANDIDATE".equals(loginUser.getUserType())) {
            return Result.success();
        }
        Long userId = loginUser.getUser().getId();
        Set<String> readMarks = loadReadMarks(userId);
        readMarks.add(readKey(request.getType(), request.getId()));
        saveReadMarks(userId, readMarks);
        return Result.success();
    }

    @Operation(summary = "全部标记已读")
    @ApiOperation(value = "全部标记已读", notes = "将当前用户可见消息全部标记为已读")
    @PutMapping("/read-all")
    public Result<Void> markAllRead(
            @ApiParam(value = "当前登录用户", hidden = true)
            @AuthenticationPrincipal LoginUser loginUser) {
        if ("CANDIDATE".equals(loginUser.getUserType())) {
            return Result.success();
        }
        Long userId = loginUser.getUser().getId();
        Long employeeId = loginUser.getUser().getEmployeeId();
        List<NotificationVO> notifications = buildNotifications(userId, employeeId);
        Set<String> readMarks = loadReadMarks(userId);
        for (NotificationVO notification : notifications) {
            readMarks.add(readKey(notification.getType(), notification.getId()));
        }
        saveReadMarks(userId, readMarks);
        return Result.success();
    }

    private List<NotificationVO> buildNotifications(Long userId, Long employeeId) {
        Set<String> readMarks = loadReadMarks(userId);
        List<NotificationVO> notifications = new ArrayList<>();
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        // 判断是否为管理员角色
        boolean isAdmin = isAdminUser(userId);

        // 待审批通知：管理员看所有待审批，普通用户只看自己的
        LambdaQueryWrapper<ApprovalRecord> approvalWrapper = new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getStatus, 0)
                .ge(ApprovalRecord::getCreateTime, threeDaysAgo)
                .orderByDesc(ApprovalRecord::getCreateTime)
                .last("LIMIT 10");
        if (!isAdmin) {
            approvalWrapper.eq(ApprovalRecord::getApproverId, userId);
        }

        List<ApprovalRecord> pendingApprovals = approvalRecordMapper.selectList(approvalWrapper);

        for (ApprovalRecord record : pendingApprovals) {
            NotificationVO vo = new NotificationVO();
            vo.setId(record.getId());
            vo.setType("approval");
            vo.setTitle("待审批通知");
            vo.setContent(getApprovalContent(record.getBusinessType()));
            vo.setCreateTime(record.getCreateTime());
            Map<String, Object> extra = new HashMap<>();
            extra.put("businessType", record.getBusinessType());
            extra.put("businessId", record.getBusinessId());
            vo.setExtra(extra);
            setReadStatus(vo, readMarks);
            notifications.add(vo);
        }

        if (employeeId != null) {
            List<LeaveApplication> myLeaves = leaveApplicationMapper.selectList(
                    new LambdaQueryWrapper<LeaveApplication>()
                            .eq(LeaveApplication::getEmployeeId, employeeId)
                            .in(LeaveApplication::getStatus, 2, 3)
                            .ge(LeaveApplication::getUpdateTime, threeDaysAgo)
                            .orderByDesc(LeaveApplication::getUpdateTime)
                            .last("LIMIT 5"));
            for (LeaveApplication leave : myLeaves) {
                NotificationVO vo = new NotificationVO();
                vo.setId(leave.getId());
                vo.setType("leave");
                vo.setTitle("请假审批结果");
                vo.setContent(leave.getStatus() == 2 ? "您的请假申请已通过" : "您的请假申请已被驳回");
                vo.setCreateTime(leave.getUpdateTime());
                setReadStatus(vo, readMarks);
                notifications.add(vo);
            }

            List<TrainParticipant> myParticipants = trainParticipantMapper.selectList(
                    new LambdaQueryWrapper<TrainParticipant>()
                            .eq(TrainParticipant::getEmployeeId, employeeId));
            List<Long> myTrainPlanIds = myParticipants.stream()
                    .map(TrainParticipant::getPlanId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (!myTrainPlanIds.isEmpty()) {
                List<TrainPlan> trains = trainPlanMapper.selectList(
                        new LambdaQueryWrapper<TrainPlan>()
                                .in(TrainPlan::getId, myTrainPlanIds)
                                .eq(TrainPlan::getDeleted, 0)
                                .in(TrainPlan::getStatus, 0, 1)
                                .ge(TrainPlan::getCreateTime, threeDaysAgo)
                                .orderByDesc(TrainPlan::getCreateTime)
                                .last("LIMIT 5"));
                for (TrainPlan train : trains) {
                    NotificationVO vo = new NotificationVO();
                    vo.setId(train.getId());
                    vo.setType("train");
                    vo.setTitle("培训通知");
                    vo.setContent("您有培训计划: " + train.getPlanName());
                    vo.setCreateTime(train.getCreateTime());
                    setReadStatus(vo, readMarks);
                    notifications.add(vo);
                }
            }

            List<SalaryRecord> salaries = salaryRecordMapper.selectList(
                    new LambdaQueryWrapper<SalaryRecord>()
                            .eq(SalaryRecord::getEmployeeId, employeeId)
                            .eq(SalaryRecord::getStatus, 2)
                            .ge(SalaryRecord::getPayTime, threeDaysAgo)
                            .orderByDesc(SalaryRecord::getPayTime)
                            .last("LIMIT 3"));
            for (SalaryRecord salary : salaries) {
                NotificationVO vo = new NotificationVO();
                vo.setId(salary.getId());
                vo.setType("salary");
                vo.setTitle("薪资发放通知");
                vo.setContent(salary.getSalaryYear() + "年" + salary.getSalaryMonth() + "月工资已发放");
                vo.setCreateTime(salary.getPayTime());
                setReadStatus(vo, readMarks);
                notifications.add(vo);
            }

            List<AssessSubmission> assessSubmissions = assessSubmissionMapper.selectList(
                    new LambdaQueryWrapper<AssessSubmission>()
                            .eq(AssessSubmission::getUserId, userId)
                            .in(AssessSubmission::getStatus, 1, 2)
                            .ge(AssessSubmission::getUpdateTime, threeDaysAgo)
                            .orderByDesc(AssessSubmission::getUpdateTime)
                            .last("LIMIT 10"));
            if (!assessSubmissions.isEmpty()) {
                List<Long> taskIds = assessSubmissions.stream()
                        .map(AssessSubmission::getTaskId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();
                Map<Long, AssessTask> taskMap = taskIds.isEmpty()
                        ? Collections.emptyMap()
                        : assessTaskMapper.selectList(new LambdaQueryWrapper<AssessTask>().in(AssessTask::getId, taskIds))
                                .stream()
                                .collect(Collectors.toMap(AssessTask::getId, t -> t));

                for (AssessSubmission submission : assessSubmissions) {
                    NotificationVO vo = new NotificationVO();
                    vo.setId(submission.getId());
                    vo.setType("assess");
                    String taskTitle = Optional.ofNullable(taskMap.get(submission.getTaskId()))
                            .map(AssessTask::getTitle)
                            .orElse("考核任务");

                    if (submission.getStatus() != null && submission.getStatus() == 1) {
                        vo.setTitle("考核评分通知");
                        String scoreText = submission.getScore() == null
                                ? "-"
                                : submission.getScore().stripTrailingZeros().toPlainString();
                        String gradeText = StringUtils.hasText(submission.getGrade())
                                ? "，等级" + submission.getGrade()
                                : "";
                        vo.setContent(taskTitle + " 已评分：得分 " + scoreText + gradeText);
                    } else {
                        vo.setTitle("考核作业退回");
                        String comment = StringUtils.hasText(submission.getScorerComment())
                                ? submission.getScorerComment()
                                : "请根据要求修改后重新提交";
                        vo.setContent(taskTitle + " 已被退回：" + comment);
                    }

                    vo.setCreateTime(submission.getScoreTime() != null ? submission.getScoreTime() : submission.getUpdateTime());
                    Map<String, Object> extra = new HashMap<>();
                    extra.put("taskId", submission.getTaskId());
                    extra.put("taskTitle", taskTitle);
                    extra.put("assessStatus", submission.getStatus());
                    extra.put("score", submission.getScore());
                    extra.put("grade", submission.getGrade());
                    vo.setExtra(extra);
                    setReadStatus(vo, readMarks);
                    notifications.add(vo);
                }
            }
        }

        notifications.sort((a, b) -> {
            LocalDateTime at = a.getCreateTime();
            LocalDateTime bt = b.getCreateTime();
            if (at == null && bt == null) {
                return 0;
            }
            if (at == null) {
                return 1;
            }
            if (bt == null) {
                return -1;
            }
            return bt.compareTo(at);
        });
        return notifications;
    }

    private long countUnreadByType(List<NotificationVO> notifications, String type) {
        return notifications.stream()
                .filter(notification -> Objects.equals(notification.getStatus(), 0))
                .filter(notification -> Objects.equals(notification.getType(), type))
                .count();
    }

    private void setReadStatus(NotificationVO vo, Set<String> readMarks) {
        vo.setStatus(readMarks.contains(readKey(vo.getType(), vo.getId())) ? 1 : 0);
    }

    private String readKey(String type, Long id) {
        return type.toLowerCase(Locale.ROOT) + ":" + id;
    }

    private Set<String> loadReadMarks(Long userId) {
        String key = READ_CONFIG_KEY_PREFIX + userId;
        SysConfig config = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key)
                .last("LIMIT 1"));
        if (config == null || !StringUtils.hasText(config.getConfigValue())) {
            return new HashSet<>();
        }
        return Arrays.stream(config.getConfigValue().split(","))
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private void saveReadMarks(Long userId, Set<String> readMarks) {
        String key = READ_CONFIG_KEY_PREFIX + userId;
        List<String> markList = readMarks.stream()
                .filter(StringUtils::hasText)
                .sorted()
                .collect(Collectors.toList());
        if (markList.size() > MAX_READ_MARK_SIZE) {
            markList = markList.subList(markList.size() - MAX_READ_MARK_SIZE, markList.size());
        }
        String value = String.join(",", markList);

        SysConfig config = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key)
                .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();
        if (config == null) {
            SysConfig saveConfig = new SysConfig();
            saveConfig.setConfigKey(key);
            saveConfig.setConfigValue(value);
            saveConfig.setConfigName("用户消息已读标记-" + userId);
            saveConfig.setConfigType(2);
            saveConfig.setRemark("系统自动维护");
            saveConfig.setCreateTime(now);
            saveConfig.setUpdateTime(now);
            sysConfigMapper.insert(saveConfig);
            return;
        }
        config.setConfigValue(value);
        config.setUpdateTime(now);
        sysConfigMapper.updateById(config);
    }

    private String getApprovalContent(Integer businessType) {
        switch (businessType) {
            case 1: return "有新的请假申请待审批";
            case 2: return "有新的考勤异常申诉待审批";
            case 3: return "有新的调岗申请待审批";
            case 4: return "有新的离职申请待审批";
            default: return "有新的审批待处理";
        }
    }

    private boolean isAdminUser(Long userId) {
        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(userId);
        return roles.stream().anyMatch(r ->
                "ADMIN".equalsIgnoreCase(r.getRoleCode()) || "SUPER_ADMIN".equalsIgnoreCase(r.getRoleCode()));
    }
}
