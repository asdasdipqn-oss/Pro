package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.AssessIndicator;
import cn.edu.ccst.manpower_resource.entity.AssessSubmission;
import cn.edu.ccst.manpower_resource.entity.AssessTask;
import cn.edu.ccst.manpower_resource.entity.EmpEmployee;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.AssessIndicatorMapper;
import cn.edu.ccst.manpower_resource.security.LoginUser;
import cn.edu.ccst.manpower_resource.service.IAssessSubmissionService;
import cn.edu.ccst.manpower_resource.service.IAssessTaskService;
import cn.edu.ccst.manpower_resource.service.IEmpEmployeeService;
import cn.edu.ccst.manpower_resource.vo.AssessSubmissionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Api(tags = "考核提交管理")
@Tag(name = "考核提交管理", description = "考核作业提交与评分")
@RestController
@RequestMapping("/assess/submission")
@RequiredArgsConstructor
public class AssessSubmissionController {

    private final IAssessSubmissionService assessSubmissionService;
    private final IAssessTaskService assessTaskService;
    private final IEmpEmployeeService empEmployeeService;
    private final AssessIndicatorMapper assessIndicatorMapper;

    @Value("${file.upload-dir}")
    private String storageDir;

    @Data
    @ApiModel(value = "SubmissionScoreDetailDTO", description = "提交评分明细")
    public static class SubmissionScoreDetailDTO {
        @ApiModelProperty(value = "指标ID", required = true)
        private Long indicatorId;

        @ApiModelProperty(value = "指标得分", required = true)
        private BigDecimal score;

        @ApiModelProperty(value = "评分备注")
        private String remark;
    }

    @Data
    @ApiModel(value = "SubmissionScoreRequest", description = "管理员评分请求")
    public static class SubmissionScoreRequest {
        @ApiModelProperty(value = "总分（不传时自动根据评分明细加权汇总）")
        private BigDecimal score;

        @ApiModelProperty(value = "等级（不传时根据总分自动计算）")
        private String grade;

        @ApiModelProperty(value = "评分评语")
        private String scorerComment;

        @ApiModelProperty(value = "多维指标评分明细")
        private List<SubmissionScoreDetailDTO> details;
    }

    @Data
    @ApiModel(value = "SubmissionScoreTemplateVO", description = "提交评分模板项")
    public static class SubmissionScoreTemplateVO {
        @ApiModelProperty(value = "指标ID")
        private Long indicatorId;

        @ApiModelProperty(value = "指标名称")
        private String indicatorName;

        @ApiModelProperty(value = "权重(%)")
        private BigDecimal weight;

        @ApiModelProperty(value = "满分")
        private Integer maxScore;

        @ApiModelProperty(value = "评分标准")
        private String scoringStandard;
    }

    @Operation(summary = "员工提交作业（上传文件）")
    @ApiOperation(value = "员工提交作业", notes = "员工上传考核作业文件，退回后可重新提交，历史版本会保留")
    @PostMapping("/upload")
    public Result<Void> upload(
            @ApiParam(value = "任务ID", required = true)
            @RequestParam("taskId") Long taskId,
            @ApiParam(value = "上传文件", required = true)
            @RequestParam("file") MultipartFile file,
            @ApiParam(value = "提交备注", required = false)
            @RequestParam(value = "remark", required = false) String remark,
            @ApiParam(value = "当前登录用户", hidden = true)
            @AuthenticationPrincipal LoginUser loginUser) throws IOException {

        // 校验任务状态
        AssessTask task = assessTaskService.getById(taskId);
        if (task == null || task.getDeleted() == 1) {
            throw new BusinessException("考核任务不存在");
        }
        if (task.getStatus() != 1) {
            throw new BusinessException("该考核任务不在提交阶段");
        }
        if (task.getDeadline() != null && LocalDateTime.now().isAfter(task.getDeadline())) {
            throw new BusinessException("已超过提交截止时间");
        }

        // 校验文件
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        Long userId = loginUser.getUserId();
        Long employeeId = loginUser.getUser().getEmployeeId();
        if (employeeId == null) {
            throw new BusinessException("当前用户未关联员工信息");
        }

        // 检查最近一次提交状态：待评分/已评分时禁止重复提交，退回后允许重提并保留历史
        AssessSubmission latest = assessSubmissionService.getOne(
                new LambdaQueryWrapper<AssessSubmission>()
                        .eq(AssessSubmission::getTaskId, taskId)
                        .eq(AssessSubmission::getUserId, userId)
                        .orderByDesc(AssessSubmission::getSubmitTime)
                        .orderByDesc(AssessSubmission::getId)
                        .last("LIMIT 1"));
        if (latest != null && Objects.equals(latest.getStatus(), 0)) {
            throw new BusinessException("已有待评分提交，请等待评分结果");
        }
        if (latest != null && Objects.equals(latest.getStatus(), 1)) {
            throw new BusinessException("该任务已完成评分，无法重复提交");
        }

        // 保存文件
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String storedName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

        Path baseDir = Paths.get(storageDir != null ? storageDir : "/tmp/manpower_uploads", "assess");
        Path uploadPath = baseDir.resolve(String.valueOf(taskId));
        Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve(storedName);
        file.transferTo(filePath.toFile());

        LocalDateTime now = LocalDateTime.now();
        AssessSubmission submission = new AssessSubmission();
        submission.setTaskId(taskId);
        submission.setEmployeeId(employeeId);
        submission.setUserId(userId);
        submission.setFileName(originalFilename);
        submission.setFilePath(filePath.toString());
        submission.setFileSize(file.getSize());
        submission.setRemark(remark);
        submission.setStatus(0);
        submission.setSubmitTime(now);
        submission.setCreateTime(now);
        submission.setUpdateTime(now);
        assessSubmissionService.save(submission);

        return Result.success();
    }

    @Operation(summary = "管理员分页查询提交记录")
    @ApiOperation(value = "管理员分页查询提交记录", notes = "按任务/状态分页查看提交记录（包含修订历史）")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public Result<PageResult<AssessSubmissionVO>> page(
            @ApiParam(value = "页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页条数")
            @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(value = "任务ID")
            @RequestParam(required = false) Long taskId,
            @ApiParam(value = "提交状态：0-待评分 1-已评分 2-已退回")
            @RequestParam(required = false) Integer status) {

        Page<AssessSubmission> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AssessSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(taskId != null, AssessSubmission::getTaskId, taskId)
                .eq(status != null, AssessSubmission::getStatus, status)
                .orderByDesc(AssessSubmission::getSubmitTime);

        Page<AssessSubmission> result = assessSubmissionService.page(page, wrapper);
        List<AssessSubmissionVO> voList = convertToVOList(result.getRecords());

        PageResult<AssessSubmissionVO> pageResult = PageResult.of(
                result.getCurrent(), result.getSize(), result.getTotal(), voList);
        return Result.success(pageResult);
    }

    @Operation(summary = "员工查看自己的提交记录")
    @ApiOperation(value = "员工查看自己的提交记录", notes = "按提交时间倒序返回当前用户所有提交历史")
    @GetMapping("/my")
    public Result<List<AssessSubmissionVO>> mySubmissions(
            @ApiParam(value = "当前登录用户", hidden = true)
            @AuthenticationPrincipal LoginUser loginUser) {
        Long userId = loginUser.getUserId();
        List<AssessSubmission> list = assessSubmissionService.list(
                new LambdaQueryWrapper<AssessSubmission>()
                        .eq(AssessSubmission::getUserId, userId)
                        .orderByDesc(AssessSubmission::getSubmitTime)
                        .orderByDesc(AssessSubmission::getId));

        List<AssessSubmissionVO> voList = convertToVOList(list);
        return Result.success(voList);
    }

    @Operation(summary = "员工查看某个任务的提交状态")
    @ApiOperation(value = "员工查看某个任务的最新提交状态", notes = "返回当前用户在该任务下最近一次提交记录")
    @GetMapping("/my/task/{taskId}")
    public Result<AssessSubmissionVO> myTaskSubmission(
            @ApiParam(value = "任务ID", required = true)
            @PathVariable Long taskId,
            @ApiParam(value = "当前登录用户", hidden = true)
            @AuthenticationPrincipal LoginUser loginUser) {
        Long userId = loginUser.getUserId();
        List<AssessSubmission> history = assessSubmissionService.list(
                new LambdaQueryWrapper<AssessSubmission>()
                        .eq(AssessSubmission::getTaskId, taskId)
                        .eq(AssessSubmission::getUserId, userId)
                        .orderByAsc(AssessSubmission::getSubmitTime)
                        .orderByAsc(AssessSubmission::getId));
        if (history.isEmpty()) {
            return Result.success(null);
        }
        AssessSubmission sub = history.get(history.size() - 1);
        List<AssessSubmissionVO> voList = convertToVOList(List.of(sub));
        AssessSubmissionVO vo = voList.get(0);
        vo.setRevisionNo(history.size());
        return Result.success(vo);
    }

    @Operation(summary = "员工查看某个任务的提交历史")
    @ApiOperation(value = "员工查看某个任务的提交历史", notes = "返回当前用户在任务下所有提交版本，便于查看修订记录")
    @GetMapping("/my/task/{taskId}/history")
    public Result<List<AssessSubmissionVO>> myTaskSubmissionHistory(
            @ApiParam(value = "任务ID", required = true)
            @PathVariable Long taskId,
            @ApiParam(value = "当前登录用户", hidden = true)
            @AuthenticationPrincipal LoginUser loginUser) {
        Long userId = loginUser.getUserId();
        List<AssessSubmission> history = assessSubmissionService.list(
                new LambdaQueryWrapper<AssessSubmission>()
                        .eq(AssessSubmission::getTaskId, taskId)
                        .eq(AssessSubmission::getUserId, userId)
                        .orderByDesc(AssessSubmission::getSubmitTime)
                        .orderByDesc(AssessSubmission::getId));
        List<AssessSubmissionVO> voList = convertToVOList(history);
        return Result.success(voList);
    }

    @Operation(summary = "获取评分模板")
    @ApiOperation(value = "获取评分模板", notes = "根据提交关联任务的考核方案返回指标模板，用于多维打分")
    @GetMapping("/{id}/score-template")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public Result<List<SubmissionScoreTemplateVO>> scoreTemplate(
            @ApiParam(value = "提交ID", required = true)
            @PathVariable Long id) {
        AssessSubmission submission = assessSubmissionService.getById(id);
        if (submission == null) {
            throw new BusinessException("提交记录不存在");
        }
        AssessTask task = assessTaskService.getById(submission.getTaskId());
        if (task == null || task.getPlanId() == null) {
            return Result.success(Collections.emptyList());
        }
        List<AssessIndicator> indicators = assessIndicatorMapper.selectList(
                new LambdaQueryWrapper<AssessIndicator>()
                        .eq(AssessIndicator::getPlanId, task.getPlanId())
                        .orderByAsc(AssessIndicator::getSort)
                        .orderByAsc(AssessIndicator::getId));
        List<SubmissionScoreTemplateVO> template = indicators.stream().map(indicator -> {
            SubmissionScoreTemplateVO vo = new SubmissionScoreTemplateVO();
            vo.setIndicatorId(indicator.getId());
            vo.setIndicatorName(indicator.getIndicatorName());
            vo.setWeight(indicator.getWeight());
            vo.setMaxScore(indicator.getMaxScore());
            vo.setScoringStandard(indicator.getScoringStandard());
            return vo;
        }).collect(Collectors.toList());
        return Result.success(template);
    }

    @Operation(summary = "管理员评分")
    @ApiOperation(value = "管理员评分", notes = "支持直接打总分，或按指标明细自动加权汇总总分")
    @PutMapping("/{id}/score")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public Result<Void> score(
            @ApiParam(value = "提交ID", required = true)
            @PathVariable Long id,
            @RequestBody SubmissionScoreRequest body,
            @ApiParam(value = "当前登录用户", hidden = true)
            @AuthenticationPrincipal LoginUser loginUser) {

        AssessSubmission submission = assessSubmissionService.getById(id);
        if (submission == null) {
            throw new BusinessException("提交记录不存在");
        }

        BigDecimal score = resolveScore(body, submission);
        String grade = StringUtils.hasText(body.getGrade())
                ? body.getGrade().trim().toUpperCase()
                : autoGrade(score);

        submission.setScore(score);
        submission.setGrade(grade);
        submission.setScorerComment(body.getScorerComment());
        submission.setScorerId(loginUser.getUserId());
        submission.setScoreTime(LocalDateTime.now());
        submission.setStatus(1);
        submission.setUpdateTime(LocalDateTime.now());
        assessSubmissionService.updateById(submission);

        return Result.success();
    }

    @Operation(summary = "管理员退回作业")
    @ApiOperation(value = "管理员退回作业", notes = "退回后员工可重新提交，历史提交保留")
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public Result<Void> reject(
            @ApiParam(value = "提交ID", required = true)
            @PathVariable Long id,
            @ApiParam(value = "请求体：包含scorerComment")
            @RequestBody Map<String, Object> body,
            @ApiParam(value = "当前登录用户", hidden = true)
            @AuthenticationPrincipal LoginUser loginUser) {

        AssessSubmission submission = assessSubmissionService.getById(id);
        if (submission == null) {
            throw new BusinessException("提交记录不存在");
        }

        String comment = body.get("scorerComment") != null ? body.get("scorerComment").toString() : "";
        submission.setScorerComment(comment);
        submission.setScorerId(loginUser.getUserId());
        submission.setStatus(2);
        submission.setUpdateTime(LocalDateTime.now());
        assessSubmissionService.updateById(submission);

        return Result.success();
    }

    @Operation(summary = "下载提交的文件")
    @ApiOperation(value = "下载提交的文件", notes = "根据提交ID下载作业文件")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @ApiParam(value = "提交ID", required = true)
            @PathVariable Long id) throws IOException {
        AssessSubmission submission = assessSubmissionService.getById(id);
        if (submission == null || submission.getFilePath() == null) {
            throw new BusinessException("文件不存在");
        }

        Path filePath = Paths.get(submission.getFilePath());
        if (!Files.exists(filePath)) {
            throw new BusinessException("文件已被删除");
        }

        Resource resource = new UrlResource(filePath.toUri());
        String encodedFileName = URLEncoder.encode(submission.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }

    private BigDecimal resolveScore(SubmissionScoreRequest body, AssessSubmission submission) {
        List<SubmissionScoreDetailDTO> details = body.getDetails();
        if (details != null && !details.isEmpty()) {
            AssessTask task = assessTaskService.getById(submission.getTaskId());
            if (task == null || task.getPlanId() == null) {
                throw new BusinessException("该提交未关联考核方案，无法按指标评分");
            }
            List<AssessIndicator> indicators = assessIndicatorMapper.selectList(
                    new LambdaQueryWrapper<AssessIndicator>()
                            .eq(AssessIndicator::getPlanId, task.getPlanId()));
            if (indicators.isEmpty()) {
                throw new BusinessException("当前考核方案未配置指标，无法按明细评分");
            }
            Map<Long, AssessIndicator> indicatorMap = indicators.stream()
                    .collect(Collectors.toMap(AssessIndicator::getId, i -> i));

            BigDecimal total = BigDecimal.ZERO;
            for (SubmissionScoreDetailDTO detail : details) {
                if (detail.getIndicatorId() == null) {
                    throw new BusinessException("评分明细缺少指标ID");
                }
                AssessIndicator indicator = indicatorMap.get(detail.getIndicatorId());
                if (indicator == null) {
                    throw new BusinessException("评分明细中存在非法指标");
                }
                if (detail.getScore() == null) {
                    throw new BusinessException("请填写所有指标得分");
                }
                if (detail.getScore().compareTo(BigDecimal.ZERO) < 0) {
                    throw new BusinessException("指标得分不能小于0");
                }
                Integer maxScore = indicator.getMaxScore();
                if (maxScore == null || maxScore <= 0) {
                    throw new BusinessException("指标配置异常：满分必须大于0");
                }
                if (detail.getScore().compareTo(BigDecimal.valueOf(maxScore)) > 0) {
                    throw new BusinessException("指标得分不能超过满分：" + indicator.getIndicatorName());
                }

                BigDecimal weight = indicator.getWeight() == null ? BigDecimal.ZERO : indicator.getWeight();
                BigDecimal weighted = detail.getScore()
                        .divide(BigDecimal.valueOf(maxScore), 6, RoundingMode.HALF_UP)
                        .multiply(weight);
                total = total.add(weighted);
            }
            return total.setScale(2, RoundingMode.HALF_UP);
        }

        if (body.getScore() == null) {
            throw new BusinessException("请填写得分");
        }
        if (body.getScore().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("得分不能小于0");
        }
        return body.getScore().setScale(2, RoundingMode.HALF_UP);
    }

    private String autoGrade(BigDecimal score) {
        if (score == null) {
            return "E";
        }
        if (score.compareTo(new BigDecimal("90")) >= 0) {
            return "A";
        }
        if (score.compareTo(new BigDecimal("80")) >= 0) {
            return "B";
        }
        if (score.compareTo(new BigDecimal("70")) >= 0) {
            return "C";
        }
        if (score.compareTo(new BigDecimal("60")) >= 0) {
            return "D";
        }
        return "E";
    }

    private List<AssessSubmissionVO> convertToVOList(List<AssessSubmission> submissions) {
        if (submissions == null || submissions.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量获取员工信息
        List<Long> employeeIds = submissions.stream()
                .map(AssessSubmission::getEmployeeId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, EmpEmployee> empMap = empEmployeeService.listByIds(employeeIds).stream()
                .collect(Collectors.toMap(EmpEmployee::getId, e -> e));

        // 批量获取任务信息
        List<Long> taskIds = submissions.stream()
                .map(AssessSubmission::getTaskId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, AssessTask> taskMap = assessTaskService.listByIds(taskIds).stream()
                .collect(Collectors.toMap(AssessTask::getId, t -> t));

        List<AssessSubmissionVO> voList = submissions.stream().map(sub -> {
            AssessSubmissionVO vo = new AssessSubmissionVO();
            BeanUtils.copyProperties(sub, vo);
            EmpEmployee emp = empMap.get(sub.getEmployeeId());
            if (emp != null) {
                vo.setEmpCode(emp.getEmpCode());
                vo.setEmpName(emp.getEmpName());
            }
            AssessTask task = taskMap.get(sub.getTaskId());
            if (task != null) {
                vo.setTaskTitle(task.getTitle());
            }
            return vo;
        }).collect(Collectors.toList());
        fillRevisionNo(voList, submissions);
        return voList;
    }

    private void fillRevisionNo(List<AssessSubmissionVO> voList, List<AssessSubmission> submissions) {
        if (voList == null || submissions == null || voList.isEmpty() || submissions.isEmpty()) {
            return;
        }

        Map<String, List<AssessSubmission>> grouped = submissions.stream()
                .collect(Collectors.groupingBy(sub -> sub.getTaskId() + "_" + sub.getUserId()));
        Map<Long, Integer> revisionNoById = new HashMap<>();

        for (List<AssessSubmission> sameTaskUserList : grouped.values()) {
            sameTaskUserList.sort(Comparator
                    .comparing(AssessSubmission::getSubmitTime, Comparator.nullsLast(LocalDateTime::compareTo))
                    .thenComparing(AssessSubmission::getId, Comparator.nullsLast(Long::compareTo)));
            int revision = 1;
            for (AssessSubmission submission : sameTaskUserList) {
                revisionNoById.put(submission.getId(), revision++);
            }
        }

        for (AssessSubmissionVO vo : voList) {
            vo.setRevisionNo(revisionNoById.getOrDefault(vo.getId(), 1));
        }
    }
}
