package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.RecruitApplication;
import cn.edu.ccst.manpower_resource.entity.RecruitInterview;
import cn.edu.ccst.manpower_resource.entity.RecruitResume;
import cn.edu.ccst.manpower_resource.mapper.RecruitApplicationMapper;
import cn.edu.ccst.manpower_resource.service.IRecruitInterviewService;
import cn.edu.ccst.manpower_resource.service.IRecruitResumeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "面试管理", description = "面试记录CRUD操作")
@RestController
@RequestMapping("/recruit/interview")
@RequiredArgsConstructor
public class RecruitInterviewController {

    private final IRecruitInterviewService recruitInterviewService;
    private final IRecruitResumeService resumeService;
    private final RecruitApplicationMapper applicationMapper;

    @Operation(summary = "分页查询面试记录")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Page<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long resumeId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer result) {
        LambdaQueryWrapper<RecruitInterview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(resumeId != null, RecruitInterview::getResumeId, resumeId)
                .eq(status != null, RecruitInterview::getStatus, status)
                .eq(result != null, RecruitInterview::getResult, result)
                .orderByDesc(RecruitInterview::getInterviewTime);
        Page<RecruitInterview> page = recruitInterviewService.page(new Page<>(pageNum, pageSize), wrapper);

        // 查询关联的简历信息
        List<Long> resumeIds = page.getRecords().stream()
                .map(RecruitInterview::getResumeId)
                .filter(id -> id != null)
                .distinct().collect(Collectors.toList());
        Map<Long, RecruitResume> resumeMap = Map.of();
        if (!resumeIds.isEmpty()) {
            List<RecruitResume> resumes = resumeService.listByIds(resumeIds);
            resumeMap = resumes.stream().collect(Collectors.toMap(RecruitResume::getId, r -> r));
        }

        // 转换为包含简历信息的Map
        Map<Long, RecruitResume> finalResumeMap = resumeMap;
        Page<Map<String, Object>> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(page.getRecords().stream().map(i -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id", i.getId());
            map.put("resumeId", i.getResumeId());
            map.put("interviewRound", i.getInterviewRound());
            map.put("interviewerId", i.getInterviewerId());
            map.put("interviewTime", i.getInterviewTime());
            map.put("interviewType", i.getInterviewType());
            map.put("interviewAddress", i.getInterviewAddress());
            map.put("evaluation", i.getEvaluation());
            map.put("score", i.getScore());
            map.put("result", i.getResult());
            map.put("remark", i.getRemark());
            map.put("status", i.getStatus());
            map.put("createTime", i.getCreateTime());
            map.put("updateTime", i.getUpdateTime());
            RecruitResume resume = i.getResumeId() != null ? finalResumeMap.get(i.getResumeId()) : null;
            map.put("candidateName", resume != null ? resume.getName() : "");
            map.put("candidatePhone", resume != null ? resume.getPhone() : "");
            map.put("candidateEducation", resume != null ? resume.getEducation() : null);
            map.put("jobId", resume != null ? resume.getJobId() : null);
            return map;
        }).collect(Collectors.toList()));
        return Result.success(resultPage);
    }

    @Operation(summary = "查询简历的面试记录")
    @GetMapping("/resume/{resumeId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<List<RecruitInterview>> getByResume(@PathVariable Long resumeId) {
        List<RecruitInterview> list = recruitInterviewService.list(new LambdaQueryWrapper<RecruitInterview>()
                .eq(RecruitInterview::getResumeId, resumeId)
                .orderByAsc(RecruitInterview::getInterviewRound));
        return Result.success(list);
    }

    @Operation(summary = "获取面试记录详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<RecruitInterview> getById(@PathVariable Long id) {
        return Result.success(recruitInterviewService.getById(id));
    }

    @Operation(summary = "安排面试")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @Transactional
    public Result<Void> add(@RequestBody RecruitInterview interview) {
        interview.setStatus(0);
        interview.setCreateTime(LocalDateTime.now());
        interview.setUpdateTime(LocalDateTime.now());
        recruitInterviewService.save(interview);

        // 同步更新简历状态为"面试中"
        RecruitResume resume = resumeService.getById(interview.getResumeId());
        if (resume != null && resume.getStatus() != 3) {
            resume.setStatus(3); // 面试中
            resume.setUpdateTime(LocalDateTime.now());
            resumeService.updateById(resume);
        }

        // 同步更新 recruit_application 状态为"面试中"
        RecruitApplication application = applicationMapper.selectOne(
                new LambdaQueryWrapper<RecruitApplication>()
                        .eq(RecruitApplication::getResumeId, interview.getResumeId()));
        if (application != null && application.getStatus() < 2) {
            application.setStatus(2); // 面试中
            application.setHrReviewTime(LocalDateTime.now());
            application.setUpdateTime(LocalDateTime.now());
            applicationMapper.updateById(application);
        }
        return Result.success();
    }

    @Operation(summary = "更新面试记录（填写评价/评分/结果）")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> update(@RequestBody RecruitInterview interview) {
        interview.setUpdateTime(LocalDateTime.now());
        recruitInterviewService.updateById(interview);
        return Result.success();
    }

    @Operation(summary = "完成面试（填写评价和结果）")
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @Transactional
    public Result<Void> complete(@PathVariable Long id, @RequestBody RecruitInterview dto) {
        RecruitInterview interview = recruitInterviewService.getById(id);
        if (interview != null) {
            interview.setEvaluation(dto.getEvaluation());
            interview.setScore(dto.getScore());
            interview.setResult(dto.getResult());
            interview.setRemark(dto.getRemark());
            interview.setStatus(1);
            interview.setUpdateTime(LocalDateTime.now());
            recruitInterviewService.updateById(interview);

            // 根据面试结果同步更新简历和申请状态
            // result: 1-通过 2-待定 3-不通过
            if (dto.getResult() != null && interview.getResumeId() != null) {
                int resumeStatus;
                int appStatus;
                if (dto.getResult() == 1) {
                    resumeStatus = 4; // 已录用
                    appStatus = 3;    // 已录用
                } else if (dto.getResult() == 3) {
                    resumeStatus = 5; // 未通过
                    appStatus = 5;    // 未通过
                } else {
                    resumeStatus = 3; // 面试中(待定)
                    appStatus = 2;    // 面试中
                }

                RecruitResume resume = resumeService.getById(interview.getResumeId());
                if (resume != null) {
                    resume.setStatus(resumeStatus);
                    resume.setUpdateTime(LocalDateTime.now());
                    resumeService.updateById(resume);
                }

                RecruitApplication application = applicationMapper.selectOne(
                        new LambdaQueryWrapper<RecruitApplication>()
                                .eq(RecruitApplication::getResumeId, interview.getResumeId()));
                if (application != null) {
                    application.setStatus(appStatus);
                    application.setUpdateTime(LocalDateTime.now());
                    applicationMapper.updateById(application);
                }
            }
        }
        return Result.success();
    }

    @Operation(summary = "取消面试")
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> cancel(@PathVariable Long id) {
        RecruitInterview interview = recruitInterviewService.getById(id);
        if (interview != null) {
            interview.setStatus(2);
            interview.setUpdateTime(LocalDateTime.now());
            recruitInterviewService.updateById(interview);
        }
        return Result.success();
    }

    @Operation(summary = "删除面试记录")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        recruitInterviewService.removeById(id);
        return Result.success();
    }
}
